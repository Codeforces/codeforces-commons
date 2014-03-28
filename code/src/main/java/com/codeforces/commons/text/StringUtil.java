package com.codeforces.commons.text;

import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.io.IoUtil;
import com.codeforces.commons.pair.SimplePair;
import com.codeforces.commons.properties.internal.CommonsPropertiesUtil;
import com.codeforces.commons.reflection.ReflectionUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.lang.reflect.Array;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

/**
 * @author Mike Mirzayanov (mirzayanovmr@gmail.com)
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 10.07.13
 */
public final class StringUtil {
    private static final Pattern FORMAT_COMMENTS_COMMENT_SPLIT_PATTERN = Pattern.compile("\\[pre\\]|\\[/pre\\]");
    private static final Pattern FORMAT_COMMENTS_LINE_BREAK_REPLACE_PATTERN = Pattern.compile("[\n\r][\n\r]+");

    private static final Map<Class, ToStringConverter> toStringConverterByClass = new HashMap<>();
    private static final ReadWriteLock toStringConverterByClassMapLock = new ReentrantReadWriteLock();

    private StringUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param s String.
     * @return {@code true} iff {@code s} is {@code null} or empty.
     */
    public static boolean isEmpty(@Nullable String s) {
        return s == null || s.isEmpty();
    }

    /**
     * @param s String.
     * @return {@code true} iff {@code s} is {@code null}, empty or contains only whitespaces.
     */
    public static boolean isBlank(@Nullable String s) {
        if (s == null || s.isEmpty()) {
            return true;
        }

        for (int charIndex = s.length() - 1; charIndex >= 0; --charIndex) {
            if (!Character.isWhitespace(s.charAt(charIndex))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Compares two strings case-sensitive.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code true} iff both strings A and B are {@code null}
     *         or the string B represents a {@code String} equivalent to the string A
     */
    public static boolean equals(@Nullable String stringA, @Nullable String stringB) {
        return stringA == null ? stringB == null : stringA.equals(stringB);
    }

    /**
     * Compares two strings case-sensitive. {@code null} and empty values considered equals.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code true} iff both strings A and B are {@link #isEmpty(String) empty}
     *         or the string B represents a {@code String} equal to the string A
     */
    public static boolean equalsOrEmpty(@Nullable String stringA, @Nullable String stringB) {
        return isEmpty(stringA) ? isEmpty(stringB) : stringA.equals(stringB);
    }

    /**
     * Compares two strings case-sensitive. {@code null}, empty and blank values considered equals.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code true} iff both strings A and B are {@link #isBlank(String) blank}
     *         or the string B represents a {@code String} equal to the string A
     */
    public static boolean equalsOrBlank(@Nullable String stringA, @Nullable String stringB) {
        return isBlank(stringA) ? isBlank(stringB) : stringA.equals(stringB);
    }

    /**
     * Compares two strings case-insensitive.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code true} iff both strings A and B are {@code null}
     *         or the string B represents a {@code String} equal to the string A
     */
    public static boolean equalsIgnoreCase(@Nullable String stringA, @Nullable String stringB) {
        return stringA == null ? stringB == null : stringA.equalsIgnoreCase(stringB);
    }

    /**
     * Compares two strings case-insensitive. {@code null} and empty values considered equals.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code true} iff both strings A and B are {@link #isEmpty(String) empty}
     *         or the string B represents a {@code String} equal to the string A
     */
    public static boolean equalsOrEmptyIgnoreCase(@Nullable String stringA, @Nullable String stringB) {
        return isEmpty(stringA) ? isEmpty(stringB) : stringA.equalsIgnoreCase(stringB);
    }

    /**
     * Compares two strings case-insensitive. {@code null}, empty and blank values considered equals.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code true} iff both strings A and B are {@link #isBlank(String) blank}
     *         or the string B represents a {@code String} equal to the string A
     */
    public static boolean equalsOrBlankIgnoreCase(@Nullable String stringA, @Nullable String stringB) {
        return isBlank(stringA) ? isBlank(stringB) : stringA.equalsIgnoreCase(stringB);
    }

    @SuppressWarnings({"OverloadedVarargsMethod", "AccessingNonPublicFieldOfAnotherObject"})
    @Nonnull
    public static <T> String toString(
            @Nonnull Class<? extends T> objectClass, @Nullable T object, boolean skipNulls, String... fieldNames) {
        ToStringOptions options = new ToStringOptions();
        options.skipNulls = skipNulls;
        return toString(objectClass, object, options, fieldNames);
    }

    @SuppressWarnings({"OverloadedVarargsMethod", "AccessingNonPublicFieldOfAnotherObject"})
    @Nonnull
    public static <T> String toString(
            @Nonnull Class<? extends T> objectClass, @Nullable T object, @Nonnull ToStringOptions options,
            String... fieldNames) {
        if (object == null) {
            return getSimpleName(objectClass, options.addEnclosingClassNames) + " {null}";
        }

        return toString(object, options.skipNulls, fieldNames);
    }

    @SuppressWarnings({"OverloadedVarargsMethod", "AccessingNonPublicFieldOfAnotherObject"})
    @Nonnull
    public static String toString(@Nonnull Object object, boolean skipNulls, String... fieldNames) {
        ToStringOptions options = new ToStringOptions();
        options.skipNulls = skipNulls;
        return toString(object, options, fieldNames);
    }

    @SuppressWarnings({"OverloadedVarargsMethod", "AssignmentToMethodParameter", "AccessingNonPublicFieldOfAnotherObject"})
    @Nonnull
    public static String toString(@Nonnull Object object, @Nonnull ToStringOptions options, String... fieldNames) {
        Class<?> objectClass = object.getClass();

        if (fieldNames.length == 0) {
            Set<String> allFieldNames = ReflectionUtil.getFieldsByNameMap(objectClass).keySet();
            fieldNames = allFieldNames.toArray(new String[allFieldNames.size()]);
        }

        StringBuilder builder = new StringBuilder(getSimpleName(objectClass, options.addEnclosingClassNames))
                .append(" {");
        boolean firstAppendix = true;

        for (int fieldIndex = 0, fieldCount = fieldNames.length; fieldIndex < fieldCount; ++fieldIndex) {
            String fieldName = fieldNames[fieldIndex];
            if (isBlank(fieldName)) {
                throw new IllegalArgumentException("Field name can not be neither 'null' nor blank.");
            }

            Object deepValue = ReflectionUtil.getDeepValue(object, fieldName);
            String fieldAsString;

            if (deepValue == null) {
                if (options.skipNulls || options.skipEmptyStrings || options.skipBlankStrings) {
                    continue;
                } else {
                    fieldAsString = fieldName + "=null";
                }
            } else {
                fieldAsString = fieldToString(deepValue, fieldName, options);
                if (fieldAsString == null) {
                    continue;
                }
            }

            if (firstAppendix) {
                firstAppendix = false;
            } else {
                builder.append(", ");
            }

            builder.append(fieldAsString);
        }

        return builder.append('}').toString();
    }

    @SuppressWarnings("unchecked")
    public static <T> ToStringConverter<? super T> getToStringConverter(
            @Nonnull Class<T> valueClass, boolean checkSuperclasses) {
        Lock readLock = toStringConverterByClassMapLock.readLock();
        readLock.lock();
        try {
            if (checkSuperclasses) {
                Class localClass = valueClass;
                while (localClass != null) {
                    ToStringConverter toStringConverter = toStringConverterByClass.get(localClass);
                    if (toStringConverter != null) {
                        return toStringConverter;
                    }
                    localClass = localClass.getSuperclass();
                }

                return null;
            } else {
                return toStringConverterByClass.get(valueClass);
            }
        } finally {
            readLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ToStringConverter<T> registerToStringConverter(
            @Nonnull Class<T> valueClass, @Nonnull ToStringConverter<T> converter, boolean overwrite) {
        Lock writeLock = toStringConverterByClassMapLock.writeLock();
        writeLock.lock();
        try {
            ToStringConverter toStringConverter = toStringConverterByClass.get(valueClass);
            if (toStringConverter == null || overwrite) {
                return toStringConverterByClass.put(valueClass, converter);
            } else {
                return toStringConverter;
            }
        } finally {
            writeLock.unlock();
        }
    }

    private static String getSimpleName(@Nonnull Class clazz, boolean addEnclosingClassNames) {
        String simpleName = clazz.getSimpleName();
        if (addEnclosingClassNames) {
            while ((clazz = clazz.getEnclosingClass()) != null) {
                simpleName = String.format("%s.%s", clazz.getSimpleName(), simpleName);
            }
        }
        return simpleName;
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    @Nullable
    private static String fieldToString(@Nonnull Object value, @Nonnull String fieldName, ToStringOptions options) {
        if (value.getClass() == Boolean.class || value.getClass() == boolean.class) {
            return (boolean) value ? fieldName : '!' + fieldName;
        }

        String stringValue = valueToString(value);

        if (options.skipNulls && stringValue == null
                || options.skipEmptyStrings && isEmpty(stringValue)
                || options.skipBlankStrings && isBlank(stringValue)) {
            return null;
        }

        return fieldName + '=' + stringValue;
    }

    @SuppressWarnings({"OverlyComplexMethod", "unchecked"})
    @Nullable
    private static String valueToString(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        Class<?> valueClass = value.getClass();
        ToStringConverter toStringConverter;

        if (valueClass.isArray()) {
            return arrayToString(value);
        } else if ((toStringConverter = getToStringConverter(valueClass, true)) != null) {
            return toStringConverter.convert(value);
        } else if (value instanceof Collection) {
            return collectionToString((Collection) value);
        } else if (value instanceof Map) {
            return mapToString((Map) value);
        } else if (value instanceof Map.Entry) {
            Map.Entry entry = (Map.Entry) value;
            return valueToString(entry.getKey()) + '=' + valueToString(entry.getValue());
        } else if (value instanceof SimplePair) {
            SimplePair pair = (SimplePair) value;
            return valueToString(pair.getFirst()) + '=' + valueToString(pair.getSecond());
        } else if (valueClass == Character.class) {
            return "'" + value + '\'';
        } else if (valueClass == Boolean.class
                || valueClass == Byte.class
                || valueClass == Short.class
                || valueClass == Integer.class
                || valueClass == Long.class
                || valueClass == Float.class
                || valueClass == Double.class) {
            return value.toString();
        } else if (valueClass.isEnum()) {
            return ((Enum) value).name();
        } else if (valueClass == String.class) {
            return '\'' + (String) value + '\'';
        } else {
            return '\'' + String.valueOf(value) + '\'';
        }
    }

    private static String arrayToString(Object array) {
        StringBuilder builder = new StringBuilder("[");
        int length = Array.getLength(array);

        if (length > 0) {
            builder.append(valueToString(Array.get(array, 0)));

            for (int i = 1; i < length; ++i) {
                builder.append(", ").append(valueToString(Array.get(array, i)));
            }
        }

        return builder.append(']').toString();
    }

    private static String collectionToString(Collection collection) {
        StringBuilder builder = new StringBuilder("[");
        Iterator iterator = collection.iterator();

        if (iterator.hasNext()) {
            builder.append(valueToString(iterator.next()));

            while (iterator.hasNext()) {
                builder.append(", ").append(valueToString(iterator.next()));
            }
        }

        return builder.append(']').toString();
    }

    private static String mapToString(Map map) {
        StringBuilder builder = new StringBuilder("{");
        Iterator iterator = map.entrySet().iterator();

        if (iterator.hasNext()) {
            builder.append(valueToString(iterator.next()));

            while (iterator.hasNext()) {
                builder.append(", ").append(valueToString(iterator.next()));
            }
        }

        return builder.append('}').toString();
    }

    /**
     * @param s String.
     * @return {@code s} without spaces or {@code null} if {@code s} is {@code null}
     */
    @Nullable
    public static String stripSpaces(String s) {
        if (s == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) != ' ') {
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }

    public static String trimRight(String s) {
        int lastIndex = s.length() - 1;
        int index = lastIndex;

        while (index >= 0 && s.charAt(index) <= ' ') {
            --index;
        }

        return index == lastIndex ? s : s.substring(0, index + 1);
    }

    public static String trimLeft(String s) {
        int lastIndex = s.length() - 1;
        int index = 0;

        while (index <= lastIndex && s.charAt(index) <= ' ') {
            ++index;
        }

        return index == 0 ? s : s.substring(index, lastIndex + 1);
    }

    /**
     * @param s Comma-separated list of integers or intervals.
     * @return set of integers
     */
    @SuppressWarnings("OverlyNestedMethod")
    public static SortedSet<Integer> parseIntegers(String s) {
        SortedSet<Integer> integers = new TreeSet<>();

        if (s != null) {
            try {
                String[] tokens = Patterns.COMMA_PATTERN.split(s);
                for (int tokenIndex = 0, tokenCount = tokens.length; tokenIndex < tokenCount; ++tokenIndex) {
                    String token = tokens[tokenIndex].trim();
                    if (!token.isEmpty()) {
                        String[] tt = Patterns.MINUS_PATTERN.split(token);
                        if (tt.length == 1) {
                            integers.add(Integer.parseInt(tt[0]));
                        } else if (tt.length == 2) {
                            int from = Integer.parseInt(tt[0]);
                            int to = Integer.parseInt(tt[1]);

                            if (from > to) {
                                throw new IllegalArgumentException("Illegal range in integer list: '" + s + "'.");
                            }

                            for (int i = from; i <= to; i++) {
                                integers.add(i);
                            }
                        } else {
                            throw new IllegalArgumentException("Illegal syntax in integer list: '" + s + "'.");
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
                throw new IllegalArgumentException("Illegal number in integer list: '" + s + "'.");
            }
        }

        return integers;
    }

    /**
     * Formats collection of integers.
     * Uses &amp;,&amp; (comma) as an item delimiter and &amp;-&amp; (minus) as a delimiter for interval ranges.
     *
     * @param numbers collection of integers
     * @return formatted list of integers or empty string if collection is empty
     */
    public static String formatIntegers(@Nonnull Collection<Integer> numbers) {
        return formatIntegers(numbers, ",", "-");
    }

    /**
     * Formats collection of integers.
     * Uses {@code itemSeparator} as an item delimiter and {@code intervalSeparator} as a delimiter for interval ranges.
     *
     * @param numbers           collection of integers
     * @param itemSeparator     item delimiter
     * @param intervalSeparator interval delimiter
     * @return formatted list of integers or empty string empty string if collection is empty
     */
    public static String formatIntegers(
            @Nonnull Collection<Integer> numbers, @Nonnull String itemSeparator, @Nonnull String intervalSeparator) {
        if (numbers.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        boolean firstAppendix = true;
        Integer intervalStart = null;
        Integer previousNumber = null;

        for (int number : numbers instanceof SortedSet ? numbers : new TreeSet<>(numbers)) {
            if (intervalStart == null) {
                intervalStart = number;
            } else if (number > previousNumber + 1) {
                if (firstAppendix) {
                    firstAppendix = false;
                } else {
                    result.append(itemSeparator);
                }

                if (previousNumber > intervalStart) {
                    result.append(intervalStart).append(intervalSeparator).append(previousNumber);
                } else {
                    result.append(intervalStart);
                }

                intervalStart = number;
            }

            previousNumber = number;
        }

        if (!firstAppendix) {
            result.append(itemSeparator);
        }

        if (previousNumber > intervalStart) {
            result.append(intervalStart).append(intervalSeparator).append(previousNumber);
        } else {
            result.append(intervalStart);
        }

        return result.toString();
    }

    /**
     * @param html Raw HTML.
     * @return Replaces "<", ">" and "&" with entities.
     */
    public static String quoteHtml(String html) {
        html = Patterns.AMP_PATTERN.matcher(html).replaceAll("&amp;");
        html = Patterns.LT_PATTERN.matcher(html).replaceAll("&lt;");
        html = Patterns.GT_PATTERN.matcher(html).replaceAll("&gt;");
        return html;
    }

    public static String formatComments(String comment) {
        String[] tokens = FORMAT_COMMENTS_COMMENT_SPLIT_PATTERN.split(comment);
        StringBuilder sb = new StringBuilder();
        boolean inside = false;
        for (String token : tokens) {
            if (inside) {
                sb.append("<pre style='display:inline;'>").append(quoteHtml(token)).append("</pre>");
            } else {
                sb.append(FORMAT_COMMENTS_LINE_BREAK_REPLACE_PATTERN.matcher(quoteHtml(token)).replaceAll("<br/>"));
            }
            inside ^= true;
        }
        return sb.toString();
    }

    /**
     * @param s Windows and Unix styled line sequence.
     * @return Text with windows line breaks.
     */
    @SuppressWarnings({"HardcodedLineSeparator"})
    public static String toWindowsLineBreaks(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == '\n') {
                if (sb.length() == 0 || sb.charAt(sb.length() - 1) != '\r') {
                    sb.append('\r');
                }
                sb.append('\n');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String wellformForWindows(String s) {
        String[] lines = Patterns.CR_PATTERN.split(s);
        if (lines.length == 1) {
            lines = Patterns.LF_PATTERN.split(s);
        }

        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(wellformSingleLineForWindows(line)).append("\r\n");
        }

        int count = 0;
        int pos = sb.length() - 4;
        while (pos >= 0 && "\r\n\r\n".equals(sb.substring(pos, pos + 4))) {
            pos -= 2;
            count++;
        }

        if (count == 0) {
            return sb.toString();
        } else {
            return sb.substring(0, sb.length() - 2 * count);
        }
    }

    /**
     * Removes leading/trailing whitespaces, double spaces -> single spaces, no chars with code less 32.
     *
     * @param line line.
     * @return Result.
     */
    private static String wellformSingleLineForWindows(String line) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c < ' ') {
                c = ' ';
            }
            if ((sb.length() == 0 || sb.charAt(sb.length() - 1) == ' ') && c == ' ') {
                continue;
            }
            sb.append(c);
        }

        return sb.toString().trim();
    }

    public static void convertFileToLinuxStyle(File file) throws IOException {
        byte[] bytes = FileUtil.getBytes(file);
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), "UTF-8"));
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.write(10);
            }
        } finally {
            IoUtil.closeQuietly(reader, writer);
        }
    }

    /**
     * @param s         Given string.
     * @param maxLength Maximal length.
     * @return Makes string to contain no more than maxLength characters.
     *         Removes middle part and inserts "..." instead of it if needed.
     *         Returns {@code null} iff {@code s} is {@code null}.
     */
    public static String shrinkTo(String s, int maxLength) {
        if (maxLength < 8) {
            throw new IllegalArgumentException("Argument maxLength is expected to be at least 8.");
        }

        if (s == null || s.length() <= maxLength) {
            return s;
        } else {
            int prefixLength = maxLength / 2;
            int suffixLength = maxLength - prefixLength - 3;
            return s.substring(0, prefixLength) + "..." + s.substring(s.length() - suffixLength);
        }
    }

    public static List<String> shrinkLinesTo(List<String> lines, int maxLineLength, int maxLineCount) {
        if (maxLineCount < 8) {
            throw new IllegalArgumentException("Argument maxLineCount is expected to be at least 8.");
        }

        if (lines == null) {
            return null;
        }

        List<String> result = new ArrayList<>(maxLineCount);
        if (lines.size() <= maxLineCount) {
            for (String line : lines) {
                result.add(shrinkTo(line, maxLineLength));
            }
        } else {
            int prefixLineCount = maxLineCount / 2;
            int suffixLineCount = maxLineCount - prefixLineCount - 1;

            for (int lineIndex = 0; lineIndex < prefixLineCount; ++lineIndex) {
                result.add(shrinkTo(lines.get(lineIndex), maxLineLength));
            }

            result.add("...");

            for (int lineIndex = lines.size() - suffixLineCount; lineIndex < lines.size(); ++lineIndex) {
                result.add(shrinkTo(lines.get(lineIndex), maxLineLength));
            }
        }

        return result;
    }

    public static String cropLines(String input, int maxLineLength, int maxLineNumber) {
        if (isEmpty(input)) {
            return input;
        }

        String[] lines = Patterns.LINE_BREAK_PATTERN.split(input);
        StringBuilder result = new StringBuilder((maxLineLength + 5) * maxLineNumber + 5);

        for (int i = 0; i < maxLineNumber && i < lines.length; ++i) {
            String line = lines[i];

            String croppedLine;
            if (line.length() > maxLineLength) {
                croppedLine = line.substring(0, maxLineLength) + "...";
            } else {
                croppedLine = line;
            }

            result.append(croppedLine).append("\r\n");
        }

        if (lines.length > maxLineNumber) {
            result.append("...\r\n");
        }

        return result.toString();
    }

    public static boolean containsRussianLetters(@Nullable String s) {
        if (s == null) {
            return false;
        }

        for (int charIndex = 0, charCount = s.length(); charIndex < charCount; ++charIndex) {
            char c = s.charAt(charIndex);
            if (c >= 'а' && c <= 'я' || c >= 'А' && c <= 'Я') {
                return true;
            }
        }

        return false;
    }

    public static byte[] sha1(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA1").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String sha1Hex(byte[] input) {
        try {
            return Hex.encodeHexString(MessageDigest.getInstance("SHA1").digest(input));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String sha1Base64(byte[] input) {
        try {
            return Base64.encodeBase64String(MessageDigest.getInstance("SHA1").digest(input));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String sha1Base64UrlSafe(byte[] input) {
        try {
            return Base64.encodeBase64URLSafeString(MessageDigest.getInstance("SHA1").digest(input));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] hmacSha1(byte[] value, byte[] key) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA1");

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            return mac.doFinal(value);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static String hmacSha1Hex(byte[] value, byte[] key) {
        return Hex.encodeHexString(hmacSha1(value, key));
    }

    public static String hmacSha1Base64(byte[] value, byte[] key) {
        return Base64.encodeBase64String(hmacSha1(value, key));
    }

    public static String hmacSha1Base64UrlSafe(byte[] value, byte[] key) {
        return Base64.encodeBase64URLSafeString(hmacSha1(value, key));
    }

    public static String subscribe(String plainText, String secretKey) {
        String toSubscribe = plainText + CommonsPropertiesUtil.getSubscriptionToken() + secretKey;
        return Base64.encodeBase64URLSafeString(DigestUtils.sha1(toSubscribe)) + '*' + plainText;
    }

    public static String unsubscribe(String subscribedText, String secretKey) {
        int separatorPosition = subscribedText.indexOf('*');
        if (separatorPosition < 0) {
            throw new IllegalArgumentException("Invalid subscribed text.");
        }

        String digest = subscribedText.substring(0, separatorPosition);
        String plainText = subscribedText.substring(separatorPosition + 1);

        String toSubscribe = plainText + CommonsPropertiesUtil.getSubscriptionToken() + secretKey;
        if (!equals(digest, Base64.encodeBase64URLSafeString(DigestUtils.sha1(toSubscribe)))) {
            throw new IllegalArgumentException("Illegal digest.");
        }

        return plainText;
    }

    /**
     * Compares two strings by splitting them on character groups.
     *
     * @param stringA the first string to be compared
     * @param stringB the second string to be compared
     * @return a negative integer, zero, or a positive integer as the first argument
     *         is less than, equal to, or greater than the second
     */
    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    public static int compareStringsSmart(@Nonnull String stringA, @Nonnull String stringB) {
        int lengthA = stringA.length();
        int lengthB = stringB.length();

        StringBuilder numberGroupA = new StringBuilder();
        StringBuilder numberGroupB = new StringBuilder();

        int offsetA = 0;
        int offsetB = 0;

        while (true) {
            char charA;
            char charB;

            while (offsetA < lengthA && !Character.isDigit(charA = stringA.charAt(offsetA))) {
                if (offsetB < lengthB && !Character.isDigit(charB = stringB.charAt(offsetB))) {
                    if (charA != charB) {
                        return (int) charA - (int) charB;
                    }
                } else {
                    return 1;
                }

                ++offsetA;
                ++offsetB;
            }

            if (offsetB < lengthB && !Character.isDigit(stringB.charAt(offsetB))) {
                return -1;
            }

            while (offsetA < lengthA && Character.isDigit(charA = stringA.charAt(offsetA))) {
                numberGroupA.append(charA);
                ++offsetA;
            }

            while (offsetB < lengthB && Character.isDigit(charB = stringB.charAt(offsetB))) {
                numberGroupB.append(charB);
                ++offsetB;
            }

            if (numberGroupA.length() == 0) {
                return numberGroupB.length() == 0 ? 0 : -1;
            }

            if (numberGroupB.length() == 0) {
                return 1;
            }

            String groupValueA = numberGroupA.toString();
            String groupValueB = numberGroupB.toString();

            numberGroupA.delete(0, numberGroupA.length());
            numberGroupB.delete(0, numberGroupB.length());

            long numberA;
            try {
                numberA = Long.parseLong(groupValueA);
            } catch (NumberFormatException ignored) {
                int numberAsStringComparisonResult = groupValueA.compareTo(groupValueB);
                if (numberAsStringComparisonResult == 0) {
                    continue;
                } else {
                    return numberAsStringComparisonResult;
                }
            }

            long numberB;
            try {
                numberB = Long.parseLong(groupValueB);
            } catch (NumberFormatException ignored) {
                return groupValueA.compareTo(groupValueB);
            }

            if (numberA > numberB) {
                return 1;
            }

            if (numberA < numberB) {
                return -1;
            }

            if (groupValueA.length() != groupValueB.length()) {
                return groupValueA.compareTo(groupValueB);
            }
        }
    }

    public static void sortStringsSmart(@Nonnull String[] strings) {
        Arrays.sort(strings, new Comparator<String>() {
            @Override
            public int compare(String stringA, String stringB) {
                return compareStringsSmart(stringA, stringB);
            }
        });
    }

    public static void sortStringsSmart(@Nonnull List<String> strings) {
        Collections.sort(strings, new Comparator<String>() {
            @Override
            public int compare(String stringA, String stringB) {
                return compareStringsSmart(stringA, stringB);
            }
        });
    }

    public static long longHashCode(@Nullable String s) {
        if (s == null) {
            return 0;
        }

        long result = 1171432692373L;
        for (int i = 0; i < s.length(); ++i) {
            result = result * 9369319L + (long) s.charAt(i) * 39916801L + 7561L;
        }
        return result;
    }

    @Nullable
    public static String toString(@Nullable Object object) {
        return object == null ? null : object.toString();
    }

    /**
     * Escape special characters from string to safely pass it to SQL query.
     * Does not escape % and _ characters.
     * @param s - unescaped string
     * @return escaped string
     */
    public static String escapeMySqlString(String s) {
        if (s == null) {
            return null;

        }
        StringBuilder result = new StringBuilder(s.length());

        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);

            switch (c) {
                case 0x00:
                    result.append("\\0");
                    break;
                case 0x08:
                    result.append("\\b");
                    break;
                case 0x09:
                    result.append("\\t");
                    break;
                case 0x0a:
                    result.append("\\n");
                    break;
                case 0x0d:
                    result.append("\\r");
                    break;
                case 0x1a:
                    result.append("\\Z");
                    break;
                case 0x22:
                    result.append("\\\"");
                    break;
                case 0x27:
                    result.append("\\'");
                    break;
                case 0x5c:
                    result.append("\\\\");
                    break;
                default:
                    if (c != '_' && c != '%' && !Character.isLetterOrDigit(c) && c < 256) {
                        result.append('\\').append(c);
                    } else {
                        result.append(c);
                    }
            }
        }

        return result.toString();
    }

    public interface ToStringConverter<T> {
        @Nonnull
        String convert(@Nullable T value);
    }

    public static final class ToStringOptions {
        private boolean skipNulls;
        private boolean skipEmptyStrings;
        private boolean skipBlankStrings;
        private boolean addEnclosingClassNames;

        public ToStringOptions() {
        }

        public ToStringOptions(
                boolean skipNulls, boolean skipEmptyStrings, boolean skipBlankStrings, boolean addEnclosingClassNames) {
            this.skipNulls = skipNulls;
            this.skipEmptyStrings = skipEmptyStrings;
            this.skipBlankStrings = skipBlankStrings;
            this.addEnclosingClassNames = addEnclosingClassNames;
        }

        public boolean isSkipNulls() {
            return skipNulls;
        }

        public void setSkipNulls(boolean skipNulls) {
            this.skipNulls = skipNulls;
        }

        public boolean isSkipEmptyStrings() {
            return skipEmptyStrings;
        }

        public void setSkipEmptyStrings(boolean skipEmptyStrings) {
            this.skipEmptyStrings = skipEmptyStrings;
        }

        public boolean isSkipBlankStrings() {
            return skipBlankStrings;
        }

        public void setSkipBlankStrings(boolean skipBlankStrings) {
            this.skipBlankStrings = skipBlankStrings;
        }

        public boolean isAddEnclosingClassNames() {
            return addEnclosingClassNames;
        }

        public void setAddEnclosingClassNames(boolean addEnclosingClassNames) {
            this.addEnclosingClassNames = addEnclosingClassNames;
        }
    }

    public static class Patterns {
        private Patterns() {
            throw new UnsupportedOperationException();
        }

        public static final Pattern LINE_BREAK_PATTERN = Pattern.compile("\\r\\n|\\r|\\n");
        public static final Pattern PLUS_PATTERN = Pattern.compile("\\+");
        public static final Pattern MINUS_PATTERN = Pattern.compile("\\-");
        public static final Pattern EQ_PATTERN = Pattern.compile("=");
        public static final Pattern LT_PATTERN = Pattern.compile("<");
        public static final Pattern GT_PATTERN = Pattern.compile(">");
        public static final Pattern SPACE_PATTERN = Pattern.compile(" ");
        public static final Pattern NBSP_PATTERN = Pattern.compile("" + (char) 160);
        public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
        public static final Pattern THIN_SPACE_PATTERN = Pattern.compile("" + '\u2009');
        public static final Pattern ZERO_WIDTH_SPACE_PATTERN = Pattern.compile("" + '\u200B');
        public static final Pattern TAB_PATTERN = Pattern.compile("\\t");
        public static final Pattern CR_LF_PATTERN = Pattern.compile("\\r\\n");
        public static final Pattern CR_PATTERN = Pattern.compile("\\r");
        public static final Pattern LF_PATTERN = Pattern.compile("\\n");
        public static final Pattern SLASH_PATTERN = Pattern.compile("/");
        public static final Pattern DOT_PATTERN = Pattern.compile("\\.");
        public static final Pattern COMMA_PATTERN = Pattern.compile(",");
        public static final Pattern SEMICOLON_PATTERN = Pattern.compile(";");
        public static final Pattern COLON_PATTERN = Pattern.compile(":");
        public static final Pattern AMP_PATTERN = Pattern.compile("&");
    }
}
