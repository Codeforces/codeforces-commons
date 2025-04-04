package com.codeforces.commons.text;

import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.io.IoUtil;
import com.codeforces.commons.text.similarity.SimilarityChecker;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 12.09.11
 */
@SuppressWarnings({"MessageMissingOnJUnitAssertion", "HardcodedLineSeparator"})
public class StringUtilTest {
    private static final String BLANK_STRING = String.format(
            " \r\n\t%c%c%c", StringUtil.ZERO_WIDTH_SPACE, StringUtil.THIN_SPACE, StringUtil.NON_BREAKING_SPACE
    );

    @SuppressWarnings("ConstantValue")
    @Test
    public void testIsEmpty() {
        assertTrue(StringUtil.isEmpty(null));
        assertTrue(StringUtil.isEmpty(""));
        assertFalse(StringUtil.isEmpty("a"));
        assertFalse(StringUtil.isEmpty(" "));
        assertFalse(StringUtil.isEmpty("          "));
        assertFalse(StringUtil.isEmpty(" a"));
        assertFalse(StringUtil.isEmpty("z "));
        assertFalse(StringUtil.isEmpty("тест"));
    }

    @Test
    public void testIsBlank() throws Exception {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
        assertFalse(StringUtil.isBlank("a"));
        assertTrue(StringUtil.isBlank(" "));
        assertTrue(StringUtil.isBlank("          "));
        assertFalse(StringUtil.isBlank(" a"));
        assertFalse(StringUtil.isBlank("z "));
        assertFalse(StringUtil.isBlank("тест"));
        assertTrue(StringUtil.isBlank(BLANK_STRING));
        assertFalse(StringUtil.isBlank(BLANK_STRING + '_'));
    }

    @Test
    public void testIsNotEmpty() {
        assertFalse(StringUtil.isNotEmpty(null));
        assertFalse(StringUtil.isNotEmpty(""));
        assertTrue(StringUtil.isNotEmpty("a"));
        assertTrue(StringUtil.isNotEmpty(" "));
        assertTrue(StringUtil.isNotEmpty("          "));
        assertTrue(StringUtil.isNotEmpty(" a"));
        assertTrue(StringUtil.isNotEmpty("z "));
        assertTrue(StringUtil.isNotEmpty("тест"));
    }

    @Test
    public void testIsNotBlank() throws Exception {
        assertFalse(StringUtil.isNotBlank(null));
        assertFalse(StringUtil.isNotBlank(""));
        assertTrue(StringUtil.isNotBlank("a"));
        assertFalse(StringUtil.isNotBlank(" "));
        assertFalse(StringUtil.isNotBlank("          "));
        assertTrue(StringUtil.isNotBlank(" a"));
        assertTrue(StringUtil.isNotBlank("z "));
        assertTrue(StringUtil.isNotBlank("тест"));
        assertFalse(StringUtil.isNotBlank(BLANK_STRING));
        assertTrue(StringUtil.isNotBlank(BLANK_STRING + '_'));
    }

    @Test
    public void testTrimLeft() throws Exception {
        assertNull(StringUtil.trimLeft(null));
        assertEquals("", StringUtil.trimLeft(""));
        assertEquals("a", StringUtil.trimLeft("a"));
        assertEquals("", StringUtil.trimLeft(" "));
        assertEquals("", StringUtil.trimLeft("          "));
        assertEquals("a", StringUtil.trimLeft(" a"));
        assertEquals("z ", StringUtil.trimLeft("z "));
        assertEquals("тест", StringUtil.trimLeft("тест"));
        assertEquals("", StringUtil.trimLeft(BLANK_STRING));
        assertEquals("_", StringUtil.trimLeft(BLANK_STRING + '_'));
        assertEquals('_' + BLANK_STRING, StringUtil.trimLeft('_' + BLANK_STRING));
        assertEquals('_' + BLANK_STRING + '_', StringUtil.trimLeft('_' + BLANK_STRING + '_'));
    }

    @Test
    public void testTrimRight() throws Exception {
        assertNull(StringUtil.trimRight(null));
        assertEquals("", StringUtil.trimRight(""));
        assertEquals("a", StringUtil.trimRight("a"));
        assertEquals("", StringUtil.trimRight(" "));
        assertEquals("", StringUtil.trimRight("          "));
        assertEquals(" a", StringUtil.trimRight(" a"));
        assertEquals("z", StringUtil.trimRight("z "));
        assertEquals("тест", StringUtil.trimRight("тест"));
        assertEquals("", StringUtil.trimRight(BLANK_STRING));
        assertEquals(BLANK_STRING + '_', StringUtil.trimRight(BLANK_STRING + '_'));
        assertEquals("_", StringUtil.trimRight('_' + BLANK_STRING));
        assertEquals('_' + BLANK_STRING + '_', StringUtil.trimRight('_' + BLANK_STRING + '_'));
    }

    @Test
    public void testTrim() throws Exception {
        assertNull(StringUtil.trim(null));
        assertEquals("", StringUtil.trim(""));
        assertEquals("a", StringUtil.trim("a"));
        assertEquals("", StringUtil.trim(" "));
        assertEquals("", StringUtil.trim("          "));
        assertEquals("a", StringUtil.trim(" a"));
        assertEquals("z", StringUtil.trim("z "));
        assertEquals("тест", StringUtil.trim("тест"));
        assertEquals("", StringUtil.trim(BLANK_STRING));
        assertEquals("_", StringUtil.trim(BLANK_STRING + '_'));
        assertEquals("_", StringUtil.trim('_' + BLANK_STRING));
        assertEquals('_' + BLANK_STRING + '_', StringUtil.trim('_' + BLANK_STRING + '_'));
    }

    @Test
    public void testFormatIntegers() throws Exception {
        List<Integer> numbersA = Arrays.asList(1, 2, 4, 6, 7, 9, 10, 11, 12, 13);
        assertEquals("1-2,4,6-7,9-13", StringUtil.formatIntegers(numbersA));
        assertEquals("1-2, 4, 6-7, 9-13", StringUtil.formatIntegers(numbersA, ", ", "-"));
        assertEquals("1::2, 4, 6::7, 9::13", StringUtil.formatIntegers(numbersA, ", ", "::"));

        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;
        List<Integer> numbersB = Arrays.asList(
                min, min + 1, min + 2, -100, -99, -98, 0, 1, 2, 5, max - 3, max - 2, max - 1, max
        );

        assertEquals(
                "-2147483648--2147483646,-100--98,0-2,5,2147483644-2147483647",
                StringUtil.formatIntegers(numbersB)
        );
        assertEquals(
                "-2147483648--2147483646, -100--98, 0-2, 5, 2147483644-2147483647",
                StringUtil.formatIntegers(numbersB, ", ", "-")
        );
        assertEquals(
                "-2147483648::-2147483646, -100::-98, 0::2, 5, 2147483644::2147483647",
                StringUtil.formatIntegers(numbersB, ", ", "::")
        );

        assertEquals("", StringUtil.formatIntegers(Collections.emptyList()));
        assertEquals("", StringUtil.formatIntegers(Collections.emptyList(), ", ", "-"));
        assertEquals("", StringUtil.formatIntegers(Collections.emptyList(), ", ", "::"));
        assertEquals("", StringUtil.formatIntegers(Collections.emptyList(), "abracadabra", ":::"));

        String notBlankString = "\r  cabaca  \r\n";
        assertEquals("\r  cabaca", StringUtil.trimRight(notBlankString));
        assertEquals("cabaca  \r\n", StringUtil.trimLeft(notBlankString));

        String blankString = "\r    \r\n";
        assertEquals("", StringUtil.trimRight(blankString));
        assertEquals("", StringUtil.trimLeft(blankString));

        String notTrimmableString = "cabaca12345";
        assertSame(notTrimmableString, StringUtil.trimRight(notTrimmableString));
        assertSame(notTrimmableString, StringUtil.trimLeft(notTrimmableString));
    }

    @Test
    public void testLineBreakPattern() {
        Pattern lineBreakPattern = Patterns.LINE_BREAK_PATTERN;

        assertTrue(Arrays.equals(
                new String[]{"", "cabaca", "abacaba", "b", "c", "", "s"},
                lineBreakPattern.split("\r\ncabaca\r\nabacaba\rb\nc\n\rs\r\n\r\n\r\n")
        ));

        assertEquals(
                "\r\ncabaca\r\nabacaba\r\nb\r\nc\r\n\r\ns\r\n\r\n\r\n",
                lineBreakPattern.matcher("\r\ncabaca\r\nabacaba\rb\nc\n\rs\r\n\r\n\r\n").replaceAll("\r\n")
        );
    }

    @Test
    public void testCompareSmart() {
        String[] strings = {"A", "BA", "AB", "", "1", "2", "21", "12", "01", "02"};
        String[] sortedStrings = {"", "01", "1", "02", "2", "12", "21", "A", "AB", "BA"};
        StringUtil.sortStringsSmart(strings);
        assertArrayEquals(sortedStrings, strings);

        strings = new String[]{"AA12", "AA1", "AA1.12", "AA1.11", "AA1.13", "AA12BC1", "AA12BA1", "AA12BC2"};
        sortedStrings = new String[]{"AA1", "AA1.11", "AA1.12", "AA1.13", "AA12", "AA12BA1", "AA12BC1", "AA12BC2"};
        StringUtil.sortStringsSmart(strings);
        assertArrayEquals(sortedStrings, strings);

        strings = new String[]{"F", "FF", "FA", "FAA", "FAA12", "F12", "FA100"};
        sortedStrings = new String[]{"F", "F12", "FA", "FA100", "FAA", "FAA12", "FF"};
        StringUtil.sortStringsSmart(strings);
        assertArrayEquals(sortedStrings, strings);

        strings = new String[]{"10", "1", "12", "13", "111", "1", "100"};
        sortedStrings = new String[]{"1", "1", "10", "12", "13", "100", "111"};
        StringUtil.sortStringsSmart(strings);
        assertArrayEquals(sortedStrings, strings);

        strings = new String[]{"10", "1a1a", "12", "13", "111", "1a1a1", "100"};
        sortedStrings = new String[]{"1a1a", "1a1a1", "10", "12", "13", "100", "111"};
        StringUtil.sortStringsSmart(strings);
        assertArrayEquals(sortedStrings, strings);

        strings = new String[]{
                "AA.9999999999999999999999.0100", "AA.9999999999999999999999.2", "AA.9999999999999999999999.0200",
                "AA.9999999999999999999998.0100", "AB.99999999999999999999.0100", "AB.0999999999999999999999.0100"
        };
        sortedStrings = new String[]{
                "AA.9999999999999999999998.0100", "AA.9999999999999999999999.2", "AA.9999999999999999999999.0100",
                "AA.9999999999999999999999.0200", "AB.0999999999999999999999.0100", "AB.99999999999999999999.0100"
        };
        StringUtil.sortStringsSmart(strings);
        assertArrayEquals(sortedStrings, strings);
    }

    @Test
    public void testToUnixLineBreaks() {
        String text = "ABA\r\n\r\nC\rABA\nDABAC\r\r\n\nABA\r\n";
        String expected = "ABA\n\nC\nABA\nDABAC\n\n\nABA\n";
        String found = StringUtil.toUnixLineBreaks(text);
        assertEquals(expected, found);
    }

    @Test
    public void testSplit() throws Exception {
        internalTestSplit("size", '.', new String[]{"size"});
        internalTestSplit("size.dice", '.', new String[]{"size", "dice"});
        internalTestSplit("size.dice.nice.lays.mays", '.', new String[]{"size", "dice", "nice", "lays", "mays"});

        internalTestSplit("size" + StringUtils.repeat("_size", 1000), '.', new String[]{
                "size" + StringUtils.repeat("_size", 1000)
        });
        internalTestSplit("size.dice" + StringUtils.repeat("_dice", 1000), '.', new String[]{
                "size", "dice" + StringUtils.repeat("_dice", 1000)
        });
        internalTestSplit("size.dice.nice.lays.mays" + StringUtils.repeat("_mays", 1000), '.', new String[]{
                "size", "dice", "nice", "lays", "mays" + StringUtils.repeat("_mays", 1000)
        });

        internalTestSplit(".size.", '.', new String[]{"", "size", ""});
        internalTestSplit(" size  ", ' ', new String[]{"", "size", "", ""});
        internalTestSplit(",,,,", ',', new String[]{"", "", "", "", ""});
    }

    @Test
    public void testUnicodeTr39Util() {
        internalTestUnicodeTr39("paypal", "⍴ɑɣ⍴⍺⏽", "⍴⍺у⍴⍺⏽", "раγра١", "раყра1");
        internalTestUnicodeTr39("c०de", "code", "с੦ᑯҽ", "с໐ᑯҽ", "cσd℮");
        internalTestUnicodeTr39("Привет", "∏⍴ᴎʙꬲᴛ", "∏⍴ᴎʙ℮ᴛ");
        internalTestUnicodeTr39("ሀոıc၀ᑯꬲ", "ሀոıс၀Ꮷe", "Unicode");
        internalTestUnicodeTr39("MikeMirzayanov", "МikеМirzауаnоv");
    }

    @Test
    public void testLooksLikeUtil() {
        internalTestLooksLikeUtil("antontrygubO_o", "antontrygub0_o", "antontrygubO_0", "antontrygub0_O",
                "antontrygub0_0"/*, "antontrygub007"*/);
        internalTestLooksLikeUtil("tourist", /*"touristv2",*/ "tourlst"/*, "Tourist1"*/);
        internalTestLooksLikeUtil("MikeMirzayanov", "MlkeMlrzayanov", "MikeMirzayan0v"/*, "MikeMirzayanoff"*/,
                /*"MikeMirzayanow",*/ "mikemirzayanov");

        internalTestLooksLikeUtilDifferent("Errichto", "rng_58", "-is-this-fft-", "1-gon", "abacaba");
    }

    @Test
    public void testLooksLike() {
        assertTrue(SimilarityChecker.isSimilar("antontrygubO_o", "antontrYgubO_0", SimilarityChecker.Mode.SIMPLE_EN));

        assertFalse(SimilarityChecker.isSimilar("antontrygubO_o", "anttrubO_0", SimilarityChecker.Mode.SIMPLE_EN));

        Predicate<String> checker = StringUtil.getSimilarityChecker("scott_wu");
        assertTrue(checker.test("scott_wu"));
        assertTrue(checker.test("sc0tt_wu"));

        checker = StringUtil.getSimilarityChecker("Benq");
        assertFalse(checker.test("Abcd"));

        checker = StringUtil.getSimilarityChecker("vovuh");
        assertFalse(checker.test("Abcd"));
        assertFalse(checker.test("v0hah"));
        assertTrue(checker.test("v0vuh"));

        checker = StringUtil.getSimilarityChecker("tori");
        assertFalse(checker.test("tourist"));
    }

    @Test
    public void testGetRenderingWidth() {
        assertEquals(1, StringUtil.getRenderingWidth("a"));
        assertEquals(1, StringUtil.getRenderingWidth("1"));
        assertEquals(0, StringUtil.getRenderingWidth(null));
        assertEquals(0, StringUtil.getRenderingWidth(""));
        assertTrue(StringUtil.getRenderingWidth("кодефорсес") > 5
                && StringUtil.getRenderingWidth("кодефорсес") < 15);
        assertTrue(StringUtil.getRenderingWidth("ௌ") > 1);
        assertTrue(StringUtil.getRenderingWidth("⸻") > 1);
    }

    @Test
    public void testGetRenderingHeight() {
        assertEquals(1, StringUtil.getRenderingHeight("1"));
        assertEquals(1, StringUtil.getRenderingHeight(null));
        assertEquals(1, StringUtil.getRenderingHeight(""));
        assertEquals(1, StringUtil.getRenderingHeight("кодефорсес"));
        assertEquals(1, StringUtil.getRenderingHeight("ௌ"));
        assertEquals(1, StringUtil.getRenderingHeight("⸻"));
    }

    private static void internalTestUnicodeTr39(String... strings) {
        for (int i = 0; i < strings.length; i++) {
            for (int j = 0; j < strings.length; j++) {
                if (i != j) {
                    assertTrue("Not detected confusable strings: '" + strings[i] + "', '" + strings[j] + "'",
                            SimilarityChecker.isSimilar(strings[i], strings[j], SimilarityChecker.Mode.UNICODE_TR_39));
                }
            }
        }
    }

    private static void internalTestLooksLikeUtil(String... strings) {
        for (int i = 0; i < strings.length; i++) {
            for (int j = 0; j < strings.length; j++) {
                if (i != j) {
                    assertTrue("Not detected looks like strings: '" + strings[i] + "', '" + strings[j] + "'",
                            SimilarityChecker.isSimilar(strings[i], strings[j], SimilarityChecker.Mode.SIMPLE_EN));
                }
            }
        }
    }

    private static void internalTestLooksLikeUtilDifferent(String... strings) {
        for (int i = 0; i < strings.length; i++) {
            for (int j = 0; j < strings.length; j++) {
                if (i != j) {
                    assertFalse("Detected looks like strings: '" + strings[i] + "', '" + strings[j] + "'",
                            SimilarityChecker.isSimilar(strings[i], strings[j], SimilarityChecker.Mode.SIMPLE_EN));
                }
            }
        }
    }

    private static void internalTestSplit(String s, char c, String[] parts) {
        assertArrayEquals("Illegal split of '" + s + "' by '" + c + "'.", parts, StringUtil.split(s, c));
    }

    @Test
    public void testWellformForWindows() {
        assertEquals("1\r\n", StringUtil.wellformForWindows("\r\n\r\n  1   \r\n  \r\n  "));
        assertEquals("a b c\r\nd e\r\n", StringUtil.wellformForWindows("\r\n\r\n  a  b     c     \r\n  d      e"));
        assertEquals("a b c\r\n\r\nd e\r\n", StringUtil.wellformForWindows("a b c\r\n\r\nd e\r\n"));
    }

    @Test
    public void testConvertFileToLinuxStyle() throws IOException {
        testConvertFileToLinuxStyle("01");
        testConvertFileToLinuxStyle("02");
        testConvertFileToLinuxStyle("03");
        testConvertFileToLinuxStyle("04");
        testConvertFileToLinuxStyle("05");
        testConvertFileToLinuxStyle("06");
    }

    private void testConvertFileToLinuxStyle(String fileName) throws IOException {
        byte[] inputBytes = getConvertFileToLinuxStyleResourceBytes(fileName + ".in");
        byte[] answerBytes = getConvertFileToLinuxStyleResourceBytes(fileName + ".out");

        File inputFile = File.createTempFile("convertFileToLinuxStyle", fileName + ".in");
        FileUtil.writeFile(inputFile, inputBytes);

        StringUtil.convertFileToLinuxStyle(inputFile);
        byte[] outputBytes = FileUtil.getBytes(inputFile);
        FileUtil.deleteTotally(inputFile);

        Assert.assertArrayEquals("Subtest '" + fileName + "' failed.",
                answerBytes, outputBytes);
    }

    private static byte[] getConvertFileToLinuxStyleResourceBytes(String resourceName) throws IOException {
        String resource = "/com/codeforces/commons/text/convertFileToLinuxStyle/" + resourceName;

        InputStream resourceStream = StringUtilTest.class.getResourceAsStream(
                resource);
        if (resourceStream == null) {
            throw new IOException("Can't find resource '" + resource + "'.");
        }

        try {
            return IoUtil.toByteArray(resourceStream);
        } finally {
            IoUtil.closeQuietly(resourceStream);
        }
    }
}
