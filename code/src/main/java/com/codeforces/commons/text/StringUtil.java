package com.codeforces.commons.text;

import com.codeforces.commons.collection.MapBuilder;
import com.codeforces.commons.holder.Holders;
import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.io.IoUtil;
import com.codeforces.commons.pair.*;
import com.codeforces.commons.properties.internal.CommonsPropertiesUtil;
import com.codeforces.commons.reflection.ReflectionUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.text.translate.*;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.awt.event.KeyEvent;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.codeforces.commons.math.Math.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Mike Mirzayanov (mirzayanovmr@gmail.com)
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 10.07.2013
 */
@SuppressWarnings({"WeakerAccess", "ForLoopWithMissingComponent"})
public final class StringUtil {
    private static final Pattern FORMAT_COMMENTS_COMMENT_SPLIT_PATTERN = Pattern.compile("\\[pre]|\\[/pre]");
    private static final Pattern FORMAT_COMMENTS_LINE_BREAK_REPLACE_PATTERN = Pattern.compile("[\n\r][\n\r]+");

    private static final Map<Class, ToStringConverter> toStringConverterByClass = new HashMap<>();
    private static final ReadWriteLock toStringConverterByClassMapLock = new ReentrantReadWriteLock();

    private static final String ENCRYPTION_ALGO = "DES";
    private static final String ENCRYPTION_PREFIX = "cfb550";

    private static final CharSequenceTranslator ESCAPE_JAVA_RETAIN_CYRILLIC = new LookupTranslator(
            new MapBuilder<CharSequence, CharSequence>().put("\"", "\\\"").put("\\", "\\\\").buildUnmodifiable()
    ).with(
            new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE)
    ).with(
            JavaUnicodeEscaper.below(32)
    ).with(
            JavaUnicodeEscaper.between(128, (int) 'Ё' - 1)
    ).with(
            JavaUnicodeEscaper.between((int) 'Ё' + 1, (int) 'А' - 1)
    ).with(
            JavaUnicodeEscaper.between((int) 'я' + 1, (int) 'ё' - 1)
    ).with(
            JavaUnicodeEscaper.above((int) 'ё')
    );

    static final char NON_BREAKING_SPACE = (char) 160;
    static final char THIN_SPACE = '\u2009';
    static final char ZERO_WIDTH_SPACE = '\u200B';

    private StringUtil() {
        throw new UnsupportedOperationException();
    }

    public static boolean isWhitespace(char c) {
        return Character.isWhitespace(c) || c == NON_BREAKING_SPACE || c == ZERO_WIDTH_SPACE;
    }

    public static boolean isPrintable(char c) {
        if (c == '\r' || c == '\n' || c == '\t') {
            return true;
        }

        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);

        return !Character.isISOControl(c) && c != KeyEvent.CHAR_UNDEFINED
                && block != null && !Objects.equals(block, Character.UnicodeBlock.SPECIALS);
    }

    @Contract("null -> null")
    @Nullable
    public static String replaceInvisibleCharacters(@Nullable String s) {
        return replaceInvisibleCharacters(s, '�');
    }

    @Contract("null, _ -> null")
    @Nullable
    public static String replaceInvisibleCharacters(@Nullable String s, char replacement) {
        if (s == null) {
            return null;
        }

        int length = s.length();
        char[] result = new char[length];

        for (int i = length; --i >= 0; ) {
            char c = s.charAt(i);
            result[i] = isPrintable(c) ? c : replacement;
        }

        return new String(result);
    }

    @Contract(pure = true)
    public static boolean isCyrillic(char c) {
        return c >= 'а' && c <= 'я' || c >= 'А' && c <= 'Я' || c == 'ё' || c == 'Ё';
    }

    /**
     * Checks that string is cyrillic. Returns {@code true} if and only if
     * {@link #getCyrillicFactor(String) cyrillic factor} of specified string is {@code 1.0}.
     *
     * @param s string to check
     * @return {@code true} iff {@code s} is not {@link #isEmpty(String) empty} and contains only cyrillic letters
     * @see #getCyrillicFactor(String)
     * @see #isCyrillic(char)
     */
    @Contract("null -> false")
    public static boolean isCyrillic(@Nullable String s) {
        if (s == null) {
            return false;
        }

        int length = s.length();
        if (length == 0) {
            return false;
        }

        for (int i = length; --i >= 0; ) {
            if (!isCyrillic(s.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Calculates percent of cyrillic letters in the string. Returns {@code 1.0} if and only if specified string is
     * {@link #isCyrillic(String) cyrillic}.
     *
     * @param s string to calculate
     * @return value between {@code 0.0} and {@code 1.0} both inclusive
     * @see #isCyrillic(String)
     * @see #isCyrillic(char)
     */
    public static double getCyrillicFactor(@Nullable String s) {
        if (s == null) {
            return 0.0D;
        }

        int length = s.length();
        if (length == 0) {
            return 0.0D;
        }

        int matchCount = 0;

        for (int i = length; --i >= 0; ) {
            if (isCyrillic(s.charAt(i))) {
                ++matchCount;
            }
        }

        return (double) matchCount / (double) length;
    }

    /**
     * @param s String.
     * @return {@code true} iff {@code s} is {@code null} or empty.
     */
    @Contract(value = "null -> true", pure = true)
    public static boolean isEmpty(@Nullable String s) {
        return s == null || s.isEmpty();
    }

    /**
     * @param s String.
     * @return {@code true} iff {@code s} is not {@code null} and not empty.
     */
    @Contract(value = "null -> false", pure = true)
    public static boolean isNotEmpty(@Nullable String s) {
        return s != null && !s.isEmpty();
    }

    /**
     * @param s String.
     * @return {@code true} iff {@code s} is {@code null}, empty or contains only whitespaces.
     * @see #isWhitespace(char)
     */
    @Contract("null -> true")
    public static boolean isBlank(@Nullable String s) {
        if (s == null || s.isEmpty()) {
            return true;
        }

        for (int charIndex = s.length(); --charIndex >= 0; ) {
            if (!isWhitespace(s.charAt(charIndex))) {
                return false;
            }
        }

        return true;
    }

    /**
     * @param s String.
     * @return {@code true} iff {@code s} is not {@code null}, not empty
     * and contains at least one character that is not whitespace.
     * @see #isWhitespace(char)
     */
    @Contract("null -> false")
    public static boolean isNotBlank(@Nullable String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }

        for (int charIndex = s.length(); --charIndex >= 0; ) {
            if (!isWhitespace(s.charAt(charIndex))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Compares two strings case-sensitive.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code true} iff both strings A and B are {@code null}
     * or the string B represents a {@code String} equivalent to the string A
     */
    @Contract(value = "null, null -> true; null, !null -> false; !null, null -> false", pure = true)
    public static boolean equals(@Nullable String stringA, @Nullable String stringB) {
        return stringA == null ? stringB == null : stringA.equals(stringB);
    }

    /**
     * Compares two strings case-sensitive. {@code null} and empty values considered equal.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code true} iff both strings A and B are {@link #isEmpty(String) empty}
     * or the string B represents a {@code String} equal to the string A
     */
    public static boolean equalsOrEmpty(@Nullable String stringA, @Nullable String stringB) {
        return isEmpty(stringA) ? isEmpty(stringB) : stringA.equals(stringB);
    }

    /**
     * Compares two strings case-sensitive. {@code null}, empty and blank values considered equal.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code true} iff both strings A and B are {@link #isBlank(String) blank}
     * or the string B represents a {@code String} equal to the string A
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
     * or the string B represents a {@code String} equal to the string A
     */
    @Contract("null, null -> true; null, !null -> false; !null, null -> false")
    public static boolean equalsIgnoreCase(@Nullable String stringA, @Nullable String stringB) {
        return stringA == null ? stringB == null : stringA.equalsIgnoreCase(stringB);
    }

    /**
     * Compares two strings case-insensitive. {@code null} and empty values considered equal.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code true} iff both strings A and B are {@link #isEmpty(String) empty}
     * or the string B represents a {@code String} equal to the string A
     */
    public static boolean equalsOrEmptyIgnoreCase(@Nullable String stringA, @Nullable String stringB) {
        return isEmpty(stringA) ? isEmpty(stringB) : stringA.equalsIgnoreCase(stringB);
    }

    /**
     * Compares two strings case-insensitive. {@code null}, empty and blank values considered equal.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code true} iff both strings A and B are {@link #isBlank(String) blank}
     * or the string B represents a {@code String} equal to the string A
     */
    public static boolean equalsOrBlankIgnoreCase(@Nullable String stringA, @Nullable String stringB) {
        return isBlank(stringA) ? isBlank(stringB) : stringA.equalsIgnoreCase(stringB);
    }

    /**
     * Compares two strings case-sensitive and inverts result.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code false} iff both strings A and B are {@code null}
     * or the string B represents a {@code String} equivalent to the string A
     */
    @Contract(value = "null, null -> false; null, !null -> true; !null, null -> true", pure = true)
    public static boolean notEquals(@Nullable String stringA, @Nullable String stringB) {
        return stringA == null ? stringB != null : !stringA.equals(stringB);
    }

    /**
     * Compares two strings case-sensitive and inverts result. {@code null} and empty values considered equal.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code false} iff both strings A and B are {@link #isEmpty(String) empty}
     * or the string B represents a {@code String} equal to the string A
     */
    public static boolean notEqualsOrEmpty(@Nullable String stringA, @Nullable String stringB) {
        return isEmpty(stringA) ? isNotEmpty(stringB) : !stringA.equals(stringB);
    }

    /**
     * Compares two strings case-sensitive and inverts result.
     * {@code null}, empty and blank values considered equal.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code false} iff both strings A and B are {@link #isBlank(String) blank}
     * or the string B represents a {@code String} equal to the string A
     */
    public static boolean notEqualsOrBlank(@Nullable String stringA, @Nullable String stringB) {
        return isBlank(stringA) ? isNotBlank(stringB) : !stringA.equals(stringB);
    }

    /**
     * Compares two strings case-insensitive and inverts result.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code false} iff both strings A and B are {@code null}
     * or the string B represents a {@code String} equal to the string A
     */
    @Contract("null, null -> false; null, !null -> true; !null, null -> true")
    public static boolean notEqualsIgnoreCase(@Nullable String stringA, @Nullable String stringB) {
        return stringA == null ? stringB != null : !stringA.equalsIgnoreCase(stringB);
    }

    /**
     * Compares two strings case-insensitive and inverts result. {@code null} and empty values considered equal.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code false} iff both strings A and B are {@link #isEmpty(String) empty}
     * or the string B represents a {@code String} equal to the string A
     */
    public static boolean notEqualsOrEmptyIgnoreCase(@Nullable String stringA, @Nullable String stringB) {
        return isEmpty(stringA) ? isNotEmpty(stringB) : !stringA.equalsIgnoreCase(stringB);
    }

    /**
     * Compares two strings case-insensitive and inverts result.
     * {@code null}, empty and blank values considered equal.
     *
     * @param stringA first string
     * @param stringB second string
     * @return {@code false} iff both strings A and B are {@link #isBlank(String) blank}
     * or the string B represents a {@code String} equal to the string A
     */
    public static boolean notEqualsOrBlankIgnoreCase(@Nullable String stringA, @Nullable String stringB) {
        return isBlank(stringA) ? isNotBlank(stringB) : !stringA.equalsIgnoreCase(stringB);
    }

    @Contract(pure = true)
    public static int length(@Nullable String s) {
        return s == null ? 0 : s.length();
    }

    @Contract(pure = true)
    @Nonnull
    public static String nullToEmpty(@Nullable String s) {
        return s == null ? "" : s;
    }

    @Contract(value = "null -> null", pure = true)
    @Nullable
    public static String emptyToNull(@Nullable String s) {
        return s == null || s.isEmpty() ? null : s;
    }

    @Contract(value = "!null, _ -> ! null; _, !null -> ! null", pure = true)
    @Nullable
    public static String nullToDefault(@Nullable String s, @Nullable String defaultValue) {
        return s == null ? defaultValue : s;
    }

    @Contract(value = "null -> null; !null -> !null", pure = true)
    @Nullable
    public static String trim(@Nullable String s) {
        if (s == null) {
            return null;
        }

        int lastIndex = s.length() - 1;
        int beginIndex = 0;
        int endIndex = lastIndex;

        while (beginIndex <= lastIndex && isWhitespace(s.charAt(beginIndex))) {
            ++beginIndex;
        }

        while (endIndex > beginIndex && isWhitespace(s.charAt(endIndex))) {
            --endIndex;
        }

        return beginIndex == 0 && endIndex == lastIndex ? s : s.substring(beginIndex, endIndex + 1);
    }

    @Contract(value = "null -> null", pure = true)
    @Nullable
    public static String trimToNull(@Nullable String s) {
        return s == null ? null : (s = trim(s)).isEmpty() ? null : s;
    }

    @Contract(pure = true)
    @Nonnull
    public static String trimToEmpty(@Nullable String s) {
        return s == null ? "" : trim(s);
    }

    @Contract(value = "null -> null; !null -> !null", pure = true)
    @Nullable
    public static String trimRight(@Nullable String s) {
        if (s == null) {
            return null;
        }

        int lastIndex = s.length() - 1;
        int endIndex = lastIndex;

        while (endIndex >= 0 && isWhitespace(s.charAt(endIndex))) {
            --endIndex;
        }

        return endIndex == lastIndex ? s : s.substring(0, endIndex + 1);
    }

    @Contract(value = "null -> null", pure = true)
    @Nullable
    public static String trimRightToNull(@Nullable String s) {
        return s == null ? null : (s = trimRight(s)).isEmpty() ? null : s;
    }

    @Contract(pure = true)
    @Nonnull
    public static String trimRightToEmpty(@Nullable String s) {
        return s == null ? "" : trimRight(s);
    }

    @Contract(value = "null -> null; !null -> !null", pure = true)
    @Nullable
    public static String trimLeft(@Nullable String s) {
        if (s == null) {
            return null;
        }

        int lastIndex = s.length() - 1;
        int beginIndex = 0;

        while (beginIndex <= lastIndex && isWhitespace(s.charAt(beginIndex))) {
            ++beginIndex;
        }

        return beginIndex == 0 ? s : s.substring(beginIndex, lastIndex + 1);
    }

    @Contract(value = "null -> null", pure = true)
    @Nullable
    public static String trimLeftToNull(@Nullable String s) {
        return s == null ? null : (s = trimLeft(s)).isEmpty() ? null : s;
    }

    @Contract(pure = true)
    @Nonnull
    public static String trimLeftToEmpty(@Nullable String s) {
        return s == null ? "" : trimLeft(s);
    }

    @Contract(value = "null, _ -> false", pure = true)
    public static boolean startsWith(@Nullable String s, @Nonnull String prefix) {
        return s != null && s.startsWith(prefix);
    }

    @Contract(value = "null, _ -> false", pure = true)
    public static boolean endsWith(@Nullable String s, @Nonnull String suffix) {
        return s != null && s.endsWith(suffix);
    }

    /**
     * Splits given string using separator char. All empty parts are included in the result.
     *
     * @param s         the string to be split
     * @param separator the delimiting character
     * @return the array of string parts
     */
    @Nonnull
    public static String[] split(@Nonnull String s, char separator) {
        int length = s.length();
        int start = 0;
        int i = 0;

        String[] parts = null;
        int count = 0;

        while (i < length) {
            if (s.charAt(i) == separator) {
                if (parts == null) {
                    parts = new String[8];
                } else if (count == parts.length) {
                    String[] tempParts = new String[count << 1];
                    System.arraycopy(parts, 0, tempParts, 0, count);
                    parts = tempParts;
                }
                parts[count++] = s.substring(start, i);
                start = ++i;
                continue;
            }
            ++i;
        }

        if (parts == null) {
            return new String[]{s};
        }

        if (count == parts.length) {
            String[] tempParts = new String[count + 1];
            System.arraycopy(parts, 0, tempParts, 0, count);
            parts = tempParts;
        }

        parts[count++] = s.substring(start, i);

        if (count == parts.length) {
            return parts;
        }

        String[] tempParts = new String[count];
        System.arraycopy(parts, 0, tempParts, 0, count);
        return tempParts;
    }

    /**
     * Splits given string into tokens (words) using white-spaces as separators. All empty parts are excluded from the result.
     *
     * @param s the string to be tokenized
     * @return the array of string tokens
     */
    @Nonnull
    public static String[] tokenize(@Nonnull String s) {
        String[] split = Patterns.WHITESPACE_PATTERN.split(s);
        int nonEmptyCount = 0;
        for (String token : split) {
            if (isNotEmpty(token)) {
                nonEmptyCount++;
            }
        }

        String[] result = new String[nonEmptyCount];
        int index = 0;
        for (String token : split) {
            if (isNotEmpty(token)) {
                result[index++] = token;
            }
        }

        return result;
    }

    @Contract(value = "null, _, _ -> null; !null, _, _ -> !null", pure = true)
    @Nullable
    public static String replace(@Nullable String s, @Nullable String target, @Nullable String replacement) {
        if (isEmpty(s) || isEmpty(target) || replacement == null) {
            return s;
        }

        int targetIndex = s.indexOf(target);
        if (targetIndex == -1) {
            return s;
        }

        int i = 0;
        int targetLength = target.length();
        StringBuilder result = new StringBuilder(s.length() + (max(replacement.length() - targetLength, 0) << 4));

        do {
            if (targetIndex > i) {
                result.append(s.substring(i, targetIndex));
            }

            result.append(replacement);
            i = targetIndex + targetLength;
            targetIndex = s.indexOf(target, i);
        } while (targetIndex != -1);

        return result.append(s.substring(i)).toString();
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

        return toString(object, options, fieldNames);
    }

    @SuppressWarnings({"OverloadedVarargsMethod", "AccessingNonPublicFieldOfAnotherObject"})
    @Nonnull
    public static String toString(@Nonnull Object object, boolean skipNulls, String... fieldNames) {
        ToStringOptions options = new ToStringOptions();
        options.skipNulls = skipNulls;
        return toString(object, options, fieldNames);
    }

    // TODO specify max length of long data (strings, arrays, collections, maps)
    // TODO cglib???
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
    @Nullable
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

    @Nullable
    private static String fieldToString(@Nonnull Object value, @Nonnull String fieldName, ToStringOptions options) {
        if (value.getClass() == Boolean.class || value.getClass() == boolean.class) {
            return (boolean) value ? fieldName : '!' + fieldName;
        }

        MutableBoolean quoted = new MutableBoolean();
        String stringValue = valueToString(value, quoted);

        if (shouldSkipField(stringValue, options, quoted)) {
            return null;
        }

        return fieldName + '=' + stringValue;
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "OverlyComplexMethod"})
    private static boolean shouldSkipField(
            @Nullable String stringValue, ToStringOptions options, MutableBoolean quoted) {
        if (options.skipNulls && stringValue == null) {
            return true;
        }

        if (options.skipEmptyStrings) {
            if (quoted != null && quoted.booleanValue()) {
                if ("''".equals(stringValue) || "\"\"".equals(stringValue)) {
                    return true;
                }
            } else {
                if (isEmpty(stringValue)) {
                    return true;
                }
            }
        }

        if (options.skipBlankStrings) {
            if (quoted != null && quoted.booleanValue()) {
                return isBlank(stringValue) || isBlank(stringValue.substring(1, stringValue.length() - 1));
            } else {
                return isBlank(stringValue);
            }
        }

        return false;
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "unchecked"})
    @Contract("null, _ -> null")
    @Nullable
    private static String valueToString(@Nullable Object value, @Nullable MutableBoolean quoted) {
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
            return valueToString(entry.getKey(), null) + ": " + valueToString(entry.getValue(), null);
        } else if (value instanceof Pair) {
            Pair pair = (Pair) value;
            return '(' + valueToString(pair.getFirst(), null) + ", " + valueToString(pair.getSecond(), null) + ')';
        } else if (value instanceof BooleanPair) {
            BooleanPair pair = (BooleanPair) value;
            return '(' + valueToString(pair.getFirst(), null) + ", " + valueToString(pair.getSecond(), null) + ')';
        } else if (value instanceof BytePair) {
            BytePair pair = (BytePair) value;
            return '(' + valueToString(pair.getFirst(), null) + ", " + valueToString(pair.getSecond(), null) + ')';
        } else if (value instanceof ShortPair) {
            ShortPair pair = (ShortPair) value;
            return '(' + valueToString(pair.getFirst(), null) + ", " + valueToString(pair.getSecond(), null) + ')';
        } else if (value instanceof IntPair) {
            IntPair pair = (IntPair) value;
            return '(' + valueToString(pair.getFirst(), null) + ", " + valueToString(pair.getSecond(), null) + ')';
        } else if (value instanceof LongPair) {
            LongPair pair = (LongPair) value;
            return '(' + valueToString(pair.getFirst(), null) + ", " + valueToString(pair.getSecond(), null) + ')';
        } else if (value instanceof FloatPair) {
            FloatPair pair = (FloatPair) value;
            return '(' + valueToString(pair.getFirst(), null) + ", " + valueToString(pair.getSecond(), null) + ')';
        } else if (value instanceof DoublePair) {
            DoublePair pair = (DoublePair) value;
            return '(' + valueToString(pair.getFirst(), null) + ", " + valueToString(pair.getSecond(), null) + ')';
        } else if (valueClass == Character.class) {
            Holders.setQuietly(quoted, true);
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
            Holders.setQuietly(quoted, true);
            return '\'' + (String) value + '\'';
        } else {
            Holders.setQuietly(quoted, true);
            return '\'' + String.valueOf(value) + '\'';
        }
    }

    @Nonnull
    private static String arrayToString(Object array) {
        StringBuilder builder = new StringBuilder("[");
        int length = Array.getLength(array);

        if (length > 0) {
            builder.append(valueToString(Array.get(array, 0), null));

            for (int i = 1; i < length; ++i) {
                builder.append(", ").append(valueToString(Array.get(array, i), null));
            }
        }

        return builder.append(']').toString();
    }

    @Nonnull
    private static String collectionToString(Collection collection) {
        StringBuilder builder = new StringBuilder("[");
        Iterator iterator = collection.iterator();

        if (iterator.hasNext()) {
            builder.append(valueToString(iterator.next(), null));

            while (iterator.hasNext()) {
                builder.append(", ").append(valueToString(iterator.next(), null));
            }
        }

        return builder.append(']').toString();
    }

    @Nonnull
    private static String mapToString(Map map) {
        StringBuilder builder = new StringBuilder("{");
        Iterator iterator = map.entrySet().iterator();

        if (iterator.hasNext()) {
            builder.append(valueToString(iterator.next(), null));

            while (iterator.hasNext()) {
                builder.append(", ").append(valueToString(iterator.next(), null));
            }
        }

        return builder.append('}').toString();
    }

    /**
     * @param s String.
     * @return {@code s} without spaces or {@code null} if {@code s} is {@code null}
     */
    @Contract("null -> null")
    @Nullable
    public static String stripSpaces(@Nullable String s) {
        return removeCharOccurrences(s, ' ');
    }

    @Contract("null, _ -> null")
    @Nullable
    public static String removeCharOccurrences(@Nullable String s, char charToRemove) {
        if (s == null) {
            return null;
        }

        int length = s.length();
        if (length == 0) {
            return s;
        }

        char[] chars = new char[length];
        int pos = -1;

        for (int i = 0; i < length; ++i) {
            char c = s.charAt(i);
            if (c != charToRemove) {
                chars[++pos] = c;
            }
        }

        return pos == length - 1 ? s : new String(chars, 0, pos + 1);
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
                String[] tokens = split(s, ',');
                for (int tokenIndex = 0, tokenCount = tokens.length; tokenIndex < tokenCount; ++tokenIndex) {
                    String token = trim(tokens[tokenIndex]);
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

                            for (int i = from; i <= to; ++i) {
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
    @Nonnull
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
    @Nonnull
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

        Objects.requireNonNull(previousNumber);
        Objects.requireNonNull(intervalStart);

        if (previousNumber > intervalStart) {
            result.append(intervalStart).append(intervalSeparator).append(previousNumber);
        } else {
            result.append(intervalStart);
        }

        return result.toString();
    }

    /**
     * @param html Raw HTML.
     * @return Replaces "&lt;", "&gt;" and "&amp;" with entities.
     */
    public static String quoteHtml(String html) {
        html = Patterns.AMP_PATTERN.matcher(html).replaceAll("&amp;");
        html = Patterns.LT_PATTERN.matcher(html).replaceAll("&lt;");
        html = Patterns.GT_PATTERN.matcher(html).replaceAll("&gt;");
        return html;
    }

    @Nonnull
    public static String formatComments(String comment) {
        String[] tokens = FORMAT_COMMENTS_COMMENT_SPLIT_PATTERN.split(comment);
        StringBuilder sb = new StringBuilder();
        boolean inside = false;
        for (String token : tokens) {
            if (inside) {
                sb.append("<pre style='display: inline;'>").append(quoteHtml(token)).append("</pre>");
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
    @Nonnull
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

    /**
     * @param s Windows and Unix styled line sequence.
     * @return Text with unix line breaks.
     */
    public static String toUnixLineBreaks(String s) {
        return Patterns.LINE_BREAK_PATTERN.matcher(s).replaceAll("\n");
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
            ++count;
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

        for (int i = 0; i < line.length(); ++i) {
            char c = line.charAt(i);
            if (c < ' ') {
                c = ' ';
            }
            if ((sb.length() == 0 || sb.charAt(sb.length() - 1) == ' ') && c == ' ') {
                continue;
            }
            sb.append(c);
        }

        return trim(sb.toString());
    }

    public static void convertFileToLinuxStyle(File file) throws IOException {
        byte[] bytes = FileUtil.getBytes(file);
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), UTF_8));

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
     * If needed removes suffix and appends string with "...".
     * Returns {@code null} iff {@code s} is {@code null}.
     */
    @Contract(value = "!null, _ -> !null", pure = true)
    @Nullable
    public static String cropTo(@Nullable String s, int maxLength) {
        if (maxLength < 8) {
            throw new IllegalArgumentException("Argument maxLength is expected to be at least 8.");
        }

        if (s == null || s.length() <= maxLength) {
            return s;
        } else {
            return s.substring(0, maxLength - 3) + "...";
        }
    }

    /**
     * @param s         Given string.
     * @param maxLength Maximal length.
     * @return Makes string to contain no more than maxLength characters.
     * Removes middle part and inserts "..." instead of it if needed.
     * Returns {@code null} iff {@code s} is {@code null}.
     */
    @Contract(value = "!null, _ -> !null", pure = true)
    @Nullable
    public static String shrinkTo(@Nullable String s, int maxLength) {
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

    @SuppressWarnings("Convert2streamapi")
    @Nullable
    public static List<String> shrinkLinesTo(List<String> lines, int maxLineLength, int maxLineCount) {
        if (maxLineCount < 3) {
            throw new IllegalArgumentException("Argument 'maxLineCount' is expected to be at least 3.");
        }

        if (lines == null) {
            return null;
        }

        int lineCount = lines.size();
        List<String> result = new ArrayList<>(min(maxLineCount, lineCount));

        if (lineCount <= maxLineCount) {
            for (String line : lines) {
                result.add(shrinkTo(line, maxLineLength));
            }
        } else {
            int prefixLineCount = maxLineCount / 2;
            int postfixLineCount = maxLineCount - prefixLineCount - 1;

            for (int lineIndex = 0; lineIndex < prefixLineCount; ++lineIndex) {
                result.add(shrinkTo(lines.get(lineIndex), maxLineLength));
            }

            result.add("...");

            for (int lineIndex = lineCount - postfixLineCount; lineIndex < lineCount; ++lineIndex) {
                result.add(shrinkTo(lines.get(lineIndex), maxLineLength));
            }
        }

        return result;
    }

    @Contract("!null, _, _ -> !null")
    @Nullable
    public static String[] shrinkLinesTo(String[] lines, int maxLineLength, int maxLineCount) {
        if (maxLineCount < 3) {
            throw new IllegalArgumentException("Argument 'maxLineCount' is expected to be at least 3.");
        }

        if (lines == null) {
            return null;
        }

        int lineCount = lines.length;
        String[] result = new String[min(maxLineCount, lineCount)];

        if (lineCount <= maxLineCount) {
            for (int lineIndex = 0; lineIndex < lineCount; ++lineIndex) {
                result[lineIndex] = shrinkTo(lines[lineIndex], maxLineLength);
            }
        } else {
            int prefixLineCount = maxLineCount / 2;
            int postfixLineCount = maxLineCount - prefixLineCount - 1;

            for (int lineIndex = 0; lineIndex < prefixLineCount; ++lineIndex) {
                result[lineIndex] = shrinkTo(lines[lineIndex], maxLineLength);
            }

            int resultPosition = prefixLineCount;
            result[resultPosition] = "...";

            for (int lineIndex = lineCount - postfixLineCount; lineIndex < lineCount; ++lineIndex) {
                result[++resultPosition] = shrinkTo(lines[lineIndex], maxLineLength);
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

    @Contract("null -> false")
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

    @Nonnull
    public static byte[] sha1(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA1").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public static String sha1Hex(byte[] input) {
        try {
            return Hex.encodeHexString(MessageDigest.getInstance("SHA1").digest(input));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public static byte[] sha1(@Nonnull String s) {
        return sha1(s.getBytes(UTF_8));
    }

    @Nonnull
    public static String sha1Hex(@Nonnull String s) {
        return sha1Hex(s.getBytes(UTF_8));
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

    @Nonnull
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

    @SuppressWarnings("Duplicates")
    @Nonnull
    public static String hexEncrypt(@Nonnull String text, @Nonnull String secretKey) {
        text = sha1Hex(text).substring(0, ENCRYPTION_PREFIX.length()) + text;

        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO);
            byte[] input = text.getBytes();
            byte[] encrypted = new byte[input.length * 2];
            SecretKey key = SecretKeyFactory.getInstance(ENCRYPTION_ALGO).generateSecret(new DESKeySpec(sha1((ENCRYPTION_PREFIX + secretKey).getBytes(StandardCharsets.UTF_8))));
            cipher.init(Cipher.ENCRYPT_MODE, key);
            int length = cipher.update(input, 0, input.length, encrypted, 0);
            length += cipher.doFinal(encrypted, length);
            return Hex.encodeHexString(Arrays.copyOf(encrypted, length));
        } catch (Exception e) {
            throw new RuntimeException("Can't encrypt.", e);
        }
    }

    @SuppressWarnings("Duplicates")
    @Nonnull
    public static String hexDecrypt(@Nonnull String text, @Nonnull String secretKey) throws DecryptException {
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO);
            byte[] input = Hex.decodeHex(text);
            byte[] decrypted = new byte[input.length * 2];
            SecretKey key = SecretKeyFactory.getInstance(ENCRYPTION_ALGO).generateSecret(new DESKeySpec(sha1((ENCRYPTION_PREFIX + secretKey).getBytes(StandardCharsets.UTF_8))));
            cipher.init(Cipher.DECRYPT_MODE, key);
            int length = cipher.update(input, 0, input.length, decrypted, 0);
            length += cipher.doFinal(decrypted, length);

            String result = new String(decrypted, 0, length);
            if (result.length() < ENCRYPTION_PREFIX.length() || !result.substring(0, ENCRYPTION_PREFIX.length())
                    .equals(sha1Hex(result.substring(ENCRYPTION_PREFIX.length())).substring(0, ENCRYPTION_PREFIX.length()))) {
                throw new DecryptException("Expected magic prefix after decryption.");
            } else {
                return result.substring(ENCRYPTION_PREFIX.length());
            }
        } catch (Exception e) {
            throw new DecryptException("Can't encrypt.", e);
        }
    }

    /**
     * Compares two strings by splitting them on character groups.
     *
     * @param stringA the first string to be compared
     * @param stringB the second string to be compared
     * @return a negative integer, zero, or a positive integer as the first argument
     * is less than, equal to, or greater than the second
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

            numberGroupA.setLength(0);
            numberGroupB.setLength(0);

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
        Arrays.sort(strings, StringUtil::compareStringsSmart);
    }

    public static void sortStringsSmart(@Nonnull List<String> strings) {
        strings.sort(StringUtil::compareStringsSmart);
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

    @Contract(value = "null -> null; !null -> !null", pure = true)
    @Nullable
    public static String escapeJavaRetainCyrillic(@Nullable String s) {
        return s == null ? null : ESCAPE_JAVA_RETAIN_CYRILLIC.translate(s);
    }

    /**
     * Escape special characters from string to safely pass it to SQL query.
     * Does not escape % and _ characters.
     *
     * @param s - unescaped string
     * @return escaped string
     */
    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "SwitchStatementWithTooManyBranches"})
    @Contract(value = "null -> null; !null -> !null", pure = true)
    @Nullable
    public static String escapeMySqlString(@Nullable String s) {
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

    @SuppressWarnings({"IfStatementWithIdenticalBranches", "OverlyComplexMethod", "AssignmentOrReturnOfFieldWithMutableType"})
    @Contract("null -> null; !null -> !null")
    @Nullable
    public static byte[] removeBoms(@Nullable byte[] bytes) {
        int byteCount;

        if (bytes == null || (byteCount = bytes.length) == 0) {
            return bytes;
        }

        int bomLength;

        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 239 && (bytes[1] & 0xFF) == 187 && (bytes[2] & 0xFF) == 191) { // UTF-8
            bomLength = 3;
        } else if (bytes.length >= 2
                && (bytes[0] & 0xFF) == 254 && (bytes[1] & 0xFF) == 255) { // UTF-16 (BE)
            bomLength = 2;
        } else if (bytes.length >= 2
                && (bytes[0] & 0xFF) == 255 && (bytes[1] & 0xFF) == 254) { // UTF-16 (LE)
            bomLength = 2;
        } else if (bytes.length >= 4
                && (bytes[0] & 0xFF) == 0 && (bytes[1] & 0xFF) == 0
                && (bytes[0] & 0xFF) == 254 && (bytes[1] & 0xFF) == 255) { // UTF-32 (BE)
            bomLength = 4;
        } else if (bytes.length >= 4
                && (bytes[0] & 0xFF) == 255 && (bytes[1] & 0xFF) == 254
                && (bytes[0] & 0xFF) == 0 && (bytes[1] & 0xFF) == 0) { // UTF-32 (LE)
            bomLength = 4;
        } else {
            bomLength = 0;
        }

        if (bomLength == 0) {
            return bytes;
        }

        if (bomLength == byteCount) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }

        byte[] processedBytes = new byte[byteCount - bomLength];
        System.arraycopy(bytes, bomLength, processedBytes, 0, byteCount - bomLength);
        return processedBytes;
    }

    public static void ifNotEmpty(@Nullable String s, @Nonnull Consumer<String> consumer) {
        if (isNotEmpty(s)) {
            consumer.accept(s);
        }
    }

    public static void ifNotBlank(@Nullable String s, @Nonnull Consumer<String> consumer) {
        if (isNotBlank(s)) {
            consumer.accept(s);
        }
    }

    @Contract(value = " -> fail", pure = true)
    public static String firstNonNull() {
        throw new NullPointerException("Can't find non-null argument.");
    }

    @Contract(value = "null -> fail; !null -> param1", pure = true)
    public static String firstNonNull(String string) {
        if (string == null) {
            throw new NullPointerException("Can't find non-null argument.");
        } else {
            return string;
        }
    }

    @Contract(value = "!null, _ -> param1; null, !null -> param2; null, null -> fail", pure = true)
    public static String firstNonNull(String string1, String string2) {
        if (string1 != null) {
            return string1;
        } else if (string2 != null) {
            return string2;
        } else {
            throw new NullPointerException("Can't find non-null argument.");
        }
    }

    @Contract(value = "null -> fail", pure = true)
    public static String firstNonNull(String... strings) {
        if (strings != null) {
            for (String string : strings) {
                if (string != null) {
                    return string;
                }
            }
        }
        throw new NullPointerException("Can't find non-null argument.");
    }

    /**
     * Converts extremely simple HTML to text. For example, use it to convert emails HTML to text.
     *
     * @param html Html to convert
     * @return Text
     */
    @Nonnull
    public static String convertHtmlToText(@Nonnull String html) {
        final StringBuilder sb = new StringBuilder();

        HTMLEditorKit.ParserCallback parserCallback = new HTMLEditorKit.ParserCallback() {
            private final String[] BLOCK_ELEMENT_TAGS = {
                    "p",
                    "h1",
                    "h2",
                    "h3",
                    "h4",
                    "h5",
                    "h6",
                    "ol",
                    "ul",
                    "li",
                    "pre",
                    "address",
                    "blockquote",
                    "dl",
                    "div",
                    "fieldset",
                    "form",
                    "hr",
                    "noscript",
                    "table",
                    "br"
            };

            private boolean readyForNewline;

            @Override
            public void handleText(final char[] data, final int pos) {
                sb.append(new String(data));
                readyForNewline = true;
            }

            @Override
            public void handleStartTag(final HTML.Tag t, final MutableAttributeSet a, final int pos) {
                if (readyForNewline && ArrayUtils.indexOf(BLOCK_ELEMENT_TAGS, t.toString().toLowerCase()) >= 0) {
                    sb.append("\n");
                    if (t == HTML.Tag.P) {
                        sb.append("\n");
                    }
                    readyForNewline = false;
                }
            }

            @Override
            public void handleSimpleTag(final HTML.Tag t, final MutableAttributeSet a, final int pos) {
                handleStartTag(t, a, pos);
            }
        };

        try {
            new ParserDelegator().parse(new StringReader(html), parserCallback, false);
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse.", e);
        }

        return sb.toString();
    }

    @SuppressWarnings("InterfaceNeverImplemented")
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
}
