package com.codeforces.commons.math;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.lang.StrictMath.abs;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 24.07.13
 */
public class NumberUtil {
    public static final double PI = StrictMath.PI;
    public static final double HALF_PI = 0.5D * PI;
    public static final double DOUBLE_PI = 2.0D * PI;

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

        if (value instanceof Short) {
            return toByte((short) value);
        }

        if (value instanceof Integer) {
            return toByte((int) value);
        }

        if (value instanceof Long) {
            return toByte((long) value);
        }

        if (value instanceof Float) {
            return toByte((float) value);
        }

        if (value instanceof Double) {
            return toByte((double) value);
        }

        if (value instanceof Number) {
            return toByte(((Number) value).doubleValue());
        }

        return toByte(Double.parseDouble(value.toString()));
    }

    @Nullable
    public static Byte toByte(@Nullable String value) {
        return value == null ? null : toByte(Double.parseDouble(value));
    }

    public static byte toByte(short value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") byte byteValue = (byte) value;
        if ((short) byteValue == value) {
            return byteValue;
        }
        throw new IllegalArgumentException("Can't convert short " + value + " to byte.");
    }

    public static byte toByte(int value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") byte byteValue = (byte) value;
        if ((int) byteValue == value) {
            return byteValue;
        }
        throw new IllegalArgumentException("Can't convert int " + value + " to byte.");
    }

    public static byte toByte(long value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") byte byteValue = (byte) value;
        if ((long) byteValue == value) {
            return byteValue;
        }
        throw new IllegalArgumentException("Can't convert long " + value + " to byte.");
    }

    public static byte toByte(float value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") byte byteValue = (byte) value;
        if (abs((float) byteValue - value) < 1.0F) {
            return byteValue;
        }
        throw new IllegalArgumentException("Can't convert float " + value + " to byte.");
    }

    public static byte toByte(double value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") byte byteValue = (byte) value;
        if (abs((double) byteValue - value) < 1.0D) {
            return byteValue;
        }
        throw new IllegalArgumentException("Can't convert double " + value + " to byte.");
    }

    @Nullable
    public static Integer toInt(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Byte) {
            return (int) (byte) value;
        }

        if (value instanceof Short) {
            return (int) (short) value;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Long) {
            return toInt((long) value);
        }

        if (value instanceof Float) {
            return toInt((float) value);
        }

        if (value instanceof Double) {
            return toInt((double) value);
        }

        if (value instanceof Number) {
            return toInt(((Number) value).doubleValue());
        }

        return toInt(Double.parseDouble(value.toString()));
    }

    @Nullable
    public static Integer toInt(@Nullable String value) {
        return value == null ? null : toInt(Double.parseDouble(value));
    }

    public static int toInt(long value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") int intValue = (int) value;
        if ((long) intValue == value) {
            return intValue;
        }
        throw new IllegalArgumentException("Can't convert long " + value + " to int.");
    }

    public static int toInt(float value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") int intValue = (int) value;
        if (abs((float) intValue - value) < 1.0F) {
            return intValue;
        }
        throw new IllegalArgumentException("Can't convert float " + value + " to int.");
    }

    public static int toInt(double value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") int intValue = (int) value;
        if (abs((double) intValue - value) < 1.0D) {
            return intValue;
        }
        throw new IllegalArgumentException("Can't convert double " + value + " to int.");
    }

    @Nullable
    public static Long toLong(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Byte) {
            return (long) (byte) value;
        }

        if (value instanceof Short) {
            return (long) (short) value;
        }

        if (value instanceof Integer) {
            return (long) (int) value;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Float) {
            return toLong((float) value);
        }

        if (value instanceof Double) {
            return toLong((double) value);
        }

        if (value instanceof Number) {
            return toLong(((Number) value).doubleValue());
        }

        return toLong(Double.parseDouble(value.toString()));
    }

    @Nullable
    public static Long toLong(@Nullable String value) {
        return value == null ? null : toLong(Double.parseDouble(value));
    }

    public static long toLong(float value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") long longValue = (long) value;
        if (abs((float) longValue - value) < 1.0F) {
            return longValue;
        }
        throw new IllegalArgumentException("Can't convert float " + value + " to long.");
    }

    public static long toLong(double value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") long longValue = (long) value;
        if (abs((double) longValue - value) < 1.0D) {
            return longValue;
        }
        throw new IllegalArgumentException("Can't convert double " + value + " to long.");
    }

    public static boolean equals(@Nullable Byte numberA, @Nullable Byte numberB) {
        return numberA == null ? numberB == null : numberA.equals(numberB);
    }

    public static boolean equals(@Nullable Byte numberA, @Nullable Byte numberB, @Nullable Byte numberC) {
        return numberA == null ? numberB == null && numberC == null : numberA.equals(numberB) && numberA.equals(numberC);
    }

    public static boolean equals(@Nullable Short numberA, @Nullable Short numberB) {
        return numberA == null ? numberB == null : numberA.equals(numberB);
    }

    public static boolean equals(@Nullable Short numberA, @Nullable Short numberB, @Nullable Short numberC) {
        return numberA == null ? numberB == null && numberC == null : numberA.equals(numberB) && numberA.equals(numberC);
    }

    public static boolean equals(@Nullable Integer numberA, @Nullable Integer numberB) {
        return numberA == null ? numberB == null : numberA.equals(numberB);
    }

    public static boolean equals(@Nullable Integer numberA, @Nullable Integer numberB, @Nullable Integer numberC) {
        return numberA == null ? numberB == null && numberC == null : numberA.equals(numberB) && numberA.equals(numberC);
    }

    public static boolean equals(@Nullable Long numberA, @Nullable Long numberB) {
        return numberA == null ? numberB == null : numberA.equals(numberB);
    }

    public static boolean equals(@Nullable Long numberA, @Nullable Long numberB, @Nullable Long numberC) {
        return numberA == null ? numberB == null && numberC == null : numberA.equals(numberB) && numberA.equals(numberC);
    }

    public static boolean equals(@Nullable Float numberA, @Nullable Float numberB) {
        return numberA == null ? numberB == null : numberA.equals(numberB);
    }

    public static boolean equals(@Nullable Float numberA, @Nullable Float numberB, @Nullable Float numberC) {
        return numberA == null ? numberB == null && numberC == null : numberA.equals(numberB) && numberA.equals(numberC);
    }

    public static boolean equals(@Nullable Double numberA, @Nullable Double numberB) {
        return numberA == null ? numberB == null : numberA.equals(numberB);
    }

    public static boolean equals(@Nullable Double numberA, @Nullable Double numberB, @Nullable Double numberC) {
        return numberA == null ? numberB == null && numberC == null : numberA.equals(numberB) && numberA.equals(numberC);
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

    public static long findLeastCommonMultiple(@Nonnull long[] numbers) {
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

    public static int avg(int numberA, int numberB) {
        return numberA / 2 + numberB / 2 + (numberA % 2 + numberB % 2) / 2;
    }

    public static int avg(int numberA, int numberB, int numberC) {
        return numberA / 3 + numberB / 3 + numberC / 3 + (numberA % 3 + numberB % 3 + numberC % 3) / 3;
    }

    public static long avg(long numberA, long numberB) {
        return numberA / 2L + numberB / 2L + (numberA % 2L + numberB % 2L) / 2L;
    }

    public static long avg(long numberA, long numberB, long numberC) {
        return numberA / 3L + numberB / 3L + numberC / 3L + (numberA % 3L + numberB % 3L + numberC % 3L) / 3L;
    }

    public static float avg(float numberA, float numberB) {
        return numberA * 0.5F + numberB * 0.5F;
    }

    public static float avg(float numberA, float numberB, float numberC) {
        return numberA / 3.0F + numberB / 3.0F + numberC / 3.0F;
    }

    public static double avg(double numberA, double numberB) {
        return numberA * 0.5D + numberB * 0.5D;
    }

    public static double avg(double numberA, double numberB, double numberC) {
        return numberA / 3.0D + numberB / 3.0D + numberC / 3.0D;
    }

    public static double sqr(double value) {
        return value * value;
    }

    public static double sumSqr(double numberA, double numberB) {
        return numberA * numberA + numberB * numberB;
    }

    public static double sumSqr(double numberA, double numberB, double numberC) {
        return numberA * numberA + numberB * numberB + numberC * numberC;
    }
}
