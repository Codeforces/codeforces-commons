package com.codeforces.commons.geometry;

import com.codeforces.commons.text.StringUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static com.codeforces.commons.math.Math.abs;
import static com.codeforces.commons.math.Math.sqrt;

/**
 * x^2 + y^2 + ax + by + c = 0
 *
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 30.06.2015
 */
@SuppressWarnings("StandardVariableNames")
public class Circle2D {
    public static final double DEFAULT_EPSILON = Line2D.DEFAULT_EPSILON;
    public static final Circle2D[] EMPTY_CIRCLE_ARRAY = {};

    private final double a;
    private final double b;
    private final double c;

    private final double squaredRadius;
    private final double radius;

    public Circle2D(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;

        this.squaredRadius = (a * a + b * b) / 4.0D - c;

        if (this.squaredRadius < 0.0D) {
            throw new IllegalArgumentException(String.format(
                    "Squared radius of circle is negative: a=%s, b=%s, c=%s.", a, b, c
            ));
        }

        this.radius = sqrt(squaredRadius);
    }

    public Circle2D(@Nonnull Point2D center, double radius) {
        if (radius < 0.0D) {
            throw new IllegalArgumentException("Argument 'radius' is negative.");
        }

        this.squaredRadius = radius * radius;
        this.radius = radius;

        this.a = -2.0D * center.getX();
        this.b = -2.0D * center.getY();
        this.c = (a * a + b * b) / 4.0D - squaredRadius;
    }

    public Circle2D(@Nonnull Circle2D circle) {
        this.a = circle.a;
        this.b = circle.b;
        this.c = circle.c;

        this.squaredRadius = circle.squaredRadius;
        this.radius = circle.radius;
    }

    @Contract(pure = true)
    public double getA() {
        return a;
    }

    @Contract(pure = true)
    public Circle2D setA(double a) {
        return new Circle2D(a, b, c);
    }

    @Contract(pure = true)
    public double getB() {
        return b;
    }

    @Contract(pure = true)
    public Circle2D setB(double b) {
        return new Circle2D(a, b, c);
    }

    @Contract(pure = true)
    public double getC() {
        return c;
    }

    @Contract(pure = true)
    public Circle2D setC(double c) {
        return new Circle2D(a, b, c);
    }

    @Nonnull
    public double[] getXs(double y, @Nonnegative double epsilon) {
        double d = a * a - 4.0D * (y * y + b * y + c);

        if (d < -epsilon) {
            return ArrayUtils.EMPTY_DOUBLE_ARRAY;
        }

        if (abs(d) <= epsilon) {
            return new double[]{-a / 2.0D};
        }

        double sqrtD = sqrt(d);

        return new double[]{(-sqrtD - a) / 2.0D, (sqrtD - a) / 2.0D};
    }

    @Nonnull
    public double[] getXs(double y) {
        return getXs(y, DEFAULT_EPSILON);
    }

    @Nonnull
    public double[] getYs(double x, @Nonnegative double epsilon) {
        double d = b * b - 4.0D * (x * x + a * x + c);

        if (d < -epsilon) {
            return ArrayUtils.EMPTY_DOUBLE_ARRAY;
        }

        if (abs(d) <= epsilon) {
            return new double[]{-b / 2.0D};
        }

        double sqrtD = sqrt(d);

        return new double[]{(-sqrtD - b) / 2.0D, (sqrtD - b) / 2.0D};
    }

    @Nonnull
    public double[] getYs(double x) {
        return getYs(x, DEFAULT_EPSILON);
    }

    @Contract(pure = true)
    public double getSquaredRadius() {
        return squaredRadius;
    }

    @Contract(pure = true)
    public double getRadius() {
        return radius;
    }

    @Contract(pure = true)
    public double getCenterX() {
        return -a / 2.0D;
    }

    @Contract(pure = true)
    public double getCenterY() {
        return -b / 2.0D;
    }

    @Nonnull
    public Point2D[] getIntersectionPoints(@Nonnull Line2D line, @Nonnegative double epsilon) {
        return line.getIntersectionPoints(this, epsilon);
    }

    @Nonnull
    public Point2D[] getIntersectionPoints(@Nonnull Line2D line) {
        return line.getIntersectionPoints(this, DEFAULT_EPSILON);
    }

    @Contract(value = "-> !null", pure = true)
    public Circle2D copy() {
        return new Circle2D(this);
    }

    @Override
    public String toString() {
        return StringUtil.toString(this, false, "a", "b", "c");
    }
}
