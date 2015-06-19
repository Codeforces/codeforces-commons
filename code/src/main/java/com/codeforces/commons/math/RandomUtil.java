package com.codeforces.commons.math;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author Mike Mirzayanov
 */
public final class RandomUtil {
    private static final Random random = new SecureRandom(generateSeed());

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
                .putLong(Runtime.getRuntime().totalMemory()).array();
    }

    /**
     * Generates random hex-string of length 32 which used as session token.
     * Equal to {@code {@link #getRandomBytes(int) getRandomBytes(32)}}.
     *
     * @return randomToken random {@code string}
     */
    public static String getRandomToken() {
        return Hex.encodeHexString(getRandomBytes(16));
    }

    /**
     * Generates random hex-string of specific length.
     *
     * @param length hex-string length, should be divisible by 2
     * @return randomToken random {@code string}
     */
    public static String getRandomToken(int length) {
        if (length < 0 || length % 2 != 0) {
            throw new IllegalArgumentException("Argument 'length' is negative or not divisible by 2.");
        }

        return Hex.encodeHexString(getRandomBytes(length / 2));
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
     * @param n upper bound (exclusive)
     * @return random {@code int}
     */
    public static int getRandomInt(int n) {
        return random.nextInt(n);
    }

    public static byte[] getRandomBytes(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Argument 'length' must be a non-negative integer.");
        }

        if (length == 0) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }

        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }
}
