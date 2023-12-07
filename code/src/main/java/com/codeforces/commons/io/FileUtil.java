package com.codeforces.commons.io;

import com.codeforces.commons.compress.ZipUtil;
import com.codeforces.commons.io.internal.UnsafeFileUtil;
import com.codeforces.commons.math.NumberUtil;
import com.codeforces.commons.process.ThreadUtil;
import com.codeforces.commons.text.StringUtil;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author Mike Mirzayanov
 * @author Maxim Shipko (sladethe@gmail.com)
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class FileUtil {
    public static final long TB_PER_PB = 1024L;

    public static final long GB_PER_TB = 1024L;
    public static final long GB_PER_PB = GB_PER_TB * TB_PER_PB;

    public static final long MB_PER_GB = 1024L;
    public static final long MB_PER_TB = MB_PER_GB * GB_PER_TB;
    public static final long MB_PER_PB = MB_PER_TB * TB_PER_PB;

    public static final long KB_PER_MB = 1024L;
    public static final long KB_PER_GB = KB_PER_MB * MB_PER_GB;
    public static final long KB_PER_TB = KB_PER_GB * GB_PER_TB;
    public static final long KB_PER_PB = KB_PER_TB * TB_PER_PB;

    public static final long BYTES_PER_KB = 1024L;
    public static final long BYTES_PER_MB = BYTES_PER_KB * KB_PER_MB;
    public static final long BYTES_PER_GB = BYTES_PER_MB * MB_PER_GB;
    public static final long BYTES_PER_TB = BYTES_PER_GB * GB_PER_TB;
    public static final long BYTES_PER_PB = BYTES_PER_TB * TB_PER_PB;

    private static final Pattern SIZE_PATTERN = Pattern.compile("(0|[1-9][01-9]{0,5})(\\.[01-9]{1,5})? ?[KMGTP]?B?");

    private FileUtil() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static <T> T executeIoOperation(ThreadUtil.Operation<T> operation) throws IOException {
        return executeIoOperation(operation, 9);
    }

    @Nullable
    public static <T> T executeIoOperation(ThreadUtil.Operation<T> operation, int attemptCount) throws IOException {
        return executeIoOperation(operation, attemptCount, 50L, ThreadUtil.ExecutionStrategy.Type.SQUARE);
    }

    @Nullable
    public static <T> T executeIoOperation(
            ThreadUtil.Operation<T> operation, int attemptCount, long delayTimeMillis,
            ThreadUtil.ExecutionStrategy.Type strategyType) throws IOException {
        try {
            return ThreadUtil.execute(
                    operation, attemptCount,
                    new ThreadUtil.ExecutionStrategy(delayTimeMillis, strategyType)
            );
        } catch (IOException | RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    /**
     * @param file Existing file.
     * @return SHA-1 hashCode in hexadecimal.
     * @throws IOException Can't perform IO.
     */
    @Nonnull
    public static String sha1(File file) throws IOException {
        return Objects.requireNonNull(executeIoOperation(() -> UnsafeFileUtil.sha1Hex(file)));
    }

    /**
     * Copies one file to another. Overwrites it if exists.
     *
     * @param source      Source file.
     * @param destination Destination file.
     * @throws IOException Can't perform copy.
     */
    public static void copyFile(File source, File destination) throws IOException {
        executeIoOperation(() -> {
            UnsafeFileUtil.copyFile(source, destination);
            return null;
        });
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
        executeIoOperation(() -> {
            UnsafeFileUtil.copyDirectory(source, destination);
            return null;
        });
    }

    /**
     * Ensures that file does exist and creates it if not.
     *
     * @param file File to check
     * @return created file
     * @throws IOException if file does not exist and can't be created
     */
    @Nonnull
    public static File ensureFileExists(File file) throws IOException {
        return Objects.requireNonNull(executeIoOperation(() -> UnsafeFileUtil.ensureFileExists(file)));
    }

    /**
     * Ensures that directory does exist and creates it if not.
     *
     * @param directory Directory to check
     * @return created directory
     * @throws IOException if directory does not exist and can't be created
     */
    @Nonnull
    public static File ensureDirectoryExists(File directory) throws IOException {
        return Objects.requireNonNull(executeIoOperation(() -> UnsafeFileUtil.ensureDirectoryExists(directory)));
    }

    /**
     * Ensures that parent directory of specified file or directory does exist and creates it if not.
     *
     * @param file Directory or file to get parent directory
     * @return created directory
     * @throws IOException if directory does not exist and can't be created
     */
    @Nullable
    public static File ensureParentDirectoryExists(@Nonnull File file) throws IOException {
        File directory = file.getParentFile();
        if (directory == null) {
            return null;
        }

        return executeIoOperation(() -> UnsafeFileUtil.ensureDirectoryExists(directory));
    }

    /**
     * Deletes file or directory. Finishes quietly in case of no such file.
     * Directory will be deleted with each nested element.
     *
     * @param file File to be deleted.
     * @throws IOException if can't delete file.
     */
    public static void deleteTotally(@Nullable File file) throws IOException {
        executeIoOperation(() -> {
            UnsafeFileUtil.deleteTotally(file);
            return null;
        });
    }

    /**
     * Deletes file or directory. Finishes quietly in any case.
     * Directory will be deleted with each nested element.
     *
     * @param file File to be deleted.
     */
    public static void deleteQuietly(@Nullable File file) {
        try {
            deleteTotally(file);
        } catch (IOException ignored) {
            // No operations.
        }
    }

    /**
     * Deletes file or directory. Finishes quietly in _any_ case.
     * Will start new thread.
     *
     * @param file File to be deleted.
     */
    public static void deleteTotallyAsync(@Nullable File file) {
        new Thread(() -> {
            try {
                deleteTotally(file);
            } catch (IOException ignored) {
                // No operations.
            }
        }).start();
    }

    /**
     * Cleans directory. All nested elements will be recursively deleted.
     *
     * @param directory        Directory to be deleted
     * @param deleteFileFilter Filter of files to delete
     * @throws IOException if argument is not a directory or can't clean directory
     */
    public static void cleanDirectory(File directory, @Nullable FileFilter deleteFileFilter)
            throws IOException {
        executeIoOperation(() -> {
            UnsafeFileUtil.cleanDirectory(directory, deleteFileFilter);
            return null;
        });
    }

    /**
     * Cleans directory. All nested elements will be recursively deleted.
     *
     * @param directory Directory to be deleted
     * @throws IOException if argument is not a directory or can't clean directory
     */
    public static void cleanDirectory(File directory) throws IOException {
        executeIoOperation(() -> {
            UnsafeFileUtil.cleanDirectory(directory, null);
            return null;
        });
    }

    /**
     * Cleans directory asynchronously. All nested elements will be recursively deleted.
     *
     * @param directory        Directory to be deleted
     * @param deleteFileFilter Filter of files to delete
     */
    public static void cleanDirectoryAsync(File directory, @Nullable FileFilter deleteFileFilter) {
        new Thread(() -> {
            try {
                cleanDirectory(directory, deleteFileFilter);
            } catch (IOException ignored) {
                // No operations.
            }
        }).start();
    }

    /**
     * Cleans directory asynchronously. All nested elements will be recursively deleted.
     *
     * @param directory Directory to be deleted
     */
    public static void cleanDirectoryAsync(File directory) {
        new Thread(() -> {
            try {
                cleanDirectory(directory);
            } catch (IOException ignored) {
                // No operations.
            }
        }).start();
    }

    /**
     * @param file File to be read.
     * @return String containing file data.
     * @throws IOException if can't read file. Possibly, file parameter
     *                     doesn't exists, is directory or not enough permissions.
     */
    @Nonnull
    public static String readFile(File file) throws IOException {
        return Objects.requireNonNull(executeIoOperation(() -> UnsafeFileUtil.readFile(file)));
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
        UnsafeFileUtil.writeFile(file, inputStream);
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
        executeIoOperation(() -> {
            UnsafeFileUtil.writeFile(file, content);
            return null;
        });
    }

    /**
     * Writes new file into filesystem. Overwrite existing if exists.
     * Creates parent directory if needed.
     *
     * @param file     File to be write.
     * @param content  Content to be write.
     * @param encoding File encoding.
     * @throws IOException                  if can't read file.
     * @throws UnsupportedEncodingException illegal encoding.
     */
    public static void writeFile(File file, String content, String encoding) throws IOException {
        executeIoOperation(() -> {
            UnsafeFileUtil.writeFile(file, content, encoding);
            return null;
        });
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
        executeIoOperation(() -> {
            UnsafeFileUtil.writeFile(file, bytes);
            return null;
        });
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
        executeIoOperation(() -> {
            UnsafeFileUtil.createFile(file, bytes);
            return null;
        });
    }

    /**
     * Very like to writeFile but doesn't overwrite file.
     *
     * @param file    File to write.
     * @param content String to write into file.
     * @throws IOException If file exists or can't write file.
     */
    public static void createFile(File file, String content) throws IOException {
        executeIoOperation(() -> {
            UnsafeFileUtil.createFile(file, content);
            return null;
        });
    }

    /**
     * @param file File to remove.
     * @throws IOException If file not found or can't be removed.
     */
    public static void removeFile(File file) throws IOException {
        executeIoOperation(() -> {
            UnsafeFileUtil.removeFile(file);
            return null;
        });
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
        executeIoOperation(() -> {
            UnsafeFileUtil.renameFile(sourceFile, destinationFile, overwrite);
            return null;
        });
    }

    public static void moveFile(File sourceFile, File destinationFile, boolean overwrite) throws IOException {
        try {
            renameFile(sourceFile, destinationFile, overwrite);
        } catch (IOException ignored) {
            if (overwrite || !destinationFile.exists()) {
                copyFile(sourceFile, destinationFile);
                removeFile(sourceFile);
            }
        }
    }

    /**
     * @param file File to be read.
     * @return File content as a byte array.
     * @throws IOException           if can't read file.
     * @throws FileNotFoundException if can't find file.
     */
    @Nonnull
    public static byte[] getBytes(File file) throws IOException {
        return Objects.requireNonNull(executeIoOperation(() -> UnsafeFileUtil.getBytes(file)));
    }

    /**
     * @param file File to be read.
     * @return File content as a byte array.
     * @throws IOException           if can't read file.
     * @throws FileNotFoundException if can't find file.
     */
    @Nonnull
    public static byte[] getBytes(String file) throws IOException {
        return getBytes(new File(file));
    }

    /**
     * Returns 511 first bytes of the file. Returns smaller number of bytes it it contains less.
     *
     * @param file File to be read.
     * @return File content as a byte array.
     * @throws IOException           if can't read file.
     * @throws FileNotFoundException if can't find file.
     */
    @Nonnull
    public static FirstBytes getFirstBytes(File file) throws IOException {
        return Objects.requireNonNull(executeIoOperation(new ThreadUtil.Operation<FirstBytes>() {
            @Nonnull
            @Override
            public FirstBytes run() throws IOException {
                return UnsafeFileUtil.getFirstBytes(file);
            }
        }));
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
    @Nonnull
    public static FirstBytes getFirstBytes(File file, long maxSize) throws IOException {
        return Objects.requireNonNull(executeIoOperation(() -> UnsafeFileUtil.getFirstBytes(file, maxSize)));
    }

    @Nonnull
    public static InputStream getInputStream(File file) throws IOException {
        return Objects.requireNonNull(executeIoOperation(() -> UnsafeFileUtil.getInputStream(file)));
    }

    /**
     * Creates temporary directory with auto-generated name with specific prefix.
     *
     * @param prefix Prefix for directory name.
     * @return File instance.
     * @throws IOException if can't create directory.
     */
    @Nonnull
    public static File createTemporaryDirectory(String prefix) throws IOException {
        return Objects.requireNonNull(executeIoOperation(() -> UnsafeFileUtil.createTemporaryDirectory(prefix)));
    }

    /**
     * Creates temporary directory with auto-generated name with specific prefix within specified parent directory.
     *
     * @param prefix          Prefix for directory name.
     * @param parentDirectory Parent directory for created one
     * @return File instance.
     * @throws IOException if can't create directory.
     */
    @Nonnull
    public static File createTemporaryDirectory(String prefix, File parentDirectory) throws IOException {
        return Objects.requireNonNull(executeIoOperation(() -> UnsafeFileUtil.createTemporaryDirectory(prefix, parentDirectory)));
    }

    /**
     * @param file Any file.
     * @return String Name and extension of file (extension in lowercase). For example, "main.cpp".
     */
    public static String getNameAndExt(File file) {
        return UnsafeFileUtil.getNameAndExt(file);
    }

    /**
     * @param fileName Any file name.
     * @return String Name and extension of file (extension in lowercase). For example, "main.cpp".
     */
    public static String getNameAndExt(String fileName) {
        return UnsafeFileUtil.getNameAndExt(new File(fileName));
    }

    /**
     * @param file Any file.
     * @return String Name part (simple name without extension).
     */
    @Contract("null -> fail")
    public static String getName(@Nonnull File file) {
        return UnsafeFileUtil.getName(file);
    }

    /**
     * @param fileName Any file name.
     * @return String Name part (simple name without extension).
     */
    public static String getName(String fileName) {
        return getName(new File(fileName));
    }

    /**
     * @param file Any file.
     * @return String Extension with dot in lowercase. For example, ".cpp".
     */
    public static String getExt(File file) {
        return UnsafeFileUtil.getExt(file);
    }

    /**
     * @param fileName Any file name.
     * @return String Extension with dot in lowercase. For example, ".cpp".
     */
    public static String getExt(String fileName) {
        return UnsafeFileUtil.getExt(new File(fileName));
    }

    /**
     * Checks if specified file is file.
     *
     * @param file File to check.
     * @return {@code true} iff specified file is not {@code null} and is file.
     */
    @Contract("null -> false")
    public static boolean isFile(@Nullable File file) {
        return file != null && file.isFile();
    }

    /**
     * Checks if specified file is directory.
     *
     * @param file File to check.
     * @return {@code true} iff specified file is not {@code null} and is directory.
     */
    @Contract("null -> false")
    public static boolean isDirectory(@Nullable File file) {
        return file != null && file.isDirectory();
    }

    /**
     * Checks if specified file exists.
     *
     * @param file File to check.
     * @return {@code true} iff specified file is not {@code null} and exists.
     */
    @Contract("null -> false")
    public static boolean exists(@Nullable File file) {
        return file != null && file.exists();
    }

    /**
     * @return System temporary directory. It expected that current process
     * has permissions for read, write and execution in it.
     * @throws IOException error.
     */
    @Nonnull
    public static File getTemporaryDirectory() throws IOException {
        return Objects.requireNonNull(executeIoOperation(UnsafeFileUtil::getTemporaryDirectory));
    }

    /**
     * @param directory Directory to be scanned.
     * @return List of nested files (scans nested directories recursively). Doesn't scan
     * hidden directories and doesn't return hidden files.
     * @throws IOException error.
     */
    @Nonnull
    public static List<File> list(@Nonnull File directory) throws IOException {
        return Objects.requireNonNull(executeIoOperation(() -> UnsafeFileUtil.list(directory)));
    }

    @Nonnull
    public static List<String> listRelativePaths(@Nonnull File directory, @Nullable FileFilter filter,
                                                 boolean recursive) throws IOException {
        return Objects.requireNonNull(executeIoOperation(() -> UnsafeFileUtil.listRelativePaths(directory, filter, recursive)));
    }

    public static long getDirectorySize(File directory) throws IOException {
        return Objects.requireNonNull(executeIoOperation(() -> UnsafeFileUtil.getDirectorySize(directory)));
    }

    public static File hideFile(File file) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());

        Boolean dosHidden = (Boolean) Files.getAttribute(path, "dos:hidden", LinkOption.NOFOLLOW_LINKS);
        if (dosHidden != null && !dosHidden) {
            Files.setAttribute(path, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
        }

        return file;
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static File hideFileQuietly(@Nullable File file) {
        try {
            if (file != null && file.exists()) {
                hideFile(file);
            }
        } catch (IOException ignored) {
            // No operations.
        }

        return file;
    }

    @Contract("null -> fail")
    @Nonnull
    public static File getCriticalBackupFile(@Nonnull File file) {
        return new File(UnsafeFileUtil.getPrefixPath(file) + getName(file) + ".bak");
    }

    @Contract("null -> fail")
    @Nullable
    private static byte[] getSubscribedFileBytes(@Nonnull File subscribedFile) throws IOException {
        if (subscribedFile.isFile()) {
            int digestLength = 32;
            byte[] subscribedBytes = getBytes(subscribedFile);
            int subscribedByteCount = subscribedBytes.length;

            if (subscribedByteCount >= digestLength) {
                byte[] bytes = new byte[subscribedByteCount - digestLength];
                byte[] digest = new byte[digestLength];

                System.arraycopy(subscribedBytes, 0, bytes, 0, subscribedByteCount - digestLength);
                System.arraycopy(subscribedBytes, subscribedByteCount - digestLength, digest, 0, digestLength);

                if (Arrays.equals(DigestUtils.sha256(bytes), digest)) {
                    return bytes;
                }
            }
        }

        return null;
    }

    @Contract("null, _ -> fail; _, null -> fail")
    @Nonnull
    public static byte[] getCriticalFileBytes(@Nonnull File file, @Nonnull File backupFile) throws IOException {
        byte[] subscribedFileBytes = getSubscribedFileBytes(file);
        if (subscribedFileBytes != null) {
            deleteTotally(backupFile);
            return subscribedFileBytes;
        }

        subscribedFileBytes = getSubscribedFileBytes(backupFile);
        if (subscribedFileBytes != null) {
            copyFile(backupFile, file);
            deleteTotally(backupFile);
            return subscribedFileBytes;
        }

        throw new IOException("Can't read neither critical file '" + file + "', nor backup file '" + backupFile + "'.");
    }

    @Contract("null -> fail")
    @Nonnull
    public static byte[] getCriticalFileBytes(@Nonnull File file) throws IOException {
        return getCriticalFileBytes(file, getCriticalBackupFile(file));
    }

    @Contract("null, _ -> fail; _, null -> fail")
    private static void writeSubscribedFile(@Nonnull File subscribedFile, @Nonnull byte[] bytes) throws IOException {
        byte[] digest = DigestUtils.sha256(bytes);
        byte[] subscribedBytes = new byte[bytes.length + digest.length];

        System.arraycopy(bytes, 0, subscribedBytes, 0, bytes.length);
        System.arraycopy(digest, 0, subscribedBytes, bytes.length, digest.length);

        writeFile(subscribedFile, subscribedBytes);
    }

    @Contract("null, _, _ -> fail; _, null, _ -> fail; _, _, null -> fail")
    public static void writeCriticalFile(
            @Nonnull File file, @Nonnull File backupFile, @Nonnull byte[] bytes) throws IOException {
        if (isFile(file)) {
            copyFile(file, backupFile);
        }

        writeSubscribedFile(file, bytes);
        deleteTotally(backupFile);
    }

    @Contract("null, _ -> fail; _, null -> fail")
    public static void writeCriticalFile(@Nonnull File file, @Nonnull byte[] bytes) throws IOException {
        writeCriticalFile(file, getCriticalBackupFile(file), bytes);
    }

    /**
     * Compares two files or directories by content.
     *
     * @param fileA first file or directory
     * @param fileB second file or directory
     * @return {@code true} iff both items A and B are {@code null},
     * {@link File#equals(Object) equals} or have the same content
     * @throws IOException in case of any I/O-exception
     */
    @Contract("null, null -> true; null, !null -> false; !null, null -> false")
    public static boolean equalsOrSameContent(@Nullable File fileA, @Nullable File fileB) throws IOException {
        if (fileA == null && fileB == null) {
            return true;
        }

        if (fileA == null ^ fileB == null) {
            return false;
        }

        try {
            return internalEqualsOrSameContent(fileA, fileB);
        } finally {
            synchronizeZipFilesQuietly(fileA, fileB);
        }
    }

    @SuppressWarnings("OverlyComplexMethod")
    private static boolean internalEqualsOrSameContent(@Nonnull File fileA, @Nonnull File fileB) throws IOException {
        if (fileA.equals(fileB)) {
            return true;
        }

        if (fileA.isFile()) {
            if (fileB.isFile()) {
                InputStream inputStreamA = null;
                InputStream inputStreamB = null;

                try {
                    return fileA.length() == fileB.length() && IoUtil.contentEquals(
                            inputStreamA = fileA instanceof TFile
                                    ? new TFileInputStream(fileA)
                                    : new FileInputStream(fileA),
                            inputStreamB = fileB instanceof TFile
                                    ? new TFileInputStream(fileB)
                                    : new FileInputStream(fileB)
                    );
                } finally {
                    IoUtil.closeQuietly(inputStreamA, inputStreamB);
                }
            } else {
                return false;
            }
        }

        if (fileA.isDirectory()) {
            if (fileB.isDirectory()) {
                File[] childrenA = fileA.listFiles();
                int childACount = childrenA == null ? 0 : childrenA.length;

                if (childACount != ArrayUtils.getLength(fileB.listFiles())) {
                    return false;
                }

                for (int childIndex = 0; childIndex < childACount; ++childIndex) {
                    File childA = childrenA[childIndex];
                    File childB = fileB instanceof TFile
                            ? new TFile(fileB, childA.getName())
                            : new File(fileB, childA.getName());

                    if (!internalEqualsOrSameContent(childA, childB)) {
                        return false;
                    }
                }

                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    private static void synchronizeZipFilesQuietly(File... files) {
        for (int fileIndex = 0, fileCount = files.length; fileIndex < fileCount; ++fileIndex) {
            File file = files[fileIndex];
            if (file instanceof TFile) {
                ZipUtil.synchronizeQuietly((TFile) file);
            }
        }
    }

    @SuppressWarnings({"AssignmentToCollectionOrArrayFieldFromParameter", "ReturnOfCollectionOrArrayField"})
    public static class FirstBytes implements Serializable {
        private final boolean truncated;
        private final byte[] bytes;

        public FirstBytes(boolean truncated, byte[] bytes) {
            this.truncated = truncated;
            this.bytes = bytes;
        }

        public boolean isTruncated() {
            return truncated;
        }

        public byte[] getBytes() {
            return bytes;
        }

        @Nullable
        public String getUtf8String() {
            return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
        }

        @Nullable
        public String getString(String charset) throws UnsupportedEncodingException {
            return bytes == null ? null : new String(bytes, charset);
        }

        @Nullable
        public String getString(Charset charset) {
            return bytes == null ? null : new String(bytes, charset);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            FirstBytes firstBytes = (FirstBytes) o;

            return truncated == firstBytes.truncated && Arrays.equals(bytes, firstBytes.bytes);
        }

        @Override
        public int hashCode() {
            int result = truncated ? 1 : 0;
            result = 32323 * result + (bytes != null ? Arrays.hashCode(bytes) : 0);
            return result;
        }
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "AccessOfSystemProperties"})
    public static FirstBytes concatenate(FirstBytes first, FirstBytes second) {
        if (ArrayUtils.isEmpty(first.bytes)) {
            return second;
        }

        if (ArrayUtils.isEmpty(second.bytes)) {
            return first;
        }

        byte[] result;

        if (first.bytes[first.bytes.length - 1] == 10) { // ends with \n
            result = new byte[first.bytes.length + second.bytes.length];
            System.arraycopy(first.bytes, 0, result, 0, first.bytes.length);
            System.arraycopy(second.bytes, 0, result, first.bytes.length, second.bytes.length);
        } else {
            byte[] lineSep = System.getProperty("line.separator").getBytes(StandardCharsets.UTF_8);
            result = new byte[first.bytes.length + lineSep.length + second.bytes.length];
            System.arraycopy(first.bytes, 0, result, 0, first.bytes.length);
            System.arraycopy(lineSep, 0, result, first.bytes.length, lineSep.length);
            System.arraycopy(second.bytes, 0, result, first.bytes.length + lineSep.length, second.bytes.length);
        }

        return new FirstBytes(first.isTruncated() || second.isTruncated(), result);
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "ForLoopWithMissingComponent", "AssignmentToForLoopParameter"})
    public static FirstBytes removeLinesStartingWith(FirstBytes lines, byte[] prefix) {
        if (ArrayUtils.isEmpty(lines.bytes)) {
            return lines;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream(lines.bytes.length);
        for (int lineStart = 0; lineStart < lines.bytes.length; ) {
            int linePos = 0;
            boolean startsWithPrefix = true;
            while (lineStart + linePos < lines.bytes.length) {
                if (linePos < prefix.length
                        && lines.bytes[lineStart + linePos] != prefix[linePos]) {
                    startsWithPrefix = false;
                }

                if (lines.bytes[lineStart + linePos] == 10) {
                    linePos++;
                    break;
                }

                linePos++;
            }

            if (!startsWithPrefix) {
                output.write(lines.bytes, lineStart, linePos);
            }

            lineStart += linePos;
        }

        return new FirstBytes(lines.truncated, output.toByteArray());
    }

    @Contract("null -> false")
    public static boolean isSymbolicLink(@Nullable File file) {
        return exists(file) && Files.isSymbolicLink(Paths.get(file.toURI()));
    }

    public static void createSymbolicLink(@Nonnull File source, @Nonnull File target) throws IOException {
        if (!source.exists()) {
            throw new IOException("Source '" + source + "' doesn't exist.");
        }

        deleteTotally(target);
        ensureParentDirectoryExists(target);

        try {
            Files.createSymbolicLink(Paths.get(target.toURI()), Paths.get(source.toURI()));
        } catch (RuntimeException e) {
            throw new IOException(String.format(
                    "Can't create the symbolic link '%s' to '%s'.", target, source
            ), e);
        }
    }

    public static void createSymbolicLinkOrCopy(@Nonnull File source, @Nonnull File target) throws IOException {
        if (!source.exists()) {
            throw new IOException("Source '" + source + "' doesn't exist.");
        }

        deleteTotally(target);
        ensureParentDirectoryExists(target);

        try {
            Files.createSymbolicLink(Paths.get(target.toURI()), Paths.get(source.toURI()));
        } catch (UnsupportedOperationException | IOException | InternalError ignored) {
            if (isFile(source)) {
                UnsafeFileUtil.copyFile(source, target);
            } else if (isDirectory(source)) {
                UnsafeFileUtil.copyDirectory(source, target);
            } else {
                throw new IOException("Unexpected source '" + source + "'.");
            }
        }
    }

    @Nonnull
    public static String formatSize(@Nonnegative long size) {
        if (size < 0) {
            throw new IllegalArgumentException("Argument 'size' must be a positive integer or zero.");
        }

        if (size >= BYTES_PER_PB) {
            return formatSize(size, BYTES_PER_PB, "PB");
        }

        if (size >= BYTES_PER_TB) {
            return formatSize(size, BYTES_PER_TB, "TB");
        }

        if (size >= BYTES_PER_GB) {
            return formatSize(size, BYTES_PER_GB, "GB");
        }

        if (size >= BYTES_PER_MB) {
            return formatSize(size, BYTES_PER_MB, "MB");
        }

        if (size >= BYTES_PER_KB) {
            return formatSize(size, BYTES_PER_KB, "kB");
        }

        return size + " B";
    }

    @Nonnull
    private static String formatSize(@Nonnegative long size, @Nonnegative long unit, @Nonnull String unitName) {
        if (size % unit == 0) {
            return size / unit + " " + unitName;
        }

        return String.format(Locale.US, "%.1f %s", (double) size / (double) unit, unitName);
    }

    public static long parseSize(@Nullable String size) {
        size = StringUtil.trimToNull(size);
        if (size == null) {
            return 0L;
        }

        size = size.toUpperCase();

        if (!SIZE_PATTERN.matcher(size).matches()) {
            throw new IllegalArgumentException(String.format(
                    "'%s' does not match the pattern '%s'.", size, SIZE_PATTERN
            ));
        }

        int lastCharIndex = size.length() - 1;
        char lastChar = size.charAt(lastCharIndex);

        if (lastChar == 'B') {
            size = size.substring(0, lastCharIndex);

            lastCharIndex = size.length() - 1;
            lastChar = size.charAt(lastCharIndex);
        }

        switch (lastChar) {
            case 'K':
                return parseSize(size, lastCharIndex, BYTES_PER_KB);
            case 'M':
                return parseSize(size, lastCharIndex, BYTES_PER_MB);
            case 'G':
                return parseSize(size, lastCharIndex, BYTES_PER_GB);
            case 'T':
                return parseSize(size, lastCharIndex, BYTES_PER_TB);
            case 'P':
                return parseSize(size, lastCharIndex, BYTES_PER_PB);
            default:
                return NumberUtil.toLong(Double.parseDouble(size.trim()));
        }
    }

    private static long parseSize(@Nonnull String size, @Nonnegative int lastCharIndex, @Nonnegative long unit) {
        return NumberUtil.toLong(Double.parseDouble(size.substring(0, lastCharIndex).trim()) * unit);
    }

    /**
     * Excludes service files, such as hidden or svn.
     */
    public static class NonServiceFileFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            File file = new File(dir, name);
            return !file.isHidden() && !".svn".equalsIgnoreCase(file.getName());
        }
    }
}
