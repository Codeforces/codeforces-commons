package com.codeforces.commons.codec;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 28.07.2017
 */
@SuppressWarnings("NumericCastThatLosesPrecision")
public final class PackUtil {
    public static long packInts(int left, int right) {
        return ((long) left << Integer.SIZE) | (right & 0xffffffffL);
    }

    public static int unpackLeftInt(long value) {
        return (int) (value >> Integer.SIZE);
    }

    public static int unpackRightInt(long value) {
        return (int) value;
    }

    public static int packShorts(short left, short right) {
        return ((int) left << Short.SIZE) | (right & 0xffff);
    }

    public static short unpackLeftShort(int value) {
        return (short) (value >> Short.SIZE);
    }

    public static short unpackRightShort(int value) {
        return (short) value;
    }

    public static long packFloats(float left, float right) {
        return packInts(Float.floatToRawIntBits(left), Float.floatToRawIntBits(right));
    }

    public static float unpackLeftFloat(long value) {
        return Float.intBitsToFloat(unpackLeftInt(value));
    }

    public static float unpackRightFloat(long value) {
        return Float.intBitsToFloat(unpackRightInt(value));
    }

    private PackUtil() {
        throw new UnsupportedOperationException();
    }
}
