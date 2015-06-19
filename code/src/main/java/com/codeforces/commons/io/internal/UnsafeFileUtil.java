package com.codeforces.commons.io.internal;

import com.codeforces.commons.compress.ZipUtil;
import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.io.IoUtil;
import com.codeforces.commons.math.RandomUtil;
import com.codeforces.commons.properties.internal.CommonsPropertiesUtil;
import com.google.common.primitives.Ints;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import de.schlichtherle.truezip.file.TVFS;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Mirzayanov
 */
public class UnsafeFileUtil {
    private static final Logger logger = Logger.getLogger(UnsafeFileUtil.class);
    private static final int BUFFER_SIZE = 655360;
    private static final char DOS_FILE_SEPARATOR = '\\';
    private static final char UNIX_FILE_SEPARATOR = '/';

    private UnsafeFileUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param file Existing file.
     * @return SHA-1 hashCode in hexadecimal.
     * @throws IOException Can't perform IO.
     */
    public static String sha1Hex(File file) throws IOException {
        return IoUtil.sha1Hex(new BufferedInputStream(new FileInputStream(file)));
    }

    /**
     * Copies one file to another. Overwrites it if exists.
     *
     * @param source      Source file.
     * @param destination Destination file.
     * @throws IOException Can't perform copy.
     */
    public static void copyFile(File source, File destination) throws IOException {
        internalCopyFile(source, destination, true);
    }

    private static void internalCopyFile(File source, File destination, boolean synchronize) throws IOException {
        if (destination instanceof TFile) {
            throw new UnsupportedOperationException("Can't copy file into archive file.");
        }

        deleteTotally(destination);

        File destinationParentFile = destination.getParentFile();
        if (destinationParentFile != null) {
            ensureDirectoryExists(destinationParentFile);
        }

        if (source instanceof TFile) {
            try {
                writeFile(destination, getBytes(source));
            } finally {
                if (synchronize) {
                    ZipUtil.synchronizeQuietly((TFile) source);
                }
            }
        } else {
            if (!source.isFile()) {
                throw new IOException("'" + source + "' is not a file.");
            }

            FileInputStream inStream = null;
            FileOutputStream outStream = null;

            try {
                inStream = new FileInputStream(source);
                outStream = new FileOutputStream(destination);

                FileChannel inChannel = inStream.getChannel();
                FileChannel outChannel = outStream.getChannel();

                inChannel.transferTo(0, inChannel.size(), outChannel);
            } finally {
                IoUtil.closeQuietly(inStream, outStream);
            }
        }
    }

    /**
     * Copy one directory into another. If the second one exists it copies nested files from
     * the source to destination.
     *
     * @param source      Source directory.
     * @param destination Destination directory.
     * @throws IOException when can't perform copy.
     */
    public static void copyDirectory(File source, File destination) throws IOException {
        internalCopyDirectory(source, destination, true);
    }

    @SuppressWarnings("OverlyComplexMethod")
    private static void internalCopyDirectory(File source, File destination, boolean synchronize) throws IOException {
        if (destination instanceof TFile) {
            throw new UnsupportedOperationException("Can't copy directory into archive file.");
        }

        if (!source.isDirectory()) {
            throw new IOException("'" + source + "' is not a directory.");
        }

        if (destination.isFile()) {
            throw new IOException("'" + destination + "' is a file.");
        }

        ensureDirectoryExists(destination);

        try {
            for (String child : source.list()) {
                File nextSource = source instanceof TFile ? new TFile(source, child) : new File(source, child);
                File nextDestination = new File(destination, child);

                if (nextSource.isDirectory()) {
                    TFile tNextSource;
                    TFile enclosingArchive;

                    if (nextSource instanceof TFile
                            && (tNextSource = (TFile) nextSource).isArchive()
                            && (enclosingArchive = tNextSource.getEnclArchive()) != null
                            && new File(enclosingArchive.getAbsolutePath()).isFile()) {
                        deleteTotally(nextDestination);
                        ZipUtil.synchronizeQuietly(tNextSource);
                        ZipUtil.writeZipEntryBytes(
                                enclosingArchive, tNextSource.getEnclEntryName(),
                                new FileOutputStream(nextDestination)
                        );
                    } else {
                        internalCopyDirectory(nextSource, nextDestination, false);
                    }
                } else {
                    internalCopyFile(nextSource, nextDestination, false);
                }
            }
        } finally {
            if (synchronize && source instanceof TFile) {
                ZipUtil.synchronizeQuietly((TFile) source);
            }
        }
    }

    /**
     * Ensures that file does exist and creates it if not.
     *
     * @param file File to check
     * @return created file
     * @throws IOException if file does not exist and can't be created
     */
    @SuppressWarnings({"DuplicateCondition", "DuplicateBooleanBranch"})
    public static File ensureFileExists(File file) throws IOException {
        File parentFile = file.getParentFile();
        if (parentFile != null) {
            ensureDirectoryExists(parentFile);
        }

        if (file.isFile() || file.createNewFile() || file.isFile()) {
            return file;
        }

        throw new IOException("Can't create file '" + file + "'.");
    }

    /**
     * Ensures that directory does exist and creates it if not.
     *
     * @param directory Directory to check
     * @return created directory
     * @throws IOException if directory does not exist and can't be created
     */
    @SuppressWarnings({"DuplicateCondition", "DuplicateBooleanBranch"})
    public static File ensureDirectoryExists(File directory) throws IOException {
        if (directory.isDirectory() || directory.mkdirs() || directory.isDirectory()) {
            return directory;
        }

        throw new IOException("Can't create directory '" + directory + "'.");
    }

    /**
     * Deletes file or directory. Finishes quietly in case of no such file.
     * Directory will be deleted with each nested element.
     *
     * @param file File to be deleted.
     * @throws IOException if can't delete file.
     */
    @SuppressWarnings("OverlyComplexMethod")
    public static void deleteTotally(@Nullable File file) throws IOException {
        if (file == null) {
            return;
        }

        Path path = Paths.get(file.toURI());

        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            if (Files.isSymbolicLink(path)) {
                if (!file.delete() && Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                    throw new IOException("Can't delete symbolic link '" + file + "'.");
                }
            } else if (file.isFile()) {
                if (!file.delete() && file.exists()) {
                    throw new IOException("Can't delete file '" + file + "'.");
                }
            } else if (file.isDirectory()) {
                cleanDirectory(file, null);
                if (!file.delete() && file.exists()) {
                    throw new IOException("Can't delete directory '" + file + "'.");
                }
            } else if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                throw new IllegalArgumentException("Unsupported file system item '" + file + "'.");
            }
        }
    }

    /**
     * @param file File to be read.
     * @return String containing file data.
     * @throws IOException if can't read file. Possibly, file parameter
     *                     doesn't exist, is directory or not enough permissions.
     */
    public static String readFile(File file) throws IOException {
        return IoUtil.toString(new FileReader(file));
    }

    private static void ensureParentDirectoryExists(File file) throws IOException {
        File parentFile = file.getParentFile();
        if (parentFile != null) {
            ensureDirectoryExists(parentFile);
        }
    }

    /**
     * Cleans directory. All nested elements will be recursively deleted.
     *
     * @param directory        Directory to be deleted.
     * @param deleteFileFilter Filter of files to delete.
     * @throws IOException if argument is not a directory or can't clean directory
     */
    public static void cleanDirectory(File directory, @Nullable FileFilter deleteFileFilter) throws IOException {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("'" + directory + "' is not a directory.");
        }

        File[] files = directory.listFiles();
        if (files == null) {
            throw new IOException("Failed to list files of '" + directory + "'.");
        }

        for (int fileIndex = 0, fileCount = files.length; fileIndex < fileCount; ++fileIndex) {
            File file = files[fileIndex];

            if (deleteFileFilter == null || deleteFileFilter.accept(file)) {
                deleteTotally(file);
            } else if (file.isDirectory() && !Files.isSymbolicLink(Paths.get(file.toURI()))) {
                cleanDirectory(file, deleteFileFilter);
            }
        }
    }

    /**
     * Writes new file into filesystem. Overwrite existing if exists.
     * Creates parent directory if needed.
     *
     * @param file        File to be write.
     * @param inputStream Input stream to get data.
     * @throws IOException if can't read file.
     */
    public static void writeFile(File file, InputStream inputStream) throws IOException {
        ensureParentDirectoryExists(file);
        if (file.exists()) {
            deleteTotally(file);
        }

        try (OutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                int size = inputStream.read(buffer);
                if (size == -1) {
                    break;
                }
                if (size > 0) {
                    outputStream.write(buffer, 0, size);
                }
            }
        } finally {
            inputStream.close();
        }
    }

    /**
     * Writes new file into filesystem. Overwrite existing if exists.
     * Creates parent directory if needed.
     *
     * @param file    File to be write.
     * @param content Content to be write.
     * @throws IOException if can't read file.
     */
    public static void writeFile(File file, String content) throws IOException {
        writeFile(file, content, null);
    }

    /**
     * Writes new file into filesystem. Overwrite existing if exists.
     * Creates parent directory if needed.
     *
     * @param file     File to be write.
     * @param content  Content to be write.
     * @param encoding File encoding.
     * @throws UnsupportedEncodingException if the named encoding is not supported
     * @throws IOException                  if can't read file.
     */
    public static void writeFile(File file, String content, @Nullable String encoding) throws IOException {
        ensureParentDirectoryExists(file);

        Writer writer = null;
        try {
            writer = encoding == null
                    ? new OutputStreamWriter(new FileOutputStream(file))
                    : new OutputStreamWriter(new FileOutputStream(file), encoding);
            writer.write(content);
        } finally {
            IoUtil.closeQuietly(writer);
        }
    }

    /**
     * Writes new file into filesystem. Overwrite existing if exists.
     * Creates parent directory if needed.
     *
     * @param file  File to be write.
     * @param bytes Bytes to be write.
     * @throws IOException if can't write file.
     */
    public static void writeFile(File file, byte[] bytes) throws IOException {
        ensureParentDirectoryExists(file);

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
        } finally {
            IoUtil.closeQuietly(outputStream);
        }
    }

    /**
     * Very like to writeFile but doesn't overwrite file.
     * Creates parent directory if needed.
     *
     * @param file  File to write.
     * @param bytes Bytes to write into file.
     * @throws IOException If file exists or can't write file.
     */
    public static void createFile(File file, byte[] bytes) throws IOException {
        if (file.exists()) {
            throw new IOException("File exists " + file);
        }

        writeFile(file, bytes);
    }

    /**
     * Very like to writeFile but doesn't overwrite file.
     *
     * @param file    File to write.
     * @param content String to write into file.
     * @throws IOException If file exists or can't write file.
     */
    public static void createFile(File file, String content) throws IOException {
        if (file.exists()) {
            throw new IOException("File exists " + file);
        }

        writeFile(file, content);
    }

    /**
     * @param file File to remove.
     * @throws IOException If file not found or can't be removed.
     */
    public static void removeFile(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("File not found " + file);
        }

        if (!file.delete()) {
            throw new IOException("Can't delete " + file);
        }
    }

    /**
     * @param file File to be read.
     * @return File content as a byte array.
     * @throws IOException           if can't read file.
     * @throws FileNotFoundException if can't find file.
     */
    public static byte[] getBytes(File file) throws IOException {
        if (file instanceof TFile) {
            TFile trueZipFile = (TFile) file;

            try {
                if (trueZipFile.isFile()) {
                    long size = file.length();
                    InputStream stream = new TFileInputStream(file);
                    byte[] bytes = new byte[Ints.checkedCast(size)];
                    IOUtils.read(stream, bytes);
                    stream.close();
                    return bytes;
                }

                if (trueZipFile.isArchive()) {
                    TVFS.umount(trueZipFile);
                    file = new File(file.getAbsolutePath());
                    if (file.isFile()) {
                        return forceGetBytesFromExistingRegularFile(file);
                    }

                    TFile enclosingArchive = trueZipFile.getEnclArchive();
                    if (enclosingArchive != null && new File(enclosingArchive.getAbsolutePath()).isFile()) {
                        return ZipUtil.getZipEntryBytes(enclosingArchive, trueZipFile.getEnclEntryName());
                    }
                }
            } finally {
                TVFS.umount(trueZipFile);
            }
        } else {
            if (file.isFile()) {
                return forceGetBytesFromExistingRegularFile(file);
            }
        }

        throw new FileNotFoundException("'" + file + "' is not file.");
    }

    private static byte[] forceGetBytesFromExistingRegularFile(@Nonnull File file) throws IOException {
        long size = file.length();
        FileInputStream stream = new FileInputStream(file);
        FileChannel channel = stream.getChannel();
        ByteBuffer bytes = ByteBuffer.allocate(Ints.checkedCast(size));
        channel.read(bytes);
        channel.close();
        stream.close();
        return bytes.array();
    }

    /**
     * Returns 511 first bytes of the file. Returns smaller number of bytes it it contains less.
     *
     * @param file File to be read.
     * @return File content as a byte array.
     * @throws IOException           if can't read file.
     * @throws FileNotFoundException if can't find file.
     */
    public static FileUtil.FirstBytes getFirstBytes(File file) throws IOException {
        return getFirstBytes(file, 511);
    }

    /**
     * Returns {@code maxSize} first bytes of the file. Returns smaller number of bytes it it contains less.
     *
     * @param file    File to be read.
     * @param maxSize Max bytes to return.
     * @return File content as a byte array.
     * @throws IOException           if can't read file.
     * @throws FileNotFoundException if can't find file.
     */
    public static FileUtil.FirstBytes getFirstBytes(File file, long maxSize) throws IOException {
        if (file.isFile()) {
            boolean truncated = false;
            long size = file.length();
            if (size > maxSize) {
                truncated = true;
                size = maxSize;
            }
            FileInputStream stream = new FileInputStream(file);
            FileChannel channel = stream.getChannel();
            ByteBuffer bytes = ByteBuffer.allocate(Ints.checkedCast(size));
            channel.read(bytes);
            channel.close();
            stream.close();
            return new FileUtil.FirstBytes(truncated, bytes.array());
        } else {
            throw new FileNotFoundException("'" + file + "' is not file.");
        }
    }

    /**
     * Creates temporary directory with auto-generated name with specific prefix.
     *
     * @param prefix Prefix for directory name.
     * @return File instance.
     * @throws IOException if can't create directory.
     */
    public static File createTemporaryDirectory(String prefix) throws IOException {
        File file = internalCreateTempFile(prefix);
        deleteTotally(file);
        if (!file.mkdir()) {
            throw new IOException("Can't create directory '" + file + "'.");
        }
        return new TemporaryDirectory(file.getAbsolutePath());
    }

    /**
     * Creates temporary directory with auto-generated name with specific prefix.
     *
     * @param prefix          Prefix for directory name.
     * @param parentDirectory Parent directory for created one
     * @return File instance.
     * @throws IOException if can't create directory.
     */
    public static File createTemporaryDirectory(String prefix, File parentDirectory) throws IOException {
        File temporaryDirectory = new File(parentDirectory, prefix + '-' + RandomUtil.getRandomToken());
        ensureDirectoryExists(temporaryDirectory);
        return new TemporaryDirectory(temporaryDirectory.getAbsolutePath());
    }

    /**
     * @param file Any file.
     * @return String Name and extension of file (extension in lowercase). For example, "main.cpp".
     */
    public static String getNameAndExt(File file) {
        String path = file.getPath();
        int lastSep = Math.max(path.lastIndexOf(DOS_FILE_SEPARATOR), path.lastIndexOf(UNIX_FILE_SEPARATOR));
        if (lastSep == -1) {
            return path;
        } else {
            return path.substring(lastSep + 1);
        }
    }

    /**
     * @param file Any file.
     * @return String Name part (simple name without extension).
     */
    public static String getName(File file) {
        String nameAndExt = getNameAndExt(file);
        int dotIndex = nameAndExt.lastIndexOf('.');
        if (dotIndex == -1) {
            return nameAndExt;
        } else {
            return nameAndExt.substring(0, dotIndex);
        }
    }

    /**
     * @param file Any file.
     * @return String Extension with dot in lowercase. For example, ".cpp".
     */
    public static String getExt(File file) {
        String nameAndExt = getNameAndExt(file);
        int dotIndex = nameAndExt.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        } else {
            return nameAndExt.substring(dotIndex).toLowerCase();
        }
    }

    /**
     * @return System temporary directory. It expected that current process
     * has permissions for read, write and execution in it.
     */
    public static File getTemporaryDirectory() {
        return new File(getTemporaryDirFromResources());
    }

    private static File internalCreateTempFile(String prefix) {
        return new File(getTemporaryDirFromResources(), prefix + '-' + RandomUtil.getRandomToken());
    }

    private static void scanForList(File directoryOrFile, List<File> files) {
        if (!directoryOrFile.isHidden() && !".svn".equalsIgnoreCase(getNameAndExt(directoryOrFile))) {
            if (directoryOrFile.isFile()) {
                files.add(directoryOrFile);
                return;
            }

            if (directoryOrFile.isDirectory()) {
                File[] inner = directoryOrFile.listFiles();
                for (File file : inner) {
                    scanForList(file, files);
                }
                return;
            }

            throw new IllegalStateException(directoryOrFile + " doesn't exist");
        }
    }

    /**
     * @param directory Directory to be scanned.
     * @return List of nested files (scans nested directories recursively). Doesn't scan
     * hidden directories and doesn't return hidden files.
     */
    public static List<File> list(File directory) {
        if (directory.isDirectory()) {
            List<File> result = new ArrayList<>();
            scanForList(directory, result);
            return result;
        } else {
            throw new IllegalStateException(directory + " is expected to be directory");
        }
    }

    public static long getDirectorySize(File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Abstract path " + directory + " is not a directory");
        }

        long result = 0;

        File[] files = directory.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                result += file.length();
            }

            if (file.isDirectory()) {
                result += getDirectorySize(file);
            }
        }

        return result;
    }

    /**
     * @return Temporary directory.
     */
    private static String getTemporaryDirFromResources() {
        return TempDirHolder.tempDir;
    }

    public static String concatenatePaths(String... paths) {
        if (paths == null || paths.length == 0) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < paths.length; i++) {
            result.append(paths[i]);

            if (paths[i].isEmpty()) {
                result.append('.');
            }

            if (i < paths.length - 1) {
                result.append(File.separator);
            }
        }

        return result.toString();
    }

    /**
     * Renames source to destination. Or throws IOException if can't.
     *
     * @param sourceFile      Source.
     * @param destinationFile Destination.
     * @param overwrite       overwrite destinationFile if it exists
     * @throws IOException if can't rename.
     */
    public static void renameFile(File sourceFile, File destinationFile, boolean overwrite) throws IOException {
        ensureParentDirectoryExists(destinationFile);

        if (overwrite) {
            if (!sourceFile.renameTo(destinationFile)) {
                if (destinationFile.exists()) {
                    deleteTotally(destinationFile);
                    if (!sourceFile.renameTo(destinationFile)) {
                        throw new IOException("Can't overwrite while renaming " + sourceFile.getPath() + " to " +
                                destinationFile.getPath());
                    }
                } else {
                    throw new IOException("Can't rename " + sourceFile.getPath() + " to " + destinationFile.getPath());
                }
            }
        } else {
            if (!destinationFile.exists()) {
                if (!sourceFile.renameTo(destinationFile) && !destinationFile.isFile()) {
                    throw new IOException("Can't rename " + sourceFile.getPath() + " to " + destinationFile.getPath());
                }
            }
        }
    }

    public static byte[] downloadFileAsByteArray(String url, int maxSize) throws IOException {
        URL problemUrl = new URL(url);
        URLConnection connection = problemUrl.openConnection();
        return IoUtil.toByteArray(connection.getInputStream(), maxSize);
    }

    public static byte[] downloadFileAsByteArray(
            String url, int maxSize, String[] postParameterNames, String[] postParameterValues) throws IOException {
        int namesLength = ArrayUtils.getLength(postParameterNames);
        int valuesLength = ArrayUtils.getLength(postParameterValues);

        if (namesLength != valuesLength) {
            throw new IllegalArgumentException(String.format("Length of postParameterNames doesn't equal "
                    + "to length of postParameterValues: %d!=%d.", namesLength, valuesLength));
        }

        if (ArrayUtils.isEmpty(postParameterNames) || ArrayUtils.isEmpty(postParameterValues)) {
            return downloadFileAsByteArray(url, maxSize);
        }

        StringBuilder postParametersString = new StringBuilder();
        try {
            for (int i = 0; i < namesLength; i++) {
                if (i > 0) {
                    postParametersString.append('&');
                }

                postParametersString.append(URLEncoder.encode(postParameterNames[i], "UTF-8"));
                postParametersString.append('=');
                postParametersString.append(URLEncoder.encode(postParameterValues[i], "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding for encode post parameters", e);
        }

        URL problemUrl = new URL(url);
        URLConnection connection = problemUrl.openConnection();
        connection.setDoOutput(true);

        Writer writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(postParametersString.toString());
        writer.close();

        return IoUtil.toByteArray(connection.getInputStream(), maxSize);
    }

    @SuppressWarnings({"FinalizeDeclaration", "DeserializableClassInSecureContext"})
    private static class TemporaryDirectory extends File {
        private TemporaryDirectory(String pathname) {
            super(pathname);
        }

        @Override
        protected void finalize() throws Throwable {
            if (isDirectory()) {
                logger.error("Temporary directory is not deleted [path='" + getAbsolutePath() + "'].");
            }
            super.finalize();
        }
    }

    private static class TempDirHolder {
        private static final String tempDir = initializeTempDir();

        private static String initializeTempDir() {
            try {
                String tempDirName = CommonsPropertiesUtil.getApplicationTempDirName();

                File dir = File.createTempFile(tempDirName, "");
                File temp = new File(dir.getParentFile(), tempDirName);

                if (!dir.delete()) {
                    throw new IllegalStateException("Can't delete temp directory " + dir);
                }

                ensureDirectoryExists(temp);

                return temp.getAbsolutePath();
            } catch (IOException e) {
                throw new IllegalStateException("Can't create temporary directory", e);
            }
        }
    }
}
