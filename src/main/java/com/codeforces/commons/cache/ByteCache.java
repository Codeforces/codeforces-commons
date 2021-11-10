package com.codeforces.commons.cache;

import com.codeforces.commons.text.StringUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * All methods of this class throws {@code {@link IllegalArgumentException}} if either section or key
 * is {@code null}, empty or contains illegal characters.
 * Valid characters are: 'a'..'z', 'A'..'Z', '0'..'9', ',', '-', '+', '#', '_', '~'.
 *
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 14.02.2011
 */
@SuppressWarnings("ConstantConditions")
public abstract class ByteCache extends Cache<String, byte[]> {
    private static final Charset STRING_ENCODING = StandardCharsets.UTF_8;

    protected ByteCache() {
    }

    /**
     * Performs a fast check that the storage has value for the given section and key.
     * Does not guarantee value integrity. To validate value integrity
     * use {@code {@link #get(String, String) ByteCache.get(section, key)} != null}.
     *
     * @param section storage section
     * @param key     storage key (unique for each section)
     * @return {@code true} iff sought-for value is presented in the storage
     */
    @Override
    public abstract boolean contains(@Nonnull String section, @Nonnull String key);

    /**
     * Puts value into the storage using given section and key.
     * Replaces old value if exists.
     * Value will be considered outdated
     * ({@code {@link #get(String, String) ByteCache.get(section, key)}} will return {@code null})
     * after {@code lifetimeMillis}.
     *
     * @param section        storage section
     * @param key            storage key (unique for each section)
     * @param value          value to store
     * @param lifetimeMillis value lifetime
     * @throws IllegalArgumentException if value is {@code null}
     */
    @Override
    public abstract void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis);

    /**
     * Puts value into the storage using given section and key.
     * Does not overwrite existing value.
     * Value will be considered outdated
     * ({@code {@link #get(String, String) ByteCache.get(section, key)}} will return {@code null})
     * after {@code lifetimeMillis}.
     *
     * @param section        storage section
     * @param key            storage key (unique for each section)
     * @param value          value to store
     * @param lifetimeMillis value lifetime
     * @throws IllegalArgumentException if value is {@code null}
     */
    @Override
    public abstract void putIfAbsent(
            @Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis);

    /**
     * Puts string into the storage using given section and key.
     * The old value is replaced if exists.
     * This call is equivalent
     * of {@code {@link Cache#put(String, Object, Object) put(section, key, value.getBytes("UTF-8"))}}.
     *
     * @param section storage section
     * @param key     storage key (unique for each section)
     * @param value   string value to store
     * @throws IllegalArgumentException if value is {@code >null}
     */
    public final void putString(@Nonnull String section, @Nonnull String key, @Nonnull String value) {
        put(section, key, value == null ? null : value.getBytes(STRING_ENCODING));
    }

    /**
     * Puts string into the storage using given section and key.
     * The old value is replaced if exists.
     * This call is equivalent
     * of {@code {@link ByteCache#put(String, Object, Object) put(section, key, value.getBytes("UTF-8"))}}.
     * Value will be considered outdated
     * ({@code {@link #get(String, String) ByteCache.get(section, key)}} will return {@code null})
     * after {@code lifetimeMillis}.
     *
     * @param section        storage section
     * @param key            storage key (unique for each section)
     * @param value          string value to store
     * @param lifetimeMillis value lifetime
     * @throws IllegalArgumentException if value is {@code >null}
     */
    public final void putString(
            @Nonnull String section, @Nonnull String key, @Nonnull String value, long lifetimeMillis) {
        put(section, key, value == null ? null : value.getBytes(STRING_ENCODING), lifetimeMillis);
    }

    /**
     * Puts string into the storage using given section and key.
     * Does not overwrite existing value.
     * This call is equivalent
     * of {@code {@link ByteCache#putIfAbsent(String, Object, Object) putIfAbsent(section, key, value.getBytes("UTF-8"))}}.
     *
     * @param section storage section
     * @param key     storage key (unique for each section)
     * @param value   string value to store
     * @throws IllegalArgumentException if value is {@code null}
     */
    public final void putStringIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull String value) {
        putIfAbsent(section, key, value == null ? null : value.getBytes(STRING_ENCODING));
    }

    /**
     * Puts string into the storage using given section and key.
     * Does not overwrite existing value.
     * This call is equivalent
     * of {@code {@link ByteCache#putIfAbsent(String, Object, Object) putIfAbsent(section, key, value.getBytes("UTF-8"))}}.
     * Value will be considered outdated
     * ({@code {@link #get(String, String) ByteCache.get(section, key)}} will return {@code null})
     * after {@code lifetimeMillis}.
     *
     * @param section        storage section
     * @param key            storage key (unique for each section)
     * @param value          string value to store
     * @param lifetimeMillis value lifetime
     * @throws IllegalArgumentException if value is {@code null}
     */
    public final void putStringIfAbsent(
            @Nonnull String section, @Nonnull String key, @Nonnull String value, long lifetimeMillis) {
        putIfAbsent(section, key, value == null ? null : value.getBytes(STRING_ENCODING), lifetimeMillis);
    }

    /**
     * Extracts value from the storage and checks it for consistency.
     *
     * @param section storage section
     * @param key     storage key (unique for each section)
     * @return value iff it's presented in the storage and is consistent, otherwise returns {@code null}
     */
    @Override
    @Nullable
    public abstract byte[] get(@Nonnull String section, @Nonnull String key);

    /**
     * Extracts value from the storage, checks it for consistency and returns it as string.
     * This call is equivalent of {@code new String({@link ByteCache#get(String, String) get(section, key)}, "UTF-8")}
     * if value is not {@code null}.
     *
     * @param section storage section
     * @param key     storage key (unique for each section)
     * @return value iff it's presented in the storage and is consistent, otherwise returns {@code null}
     */
    @Nullable
    public final String getString(@Nonnull String section, @Nonnull String key) {
        byte[] bytes = get(section, key);
        return bytes == null ? null : new String(bytes, STRING_ENCODING);
    }

    @Contract(pure = true)
    private static boolean isValidChar(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z'
                || c == '0' || c >= '1' && c <= '9'
                || c == '[' || c == ']' || c == '(' || c == ')'
                || c == '-' || c == '+'
                || c == ',' || c == '#' || c == '$' || c == '_' || c == '~';
    }

    @SuppressWarnings("ForLoopWithMissingComponent")
    protected static void ensureCacheSectionName(@Nonnull String section) {
        if (StringUtil.isBlank(section)) {
            throw new IllegalArgumentException("Argument 'section' can't be blank.");
        }

        for (int i = section.length(); --i >= 0; ) {
            char c = section.charAt(i);

            if (!isValidChar(c)) {
                throw new IllegalArgumentException(String.format(
                        "Argument 'section' (value='%s') contains forbidden character (code=%d).",
                        section, Character.getNumericValue(c)
                ));
            }
        }
    }

    @SuppressWarnings("ForLoopWithMissingComponent")
    protected static void ensureCacheKeyName(@Nonnull String key) {
        if (StringUtil.isBlank(key)) {
            throw new IllegalArgumentException("Argument 'key' can't be blank.");
        }

        for (int i = key.length(); --i >= 0; ) {
            char c = key.charAt(i);

            if (!isValidChar(c)) {
                throw new IllegalArgumentException(String.format(
                        "Argument 'key' (value='%s') contains forbidden character '%c' (code=%d).",
                        key, c, Character.getNumericValue(c)
                ));
            }
        }
    }
}
