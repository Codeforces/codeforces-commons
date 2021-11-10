package com.codeforces.commons.compress;

import com.codeforces.commons.io.ByteArrayOutputStream;
import com.codeforces.commons.io.*;
import com.codeforces.commons.text.Patterns;
import com.codeforces.commons.text.StringUtil;
import de.schlichtherle.truezip.file.*;
import de.schlichtherle.truezip.fs.FsSyncException;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;

import static com.codeforces.commons.math.Math.max;

/**
 * @author Mike Mirzayanov
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class ZipUtil {
    @SuppressWarnings("unused")
    public static final int MINIMAL_COMPRESSION_LEVEL = 0;
    public static final int DEFAULT_COMPRESSION_LEVEL = 5;
    public static final int MAXIMAL_COMPRESSION_LEVEL = 9;

    private static final long MAX_ZIP_ENTRY_SIZE = FileUtil.BYTES_PER_GB;
    private static final long MAX_ZIP_ENTRY_COUNT = 100_000L;

    private static final int DEFAULT_BUFFER_SIZE = IoUtil.BUFFER_SIZE;

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
        IoUtil.copy(new DeflaterInputStream(plainTextInputStream, compressor), compressedTextOutputStream);
    }

    public static void decompress(InputStream compressedTextInputStream, OutputStream plainTextOutputStream)
            throws IOException {
        IoUtil.copy(compressedTextInputStream, new InflaterOutputStream(plainTextOutputStream));
    }

    public static byte[] compress(byte[] bytes) {
        return compress(bytes, DEFAULT_COMPRESSION_LEVEL);
    }

    public static byte[] compress(byte[] bytes, int level) {
        Deflater deflater = new Deflater();
        deflater.setLevel(level);
        deflater.setInput(bytes);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bytes.length);
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        while (!deflater.finished()) {
            outputStream.write(buffer, 0, deflater.deflate(buffer));
        }

        return outputStream.toByteArray();
    }

    public static byte[] decompress(byte[] bytes) throws DataFormatException {
        if (bytes.length == 0) {
            return bytes;
        }

        Inflater inflater = new Inflater();
        inflater.setInput(bytes);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        while (!inflater.finished()) {
            outputStream.write(buffer, 0, inflater.inflate(buffer));
        }

        inflater.end();
        return outputStream.toByteArray();
    }

    /**
     * Adds a directory to a ZIP-archive. Uses default level of compression.
     * Ignores &quot;.svn&quot; files and directories.
     *
     * @param source      directory to compress, will not be added itself;
     *                    source directory child files will be placed in the root of archive
     * @param destination ZIP-archive
     * @throws IOException if any I/O-exception occurred
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
     * @throws IOException if any I/O-exception occurred
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
     * @throws IOException if any I/O-exception occured
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
     * @throws IOException if any I/O-exception occured
     */
    public static void zip(File source, File destination, int level, @Nullable FileFilter skipFilter)
            throws IOException {
        try {
            ZipParameters parameters = new ZipParameters();
            parameters.setIncludeRootFolder(false);
            parameters.setCompressionLevel(CompressionLevel.values()[level - 1]);
            parameters.setReadHiddenFiles(true);
            parameters.setDefaultFolderPath(source.getAbsolutePath());

            net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(destination);

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
     * @throws IOException if any I/O-exception occured
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
     * @throws IOException if any I/O-exception occured
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
     * @throws IOException if any I/O-exception occured
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
     * @throws IOException if any I/O-exception occured
     */
    public static byte[] zip(File source, int level, @Nullable FileFilter skipFilter) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipOutputStream.setLevel(level);

        try {
            addDirectory("", source, zipOutputStream, skipFilter, false);
        } finally {
            IoUtil.closeQuietly(zipOutputStream, outputStream);
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
            zipArchive = File.createTempFile(System.currentTimeMillis() + "-", ".tmp.zip");
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
            net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(zipArchive);

            int count = 0;

            for (FileHeader fileHeader : zipFile.getFileHeaders()) {
                if (count >= MAX_ZIP_ENTRY_COUNT) {
                    break;
                }

                File file = new File(destinationDirectory, fileHeader.getFileName());
                if (skipFilter != null && skipFilter.accept(file)) {
                    continue;
                }

                if (fileHeader.isDirectory()) {
                    FileUtil.ensureDirectoryExists(file);
                } else {
                    long maxSize = max(fileHeader.getUncompressedSize(), fileHeader.getCompressedSize());

                    if (maxSize <= MAX_ZIP_ENTRY_SIZE) {
                        FileUtil.ensureDirectoryExists(file.getParentFile());
                        zipFile.extractFile(fileHeader, destinationDirectory.getAbsolutePath());
                    } else {
                        throw new IOException(String.format(
                                "Entry '%s' (%s) is larger than %s.",
                                fileHeader.getFileName(), FileUtil.formatSize(maxSize),
                                FileUtil.formatSize(MAX_ZIP_ENTRY_SIZE)
                        ));
                    }
                }

                ++count;
            }
        } catch (ZipException e) {
            throw new IOException("Can't extract ZIP-file to directory.", e);
        }
    }

    /**
     * Extracts ZIP-archive bytes into temporary directory
     * and then repacks this directory to a new ZIP-archive and returns its bytes.
     * Uses maximal level of compression.
     * Optionally can skip some files using file filter.
     *
     * @param bytes      original ZIP-archive bytes
     * @param skipFilter skipped files filter or {@code null} to accept all files
     * @return repacked ZIP-archive bytes
     * @throws IOException if any I/O-exception occured
     */
    public static byte[] rezip(byte[] bytes, @Nullable FileFilter skipFilter) throws IOException {
        File tempDir = null;
        try {
            tempDir = FileUtil.createTemporaryDirectory("rezip");
            unzip(bytes, tempDir, skipFilter);
            byte[] rezippedBytes = zip(tempDir, MAXIMAL_COMPRESSION_LEVEL, skipFilter);
            return rezippedBytes.length < bytes.length ? rezippedBytes : bytes;
        } finally {
            FileUtil.deleteTotallyAsync(tempDir);
        }
    }

    public static void rezip(File source, File destination, @Nullable FileFilter skipFilter) throws IOException {
        byte[] sourceBytes = FileUtil.getBytes(source);
        byte[] destinationBytes = rezip(sourceBytes, skipFilter);
        if (destinationBytes.length < sourceBytes.length || !source.equals(destination)) {
            FileUtil.writeFile(destination, destinationBytes);
        }
    }

    public static void rezip(File file, @Nullable FileFilter skipFilter) throws IOException {
        rezip(file, file, skipFilter);
    }

    @SuppressWarnings("OverlyComplexMethod")
    private static ArrayList<File> deepListFilesInDirectory(
            @Nonnull File directory, @Nullable FileFilter skipFilter, boolean ignoreHiddenFiles)
            throws IOException {
        ArrayList<File> filesToAdd = new ArrayList<>();

        File[] files = skipFilter == null ? directory.listFiles() : directory.listFiles(
                pathname -> !skipFilter.accept(pathname)
        );

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

    @SuppressWarnings("OverlyComplexMethod")
    private static void addDirectory(
            String prefix, File source, ZipOutputStream zipOutputStream,
            @Nullable FileFilter skipFilter, boolean ignoreHiddenFiles)
            throws IOException {
        File[] files = skipFilter == null ? source.listFiles()
                : source.listFiles(pathname -> !skipFilter.accept(pathname));

        if (files == null) {
            throw new IOException(String.format(
                    "Can't list files in directory '%s' (isDirectory=%b, isFile=%b).",
                    source.getPath(), source.isDirectory(), source.isFile()
            ));
        }

        if (StringUtil.isNotEmpty(prefix)) {
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

                if (file.length() > FileUtil.BYTES_PER_GB) {
                    try (InputStream inputStream = FileUtil.getInputStream(file)) {
                        IOUtils.copyLarge(inputStream, zipOutputStream, new byte[IoUtil.BUFFER_SIZE]);
                    }
                } else {
                    zipOutputStream.write(FileUtil.getBytes(file));
                }

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
        try {
            OutputStream outputStream = new TFileOutputStream(trueZipFile, false);
            IoUtil.copy(inputStream, outputStream, true, true);
        } finally {
            synchronizeQuietly(trueZipFile);
        }
    }

    @Nonnull
    public static byte[] getZipEntryBytes(File zipFile, String zipEntryPath) throws IOException {
        ByteArrayOutputStream zipEntryOutputStream = new ByteArrayOutputStream();
        writeZipEntryBytes(zipFile, zipEntryPath, zipEntryOutputStream);
        return zipEntryOutputStream.toByteArray();
    }

    public static void writeZipEntryBytes(File zipFile, String zipEntryPath, OutputStream outputStream)
            throws IOException {
        TFile trueZipFile = new TFile(new File(zipFile, zipEntryPath));
        try {
            try {
                InputStream inputStream;
                if (trueZipFile.isArchive()) {
                    synchronizeQuietly(trueZipFile);
                    net.lingala.zip4j.ZipFile internalZipFile = new net.lingala.zip4j.ZipFile(zipFile);

                    inputStream = internalZipFile.getInputStream(internalZipFile.getFileHeader(zipEntryPath));
                } else {
                    inputStream = new TFileInputStream(trueZipFile);
                }
                IoUtil.copy(inputStream, outputStream);
            } catch (ZipException e) {
                throw new IOException("Can't write ZIP-entry bytes.", e);
            }
        } finally {
            IoUtil.closeQuietly(outputStream);
            synchronizeQuietly(trueZipFile);
        }
    }

    public static void deleteZipEntry(@Nonnull File zipFile, @Nonnull String zipEntryPath) throws IOException {
        synchronizeQuietly(new TFile(new File(zipFile, zipEntryPath)).rm_r());
    }

    public static boolean isZipEntryExists(File zipFile, String zipEntryPath) throws IOException {
        try {
            return new net.lingala.zip4j.ZipFile(zipFile).getFileHeader(normalizeZipEntryPath(zipEntryPath)) != null;
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
     * @throws IOException if any I/O-exception occurred
     */
    public static long getZipEntrySize(File zipFile, String zipEntryPath) throws IOException {
        try {
            return new net.lingala.zip4j.ZipFile(zipFile).getFileHeader(normalizeZipEntryPath(zipEntryPath)).getUncompressedSize();
        } catch (ZipException e) {
            throw new IOException("Can't get ZIP-entry size.", e);
        }
    }

    public static ZipArchiveInfo getZipArchiveInfo(File zipFile) throws IOException {
        try {
            net.lingala.zip4j.ZipFile internalZipFile = new net.lingala.zip4j.ZipFile(zipFile);
            long totalSize = 0;
            long entryCount = 0;

            for (FileHeader fileHeader : internalZipFile.getFileHeaders()) {
                long size = fileHeader.getUncompressedSize();
                if (size > 0L) {
                    totalSize += size;
                }
                ++entryCount;
            }

            return new ZipArchiveInfo(totalSize, entryCount);
        } catch (ZipException e) {
            throw new IOException("Can't get ZIP-archive info.", e);
        }
    }

    public static long getZipArchiveSize(File zipFile) throws IOException {
        return getZipArchiveInfo(zipFile).getUncompressedSize();
    }

    public static long getZipArchiveEntryCount(File zipFile) throws IOException {
        return getZipArchiveInfo(zipFile).getEntryCount();
    }

    public static ZipArchiveInfo getZipArchiveInfo(byte[] zipFileBytes) throws IOException {
        ZipArchiveInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipFileBytes));
            long totalSize = 0;
            long entryCount = 0;
            ZipArchiveEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextZipEntry()) != null) {
                long size = zipEntry.getSize();
                if (size > 0L) {
                    totalSize += size;
                }
                ++entryCount;
            }

            zipInputStream.close();
            return new ZipArchiveInfo(totalSize, entryCount);
        } catch (IOException e) {
            IoUtil.closeQuietly(zipInputStream);
            throw new IOException("Can't get inmemory ZIP-archive info.", e);
        }
    }

    public static long getZipArchiveSize(byte[] zipFileBytes) throws IOException {
        return getZipArchiveInfo(zipFileBytes).getUncompressedSize();
    }

    public static long getZipArchiveEntryCount(byte[] zipFileBytes) throws IOException {
        return getZipArchiveInfo(zipFileBytes).getEntryCount();
    }

    /**
     * Checks that file is correct non-empty ZIP-archive.
     * Equivalent of {@code {@link #isCorrectZipFile(File, boolean) isCorrectZipFile(file, true)}}.
     *
     * @param file file to check
     * @return {@code true} iff file is correct non-empty ZIP-archive
     */
    @Contract("null -> false")
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
    @Contract("null, _ -> false")
    public static boolean isCorrectZipFile(@Nullable File file, boolean checkNotEmpty) {
        if (file == null || !(file instanceof TFile) && !file.isFile()) {
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

    @Nonnull
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

    /**
     * Formats content of the ZIP-archive for view and returns result as UTF-8 bytes. The {@code truncated} flag
     * indicates that the length of returned view was restricted by {@code maxLength} parameter.
     * This method delegates to
     * {@code {@link #formatZipArchiveContentForView(File, int, int, int)}}
     * using default values for different string patterns.
     *
     * @param zipFileBytes       bytes of ZIP-archive to format
     * @param maxLength          maximal allowed length of result
     * @param maxEntryLineCount  maximal allowed number of lines to display for a single ZIP-archive entry
     * @param maxEntryLineLength maximal allowed length of ZIP-archive entry line
     * @return formatted view of ZIP-archive
     * @throws IOException if {@code zipFileBytes} is not a correct bytes of ZIP-archive or any other I/O-error has been occured
     * @see #formatZipArchiveContentForView(File, int, int, int)
     */
    public static FileUtil.FirstBytes formatZipArchiveContentForView(
            byte[] zipFileBytes, int maxLength, int maxEntryLineCount, int maxEntryLineLength)
            throws IOException {
        return formatZipArchiveContentForView(zipFileBytes, zipFile -> formatZipArchiveContentForView(
                zipFile, maxLength, maxEntryLineCount, maxEntryLineLength
        ));
    }

    /**
     * Formats content of the ZIP-archive for view and returns result as UTF-8 bytes. The {@code truncated} flag
     * indicates that the length of returned view was restricted by {@code maxLength} parameter.
     * This method delegates to
     * {@code {@link #formatZipArchiveContentForView(File, int, int, int, ZipUtil.ZipFileFormatConfiguration)}}
     * using {@code configuration} values for different string patterns.
     *
     * @param zipFileBytes       bytes of ZIP-archive to format
     * @param maxLength          maximal allowed length of result
     * @param maxEntryLineCount  maximal allowed number of lines to display for a single ZIP-archive entry
     * @param maxEntryLineLength maximal allowed length of ZIP-archive entry line
     * @param configuration      configuration containing string patterns
     * @return formatted view of ZIP-archive
     * @throws IOException if {@code zipFileBytes} is not a correct bytes of ZIP-archive or any other I/O-error has been occured
     * @see #formatZipArchiveContentForView(File, int, int, int, ZipUtil.ZipFileFormatConfiguration)
     */
    public static FileUtil.FirstBytes formatZipArchiveContentForView(
            byte[] zipFileBytes, int maxLength, int maxEntryLineCount, int maxEntryLineLength,
            ZipFileFormatConfiguration configuration) throws IOException {
        return formatZipArchiveContentForView(zipFileBytes, zipFile -> formatZipArchiveContentForView(
                zipFile, maxLength, maxEntryLineCount, maxEntryLineLength, configuration
        ));
    }

    /**
     * Formats content of the ZIP-archive for view and returns result as UTF-8 bytes. The {@code truncated} flag
     * indicates that the length of returned view was restricted by {@code maxLength} parameter.
     * This method delegates to
     * {@code {@link #formatZipArchiveContentForView(File, int, int, int, String, String, String, String, String, String, String, String, String, String)}}
     * using {@code configuration} values for different string patterns.
     *
     * @param zipFileBytes                         bytes of ZIP-archive to format
     * @param maxLength                            maximal allowed length of result
     * @param maxEntryLineCount                    maximal allowed number of content lines to display for a single ZIP-archive entry
     * @param maxEntryLineLength                   maximal allowed length of ZIP-archive entry content line
     * @param entryListHeaderPattern               pattern of entry list header; parameters: {@code fileName}, {@code filePath}, {@code entryCount}
     * @param entryListItemPattern                 pattern of entry list item; parameters: {@code entryName}, {@code entrySize}, {@code entryIndex} (1-based)
     * @param entryListItemSeparatorPattern        pattern of entry list separator
     * @param entryListCloserPattern               pattern of entry list closer; parameters: {@code fileName}, {@code filePath}
     * @param entryContentHeaderPattern            pattern of entry content header; parameters: {@code entryName}, {@code entrySize}
     * @param entryContentLinePattern              pattern of entry content line; parameters: {@code entryLine}
     * @param entryContentLineSeparatorPattern     pattern of entry content separator
     * @param entryContentCloserPattern            pattern of entry content closer; parameters: {@code entryName}
     * @param binaryEntryContentPlaceholderPattern pattern of binary entry content placeholder; parameters: {@code entrySize}
     * @param emptyZipFilePlaceholderPattern       pattern of empty (no entries) ZIP-file placeholder; parameters: {@code fileName}, {@code filePath}
     * @return formatted view of ZIP-archive
     * @throws IOException if {@code zipFileBytes} is not a correct bytes of ZIP-archive or any other I/O-error has been occured
     * @see #formatZipArchiveContentForView(File, int, int, int, String, String, String, String, String, String, String, String, String, String)
     */
    public static FileUtil.FirstBytes formatZipArchiveContentForView(
            byte[] zipFileBytes, int maxLength, int maxEntryLineCount, int maxEntryLineLength,
            String entryListHeaderPattern, String entryListItemPattern,
            String entryListItemSeparatorPattern, String entryListCloserPattern,
            String entryContentHeaderPattern, String entryContentLinePattern,
            String entryContentLineSeparatorPattern, String entryContentCloserPattern,
            String binaryEntryContentPlaceholderPattern, String emptyZipFilePlaceholderPattern)
            throws IOException {
        return formatZipArchiveContentForView(zipFileBytes, zipFile -> formatZipArchiveContentForView(
                zipFile, maxLength, maxEntryLineCount, maxEntryLineLength,
                entryListHeaderPattern, entryListItemPattern,
                entryListItemSeparatorPattern, entryListCloserPattern,
                entryContentHeaderPattern, entryContentLinePattern,
                entryContentLineSeparatorPattern, entryContentCloserPattern,
                binaryEntryContentPlaceholderPattern, emptyZipFilePlaceholderPattern
        ));
    }

    private static FileUtil.FirstBytes formatZipArchiveContentForView(
            byte[] zipFileBytes, ZipFileFormatHandler handler) throws IOException {
        File tempDir = null;

        try {
            tempDir = FileUtil.createTemporaryDirectory("zip-file-for-view");

            File zipFile = new File(tempDir, "zip.zip");
            FileUtil.writeFile(zipFile, zipFileBytes);

            return handler.formatZipArchiveContentForView(zipFile);
        } finally {
            FileUtil.deleteTotallyAsync(tempDir);
        }
    }

    /**
     * Formats content of the ZIP-archive for view and returns result as UTF-8 bytes. The {@code truncated} flag
     * indicates that the length of returned view was restricted by {@code maxLength} parameter.
     * This method delegates to
     * {@code {@link #formatZipArchiveContentForView(File, int, int, int, String, String, String, String, String, String, String, String, String, String)}}
     * using default values for different string patterns.
     *
     * @param zipFile            ZIP-archive to format
     * @param maxLength          maximal allowed length of result
     * @param maxEntryLineCount  maximal allowed number of lines to display for a single ZIP-archive entry
     * @param maxEntryLineLength maximal allowed length of ZIP-archive entry line
     * @return formatted view of ZIP-archive
     * @throws IOException if {@code zipFile} is not a correct ZIP-archive or any other I/O-error has been occured
     * @see #formatZipArchiveContentForView(File, int, int, int, String, String, String, String, String, String, String, String, String, String)
     */
    public static FileUtil.FirstBytes formatZipArchiveContentForView(
            File zipFile, int maxLength, int maxEntryLineCount, int maxEntryLineLength) throws IOException {
        return formatZipArchiveContentForView(
                zipFile, maxLength, maxEntryLineCount, maxEntryLineLength,
                null, null, null, null, null, null, null, null, null, null
        );
    }

    /**
     * Formats content of the ZIP-archive for view and returns result as UTF-8 bytes. The {@code truncated} flag
     * indicates that the length of returned view was restricted by {@code maxLength} parameter.
     * This method delegates to
     * {@code {@link #formatZipArchiveContentForView(File, int, int, int, String, String, String, String, String, String, String, String, String, String)}}
     * using {@code configuration} values for different string patterns.
     *
     * @param zipFile            ZIP-archive to format
     * @param maxLength          maximal allowed length of result
     * @param maxEntryLineCount  maximal allowed number of lines to display for a single ZIP-archive entry
     * @param maxEntryLineLength maximal allowed length of ZIP-archive entry line
     * @param configuration      configuration containing string patterns
     * @return formatted view of ZIP-archive
     * @throws IOException if {@code zipFile} is not a correct ZIP-archive or any other I/O-error has been occured
     * @see #formatZipArchiveContentForView(File, int, int, int, String, String, String, String, String, String, String, String, String, String)
     */
    public static FileUtil.FirstBytes formatZipArchiveContentForView(
            File zipFile, int maxLength, int maxEntryLineCount, int maxEntryLineLength,
            ZipFileFormatConfiguration configuration) throws IOException {
        return formatZipArchiveContentForView(
                zipFile, maxLength, maxEntryLineCount, maxEntryLineLength,
                configuration.getEntryListHeaderPattern(), configuration.getEntryListItemPattern(),
                configuration.getEntryListItemSeparatorPattern(), configuration.getEntryListCloserPattern(),
                configuration.getEntryContentHeaderPattern(), configuration.getEntryContentLinePattern(),
                configuration.getEntryContentLineSeparatorPattern(), configuration.getEntryContentCloserPattern(),
                configuration.getBinaryEntryContentPlaceholderPattern(), configuration.getEmptyZipFilePlaceholderPattern()
        );
    }

    /**
     * Formats content of the ZIP-archive for view and returns result as UTF-8 bytes. The {@code truncated} flag
     * indicates that the length of returned view was restricted by {@code maxLength} parameter.
     *
     * @param zipFile                              ZIP-archive to format
     * @param maxLength                            maximal allowed length of result
     * @param maxEntryLineCount                    maximal allowed number of content lines to display for a single ZIP-archive entry
     * @param maxEntryLineLength                   maximal allowed length of ZIP-archive entry content line
     * @param entryListHeaderPattern               pattern of entry list header; parameters: {@code fileName}, {@code filePath}, {@code entryCount}
     * @param entryListItemPattern                 pattern of entry list item; parameters: {@code entryName}, {@code entrySize}, {@code entryIndex} (1-based)
     * @param entryListItemSeparatorPattern        pattern of entry list separator
     * @param entryListCloserPattern               pattern of entry list closer; parameters: {@code fileName}, {@code filePath}
     * @param entryContentHeaderPattern            pattern of entry content header; parameters: {@code entryName}, {@code entrySize}
     * @param entryContentLinePattern              pattern of entry content line; parameters: {@code entryLine}
     * @param entryContentLineSeparatorPattern     pattern of entry content separator
     * @param entryContentCloserPattern            pattern of entry content closer; parameters: {@code entryName}
     * @param binaryEntryContentPlaceholderPattern pattern of binary entry content placeholder; parameters: {@code entrySize}
     * @param emptyZipFilePlaceholderPattern       pattern of empty (no entries) ZIP-file placeholder; parameters: {@code fileName}, {@code filePath}
     * @return formatted view of ZIP-archive
     * @throws IOException if {@code zipFile} is not a correct ZIP-archive or any other I/O-error has been occured
     * @see String#format(String, Object...)
     */
    @SuppressWarnings("OverlyLongMethod")
    @Nonnull
    public static FileUtil.FirstBytes formatZipArchiveContentForView(
            File zipFile, int maxLength, int maxEntryLineCount, int maxEntryLineLength,
            @Nullable String entryListHeaderPattern, @Nullable String entryListItemPattern,
            @Nullable String entryListItemSeparatorPattern, @Nullable String entryListCloserPattern,
            @Nullable String entryContentHeaderPattern, @Nullable String entryContentLinePattern,
            @Nullable String entryContentLineSeparatorPattern, @Nullable String entryContentCloserPattern,
            @Nullable String binaryEntryContentPlaceholderPattern, @Nullable String emptyZipFilePlaceholderPattern
    ) throws IOException {
        entryListHeaderPattern = StringUtil.nullToDefault(entryListHeaderPattern, "ZIP-file entries {\n");
        entryListItemPattern = StringUtil.nullToDefault(entryListItemPattern, "    %3$03d. %1$s (%2$d B)");
        entryListItemSeparatorPattern = StringUtil.nullToDefault(entryListItemSeparatorPattern, "\n");
        entryListCloserPattern = StringUtil.nullToDefault(entryListCloserPattern, "\n}\n\n");

        entryContentHeaderPattern = StringUtil.nullToDefault(entryContentHeaderPattern, "Entry %1$s (%2$d B) {\n");
        entryContentLinePattern = StringUtil.nullToDefault(entryContentLinePattern, "    %1$s");
        entryContentLineSeparatorPattern = StringUtil.nullToDefault(entryContentLineSeparatorPattern, "\n");
        entryContentCloserPattern = StringUtil.nullToDefault(entryContentCloserPattern, "\n} // %1$s\n\n");

        binaryEntryContentPlaceholderPattern = StringUtil.nullToDefault(
                binaryEntryContentPlaceholderPattern, "    *** BINARY DATA (%1$d B) ***"
        );
        emptyZipFilePlaceholderPattern = StringUtil.nullToDefault(emptyZipFilePlaceholderPattern, "Empty ZIP-file.");

        try {
            Charset charset = StandardCharsets.UTF_8;

            net.lingala.zip4j.ZipFile internalZipFile = new net.lingala.zip4j.ZipFile(zipFile);
            List<?> fileHeaders = internalZipFile.getFileHeaders();
            int headerCount = fileHeaders.size();

            if (headerCount <= 0) {
                return formatEmptyZipFilePlaceholder(zipFile, maxLength, emptyZipFilePlaceholderPattern, charset);
            }

            MutableBoolean truncated = new MutableBoolean(Boolean.FALSE);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            CountingOutputStream countingOutputStream = new CountingOutputStream(byteArrayOutputStream);

            byte[] entryListHeaderBytes = String.format(
                    entryListHeaderPattern, zipFile.getName(), zipFile.getPath(), headerCount
            ).getBytes(charset);

            if (!writeBytesForView(countingOutputStream, entryListHeaderBytes, maxLength, truncated)) {
                throw new IllegalArgumentException(String.format(
                        "Argument 'maxLength' (%d) is less than the length of entry list header '%s' (%d bytes).",
                        maxLength, new String(entryListHeaderBytes, charset), entryListHeaderBytes.length
                ));
            }

            fileHeaders.sort(Comparator.comparing(header -> ((FileHeader) header).getFileName()));

            for (int headerIndex = 0; headerIndex < headerCount; ++headerIndex) {
                FileHeader header = (FileHeader) fileHeaders.get(headerIndex);
                String fileName = header.getFileName();

                String entryListItemAppendix = headerIndex == headerCount - 1
                        ? String.format(entryListCloserPattern, zipFile.getName(), zipFile.getPath())
                        : entryListItemSeparatorPattern;

                byte[] entryListItemBytes = (String.format(
                        entryListItemPattern, fileName, header.getUncompressedSize(), headerIndex + 1
                ) + entryListItemAppendix).getBytes(charset);

                if (!writeBytesForView(countingOutputStream, entryListItemBytes, maxLength, truncated)) {
                    break;
                }
            }

            for (Object fileHeader : fileHeaders) {
                FileHeader header = (FileHeader) fileHeader;
                if (header.isDirectory()) {
                    continue;
                }

                formatAndAppendEntryContent(
                        countingOutputStream, maxLength, truncated, charset, internalZipFile, header,
                        maxEntryLineCount, maxEntryLineLength, entryContentHeaderPattern, entryContentLinePattern,
                        entryContentLineSeparatorPattern, entryContentCloserPattern,
                        binaryEntryContentPlaceholderPattern
                );

                if (truncated.booleanValue()) {
                    break;
                }
            }

            return new FileUtil.FirstBytes(truncated.booleanValue(), byteArrayOutputStream.toByteArray());
        } catch (ZipException e) {
            throw new IOException("Can't format ZIP-file for view.", e);
        }
    }

    private static void formatAndAppendEntryContent(
            CountingOutputStream countingOutputStream, int maxLength, MutableBoolean truncated, Charset charset,
            net.lingala.zip4j.ZipFile zipFile, FileHeader zipEntryHeader, int maxEntryLineCount, int maxEntryLineLength,
            String entryContentHeaderPattern, String entryContentLinePattern,
            String entryContentLineSeparatorPattern, String entryContentCloserPattern,
            String binaryEntryContentPlaceholderPattern) throws IOException {
        String fileName = zipEntryHeader.getFileName();

        byte[] fileBytes = IoUtil.toByteArray(zipFile.getInputStream(zipEntryHeader));
        String fileText;
        boolean binaryFile;

        try {
            fileText = new String(fileBytes, charset);
            binaryFile = false;

            for (int charIndex = 0, charCount = fileText.length(); charIndex < charCount; ++charIndex) {
                if (fileText.charAt(charIndex) < 9) {
                    binaryFile = true;
                    break;
                }
            }
        } catch (RuntimeException ignored) {
            fileText = null;
            binaryFile = true;
        }

        writeBytesForView(countingOutputStream, String.format(
                entryContentHeaderPattern, fileName, fileBytes.length
        ).getBytes(charset), maxLength, truncated);

        if (binaryFile) {
            writeBytesForView(countingOutputStream, String.format(
                    binaryEntryContentPlaceholderPattern, fileBytes.length
            ).getBytes(charset), maxLength, truncated);

            writeBytesForView(countingOutputStream, String.format(
                    entryContentCloserPattern, fileName
            ).getBytes(charset), maxLength, truncated);
        } else {
            String[] fileLines = StringUtil.shrinkLinesTo(
                    Patterns.LINE_BREAK_PATTERN.split(fileText), maxEntryLineLength, maxEntryLineCount
            );

            for (int lineIndex = 0, lineCount = fileLines.length; lineIndex < lineCount; ++lineIndex) {
                String entryContentLineAppendix = lineIndex == lineCount - 1
                        ? String.format(entryContentCloserPattern, fileName)
                        : entryContentLineSeparatorPattern;

                byte[] entryContentLineBytes = (String.format(
                        entryContentLinePattern, fileLines[lineIndex]
                ) + entryContentLineAppendix).getBytes(charset);

                if (!writeBytesForView(countingOutputStream, entryContentLineBytes, maxLength, truncated)) {
                    break;
                }
            }
        }
    }

    private static FileUtil.FirstBytes formatEmptyZipFilePlaceholder(
            File zipFile, int maxLength, String emptyZipFilePlaceholderPattern, Charset charset) {
        byte[] emptyZipFilePlaceholderBytes = String.format(
                emptyZipFilePlaceholderPattern, zipFile.getName(), zipFile.getPath()
        ).getBytes(charset);

        if (maxLength < emptyZipFilePlaceholderBytes.length) {
            throw new IllegalArgumentException(String.format(
                    "Argument 'maxLength' (%d) is less than the length of empty ZIP-file placeholder '%s' (%d bytes).",
                    maxLength, new String(emptyZipFilePlaceholderBytes, charset), emptyZipFilePlaceholderBytes.length
            ));
        }

        return new FileUtil.FirstBytes(false, emptyZipFilePlaceholderBytes);
    }

    private static boolean writeBytesForView(
            CountingOutputStream countingOutputStream, byte[] bytes, int maxLength, MutableBoolean truncated) {
        if (truncated.booleanValue()) {
            return false;
        }

        if (countingOutputStream.getTotalWrittenByteCount() + bytes.length > maxLength) {
            truncated.setTrue();
            return false;
        } else {
            try {
                countingOutputStream.write(bytes);
                return true;
            } catch (IOException ignored) {
                truncated.setTrue();
                return false;
            }
        }
    }

    public static final class ZipArchiveInfo {
        private final long uncompressedSize;
        private final long entryCount;

        public ZipArchiveInfo(long uncompressedSize, long entryCount) {
            this.uncompressedSize = uncompressedSize;
            this.entryCount = entryCount;
        }

        public long getUncompressedSize() {
            return uncompressedSize;
        }

        public long getEntryCount() {
            return entryCount;
        }

        @Override
        public String toString() {
            return StringUtil.toString(this, true, "uncompressedSize", "entryCount");
        }
    }

    public static final class ZipFileFormatConfiguration {
        private String entryListHeaderPattern = "ZIP-file entries {\n";
        private String entryListItemPattern = "    %3$03d. %1$s (%2$d B)";
        private String entryListItemSeparatorPattern = "\n";
        private String entryListCloserPattern = "\n}\n\n";

        private String entryContentHeaderPattern = "Entry %1$s (%2$d B) {\n";
        private String entryContentLinePattern = "    %1$s";
        private String entryContentLineSeparatorPattern = "\n";
        private String entryContentCloserPattern = "\n} // %1$s\n\n";

        private String binaryEntryContentPlaceholderPattern = "    *** BINARY DATA (%1$d B) ***";
        private String emptyZipFilePlaceholderPattern = "Empty ZIP-file.";

        public String getEntryListHeaderPattern() {
            return entryListHeaderPattern;
        }

        public ZipFileFormatConfiguration setEntryListHeaderPattern(String entryListHeaderPattern) {
            this.entryListHeaderPattern = entryListHeaderPattern;
            return this;
        }

        public String getEntryListItemPattern() {
            return entryListItemPattern;
        }

        public ZipFileFormatConfiguration setEntryListItemPattern(String entryListItemPattern) {
            this.entryListItemPattern = entryListItemPattern;
            return this;
        }

        public String getEntryListItemSeparatorPattern() {
            return entryListItemSeparatorPattern;
        }

        public ZipFileFormatConfiguration setEntryListItemSeparatorPattern(String entryListItemSeparatorPattern) {
            this.entryListItemSeparatorPattern = entryListItemSeparatorPattern;
            return this;
        }

        public String getEntryListCloserPattern() {
            return entryListCloserPattern;
        }

        public ZipFileFormatConfiguration setEntryListCloserPattern(String entryListCloserPattern) {
            this.entryListCloserPattern = entryListCloserPattern;
            return this;
        }

        public String getEntryContentHeaderPattern() {
            return entryContentHeaderPattern;
        }

        public ZipFileFormatConfiguration setEntryContentHeaderPattern(String entryContentHeaderPattern) {
            this.entryContentHeaderPattern = entryContentHeaderPattern;
            return this;
        }

        public String getEntryContentLinePattern() {
            return entryContentLinePattern;
        }

        public ZipFileFormatConfiguration setEntryContentLinePattern(String entryContentLinePattern) {
            this.entryContentLinePattern = entryContentLinePattern;
            return this;
        }

        public String getEntryContentLineSeparatorPattern() {
            return entryContentLineSeparatorPattern;
        }

        public ZipFileFormatConfiguration setEntryContentLineSeparatorPattern(String entryContentLineSeparatorPattern) {
            this.entryContentLineSeparatorPattern = entryContentLineSeparatorPattern;
            return this;
        }

        public String getEntryContentCloserPattern() {
            return entryContentCloserPattern;
        }

        public ZipFileFormatConfiguration setEntryContentCloserPattern(String entryContentCloserPattern) {
            this.entryContentCloserPattern = entryContentCloserPattern;
            return this;
        }

        public String getBinaryEntryContentPlaceholderPattern() {
            return binaryEntryContentPlaceholderPattern;
        }

        public ZipFileFormatConfiguration setBinaryEntryContentPlaceholderPattern(String binaryEntryContentPlaceholderPattern) {
            this.binaryEntryContentPlaceholderPattern = binaryEntryContentPlaceholderPattern;
            return this;
        }

        public String getEmptyZipFilePlaceholderPattern() {
            return emptyZipFilePlaceholderPattern;
        }

        public ZipFileFormatConfiguration setEmptyZipFilePlaceholderPattern(String emptyZipFilePlaceholderPattern) {
            this.emptyZipFilePlaceholderPattern = emptyZipFilePlaceholderPattern;
            return this;
        }
    }

    private interface ZipFileFormatHandler {
        FileUtil.FirstBytes formatZipArchiveContentForView(File zipFile) throws IOException;
    }
}
