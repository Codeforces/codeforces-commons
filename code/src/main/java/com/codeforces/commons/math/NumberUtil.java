package com.codeforces.commons.math;

import com.google.common.primitives.Ints;

import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 24.07.13
 */
public class NumberUtil {
    private NumberUtil() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static Byte toByte(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Byte) {
            return (Byte) value;
        }

        if (value instanceof Number) {
            return toByte(((Number) value).longValue());
        }

        return toByte((double) Double.valueOf(value.toString()));
    }

    public static byte toByte(double value) {
        return toByte(toInt(value));
    }

    public static byte toByte(long value) {
        return toByte(toInt(value));
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    public static byte toByte(int value) {
        if (value > Byte.MAX_VALUE || value < Byte.MIN_VALUE) {
            throw new IllegalArgumentException("Can't convert " + value + " to byte.");
        }

        return (byte) value;
    }

    @Nullable
    public static Integer toInt(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Number) {
            return toInt(((Number) value).longValue());
        }

        return toInt((double) Double.valueOf(value.toString()));
    }

    public static int toInt(double value) {
        return toInt(((Double) value).longValue());
    }

    public static int toInt(long value) {
        return Ints.checkedCast(value);
    }

    @Nullable
    public static Long toLong(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        return toLong((double) Double.valueOf(value.toString()));
    }

    public static long toLong(double value) {
        return ((Double) value).longValue();
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    public static long findGreatestCommonDivisor(long numberA, long numberB) {
        @SuppressWarnings("TooBroadScope") long temp;
        while (numberA != 0 && numberB != 0) {
            numberA %= numberB;
            temp = numberA;
            numberA = numberB;
            numberB = temp;
        }

        return numberA + numberB;
    }

    public static long findLeastCommonMultiple(long numberA, long numberB) {
        return numberA / findGreatestCommonDivisor(numberA, numberB) * numberB;
    }

    public static long findLeastCommonMultiple(long[] numbers) {
        int numberCount = numbers.length;

        if (numberCount == 0) {
            throw new IllegalArgumentException("Can't find LCM for zero numbers.");
        }

        if (numberCount == 1) {
            return numbers[0];
        }

        long leastCommonMultiple = findLeastCommonMultiple(numbers[0], numbers[1]);

        for (int numberIndex = 2; numberIndex < numberCount; ++numberIndex) {
            leastCommonMultiple = findLeastCommonMultiple(leastCommonMultiple, numbers[numberIndex]);
        }

        return leastCommonMultiple;
    }
}
