package com.codeforces.commons.compress;

import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.text.StringUtil;
import com.google.common.primitives.Ints;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import de.schlichtherle.truezip.file.TFileOutputStream;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.FsSyncException;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.NameFileFilter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.*;

import static java.lang.StrictMath.max;

/**
 * @author Mike Mirzayanov
 */
public final class ZipUtil {
    public static final int MINIMAL_COMPRESSION_LEVEL = 0;
    public static final int DEFAULT_COMPRESSION_LEVEL = 5;
    public static final int MAXIMAL_COMPRESSION_LEVEL = 9;

    private static final long MAX_ZIP_ENTRY_SIZE = 512L * FileUtil.BYTES_PER_MB;
    private static final long MAX_ZIP_ENTRY_COUNT = 50000L;

    private static final int DEFAULT_BUFFER_SIZE = Ints.checkedCast(FileUtil.BYTES_PER_MB);

    private ZipUtil() {
        throw new UnsupportedOperationException();
    }

    public static void compress(InputStream plainTextInputStream, OutputStream compressedTextOutputStream)
            throws IOException {
        compress(plainTextInputStream, compressedTextOutputStream, DEFAULT_COMPRESSION_LEVEL);
    }

    public static void compress(InputStream plainTextInputStream, OutputStream compressedTextOutputStream, int level)
            throws IOException {
        Deflater compressor = new Deflater();
        compressor.setLevel(level);
        IOUtils.copy(new DeflaterInputStream(plainTextInputStream, compressor), compressedTextOutputStream);
    }

    public static void decompress(InputStream compressedTextInputStream, OutputStream plainTextOutputStream)
            throws IOException {
        IOUtils.copy(compressedTextInputStream, new InflaterOutputStream(plainTextOutputStream));
    }

    public static byte[] compress(byte[] bytes) {
        return compress(bytes, DEFAULT_COMPRESSION_LEVEL);
    }

    public static byte[] compress(byte[] bytes, int level) {
        if (bytes.length == 0) {
            return bytes;
        }

        Deflater compressor = new Deflater();
        compressor.setLevel(level);
        compressor.setInput(bytes);
        compressor.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bytes.length);

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        while (!compressor.finished()) {
            outputStream.write(buffer, 0, compressor.deflate(buffer));
        }

        IOUtils.closeQuietly(outputStream);

        return outputStream.toByteArray();
    }

    public static byte[] decompress(byte[] bytes) throws DataFormatException {
        if (bytes.length == 0) {
            return bytes;
        }

        Inflater decompressor = new Inflater();
        decompressor.setInput(bytes);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        while (!decompressor.finished()) {
            outputStream.write(buffer, 0, decompressor.inflate(buffer));
        }

        decompressor.end();
        return outputStream.toByteArray();
    }

    /**
     * Adds a directory to a ZIP-archive. Uses default level of compression.
     * Ignores &quot;.svn&quot; files and directories.
     *
     * @param source      directory to compress, will not be added itself;
     *                    source directory child files will be placed in the root of archive
     * @param destination ZIP-archive
     * @throws java.io.IOException if any IO-exception occured
     */
    public static void zipExceptSvn(File source, File destination) throws IOException {
        zipExceptSvn(source, destination, DEFAULT_COMPRESSION_LEVEL);
    }

    /**
     * Adds a directory to a ZIP-archive. Uses default level of compression.
     *
     * @param source      directory to compress, will not be added itself;
     *                    source directory child files will be placed in the root of archive
     * @param destination ZIP-archive
     * @param skipFilter  skipped files filter or {@code null} to accept all files
     * @throws java.io.IOException if any IO-exception occured
     */
    public static void zip(File source, File destination, @Nullable FileFilter skipFilter) throws IOException {
        zip(source, destination, DEFAULT_COMPRESSION_LEVEL, skipFilter);
    }

    /**
     * Adds a directory to a ZIP-archive. Ignores &quot;.svn&quot; files and directories.
     *
     * @param source      directory to compress, will not be added itself;
     *                    source directory child files will be placed in the root of archive
     * @param destination ZIP-archive
     * @param level       compression level (0-9)
     * @throws java.io.IOException if any IO-exception occured
     */
    public static void zipExceptSvn(File source, File destination, int level) throws IOException {
        zip(source, destination, level, new NameFileFilter(".svn", IOCase.SYSTEM));
    }

    /**
     * Adds a directory to a ZIP-archive.
     *
     * @param source      directory to compress, will not be added itself;
     *                    source directory child files will be placed in the root of archive
     * @param destination ZIP-archive
     * @param level       compression level (0-9)
     * @param skipFilter  skipped files filter or {@code null} to accept all files
     * @throws java.io.IOException if any IO-exception occured
     */
    public static void zip(File source, File destination, int level, @Nullable FileFilter skipFilter)
            throws IOException {
        try {
            ZipParameters parameters = new ZipParameters();
            parameters.setIncludeRootFolder(false);
            parameters.setCompressionLevel(level);
            parameters.setReadHiddenFiles(true);
            parameters.setDefaultFolderPath(source.getAbsolutePath());

            ZipFile zipFile = new ZipFile(destination);
            zipFile.addFiles(deepListFilesInDirectory(source, skipFilter, !parameters.isReadHiddenFiles()), parameters);
        } catch (ZipException e) {
            throw new IOException("Can't add directory to ZIP-file.", e);
        }
    }

    /**
     * Adds a directory to a ZIP-archive and returns its bytes. Uses default level of compression.
     * Ignores &quot;.svn&quot; files and directories.
     *
     * @param source directory to compress, will not be added itself;
     *               source directory child files will be placed in the root of archive
     * @return ZIP-archive bytes
     * @throws java.io.IOException if any IO-exception occured
     */
    public static byte[] zipExceptSvn(File source) throws IOException {
        return zipExceptSvn(source, DEFAULT_COMPRESSION_LEVEL);
    }

    /**
     * Adds a directory to a ZIP-archive and returns its bytes. Uses default level of compression.
     *
     * @param source     directory to compress, will not be added itself;
     *                   source directory child files will be placed in the root of archive
     * @param skipFilter skipped files filter or {@code null} to accept all files
     * @return ZIP-archive bytes
     * @throws java.io.IOException if any IO-exception occured
     */
    public static byte[] zip(File source, @Nullable FileFilter skipFilter) throws IOException {
        return zip(source, DEFAULT_COMPRESSION_LEVEL, skipFilter);
    }

    /**
     * Adds a directory to a ZIP-archive and returns its bytes. Ignores &quot;.svn&quot; files and directories.
     *
     * @param source directory to compress, will not be added itself;
     *               source directory child files will be placed in the root of archive
     * @param level  compression level (0-9)
     * @return ZIP-archive bytes
     * @throws java.io.IOException if any IO-exception occured
     */
    public static byte[] zipExceptSvn(File source, int level) throws IOException {
        return zip(source, level, new NameFileFilter(".svn", IOCase.SYSTEM));
    }

    /**
     * Adds a directory to a ZIP-archive and returns its bytes.
     *
     * @param source     directory to compress, will not be added itself;
     *                   source directory child files will be placed in the root of archive
     * @param level      compression level (0-9)
     * @param skipFilter skipped files filter or {@code null} to accept all files
     * @return ZIP-archive bytes
     * @throws java.io.IOException if any IO-exception occured
     */
    public static byte[] zip(File source, int level, @Nullable FileFilter skipFilter) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipOutputStream.setLevel(level);

        try {
            addDirectory("", source, zipOutputStream, skipFilter, false);
        } finally {
            IOUtils.closeQuietly(zipOutputStream);
            IOUtils.closeQuietly(outputStream);
        }

        return outputStream.toByteArray();
    }

    public static void unzip(byte[] bytes, File destinationDirectory) throws IOException {
        unzip(bytes, destinationDirectory, null);
    }

    public static void unzip(byte[] bytes, File destinationDirectory, @Nullable FileFilter skipFilter)
            throws IOException {
        File zipArchive = null;
        try {
            zipArchive = File.createTempFile("zip", String.valueOf(System.currentTimeMillis()));
            FileUtil.writeFile(zipArchive, bytes);
            unzip(zipArchive, destinationDirectory, skipFilter);
        } finally {
            if (zipArchive != null) {
                FileUtil.deleteTotallyAsync(zipArchive);
            }
        }
    }

    public static void unzip(File zipArchive, File destinationDirectory) throws IOException {
        unzip(zipArchive, destinationDirectory, null);
    }

    public static void unzip(File zipArchive, File destinationDirectory, @Nullable FileFilter skipFilter)
            throws IOException {
        try {
            FileUtil.ensureDirectoryExists(destinationDirectory);
            ZipFile zipFile = new ZipFile(zipArchive);

            int count = 0;

            for (Object fileHeader : zipFile.getFileHeaders()) {
                if (count >= MAX_ZIP_ENTRY_COUNT) {
                    break;
                }

                FileHeader entry = (FileHeader) fileHeader;
                File file = new File(destinationDirectory, entry.getFileName());
                if (skipFilter != null && skipFilter.accept(file)) {
                    continue;
                }

                if (entry.isDirectory()) {
                    FileUtil.ensureDirectoryExists(file);
                } else if (entry.getUncompressedSize() <= MAX_ZIP_ENTRY_SIZE
                        && entry.getCompressedSize() <= MAX_ZIP_ENTRY_SIZE) {
                    FileUtil.ensureDirectoryExists(file.getParentFile());
                    zipFile.extractFile(entry, destinationDirectory.getAbsolutePath());
                } else {
                    long size = max(entry.getUncompressedSize(), entry.getCompressedSize());
                    throw new IOException("Entry '" + entry.getFileName() + "' is larger than " + size + " B.");
                }

                ++count;
            }
        } catch (ZipException e) {
            throw new IOException("Can't extract ZIP-file to directory.", e);
        }
    }

    private static ArrayList<File> deepListFilesInDirectory(
            @Nonnull File directory, @Nullable final FileFilter skipFilter, boolean ignoreHiddenFiles)
            throws IOException {
        ArrayList<File> filesToAdd = new ArrayList<>();

        File[] files = skipFilter == null ? directory.listFiles() : directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !skipFilter.accept(pathname);
            }
        });

        if (files == null) {
            throw new IOException(String.format(
                    "Can't list files in directory '%s' (isDirectory=%b, isFile=%b).",
                    directory.getPath(), directory.isDirectory(), directory.isFile()
            ));
        }

        for (File file : files) {
            if (file.isDirectory() && (!ignoreHiddenFiles || !file.isHidden())) {
                filesToAdd.add(file);
                filesToAdd.addAll(deepListFilesInDirectory(file, skipFilter, ignoreHiddenFiles));
            }
        }

        for (File file : files) {
            if (file.isFile() && (!ignoreHiddenFiles || !file.isHidden())) {
                filesToAdd.add(file);
            }
        }

        return filesToAdd;
    }

    private static void addDirectory(
            String prefix, File source, ZipOutputStream zipOutputStream,
            @Nullable final FileFilter skipFilter, boolean ignoreHiddenFiles)
            throws IOException {
        File[] files = skipFilter == null ? source.listFiles() : source.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !skipFilter.accept(pathname);
            }
        });

        if (files == null) {
            throw new IOException(String.format(
                    "Can't list files in directory '%s' (isDirectory=%b, isFile=%b).",
                    source.getPath(), source.isDirectory(), source.isFile()
            ));
        }

        if (!StringUtil.isEmpty(prefix)) {
            zipOutputStream.putNextEntry(new ZipEntry(prefix));
            zipOutputStream.closeEntry();
        }

        for (File file : files) {
            if (file.isDirectory() && (!ignoreHiddenFiles || !file.isHidden())) {
                addDirectory(prefix + file.getName() + '/', file, zipOutputStream, skipFilter, ignoreHiddenFiles);
            }
        }

        for (File file : files) {
            if (file.isFile() && (!ignoreHiddenFiles || !file.isHidden())) {
                String path = prefix + file.getName();
                zipOutputStream.putNextEntry(new ZipEntry(path));
                zipOutputStream.write(FileUtil.getBytes(file));
                zipOutputStream.closeEntry();
            }
        }
    }

    public static void addEntryToZipArchive(
            File zipFile, String newZipEntryPath, byte[] newZipEntryData)
            throws IOException {
        addEntryToZipArchive(zipFile, newZipEntryPath, new ByteArrayInputStream(newZipEntryData));
    }

    public static void addEntryToZipArchive(File zipFile, String zipEntryPath, InputStream inputStream)
            throws IOException {
        TFile trueZipFile = new TFile(new File(zipFile, zipEntryPath));
        OutputStream outputStream = null;
        try {
            outputStream = new TFileOutputStream(trueZipFile, false);
            IOUtils.copy(inputStream, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(inputStream);
            synchronizeQuietly(trueZipFile);
        }
    }

    public static byte[] getZipEntryBytes(File zipFile, String zipEntryPath) throws IOException {
        ByteArrayOutputStream zipEntryOutputStream = new ByteArrayOutputStream();
        writeZipEntryBytes(zipFile, zipEntryPath, zipEntryOutputStream);
        return zipEntryOutputStream.toByteArray();
    }

    public static void writeZipEntryBytes(File zipFile, String zipEntryPath, OutputStream outputStream)
            throws IOException {
        TFile trueZipFile = new TFile(new File(zipFile, zipEntryPath));
        try {
            InputStream inputStream = null;
            try {
                if (trueZipFile.isArchive()) {
                    synchronizeQuietly(trueZipFile);
                    ZipFile internalZipFile = new ZipFile(zipFile);

                    inputStream = internalZipFile.getInputStream(internalZipFile.getFileHeader(zipEntryPath));
                } else {
                    inputStream = new TFileInputStream(trueZipFile);
                }
                IOUtils.copy(inputStream, outputStream);
            } catch (ZipException e) {
                throw new IOException("Can't write ZIP-entry bytes.", e);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        } finally {
            IOUtils.closeQuietly(outputStream);
            synchronizeQuietly(trueZipFile);
        }
    }

    public static void deleteZipEntry(File zipFile, String zipEntryPath) throws IOException {
        TFile trueZipFile = new TFile(new File(zipFile, zipEntryPath));
        //noinspection ResultOfMethodCallIgnored
        trueZipFile.rm_r();
        synchronizeQuietly(trueZipFile);
    }

    public static boolean isZipEntryExists(File zipFile, String zipEntryPath) throws IOException {
        try {
            return new ZipFile(zipFile).getFileHeader(normalizeZipEntryPath(zipEntryPath)) != null;
        } catch (ZipException e) {
            throw new IOException("Can't check ZIP-entry existence.", e);
        }
    }

    /**
     * Returns the uncompressed size of the entry data, or -1 if not known.
     *
     * @param zipFile      ZIP-file containing entry
     * @param zipEntryPath path to the entry of specified ZIP-file
     * @return the uncompressed size of the entry data, or -1 if not known
     * @throws java.io.IOException if any I/O-exception occurred
     */
    public static long getZipEntrySize(File zipFile, String zipEntryPath) throws IOException {
        try {
            return new ZipFile(zipFile).getFileHeader(normalizeZipEntryPath(zipEntryPath)).getUncompressedSize();
        } catch (ZipException e) {
            throw new IOException("Can't get ZIP-entry size.", e);
        }
    }

    public static long getZipArchiveSize(File zipFile) throws IOException {
        try {
            ZipFile internalZipFile = new ZipFile(zipFile);
            long totalSize = 0;

            for (Object fileHeader : internalZipFile.getFileHeaders()) {
                FileHeader entry = (FileHeader) fileHeader;
                long size = entry.getUncompressedSize();
                if (size != -1) {
                    totalSize += size;
                }
            }

            return totalSize;
        } catch (ZipException e) {
            throw new IOException("Can't get ZIP-archive size.", e);
        }
    }

    /**
     * Checks that file is correct non-empty ZIP-archive.
     * Equivalent of {@code {@link #isCorrectZipFile(java.io.File, boolean) isCorrectZipFile(file, true)}}.
     *
     * @param file file to check
     * @return {@code true} iff file is correct non-empty ZIP-archive
     */
    public static boolean isCorrectZipFile(@Nullable File file) {
        return isCorrectZipFile(file, true);
    }

    /**
     * Checks that file is correct ZIP-archive.
     *
     * @param file          file to check
     * @param checkNotEmpty flag which indicates whether we should treat empty archive as correct or not
     * @return {@code true} iff file is correct ZIP-archive
     */
    public static boolean isCorrectZipFile(@Nullable File file, boolean checkNotEmpty) {
        if (file == null) {
            return false;
        }

        TFile trueZipFile = new TFile(file);
        try {
            if (!trueZipFile.isArchive()) {
                return false;
            }

            TFile[] zipEntries = trueZipFile.listFiles();
            return zipEntries != null && (!checkNotEmpty || zipEntries.length > 0);
        } finally {
            synchronizeQuietly(trueZipFile);
        }
    }

    public static String normalizeZipEntryPath(@Nonnull String zipEntryPath) {
        return zipEntryPath.replace(File.separatorChar, '/');
    }

    public static void synchronizeQuietly() {
        try {
            TVFS.umount();
        } catch (FsSyncException ignored) {
            // No operations.
        }
    }

    public static void synchronizeQuietly(@Nullable TFile trueZipFile) {
        if (trueZipFile != null) {
            TFile topLevelArchive = trueZipFile.getTopLevelArchive();

            try {
                if (topLevelArchive == null) {
                    TVFS.umount(trueZipFile);
                } else {
                    TVFS.umount(topLevelArchive);
                }
            } catch (FsSyncException ignored) {
                // No operations.
            }
        }
    }
}
