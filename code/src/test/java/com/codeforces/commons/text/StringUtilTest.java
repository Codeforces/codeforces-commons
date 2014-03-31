package com.codeforces.commons.text;

import junit.framework.TestCase;
import org.junit.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 12.09.11
 */
@SuppressWarnings({"MessageMissingOnJUnitAssertion", "HardcodedLineSeparator"})
public class StringUtilTest extends TestCase {
    public void testEmptyOrNull() {
        assertTrue(StringUtil.isEmpty(null));
        assertTrue(StringUtil.isEmpty(""));
        assertFalse(StringUtil.isEmpty("a"));
        assertFalse(StringUtil.isEmpty(" "));
        assertFalse(StringUtil.isEmpty(" a"));
        assertFalse(StringUtil.isEmpty("z "));
        assertFalse(StringUtil.isEmpty("тест"));
    }

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

        assertEquals("", StringUtil.formatIntegers(Collections.<Integer>emptyList()));
        assertEquals("", StringUtil.formatIntegers(Collections.<Integer>emptyList(), ", ", "-"));
        assertEquals("", StringUtil.formatIntegers(Collections.<Integer>emptyList(), ", ", "::"));
        assertEquals("", StringUtil.formatIntegers(Collections.<Integer>emptyList(), "abracadabra", ":::"));

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

    public void testLineBreakPattern() {
        Pattern lineBreakPattern = StringUtil.Patterns.LINE_BREAK_PATTERN;

        assertTrue(Arrays.equals(
                new String[]{"", "cabaca", "abacaba", "b", "c", "", "s"},
                lineBreakPattern.split("\r\ncabaca\r\nabacaba\rb\nc\n\rs\r\n\r\n\r\n")
        ));

        assertEquals(
                "\r\ncabaca\r\nabacaba\r\nb\r\nc\r\n\r\ns\r\n\r\n\r\n",
                lineBreakPattern.matcher("\r\ncabaca\r\nabacaba\rb\nc\n\rs\r\n\r\n\r\n").replaceAll("\r\n")
        );
    }

    public void testCompareSmart() {
        String[] strings = {"A", "BA", "AB", "", "1", "2", "21", "12", "01", "02"};
        String[] sortedStrings = {"", "01", "1", "02", "2", "12", "21", "A", "AB", "BA"};
        StringUtil.sortStringsSmart(strings);
        Assert.assertArrayEquals(sortedStrings, strings);

        strings = new String[]{"AA12", "AA1", "AA1.12", "AA1.11", "AA1.13", "AA12BC1", "AA12BA1", "AA12BC2"};
        sortedStrings = new String[]{"AA1", "AA1.11", "AA1.12", "AA1.13", "AA12", "AA12BA1", "AA12BC1", "AA12BC2"};
        StringUtil.sortStringsSmart(strings);
        Assert.assertArrayEquals(sortedStrings, strings);

        strings = new String[]{"F", "FF", "FA", "FAA", "FAA12", "F12", "FA100"};
        sortedStrings = new String[]{"F", "F12", "FA", "FA100", "FAA", "FAA12", "FF"};
        StringUtil.sortStringsSmart(strings);
        Assert.assertArrayEquals(sortedStrings, strings);

        strings = new String[]{"10", "1", "12", "13", "111", "1", "100"};
        sortedStrings = new String[]{"1", "1", "10", "12", "13", "100", "111"};
        StringUtil.sortStringsSmart(strings);
        Assert.assertArrayEquals(sortedStrings, strings);

        strings = new String[]{"10", "1a1a", "12", "13", "111", "1a1a1", "100"};
        sortedStrings = new String[]{"1a1a", "1a1a1", "10", "12", "13", "100", "111"};
        StringUtil.sortStringsSmart(strings);
        Assert.assertArrayEquals(sortedStrings, strings);

        strings = new String[]{
                "AA.9999999999999999999999.0100", "AA.9999999999999999999999.2", "AA.9999999999999999999999.0200",
                "AA.9999999999999999999998.0100", "AB.99999999999999999999.0100", "AB.0999999999999999999999.0100"
        };
        sortedStrings = new String[]{
                "AA.9999999999999999999998.0100", "AA.9999999999999999999999.2", "AA.9999999999999999999999.0100",
                "AA.9999999999999999999999.0200", "AB.0999999999999999999999.0100", "AB.99999999999999999999.0100"
        };
        StringUtil.sortStringsSmart(strings);
        Assert.assertArrayEquals(sortedStrings, strings);
    }

    public void testToUnixLineBreaks() {
        String text = "ABA\r\n\r\nC\rABA\nDABAC\r\r\n\nABA\r\n";
        String expected = "ABA\n\nC\nABA\nDABAC\n\n\nABA\n";
        String found = StringUtil.toUnixLineBreaks(text);
        assertEquals(expected, found);
    }
}
