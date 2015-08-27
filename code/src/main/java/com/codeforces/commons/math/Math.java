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
    public static final double DOUBLE_PI = 2.0D * PI;
    public static final double HALF_PI = PI / 2.0D;
    public static final double THIRD_PI = PI / 3.0D;
    public static final double QUARTER_PI = PI / 4.0D;
    public static final double SIXTH_PI = PI / 6.0D;

    public static final double RADIANS_PER_DEGREE = PI / 180.0D;
    public static final double DEGREES_PER_RADIAN = 180.0D / PI;

    public static final double SQRT_2 = sqrt(2.0D);
    public static final double SQRT_3 = sqrt(3.0D);
    public static final double SQRT_5 = sqrt(5.0D);
    public static final double SQRT_6 = sqrt(6.0D);
    public static final double SQRT_7 = sqrt(7.0D);
    public static final double SQRT_8 = sqrt(8.0D);

    public static final double CBRT_2 = cbrt(2.0D);
    public static final double CBRT_3 = cbrt(3.0D);
    public static final double CBRT_4 = cbrt(4.0D);
    public static final double CBRT_5 = cbrt(5.0D);
    public static final double CBRT_6 = cbrt(6.0D);
    public static final double CBRT_7 = cbrt(7.0D);
    public static final double CBRT_9 = cbrt(9.0D);

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
    public static int avg(int numberA, int numberB, int numberC, int numberD) {
        return numberA / 4 + numberB / 4 + numberC / 4 + numberD / 4
                + (numberA % 4 + numberB % 4 + numberC % 4 + numberD % 4) / 4;
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
    public static long avg(long numberA, long numberB, long numberC, long numberD) {
        return numberA / 4L + numberB / 4L + numberC / 4L + numberD / 4L
                + (numberA % 4L + numberB % 4L + numberC % 4L + numberD % 4L) / 4L;
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
    public static float avg(float numberA, float numberB, float numberC, float numberD) {
        return numberA * 0.25F + numberB * 0.25F + numberC * 0.25F + numberD * 0.25F;
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
    public static double avg(double numberA, double numberB, double numberC, double numberD) {
        return numberA * 0.25D + numberB * 0.25D + numberC * 0.25D + numberD * 0.25D;
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
    public static double sumSqr(double numberA, double numberB, double numberC, double numberD) {
        return numberA * numberA + numberB * numberB + numberC * numberC + numberD * numberD;
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
    public static int min(int numberA, int numberB, int numberC) {
        return min(numberA <= numberB ? numberA : numberB, numberC);
    }

    @Contract(pure = true)
    public static int min(int numberA, int numberB, int numberC, int numberD) {
        return min(numberA <= numberB ? numberA : numberB, numberC <= numberD ? numberC : numberD);
    }

    @Contract(pure = true)
    public static long min(long numberA, long numberB) {
        return numberA <= numberB ? numberA : numberB;
    }

    @Contract(pure = true)
    public static long min(long numberA, long numberB, long numberC) {
        return min(numberA <= numberB ? numberA : numberB, numberC);
    }

    @Contract(pure = true)
    public static long min(long numberA, long numberB, long numberC, long numberD) {
        return min(numberA <= numberB ? numberA : numberB, numberC <= numberD ? numberC : numberD);
    }

    @Contract(pure = true)
    public static float min(float numberA, float numberB) {
        return java.lang.Math.min(numberA, numberB);
    }

    @Contract(pure = true)
    public static float min(float numberA, float numberB, float numberC) {
        return min(min(numberA, numberB), numberC);
    }

    @Contract(pure = true)
    public static float min(float numberA, float numberB, float numberC, float numberD) {
        return min(min(numberA, numberB), min(numberC, numberD));
    }

    @Contract(pure = true)
    public static double min(double numberA, double numberB) {
        return java.lang.Math.min(numberA, numberB);
    }

    @Contract(pure = true)
    public static double min(double numberA, double numberB, double numberC) {
        return min(min(numberA, numberB), numberC);
    }

    @Contract(pure = true)
    public static double min(double numberA, double numberB, double numberC, double numberD) {
        return min(min(numberA, numberB), min(numberC, numberD));
    }

    @Contract(pure = true)
    public static int max(int numberA, int numberB) {
        return numberA >= numberB ? numberA : numberB;
    }

    @Contract(pure = true)
    public static int max(int numberA, int numberB, int numberC) {
        return max(numberA >= numberB ? numberA : numberB, numberC);
    }

    @Contract(pure = true)
    public static int max(int numberA, int numberB, int numberC, int numberD) {
        return max(numberA >= numberB ? numberA : numberB, numberC >= numberD ? numberC : numberD);
    }

    @Contract(pure = true)
    public static long max(long numberA, long numberB) {
        return numberA >= numberB ? numberA : numberB;
    }

    @Contract(pure = true)
    public static long max(long numberA, long numberB, long numberC) {
        return max(numberA >= numberB ? numberA : numberB, numberC);
    }

    @Contract(pure = true)
    public static long max(long numberA, long numberB, long numberC, long numberD) {
        return max(numberA >= numberB ? numberA : numberB, numberC >= numberD ? numberC : numberD);
    }

    @Contract(pure = true)
    public static float max(float numberA, float numberB) {
        return java.lang.Math.max(numberA, numberB);
    }

    @Contract(pure = true)
    public static float max(float numberA, float numberB, float numberC) {
        return max(max(numberA, numberB), numberC);
    }

    @Contract(pure = true)
    public static float max(float numberA, float numberB, float numberC, float numberD) {
        return max(max(numberA, numberB), max(numberC, numberD));
    }

    @Contract(pure = true)
    public static double max(double numberA, double numberB) {
        return java.lang.Math.max(numberA, numberB);
    }

    @Contract(pure = true)
    public static double max(double numberA, double numberB, double numberC) {
        return max(max(numberA, numberB), numberC);
    }

    @Contract(pure = true)
    public static double max(double numberA, double numberB, double numberC, double numberD) {
        return max(max(numberA, numberB), max(numberC, numberD));
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
    public static double cbrt(double value) {
        return StrictMath.cbrt(value);
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
