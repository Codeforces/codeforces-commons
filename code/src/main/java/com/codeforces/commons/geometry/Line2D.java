package com.codeforces.commons.geometry;

import com.codeforces.commons.text.StringUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.codeforces.commons.math.Math.*;

/**
 * ax + by + c = 0
 *
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 22.07.2013
 */
@SuppressWarnings("StandardVariableNames")
public class Line2D {
    public static final double DEFAULT_EPSILON = 1.0E-6D;
    public static final Line2D[] EMPTY_LINE_ARRAY = {};

    private final double a;
    private final double b;
    private final double c;

    private final double pseudoLength;

    public Line2D(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;

        this.pseudoLength = hypot(this.a, this.b);
    }

    public Line2D(@Nonnull Line2D line) {
        this.a = line.a;
        this.b = line.b;
        this.c = line.c;

        this.pseudoLength = line.pseudoLength;
    }

    @Contract(pure = true)
    public double getA() {
        return a;
    }

    @Contract(pure = true)
    public Line2D setA(double a) {
        return new Line2D(a, b, c);
    }

    @Contract(pure = true)
    public double getB() {
        return b;
    }

    @Contract(pure = true)
    public Line2D setB(double b) {
        return new Line2D(a, b, c);
    }

    @Contract(pure = true)
    public double getC() {
        return c;
    }

    @Contract(pure = true)
    public Line2D setC(double c) {
        return new Line2D(a, b, c);
    }

    public double getX(double y) {
        return a == 0.0D ? Double.NaN : -(b * y + c) / a;
    }

    public double getY(double x) {
        return b == 0.0D ? Double.NaN : -(a * x + c) / b;
    }

    public Line2D[] getParallelLines(double distance) {
        double shift = distance * pseudoLength;
        return new Line2D[]{new Line2D(a, b, c + shift), new Line2D(a, b, c - shift)};
    }

    public Line2D getParallelLine(double x, double y) {
        double shift = a * x + b * y + c;
        return new Line2D(a, b, c - shift);
    }

    public Line2D getParallelLine(@Nonnull Point2D point) {
        return getParallelLine(point.getX(), point.getY());
    }

    public double getDistanceFrom(double x, double y) {
        return abs((a * x + b * y + c) / pseudoLength);
    }

    public double getDistanceFrom(@Nonnull Point2D point) {
        return getDistanceFrom(point.getX(), point.getY());
    }

    public double getDistanceFrom(@Nonnull Line2D line, @Nonnegative double epsilon) {
        if (getIntersectionPoint(line, epsilon) != null) {
            return Double.NaN;
        }

        return abs(c - line.c) / pseudoLength;
    }

    public double getDistanceFrom(@Nonnull Line2D line) {
        return getDistanceFrom(line, DEFAULT_EPSILON);
    }

    public double getSignedDistanceFrom(double x, double y) {
        return (a * x + b * y + c) / pseudoLength;
    }

    public double getSignedDistanceFrom(@Nonnull Point2D point) {
        return getSignedDistanceFrom(point.getX(), point.getY());
    }

    public double getSignedDistanceFrom(@Nonnull Line2D line, @Nonnegative double epsilon) {
        if (getIntersectionPoint(line, epsilon) != null) {
            return Double.NaN;
        }

        return (c - line.c) / pseudoLength;
    }

    public double getSignedDistanceFrom(@Nonnull Line2D line) {
        return getSignedDistanceFrom(line, DEFAULT_EPSILON);
    }

    public Vector2D getUnitNormal() {
        return new Vector2D(a / pseudoLength, b / pseudoLength);
    }

    public Vector2D getUnitNormalFrom(double x, double y, @Nonnegative double epsilon) {
        double signedDistance = getSignedDistanceFrom(x, y);

        if (signedDistance < -epsilon) {
            return new Vector2D(a / pseudoLength, b / pseudoLength);
        } else if (signedDistance > epsilon) {
            return new Vector2D(-a / pseudoLength, -b / pseudoLength);
        } else {
            throw new IllegalArgumentException(String.format("Point {x=%s, y=%s} is on the %s.", x, y, this));
        }
    }

    public Vector2D getUnitNormalFrom(double x, double y) {
        return getUnitNormalFrom(x, y, DEFAULT_EPSILON);
    }

    public Vector2D getUnitNormalFrom(@Nonnull Point2D point, @Nonnegative double epsilon) {
        return getUnitNormalFrom(point.getX(), point.getY(), epsilon);
    }

    public Vector2D getUnitNormalFrom(@Nonnull Point2D point) {
        return getUnitNormalFrom(point.getX(), point.getY(), DEFAULT_EPSILON);
    }

    public Point2D getProjectionOf(double x, double y, @Nonnegative double epsilon) {
        double distance = getDistanceFrom(x, y);
        if (distance <= epsilon) {
            return new Point2D(x, y);
        }

        Vector2D unitNormal = getUnitNormalFrom(x, y, epsilon);
        return new Point2D(x + unitNormal.getX() * distance, y + unitNormal.getY() * distance);
    }

    public Point2D getProjectionOf(double x, double y) {
        return getProjectionOf(x, y, DEFAULT_EPSILON);
    }

    public Point2D getProjectionOf(@Nonnull Point2D point, @Nonnegative double epsilon) {
        return getProjectionOf(point.getX(), point.getY(), epsilon);
    }

    public Point2D getProjectionOf(@Nonnull Point2D point) {
        return getProjectionOf(point.getX(), point.getY(), DEFAULT_EPSILON);
    }

    /**
     * Get intersection point or {@code null} if lines are parallel.
     *
     * @param line    to intersect with
     * @param epsilon to check if lines are parallel
     * @return intersection point or {@code null} if lines are parallel
     */
    @Nullable
    public Point2D getIntersectionPoint(@Nonnull Line2D line, @Nonnegative double epsilon) {
        double d = a * line.b - line.a * b;
        return abs(d) <= epsilon ? null : new Point2D((b * line.c - line.b * c) / d, (line.a * c - a * line.c) / d);
    }

    /**
     * Get intersection point or {@code null} if lines are parallel.
     * Uses {@link #DEFAULT_EPSILON {@code DEFAULT_EPSILON}} as epsilon when checking if lines are parallel.
     *
     * @param line to intersect with
     * @return intersection point or {@code null} if lines are parallel
     */
    @Nullable
    public Point2D getIntersectionPoint(@Nonnull Line2D line) {
        return getIntersectionPoint(line, DEFAULT_EPSILON);
    }

    @Nonnull
    public Point2D[] getIntersectionPoints(@Nonnull Circle2D circle, @Nonnegative double epsilon) {
        double sqrA = a * a;
        double sqrB = b * b;
        double sqrC = c * c;

        double ab = a * b;
        double ac = a * c;
        double bc = b * c;

        double circleA = circle.getA();
        double circleB = circle.getB();
        double circleC = circle.getC();

        double axy = sqrA + sqrB; // a-factor for both x and y quadratic equations

        double bx = 2.0D * ac + sqrB * circleA - ab * circleB; // b-factor for x quadratic equation
        double cx = sqrB * circleC - bc * circleB + sqrC; // c-factor for x quadratic equation

        double dx = bx * bx - 4 * axy * cx;

        if (dx < -epsilon) {
            return Point2D.EMPTY_POINT_ARRAY;
        }

        double by = 2.0D * bc + sqrA * circleB - ab * circleA; // b-factor for y quadratic equation
        double cy = sqrA * circleC - ac * circleA + sqrC; // c-factor for y quadratic equation

        double dy = by * by - 4 * axy * cy;

        if (dy < -epsilon) {
            return Point2D.EMPTY_POINT_ARRAY;
        }

        if (abs(dx) <= epsilon && abs(dy) <= epsilon) {
            return new Point2D[]{new Point2D(-bx / axy / 2.0D, -by / axy / 2.0D)};
        }

        double sqrtDx = sqrt(dx);
        double sqrtDy = sqrt(dy);

        return ab <= 0.0D ? new Point2D[]{
                new Point2D((-bx - sqrtDx) / axy / 2.0D, (-by - sqrtDy) / axy / 2.0D),
                new Point2D((-bx + sqrtDx) / axy / 2.0D, (-by + sqrtDy) / axy / 2.0D)
        } : new Point2D[]{
                new Point2D((-bx - sqrtDx) / axy / 2.0D, (-by + sqrtDy) / axy / 2.0D),
                new Point2D((-bx + sqrtDx) / axy / 2.0D, (-by - sqrtDy) / axy / 2.0D)
        };
    }

    @Nonnull
    public Point2D[] getIntersectionPoints(@Nonnull Circle2D circle) {
        return getIntersectionPoints(circle, DEFAULT_EPSILON);
    }

    @Contract(value = "-> !null", pure = true)
    public Line2D copy() {
        return new Line2D(this);
    }

    @Override
    public String toString() {
        return StringUtil.toString(this, false, "a", "b", "c");
    }

    @Nonnull
    public static Line2D getLineByTwoPoints(double x1, double y1, double x2, double y2) {
        return new Line2D(y2 - y1, x1 - x2, (y1 - y2) * x1 + (x2 - x1) * y1);
    }

    @Nonnull
    public static Line2D getLineByTwoPoints(@Nonnull Point2D point1, @Nonnull Point2D point2) {
        return getLineByTwoPoints(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }
}
