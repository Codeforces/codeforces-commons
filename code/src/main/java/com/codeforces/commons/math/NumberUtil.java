package com.codeforces.commons.math;

import com.codeforces.commons.text.StringUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;

import static com.codeforces.commons.math.Math.abs;
import static com.codeforces.commons.math.Math.round;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 24.07.2013
 */
@SuppressWarnings("WeakerAccess")
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
            return toByte(((Number) value).longValue());
        }

        return toByte(value.toString());
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Byte toByte(@Nullable String value) {
        return toByte(toDouble(value));
    }

    public static byte toByte(short value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") byte byteValue = (byte) value;
        if ((short) byteValue == value) {
            return byteValue;
        }
        throw new IllegalArgumentException("Can't convert short " + value + " to byte.");
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Byte toByte(@Nullable Short value) {
        return value == null ? null : toByte((short) value);
    }

    public static byte toByte(int value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") byte byteValue = (byte) value;
        if ((int) byteValue == value) {
            return byteValue;
        }
        throw new IllegalArgumentException("Can't convert int " + value + " to byte.");
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Byte toByte(@Nullable Integer value) {
        return value == null ? null : toByte((int) value);
    }

    public static byte toByte(long value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") byte byteValue = (byte) value;
        if ((long) byteValue == value) {
            return byteValue;
        }
        throw new IllegalArgumentException("Can't convert long " + value + " to byte.");
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Byte toByte(@Nullable Long value) {
        return value == null ? null : toByte((long) value);
    }

    public static byte toByte(float value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") byte byteValue = (byte) value;
        if (abs((float) byteValue - value) < 1.0f) {
            return byteValue;
        }
        throw new IllegalArgumentException("Can't convert float " + value + " to byte.");
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Byte toByte(@Nullable Float value) {
        return value == null ? null : toByte((float) value);
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
    public static Byte toByte(@Nullable Double value) {
        return value == null ? null : toByte((double) value);
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Short toShort(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Short) {
            return (Short) value;
        }

        if (value instanceof Byte) {
            return (short) (byte) value;
        }

        if (value instanceof Integer) {
            return toShort((int) value);
        }

        if (value instanceof Long) {
            return toShort((long) value);
        }

        if (value instanceof Float) {
            return toShort((float) value);
        }

        if (value instanceof Double) {
            return toShort((double) value);
        }

        if (value instanceof Number) {
            return toShort(((Number) value).longValue());
        }

        return toShort(value.toString());
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Short toShort(@Nullable String value) {
        return toShort(toDouble(value));
    }

    public static short toShort(int value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") short shortValue = (short) value;
        if ((int) shortValue == value) {
            return shortValue;
        }
        throw new IllegalArgumentException("Can't convert int " + value + " to short.");
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Short toShort(@Nullable Integer value) {
        return value == null ? null : toShort((int) value);
    }

    public static short toShort(long value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") short shortValue = (short) value;
        if ((long) shortValue == value) {
            return shortValue;
        }
        throw new IllegalArgumentException("Can't convert long " + value + " to short.");
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Short toShort(@Nullable Long value) {
        return value == null ? null : toShort((long) value);
    }

    public static short toShort(float value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") short shortValue = (short) value;
        if (abs((float) shortValue - value) < 1.0f) {
            return shortValue;
        }
        throw new IllegalArgumentException("Can't convert float " + value + " to short.");
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Short toShort(@Nullable Float value) {
        return value == null ? null : toShort((float) value);
    }

    public static short toShort(double value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") short shortValue = (short) value;
        if (abs((double) shortValue - value) < 1.0D) {
            return shortValue;
        }
        throw new IllegalArgumentException("Can't convert double " + value + " to short.");
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Short toShort(@Nullable Double value) {
        return value == null ? null : toShort((double) value);
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Integer toInt(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Byte) {
            return (int) (byte) value;
        }

        if (value instanceof Short) {
            return (int) (short) value;
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
            return toInt(((Number) value).longValue());
        }

        return toInt(value.toString());
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Integer toInt(@Nullable String value) {
        return toInt(toDouble(value));
    }

    public static int toInt(long value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") int intValue = (int) value;
        if ((long) intValue == value) {
            return intValue;
        }
        throw new IllegalArgumentException("Can't convert long " + value + " to int.");
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Integer toInt(@Nullable Long value) {
        return value == null ? null : toInt((long) value);
    }

    public static int toInt(float value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") int intValue = (int) value;
        if (abs((float) intValue - value) < 1.0f) {
            return intValue;
        }
        throw new IllegalArgumentException("Can't convert float " + value + " to int.");
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Integer toInt(@Nullable Float value) {
        return value == null ? null : toInt((float) value);
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
    public static Integer toInt(@Nullable Double value) {
        return value == null ? null : toInt((double) value);
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Long toLong(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
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

        if (value instanceof Float) {
            return toLong((float) value);
        }

        if (value instanceof Double) {
            return toLong((double) value);
        }

        if (value instanceof Number) {
            return toLong(((Number) value).longValue());
        }

        return toLong(value.toString());
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Long toLong(@Nullable String value) {
        return toLong(toDouble(value));
    }

    public static long toLong(float value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") long longValue = (long) value;
        if (abs((float) longValue - value) < 1.0f) {
            return longValue;
        }
        throw new IllegalArgumentException("Can't convert float " + value + " to long.");
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Long toLong(@Nullable Float value) {
        return value == null ? null : toLong((float) value);
    }

    public static long toLong(double value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") long longValue = (long) value;
        if (abs((double) longValue - value) < 1.0D) {
            return longValue;
        }
        throw new IllegalArgumentException("Can't convert double " + value + " to long.");
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Long toLong(@Nullable Double value) {
        return value == null ? null : toLong((double) value);
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Float toFloat(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Float) {
            return (Float) value;
        }

        if (value instanceof Byte) {
            return (float) (byte) value;
        }

        if (value instanceof Short) {
            return (float) (short) value;
        }

        if (value instanceof Integer) {
            return (float) (int) value;
        }

        if (value instanceof Long) {
            return (float) (long) value;
        }

        if (value instanceof Double) {
            return toFloat((double) value);
        }

        if (value instanceof Number) {
            return toFloat(((Number) value).doubleValue());
        }

        return toFloat(value.toString());
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Float toFloat(@Nullable String value) {
        return toFloat(toDouble(value));
    }

    public static float toFloat(double value) {
        @SuppressWarnings("NumericCastThatLosesPrecision") float floatValue = (float) value;
        if (abs((double) floatValue - value) < 1.0D) {
            return floatValue;
        }
        throw new IllegalArgumentException("Can't convert double " + value + " to float.");
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Float toFloat(@Nullable Double value) {
        return value == null ? null : toFloat((double) value);
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Double toDouble(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Double) {
            return (Double) value;
        }

        if (value instanceof Byte) {
            return (double) (byte) value;
        }

        if (value instanceof Short) {
            return (double) (short) value;
        }

        if (value instanceof Integer) {
            return (double) (int) value;
        }

        if (value instanceof Long) {
            return (double) (long) value;
        }

        if (value instanceof Float) {
            return (double) (float) value;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        return toDouble(value.toString());
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Double toDouble(@Nullable String value) {
        return value == null ? null : Double.parseDouble(StringUtil.trim(value));
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
    @Contract(value = "null, null, _ -> true; null, !null, _ -> false; !null, null, _ -> false", pure = true)
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

        return abs(numberA - numberB) <= epsilon;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Contract(value = "null, null, _ -> true; null, !null, _ -> false; !null, null, _ -> false", pure = true)
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

        return abs(numberA - numberB) <= epsilon;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Contract(pure = true)
    public static boolean nearlyEquals(float numberA, float numberB, float epsilon) {
        if (Float.compare(numberA, numberB) == 0) {
            return true;
        }

        if (Float.isInfinite(numberA) || Float.isNaN(numberA)
                || Float.isInfinite(numberB) || Float.isNaN(numberB)) {
            return false;
        }

        return abs(numberA - numberB) <= epsilon;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Contract(pure = true)
    public static boolean nearlyEquals(double numberA, double numberB, double epsilon) {
        if (Double.compare(numberA, numberB) == 0) {
            return true;
        }

        if (Double.isInfinite(numberA) || Double.isNaN(numberA)
                || Double.isInfinite(numberB) || Double.isNaN(numberB)) {
            return false;
        }

        return abs(numberA - numberB) <= epsilon;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Contract(value = "null, null, _ -> true; null, !null, _ -> false; !null, null, _ -> false", pure = true)
    public static boolean roundEquals(@Nullable Float numberA, @Nullable Float numberB, float factor) {
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

        return Float.compare(round(numberA * factor) / factor, round(numberB * factor) / factor) == 0;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Contract(value = "null, null, _ -> true; null, !null, _ -> false; !null, null, _ -> false", pure = true)
    public static boolean roundEquals(@Nullable Double numberA, @Nullable Double numberB, double factor) {
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

        return Double.compare(round(numberA * factor) / factor, round(numberB * factor) / factor) == 0;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Contract(pure = true)
    public static boolean roundEquals(float numberA, float numberB, float factor) {
        if (Float.compare(numberA, numberB) == 0) {
            return true;
        }

        if (Float.isInfinite(numberA) || Float.isNaN(numberA)
                || Float.isInfinite(numberB) || Float.isNaN(numberB)) {
            return false;
        }

        return Float.compare(round(numberA * factor) / factor, round(numberB * factor) / factor) == 0;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Contract(pure = true)
    public static boolean roundEquals(double numberA, double numberB, double factor) {
        if (Double.compare(numberA, numberB) == 0) {
            return true;
        }

        if (Double.isInfinite(numberA) || Double.isNaN(numberA)
                || Double.isInfinite(numberB) || Double.isNaN(numberB)) {
            return false;
        }

        return Double.compare(round(numberA * factor) / factor, round(numberB * factor) / factor) == 0;
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
        return value == null ? 0.0f : value;
    }

    @Contract(pure = true)
    public static double nullToZero(@Nullable Double value) {
        return value == null ? 0.0D : value;
    }

    @Contract(value = "null -> null", pure = true)
    @Nullable
    public static Byte zeroToNull(@Nullable Byte value) {
        return value == null || value == (byte) 0 ? null : value;
    }

    @Contract(value = "null -> null", pure = true)
    @Nullable
    public static Short zeroToNull(@Nullable Short value) {
        return value == null || value == (short) 0 ? null : value;
    }

    @Contract(value = "null -> null", pure = true)
    @Nullable
    public static Integer zeroToNull(@Nullable Integer value) {
        return value == null || value == 0 ? null : value;
    }

    @Contract(value = "null -> null", pure = true)
    @Nullable
    public static Long zeroToNull(@Nullable Long value) {
        return value == null || value == 0L ? null : value;
    }

    @Contract(value = "null -> null", pure = true)
    @Nullable
    public static Float zeroToNull(@Nullable Float value) {
        return value == null || value == 0.0f ? null : value;
    }

    @Contract(value = "null -> null", pure = true)
    @Nullable
    public static Double zeroToNull(@Nullable Double value) {
        return value == null || value == 0.0D ? null : value;
    }
}
