package com.codeforces.commons.codec;

import javax.annotation.Nonnull;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 22.07.11
 */
public final class Base65536 {
    private Base65536() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts a byte array into a char array using a 65536-based encoding.
     *
     * @param dataBytes array to encode
     * @return encoded char array
     */
    @Nonnull
    @SuppressWarnings({"NumericCastThatLosesPrecision"})
    public static char[] encodeBase65536(@Nonnull byte[] dataBytes) {
        int byteCount = dataBytes.length;
        int charCount = byteCount / 2 + 1;

        char[] dataChars = new char[charCount];

        for (int byteIndex = 0; byteIndex < byteCount; byteIndex += 2) {
            int leftByte = (dataBytes[byteIndex] & 0xFF) << 8;
            int rightByte = (byteIndex + 1 < byteCount ? dataBytes[byteIndex + 1] : 0x01) & 0xFF;

            dataChars[byteIndex / 2] = (char) (leftByte | rightByte);
        }

        if (byteCount % 2 == 0) {
            dataChars[charCount - 1] = (char) 0x0100;
        }

        return dataChars;
    }

    /**
     * Converts a byte array into a {@link String} using a 65536-based encoding.
     * Equivalent to {@code new String(Base65536.encodeBase65536(dataBytes))}.
     *
     * @param dataBytes array to encode
     * @return encoded {@link String}
     */
    @Nonnull
    public static String encodeBase65536String(@Nonnull byte[] dataBytes) {
        return new String(encodeBase65536(dataBytes));
    }

    /**
     * Restores a byte array from a char array encoded with a 65536-based encoding.
     *
     * @param dataChars array to decode
     * @return restored byte array
     * @throws IllegalArgumentException if char array is not a correctly encoded byte array
     */
    @Nonnull
    @SuppressWarnings({"NumericCastThatLosesPrecision"})
    public static byte[] decodeBase65536(@Nonnull char[] dataChars) {
        int charCount = dataChars.length;

        if (dataChars[charCount - 1] != (char) 0x0100 && ((int) dataChars[charCount - 1] & 0xFF) != 0x01) {
            throw new IllegalArgumentException("Argument 'dataChars' is not a correctly Base65536-encoded bytes.");
        }

        int byteCount = dataChars[charCount - 1] == (char) 0x0100 ? (charCount - 1) * 2 : charCount * 2 - 1;

        byte[] dataBytes = new byte[byteCount];

        for (int byteIndex = 0; byteIndex < byteCount; byteIndex += 2) {
            char dataChar = dataChars[byteIndex / 2];

            dataBytes[byteIndex] = (byte) ((int) dataChar >>> 8);

            if (byteIndex + 1 < byteCount) {
                dataBytes[byteIndex + 1] = (byte) ((int) dataChar & 0xFF);
            }
        }

        return dataBytes;
    }

    /**
     * Restores a byte array from a {@link String} encoded with a 65536-based encoding.
     * Equivalent to {@code decodeBase65536(dataString.toCharArray)}.
     *
     * @param dataString {@link String} to decode
     * @return restored byte array
     * @throws IllegalArgumentException if {@link String} is not a correctly encoded byte array
     */
    @Nonnull
    public static byte[] decodeBase65536(@Nonnull String dataString) {
        return decodeBase65536(dataString.toCharArray());
    }
}
