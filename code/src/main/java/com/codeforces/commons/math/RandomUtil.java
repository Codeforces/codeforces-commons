package com.codeforces.commons.math;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author Mike Mirzayanov
 */
public final class RandomUtil {
    private static final Random random = new SecureRandom(generateSeed());

    @SuppressWarnings("SpellCheckingInspection")
    private static final char[] HEX_CHARACTER_SET = "0123456789abcdef".toCharArray();

    @SuppressWarnings("SpellCheckingInspection")
    private static final char[] ALPHABETIC_CHARACTER_SET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    @SuppressWarnings("SpellCheckingInspection")
    private static final char[] NUMERIC_CHARACTER_SET = "0123456789".toCharArray();

    @SuppressWarnings("SpellCheckingInspection")
    private static final char[] ALPHANUMERIC_CHARACTER_SET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private RandomUtil() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    private static byte[] generateSeed() {
        return ByteBuffer.allocate(5 * Long.SIZE / Byte.SIZE)
                .putLong(System.nanoTime())
                .putLong(Thread.currentThread().getId())
                .putLong(Runtime.getRuntime().maxMemory())
                .putLong(Runtime.getRuntime().freeMemory())
                .putLong(Runtime.getRuntime().totalMemory())
                .array();
    }

    /**
     * Generates random hex-string of length 32 which is used as a session token.
     * Equal to {@code {@link #getRandomToken(int) getRandomToken(32)}}.
     *
     * @return randomToken random {@code string}
     * @deprecated Use {@link #getRandomHexToken()}
     */
    @Deprecated
    public static String getRandomToken() {
        return Hex.encodeHexString(getRandomBytes(16));
    }

    /**
     * Generates random hex-string of specific length.
     *
     * @param length hex-string length, should be divisible by 2
     * @return randomToken random {@code string}
     * @deprecated Use {@link #getRandomHex(int)}
     */
    @Deprecated
    public static String getRandomToken(@Nonnegative int length) {
        if (length < 0 || length % 2 != 0) {
            throw new IllegalArgumentException("Argument 'length' is negative or not divisible by 2.");
        }

        return Hex.encodeHexString(getRandomBytes(length / 2));
    }

    /**
     * Generates random hex-string of length 32 which is commonly used as a session token.
     * Equal to {@code {@link #getRandomHex(int) getRandomHex(32)}}.
     *
     * @return random hex-token
     */
    public static String getRandomHexToken() {
        return getRandomHex(32);
    }

    /**
     * Generates random hex-string of specific length.
     *
     * @param length non-negative length
     * @return random hex-string
     */
    public static String getRandomHex(@Nonnegative int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Argument 'length' must be a non-negative integer.");
        }

        return length == 0 ? ""
                : (length & 1) == 0 ? Hex.encodeHexString(getRandomBytesUnchecked(length >> 1))
                : getRandomStringUnchecked(HEX_CHARACTER_SET, length);
    }

    /**
     * Generates random alphabetic-string of specific length.
     *
     * @param length non-negative length
     * @return random alphabetic-string
     */
    public static String getRandomAlphabetic(@Nonnegative int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Argument 'length' must be a non-negative integer.");
        }

        return length == 0 ? "" : getRandomStringUnchecked(ALPHABETIC_CHARACTER_SET, length);
    }

    /**
     * Generates random numeric-string of specific length.
     *
     * @param length non-negative length
     * @return random numeric-string
     */
    public static String getRandomNumeric(@Nonnegative int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Argument 'length' must be a non-negative integer.");
        }

        return length == 0 ? "" : getRandomStringUnchecked(NUMERIC_CHARACTER_SET, length);
    }

    /**
     * Generates random alphanumeric-string of specific length.
     *
     * @param length non-negative length
     * @return random alphanumeric-string
     */
    public static String getRandomAlphanumeric(@Nonnegative int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Argument 'length' must be a non-negative integer.");
        }

        return length == 0 ? "" : getRandomStringUnchecked(ALPHANUMERIC_CHARACTER_SET, length);
    }

    /**
     * Returns the next pseudo-random, uniformly distributed {@code long} value.
     *
     * @return random {@code long}
     */
    public static long getRandomLong() {
        return random.nextLong();
    }

    /**
     * Returns a pseudo-random, uniformly distributed {@code int} value
     * between 0 (inclusive) and the specified value (exclusive).
     *
     * @param n positive upper bound (exclusive)
     * @return random {@code int}
     */
    public static int getRandomInt(@Nonnegative int n) {
        return random.nextInt(n);
    }

    public static byte[] getRandomBytes(@Nonnegative int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Argument 'length' must be a non-negative integer.");
        }

        return length == 0 ? ArrayUtils.EMPTY_BYTE_ARRAY : getRandomBytesUnchecked(length);
    }

    private static byte[] getRandomBytesUnchecked(@Nonnegative int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    private static String getRandomStringUnchecked(@Nonnull char[] characterSet, @Nonnegative int length) {
        Random random = RandomUtil.random;
        int setLength = characterSet.length;
        char[] randomCharacters = new char[length];

        while (--length >= 0) {
            randomCharacters[length] = characterSet[random.nextInt(setLength)];
        }

        return new String(randomCharacters);
    }
}
