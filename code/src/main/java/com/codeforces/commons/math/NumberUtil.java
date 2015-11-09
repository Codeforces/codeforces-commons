package com.codeforces.commons.math;

import com.codeforces.commons.text.StringUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;

import static com.codeforces.commons.math.Math.abs;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 24.07.2013
 */
public final class NumberUtil {
    private NumberUtil() {
        throw new UnsupportedOperationException();
    }

    @Contract("null -> null; !null -> !null")
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

        return toByte(Double.parseDouble(StringUtil.trim(value.toString())));
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Byte toByte(@Nullable String value) {
        return value == null ? null : toByte(Double.parseDouble(StringUtil.trim(value)));
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

    @Contract("null -> null; !null -> !null")
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

        return toInt(Double.parseDouble(StringUtil.trim(value.toString())));
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Integer toInt(@Nullable String value) {
        return value == null ? null : toInt(Double.parseDouble(StringUtil.trim(value)));
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

    @Contract("null -> null; !null -> !null")
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

        return toLong(Double.parseDouble(StringUtil.trim(value.toString())));
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Long toLong(@Nullable String value) {
        return value == null ? null : toLong(Double.parseDouble(StringUtil.trim(value)));
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

    @Contract(value = "null, null -> true; null, !null -> false; !null, null -> false", pure = true)
    public static boolean equals(@Nullable Byte numberA, @Nullable Byte numberB) {
        return numberA == null ? numberB == null : numberA.equals(numberB);
    }

    @Contract(value = "null, null, null -> true; null, !null, _ -> false; !null, null, _ -> false; null, _, !null -> false; !null, _, null -> false; _, null, !null -> false; _, !null, null -> false", pure = true)
    public static boolean equals(@Nullable Byte numberA, @Nullable Byte numberB, @Nullable Byte numberC) {
        return numberA == null ? numberB == null && numberC == null : numberA.equals(numberB) && numberA.equals(numberC);
    }

    @Contract(value = "null, null -> true; null, !null -> false; !null, null -> false", pure = true)
    public static boolean equals(@Nullable Short numberA, @Nullable Short numberB) {
        return numberA == null ? numberB == null : numberA.equals(numberB);
    }

    @Contract(value = "null, null, null -> true; null, !null, _ -> false; !null, null, _ -> false; null, _, !null -> false; !null, _, null -> false; _, null, !null -> false; _, !null, null -> false", pure = true)
    public static boolean equals(@Nullable Short numberA, @Nullable Short numberB, @Nullable Short numberC) {
        return numberA == null ? numberB == null && numberC == null : numberA.equals(numberB) && numberA.equals(numberC);
    }

    @Contract(value = "null, null -> true; null, !null -> false; !null, null -> false", pure = true)
    public static boolean equals(@Nullable Integer numberA, @Nullable Integer numberB) {
        return numberA == null ? numberB == null : numberA.equals(numberB);
    }

    @Contract(value = "null, null, null -> true; null, !null, _ -> false; !null, null, _ -> false; null, _, !null -> false; !null, _, null -> false; _, null, !null -> false; _, !null, null -> false", pure = true)
    public static boolean equals(@Nullable Integer numberA, @Nullable Integer numberB, @Nullable Integer numberC) {
        return numberA == null ? numberB == null && numberC == null : numberA.equals(numberB) && numberA.equals(numberC);
    }

    @Contract(value = "null, null -> true; null, !null -> false; !null, null -> false", pure = true)
    public static boolean equals(@Nullable Long numberA, @Nullable Long numberB) {
        return numberA == null ? numberB == null : numberA.equals(numberB);
    }

    @Contract(value = "null, null, null -> true; null, !null, _ -> false; !null, null, _ -> false; null, _, !null -> false; !null, _, null -> false; _, null, !null -> false; _, !null, null -> false", pure = true)
    public static boolean equals(@Nullable Long numberA, @Nullable Long numberB, @Nullable Long numberC) {
        return numberA == null ? numberB == null && numberC == null : numberA.equals(numberB) && numberA.equals(numberC);
    }

    @Contract(value = "null, null -> true; null, !null -> false; !null, null -> false", pure = true)
    public static boolean equals(@Nullable Float numberA, @Nullable Float numberB) {
        return numberA == null ? numberB == null : numberA.equals(numberB);
    }

    @Contract(value = "null, null, null -> true; null, !null, _ -> false; !null, null, _ -> false; null, _, !null -> false; !null, _, null -> false; _, null, !null -> false; _, !null, null -> false", pure = true)
    public static boolean equals(@Nullable Float numberA, @Nullable Float numberB, @Nullable Float numberC) {
        return numberA == null ? numberB == null && numberC == null : numberA.equals(numberB) && numberA.equals(numberC);
    }

    @Contract(value = "null, null -> true; null, !null -> false; !null, null -> false", pure = true)
    public static boolean equals(@Nullable Double numberA, @Nullable Double numberB) {
        return numberA == null ? numberB == null : numberA.equals(numberB);
    }

    @Contract(value = "null, null, null -> true; null, !null, _ -> false; !null, null, _ -> false; null, _, !null -> false; !null, _, null -> false; _, null, !null -> false; _, !null, null -> false", pure = true)
    public static boolean equals(@Nullable Double numberA, @Nullable Double numberB, @Nullable Double numberC) {
        return numberA == null ? numberB == null && numberC == null : numberA.equals(numberB) && numberA.equals(numberC);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Contract("null, null, _ -> true; null, !null, _ -> false; !null, null, _ -> false")
    public static boolean nearlyEquals(@Nullable Float numberA, @Nullable Float numberB, float epsilon) {
        if (numberA == null) {
            return numberB == null;
        }

        if (numberB == null) {
            return false;
        }

        if (numberA.equals(numberB)) {
            return true;
        }

        if (Float.isInfinite(numberA) || Float.isNaN(numberA)
                || Float.isInfinite(numberB) || Float.isNaN(numberB)) {
            return false;
        }

        return abs(numberA - numberB) < epsilon;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Contract("null, null, _ -> true; null, !null, _ -> false; !null, null, _ -> false")
    public static boolean nearlyEquals(@Nullable Double numberA, @Nullable Double numberB, double epsilon) {
        if (numberA == null) {
            return numberB == null;
        }

        if (numberB == null) {
            return false;
        }

        if (numberA.equals(numberB)) {
            return true;
        }

        if (Double.isInfinite(numberA) || Double.isNaN(numberA)
                || Double.isInfinite(numberB) || Double.isNaN(numberB)) {
            return false;
        }

        return abs(numberA - numberB) < epsilon;
    }

    @Contract(pure = true)
    public static byte nullToZero(@Nullable Byte value) {
        return value == null ? (byte) 0 : value;
    }

    @Contract(pure = true)
    public static short nullToZero(@Nullable Short value) {
        return value == null ? (short) 0 : value;
    }

    @Contract(pure = true)
    public static int nullToZero(@Nullable Integer value) {
        return value == null ? 0 : value;
    }

    @Contract(pure = true)
    public static long nullToZero(@Nullable Long value) {
        return value == null ? 0L : value;
    }

    @Contract(pure = true)
    public static float nullToZero(@Nullable Float value) {
        return value == null ? 0.0F : value;
    }

    @Contract(pure = true)
    public static double nullToZero(@Nullable Double value) {
        return value == null ? 0.0D : value;
    }
}
