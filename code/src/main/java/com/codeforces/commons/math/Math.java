package com.codeforces.commons.math;

import org.apache.commons.math3.util.FastMath;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 19.06.2015
 */
public final class Math {
    public static final double E = StrictMath.E;

    public static final double PI = StrictMath.PI;
    public static final double HALF_PI = 0.5D * PI;
    public static final double DOUBLE_PI = 2.0D * PI;

    private Math() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    @Contract(pure = true)
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

    @Contract(pure = true)
    public static long findLeastCommonMultiple(long numberA, long numberB) {
        return numberA / findGreatestCommonDivisor(numberA, numberB) * numberB;
    }

    @Contract(pure = true)
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

    @Contract(pure = true)
    public static int avg(int numberA, int numberB) {
        return numberA / 2 + numberB / 2 + (numberA % 2 + numberB % 2) / 2;
    }

    @Contract(pure = true)
    public static int avg(int numberA, int numberB, int numberC) {
        return numberA / 3 + numberB / 3 + numberC / 3 + (numberA % 3 + numberB % 3 + numberC % 3) / 3;
    }

    @Contract(pure = true)
    public static long avg(long numberA, long numberB) {
        return numberA / 2L + numberB / 2L + (numberA % 2L + numberB % 2L) / 2L;
    }

    @Contract(pure = true)
    public static long avg(long numberA, long numberB, long numberC) {
        return numberA / 3L + numberB / 3L + numberC / 3L + (numberA % 3L + numberB % 3L + numberC % 3L) / 3L;
    }

    @Contract(pure = true)
    public static float avg(float numberA, float numberB) {
        return numberA * 0.5F + numberB * 0.5F;
    }

    @Contract(pure = true)
    public static float avg(float numberA, float numberB, float numberC) {
        return numberA / 3.0F + numberB / 3.0F + numberC / 3.0F;
    }

    @Contract(pure = true)
    public static double avg(double numberA, double numberB) {
        return numberA * 0.5D + numberB * 0.5D;
    }

    @Contract(pure = true)
    public static double avg(double numberA, double numberB, double numberC) {
        return numberA / 3.0D + numberB / 3.0D + numberC / 3.0D;
    }

    @Contract(pure = true)
    public static double sqr(double value) {
        return value * value;
    }

    @Contract(pure = true)
    public static double sumSqr(double numberA, double numberB) {
        return numberA * numberA + numberB * numberB;
    }

    @Contract(pure = true)
    public static double sumSqr(double numberA, double numberB, double numberC) {
        return numberA * numberA + numberB * numberB + numberC * numberC;
    }

    @Contract(pure = true)
    public static double pow(double base, double exponent) {
        return StrictMath.pow(base, exponent);
    }

    @Contract(pure = true)
    public static double pow(double base, int exponent) {
        return FastMath.pow(base, exponent);
    }

    @Contract(pure = true)
    public static int min(int numberA, int numberB) {
        return numberA <= numberB ? numberA : numberB;
    }

    @Contract(pure = true)
    public static long min(long numberA, long numberB) {
        return numberA <= numberB ? numberA : numberB;
    }

    @Contract(pure = true)
    public static float min(float numberA, float numberB) {
        return java.lang.Math.min(numberA, numberB);
    }

    @Contract(pure = true)
    public static double min(double numberA, double numberB) {
        return java.lang.Math.min(numberA, numberB);
    }

    @Contract(pure = true)
    public static int max(int numberA, int numberB) {
        return numberA >= numberB ? numberA : numberB;
    }

    @Contract(pure = true)
    public static long max(long numberA, long numberB) {
        return numberA >= numberB ? numberA : numberB;
    }

    @Contract(pure = true)
    public static float max(float numberA, float numberB) {
        return java.lang.Math.max(numberA, numberB);
    }

    @Contract(pure = true)
    public static double max(double numberA, double numberB) {
        return java.lang.Math.max(numberA, numberB);
    }

    @Contract(pure = true)
    public static int abs(int value) {
        return value < 0 ? -value : value;
    }

    @Contract(pure = true)
    public static long abs(long value) {
        return value < 0 ? -value : value;
    }

    @Contract(pure = true)
    public static float abs(float value) {
        return java.lang.Math.abs(value);
    }

    @Contract(pure = true)
    public static double abs(double value) {
        return java.lang.Math.abs(value);
    }

    @Contract(pure = true)
    public static double sqrt(double value) {
        return StrictMath.sqrt(value);
    }

    @Contract(pure = true)
    public static float round(float value) {
        return java.lang.Math.round(value);
    }

    @Contract(pure = true)
    public static double round(double value) {
        return java.lang.Math.round(value);
    }

    @Contract(pure = true)
    public static double floor(double value) {
        return StrictMath.floor(value);
    }

    @Contract(pure = true)
    public static double ceil(double value) {
        return StrictMath.ceil(value);
    }

    @Contract(pure = true)
    public static double hypot(double cathetusA, double cathetusB) {
        return FastMath.hypot(cathetusA, cathetusB);
    }

    @Contract(pure = true)
    public static double sin(double value) {
        return FastMath.sin(value);
    }

    @Contract(pure = true)
    public static double cos(double value) {
        return FastMath.cos(value);
    }

    @Contract(pure = true)
    public static double tan(double value) {
        return FastMath.tan(value);
    }

    @Contract(pure = true)
    public static double asin(double value) {
        return StrictMath.asin(value);
    }

    @Contract(pure = true)
    public static double acos(double value) {
        return StrictMath.acos(value);
    }

    @Contract(pure = true)
    public static double atan(double value) {
        return StrictMath.atan(value);
    }

    @Contract(pure = true)
    public static double atan2(double y, double x) {
        return StrictMath.atan2(y, x);
    }
}
