package com.codeforces.commons.io;

import com.codeforces.commons.text.Patterns;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.lang3.RandomUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.codeforces.commons.math.Math.abs;

/**
 * @author Mike Mirzayanov
 */
@SuppressWarnings({"MessageMissingOnJUnitAssertion"})
public class FileUtilTest extends TestCase {
    private File directory;
    private final Random random = new Random();

    public static Test suite() {
        return new TestSuite(FileUtilTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        directory = File.createTempFile(FileUtilTest.class.getSimpleName(), "");
        directory.delete();
        directory.mkdir();
    }

    @Override
    protected void tearDown() throws Exception {
        FileUtil.deleteTotally(directory);
    }

    private File createFile(int size, boolean binary) throws IOException {
        String name = "file." + random.nextLong();

        byte[] bytes = new byte[size];
        OutputStream output = new FileOutputStream(new File(directory, name));

        for (int i = 0; i < size; ++i) {
            if (binary) {
                bytes[i] = (byte) (random.nextInt(200) + 32);
            } else {
                bytes[i] = (byte) (random.nextInt(26) + 'a');
            }
        }

        output.write(bytes);
        output.close();

        return new File(directory, name);
    }

    public void testCopyEmptyFile() throws IOException {
        File emptyFile = createFile(0, false);
        File targetFile = new File(emptyFile.getParentFile(), emptyFile.getName() + ".copy");
        FileUtil.copyFile(emptyFile, targetFile);
        assertTrue(targetFile.isFile() && targetFile.length() == 0);
    }

    public void testCopyTextFile() throws IOException {
        File textFile = createFile(1024, false);
        File targetFile = new File(textFile.getParentFile(), textFile.getName() + ".copy");
        FileUtil.copyFile(textFile, targetFile);
        assertTrue(targetFile.isFile() && targetFile.length() == 1024);
        assertSameFiles(textFile, targetFile);
    }

    public void testCopyBinaryFile() throws IOException {
        File binaryFile = createFile(1024, true);
        File targetFile = new File(binaryFile.getParentFile(), binaryFile.getName() + ".copy");
        FileUtil.copyFile(binaryFile, targetFile);
        assertTrue(targetFile.isFile() && targetFile.length() == 1024);
        assertSameFiles(binaryFile, targetFile);
    }

    public void testGetBytesOfEmptyFile() throws IOException {
        testGetBytes(0);
    }

    public void testGetBytesOfSmallFile() throws IOException {
        testGetBytes(18);
    }

    public void testGetBytesOfMediumFile() throws IOException {
        testGetBytes(286111);
    }

    public void testGetBytesOfLargeFile() throws IOException {
        testGetBytes(2171901);
    }

    public void testGetBytesOfSpeclFile() throws IOException {
        testGetBytes(1024 * 1024 * 4);
    }

    private void testGetBytes(int size) throws IOException {
        String name = "file." + random.nextLong();

        byte[] bytes = new byte[size];
        OutputStream output = new FileOutputStream(new File(directory, name));

        for (int i = 0; i < size; i++) {
            bytes[i] = (byte) (random.nextInt(200) + 32);
        }

        output.write(bytes);
        output.close();

        byte[] foundBytes = FileUtil.getBytes(new File(directory, name));

        assertTrue(bytes.length == size && foundBytes.length == size);

        for (int i = 0; i < size; i++) {
            assertTrue(bytes[i] == foundBytes[i]);
        }
    }

    public void testLongCopyTextFile() throws IOException {
        File textFile = createFile(1024 * 1024 * 4, false);
        File targetFile = new File(textFile.getParentFile(), textFile.getName() + ".copy");
        FileUtil.copyFile(textFile, targetFile);
        assertTrue(targetFile.isFile() && targetFile.length() == 1024 * 1024 * 4);
        assertSameFiles(textFile, targetFile);
    }

    public void testLongCopyBinaryFile() throws IOException {
        File binaryFile = createFile(3171811, true);
        File targetFile = new File(binaryFile.getParentFile(), binaryFile.getName() + ".copy");
        FileUtil.copyFile(binaryFile, targetFile);
        assertTrue(targetFile.isFile() && targetFile.length() == 3171811);
        assertSameFiles(binaryFile, targetFile);
    }

    public void testFirstBytesConcat() {
        String lineSep = System.getProperty("line.separator");

        {
            FileUtil.FirstBytes first = new FileUtil.FirstBytes(false, "first".getBytes());
            FileUtil.FirstBytes second = new FileUtil.FirstBytes(true, "second".getBytes());

            FileUtil.FirstBytes expected = new FileUtil.FirstBytes(true, ("first" + lineSep + "second").getBytes());
            FileUtil.FirstBytes found = FileUtil.concatenate(first, second);

            assertEquals(expected, found);
        }

        {
            FileUtil.FirstBytes first = new FileUtil.FirstBytes(false, ("first" + lineSep).getBytes());
            FileUtil.FirstBytes second = new FileUtil.FirstBytes(true, "second".getBytes());

            FileUtil.FirstBytes expected = new FileUtil.FirstBytes(true, ("first" + lineSep + "second").getBytes());
            FileUtil.FirstBytes found = FileUtil.concatenate(first, second);

            assertEquals(expected, found);
        }
    }

    public void testFirstBytesRemoveLineStartingWith() {
        String lineSep = System.getProperty("line.separator");

        FileUtil.FirstBytes text = new FileUtil.FirstBytes(true,
                ("line1" + lineSep
                        + "line2" + lineSep
                        + "line3" + lineSep
                        + "line21" + lineSep
                        + "line11" + lineSep
                        + "line31" + lineSep
                        + "line10").getBytes()
        );

        FileUtil.FirstBytes found = FileUtil.removeLinesStartingWith(text, "line1".getBytes());
        FileUtil.FirstBytes expected = new FileUtil.FirstBytes(true,
                ("line2" + lineSep
                        + "line3" + lineSep
                        + "line21" + lineSep
                        + "line31" + lineSep).getBytes()
        );

        assertEquals(expected, found);
    }

    public void testParseSize() throws Exception {
        Map<String, Long> sizeByString = new HashMap<>();
        sizeByString.put("0", 0L);
        sizeByString.put("0.1b", 0L);
        sizeByString.put("0 GB", 0L);
        sizeByString.put("0.0kb", 0L);
        sizeByString.put("1B", 1L);
        sizeByString.put("1 B", 1L);
        sizeByString.put("1 b", 1L);
        sizeByString.put("1kB", FileUtil.BYTES_PER_KB);
        sizeByString.put("1 kb", FileUtil.BYTES_PER_KB);
        sizeByString.put("1KB", FileUtil.BYTES_PER_KB);
        sizeByString.put("1 K", FileUtil.BYTES_PER_KB);
        sizeByString.put("1mB", FileUtil.BYTES_PER_MB);
        sizeByString.put("1 Mb", FileUtil.BYTES_PER_MB);
        sizeByString.put("1MB", FileUtil.BYTES_PER_MB);
        sizeByString.put("1 M", FileUtil.BYTES_PER_MB);
        sizeByString.put("1.5 G", FileUtil.BYTES_PER_GB * 3L / 2L);
        sizeByString.put("1000 P", FileUtil.BYTES_PER_PB * 1000L);
        sizeByString.put("1000p", FileUtil.BYTES_PER_PB * 1000L);

        for (Map.Entry<String, Long> stringAndSize : sizeByString.entrySet()) {
            assertEquals(
                    "Can't parse size '" + stringAndSize.getKey() + "'.",
                    (long) stringAndSize.getValue(),
                    FileUtil.parseSize(stringAndSize.getKey())
            );
        }
    }

    public void testFormatAndParseSize() throws Exception {
        for (int i = 0; i < 10_000; ++i) {
            long size = abs(random.nextLong());
            String formattedSize = FileUtil.formatSize(size);
            double delta = size / 10.0D;

            assertEquals("Can't parse size '" + formattedSize + "'.", size, FileUtil.parseSize(formattedSize), delta);
        }

        executeTestFormatAndParseSizeForUnit(FileUtil.BYTES_PER_PB);
        executeTestFormatAndParseSizeForUnit(FileUtil.BYTES_PER_TB);
        executeTestFormatAndParseSizeForUnit(FileUtil.BYTES_PER_GB);
        executeTestFormatAndParseSizeForUnit(FileUtil.BYTES_PER_MB);
        executeTestFormatAndParseSizeForUnit(FileUtil.BYTES_PER_KB);
        executeTestFormatAndParseSizeForUnit(1L);
    }

    private void executeTestFormatAndParseSizeForUnit(long unit) throws Exception {
        long maxValueExclusive = 1024L * unit;
        double delta = unit / 19.0D;

        for (int i = 0; i < 10_000; ++i) {
            long size = RandomUtils.nextLong(0L, maxValueExclusive);
            String formattedSize = FileUtil.formatSize(size);

            if (random.nextBoolean()) {
                formattedSize = Patterns.WHITESPACE_PATTERN.matcher(formattedSize).replaceAll("");
            }

            if (random.nextBoolean() && (formattedSize.endsWith("B") || formattedSize.endsWith("b"))) {
                formattedSize = formattedSize.substring(0, formattedSize.length() - 1);
            }

            if (random.nextBoolean()) {
                StringBuilder sizeBuilder = new StringBuilder(formattedSize.length());

                for (int charIndex = 0; charIndex < formattedSize.length(); ++charIndex) {
                    if (random.nextBoolean()) {
                        sizeBuilder.append(Character.toUpperCase(formattedSize.charAt(charIndex)));
                    } else {
                        sizeBuilder.append(Character.toLowerCase(formattedSize.charAt(charIndex)));
                    }
                }

                formattedSize = sizeBuilder.toString();
            }

            assertEquals("Can't parse size '" + formattedSize + "'.", size, FileUtil.parseSize(formattedSize), delta);
        }
    }

    private static void assertSameFiles(File a, File b) throws IOException {
        assertTrue(a.isFile());
        assertTrue(b.isFile());
        assertEquals(a.length(), b.length());

        byte[] bytesA = FileUtil.getBytes(a);
        byte[] bytesB = FileUtil.getBytes(b);

        long size = a.length();
        for (int i = 0; i < size; i++) {
            assertEquals(bytesA[i], bytesB[i]);
        }
    }
}
