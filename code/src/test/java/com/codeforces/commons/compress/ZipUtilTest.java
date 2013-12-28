package com.codeforces.commons.compress;

import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.io.IoUtil;
import com.codeforces.commons.math.NumberUtil;
import com.codeforces.commons.math.RandomUtil;
import com.google.common.primitives.Ints;
import de.schlichtherle.truezip.file.TFile;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 16.09.11
 */
@SuppressWarnings({"OverlyLongMethod", "CallToPrintStackTrace", "JUnitTestMethodWithNoAssertions"})
public class ZipUtilTest extends TestCase {
    private static void prepareFilesForTestZip(File dir) throws IOException {
        FileUtil.writeFile(new File(dir, "files/description"), getBytes("description"));
        FileUtil.writeFile(new File(dir, "random"), RandomUtil.getRandomBytes(NumberUtil.toInt(
                10L * FileUtil.BYTES_PER_MB + RandomUtil.getRandomInt(1000)
        )));

        File fileSubDir = FileUtil.ensureDirectoryExists(new File(dir, "subdir"));
        FileUtil.writeFile(new File(fileSubDir, "files/memoryx"), getBytes("memoryx"));

        File fileSubSubDir = FileUtil.ensureDirectoryExists(new File(fileSubDir, "subsubdir"));
        FileUtil.writeFile(new File(fileSubSubDir, "files/realtek.log"), getBytes("realtek.log"));

        FileUtil.ensureDirectoryExists(new File(dir, "emptydir"));
    }

    public void testCompressDecompress() throws Exception {
        byte[] plainBytes = RandomUtil.getRandomBytes(10 * Ints.checkedCast(FileUtil.BYTES_PER_MB));
        byte[] compressedBytes = ZipUtil.compress(plainBytes);
        assertTrue(
                "Decompressed bytes does not equal to plain bytes.",
                Arrays.equals(plainBytes, ZipUtil.decompress(compressedBytes))
        );
    }

    public void testZipUnzipAndZipRelatedFileUtilOperations() throws Exception {
        File fileDir = null;
        File fileCopyDir = null;
        File archiveDir = null;
        File unpackedDirFromBytes = null;
        File unpackedDirFromFile = null;
        File copiedDirFromFile = null;
        try {
            String randomToken = RandomUtil.getRandomToken();
            fileDir = FileUtil.createTemporaryDirectory("test-zip-files-" + randomToken);
            fileCopyDir = FileUtil.createTemporaryDirectory("test-zip-files-copy-" + randomToken);
            archiveDir = FileUtil.createTemporaryDirectory("test-zip-archive-" + randomToken);
            unpackedDirFromBytes = FileUtil.createTemporaryDirectory("test-zip-unpacked-from-bytes-" + randomToken);
            unpackedDirFromFile = FileUtil.createTemporaryDirectory("test-zip-unpacked-from-file-" + randomToken);
            copiedDirFromFile = FileUtil.createTemporaryDirectory("test-zip-copied-from-file-" + randomToken);

            prepareFilesForTestZip(fileDir);

            byte[] zipBytes = ZipUtil.zip(fileDir, null);

            File zipFile = new File(archiveDir, "test.zip");
            ZipUtil.zip(fileDir, zipFile, null);

            File zipCopyFile = new File(archiveDir, "copy.zip");
            FileUtil.copyFile(zipFile, zipCopyFile);

            ZipUtil.unzip(zipBytes, unpackedDirFromBytes);
            ZipUtil.unzip(zipFile, unpackedDirFromFile);

            FileUtil.copyDirectory(fileDir, fileCopyDir);
            FileUtil.copyDirectory(new TFile(zipFile), copiedDirFromFile);

            assertTrue(
                    "Unzipped from file directory does not equal to the original directory.",
                    FileUtil.equalsOrSameContent(unpackedDirFromFile, fileDir)
            );

            assertTrue(
                    "Unzipped from bytes directory does not equal to the original directory.",
                    FileUtil.equalsOrSameContent(unpackedDirFromBytes, fileDir)
            );

            assertTrue(
                    "Copied directory does not equal to the original directory.",
                    FileUtil.equalsOrSameContent(fileCopyDir, fileDir)
            );

            assertTrue(
                    "Copied from file directory does not equal to the original directory.",
                    FileUtil.equalsOrSameContent(copiedDirFromFile, fileDir)
            );

            assertTrue(
                    "ZIP-file content does not equal to the original directory.",
                    FileUtil.equalsOrSameContent(new TFile(zipFile), fileDir)
            );

            assertTrue(
                    "ZIP-file content does not equal to the content of ZIP-file copy.",
                    FileUtil.equalsOrSameContent(new TFile(zipFile), new TFile(zipCopyFile))
            );

            assertFalse(
                    "ZIP-file content does equal to the copy of ZIP-file.",
                    FileUtil.equalsOrSameContent(new TFile(zipFile), zipCopyFile)
            );

            assertFalse(
                    "ZIP-file does equal to the content of ZIP-file copy.",
                    FileUtil.equalsOrSameContent(zipFile, new TFile(zipCopyFile))
            );

            assertTrue(
                    "ZIP-file does not equal to the copy of ZIP-file.",
                    FileUtil.equalsOrSameContent(zipFile, zipCopyFile)
            );
        } finally {
            FileUtil.deleteTotally(fileDir);
            FileUtil.deleteTotally(fileCopyDir);
            FileUtil.deleteTotally(archiveDir);
            FileUtil.deleteTotally(unpackedDirFromBytes);
            FileUtil.deleteTotally(unpackedDirFromFile);
            FileUtil.deleteTotally(copiedDirFromFile);
        }
    }

    public void testZipEntryManipulations() throws Exception {
        File fileDir = null;
        File archiveDir = null;
        try {
            String randomToken = RandomUtil.getRandomToken();
            fileDir = FileUtil.createTemporaryDirectory("test-zip-files-" + randomToken);
            archiveDir = FileUtil.createTemporaryDirectory("test-zip-archive-" + randomToken);

            prepareFilesForTestZip(fileDir);

            File zipFile = new File(archiveDir, "test.zip");
            ZipUtil.zip(fileDir, zipFile, null);

            assertTrue(
                    '\'' + zipFile.getName() + "' is not correct non-empty ZIP-file.",
                    ZipUtil.isCorrectZipFile(zipFile, true)
            );

            checkZipEntryExists(zipFile, "random");
            checkZipEntryExists(zipFile, "subdir/subsubdir/files/realtek.log");
            checkZipEntryNotExists(zipFile, "sabako/sabako");
            checkZipEntryNotExists(zipFile, "root-sabako");

            byte[] dogDogBytes = RandomUtil.getRandomBytes(Ints.checkedCast(10L * FileUtil.BYTES_PER_MB));
            ZipUtil.addEntryToZipArchive(zipFile, "sabako/sabako", dogDogBytes);

            byte[] rootDogBytes = RandomUtil.getRandomBytes(Ints.checkedCast(10L * FileUtil.BYTES_PER_MB));
            ZipUtil.addEntryToZipArchive(zipFile, "root-sabako", rootDogBytes);

            assertTrue(
                    '\'' + zipFile.getName() + "' is not correct non-empty ZIP-file (after 'addEntryToZipArchive').",
                    ZipUtil.isCorrectZipFile(zipFile, true)
            );

            checkZipEntryExists(zipFile, "random");
            checkZipEntryExists(zipFile, "subdir/subsubdir/files/realtek.log");
            checkZipEntryExists(zipFile, "sabako/sabako");
            checkZipEntryExists(zipFile, "root-sabako");

            Assert.assertArrayEquals(
                    "'ZipUtil.getZipEntryBytes' returned unexpected result for entry 'sabako/sabako'.",
                    dogDogBytes, ZipUtil.getZipEntryBytes(zipFile, "sabako/sabako")
            );

            Assert.assertArrayEquals(
                    "'ZipUtil.getZipEntryBytes' returned unexpected result for entry 'root-sabako'.",
                    rootDogBytes, ZipUtil.getZipEntryBytes(zipFile, "root-sabako")
            );

            Assert.assertArrayEquals(
                    "'FileUtil.getBytes' returned unexpected result for entry 'sabako/sabako'.",
                    dogDogBytes, FileUtil.getBytes(new TFile(zipFile, "sabako/sabako"))
            );

            Assert.assertArrayEquals(
                    "'FileUtil.getBytes' returned unexpected result for entry 'root-sabako'.",
                    rootDogBytes, FileUtil.getBytes(new TFile(zipFile, "root-sabako"))
            );

            ZipUtil.deleteZipEntry(zipFile, "random");
            ZipUtil.deleteZipEntry(zipFile, "sabako/sabako");

            assertTrue(
                    '\'' + zipFile.getName() + "' is not correct non-empty ZIP-file (after 'deleteZipEntry').",
                    ZipUtil.isCorrectZipFile(zipFile, true)
            );

            checkZipEntryNotExists(zipFile, "random");
            checkZipEntryExists(zipFile, "subdir/subsubdir/files/realtek.log");
            checkZipEntryNotExists(zipFile, "sabako/sabako");
            checkZipEntryExists(zipFile, "root-sabako");
        } finally {
            FileUtil.deleteTotally(fileDir);
            FileUtil.deleteTotally(archiveDir);
        }
    }

    public void testIsCorrectZipFileAndGetBytesInLoopAndMultipleThreads() throws Exception {
        File tempDir = null;
        try {
            String randomToken = RandomUtil.getRandomToken();
            tempDir = FileUtil.createTemporaryDirectory("test-is-correct-zip-file-" + randomToken);

            File emptyArchive = new File(tempDir, "empty.zip");
            File invalidArchive = new File(tempDir, "invalid.zip");
            File validArchive = new File(tempDir, "valid.zip");
            File validInnerArchive = new File(tempDir, "valid-with-inner.zip");

            FileUtil.writeFile(emptyArchive, getBytes(emptyArchive.getName()));
            FileUtil.writeFile(invalidArchive, getBytes(invalidArchive.getName()));
            FileUtil.writeFile(validArchive, getBytes(validArchive.getName()));
            FileUtil.writeFile(validInnerArchive, getBytes(validInnerArchive.getName()));

            List<AssertionFailedError> errors = Collections.synchronizedList(new ArrayList<AssertionFailedError>());
            int iterationCount = 1000;

            Thread emptyArchiveTestThread = startThreadToTestIsCorrectZipFile(
                    emptyArchive, errors, iterationCount, true, true
            );

            Thread invalidArchiveTestThread = startThreadToTestIsCorrectZipFile(
                    invalidArchive, errors, iterationCount, false, false
            );

            Thread validArchiveTestThread = startThreadToTestIsCorrectZipFile(
                    validArchive, errors, iterationCount, true, false
            );

            Thread validInnerArchiveTestThread = startThreadToTestIsCorrectZipFile(
                    new TFile(validInnerArchive, "valid.zip"), errors, iterationCount, true, false
            );

            Thread directoryTestThread = startThreadToTestIsCorrectZipFile(
                    tempDir, errors, iterationCount, false, false
            );

            Thread getBytesTestThread = startThreadToTestGetBytes(
                    validArchive, validInnerArchive, errors, iterationCount
            );

            emptyArchiveTestThread.join();
            invalidArchiveTestThread.join();
            validArchiveTestThread.join();
            validInnerArchiveTestThread.join();
            directoryTestThread.join();
            getBytesTestThread.join();

            if (!errors.isEmpty()) {
                if (errors.size() > 1) {
                    for (AssertionFailedError error : errors) {
                        error.printStackTrace();
                    }
                }

                throw errors.get(0);
            }
        } finally {
            FileUtil.deleteTotally(tempDir);
        }
    }

    private static Thread startThreadToTestGetBytes(final File validArchive, final File validInnerArchive, final List<AssertionFailedError> errors, final int iterationCount) {
        Thread getBytesTestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int iterationIndex = 1; iterationIndex <= iterationCount; ++iterationIndex) {
                    try {
                        try {
                            assertTrue(
                                    "Valid archive and inner valid archive have different content.",
                                    Arrays.equals(
                                            FileUtil.getBytes(validArchive),
                                            FileUtil.getBytes(new TFile(validInnerArchive, "valid.zip"))
                                    )
                            );
                        } catch (IOException e) {
                            throw new AssertionError(e.toString());
                        }
                    } catch (@SuppressWarnings("ErrorNotRethrown") AssertionFailedError e) {
                        errors.add(e);
                        break;
                    }
                }
            }
        });
        getBytesTestThread.start();
        return getBytesTestThread;
    }

    private static Thread startThreadToTestIsCorrectZipFile(
            final File zipFile, final List<AssertionFailedError> errors, final int iterationCount,
            final boolean isCorrectZipFile, final boolean isEmptyZipFile) {
        Thread zipFileTestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int iterationIndex = 1; iterationIndex <= iterationCount; ++iterationIndex) {
                    try {
                        assertEquals(
                                String.format("ZipUtil.isCorrectZipFile(\"%s\", false)", zipFile.getName()),
                                isCorrectZipFile,
                                ZipUtil.isCorrectZipFile(zipFile, false)
                        );
                        assertEquals(
                                String.format("ZipUtil.isCorrectZipFile(\"%s\", true)", zipFile.getName()),
                                isCorrectZipFile && !isEmptyZipFile,
                                ZipUtil.isCorrectZipFile(zipFile, true)
                        );
                        assertEquals(
                                String.format("ZipUtil.isCorrectZipFile(\"%s\")", zipFile.getName()),
                                isCorrectZipFile && !isEmptyZipFile,
                                ZipUtil.isCorrectZipFile(zipFile)
                        );
                    } catch (@SuppressWarnings("ErrorNotRethrown") AssertionFailedError e) {
                        errors.add(e);
                        break;
                    }
                }
            }
        });
        zipFileTestThread.start();
        return zipFileTestThread;
    }

    private static void checkZipEntryExists(File zipFile, String entryName) throws IOException {
        assertTrue(
                '\'' + entryName + "' entry does not exist, but should.",
                ZipUtil.isZipEntryExists(zipFile, entryName)
        );
    }

    private static void checkZipEntryNotExists(File zipFile, String entryName) throws IOException {
        assertFalse(
                '\'' + entryName + "' entry does exist, but shouldn't.",
                ZipUtil.isZipEntryExists(zipFile, entryName)
        );
    }

    private static byte[] getBytes(String resourceName) throws IOException {
        InputStream resourceStream = ZipUtilTest.class.getResourceAsStream(
                "/com/codeforces/commons/compress/" + resourceName
        );

        try {
            return IoUtil.toByteArray(resourceStream);
        } finally {
            IoUtil.closeQuietly(resourceStream);
        }
    }
}
