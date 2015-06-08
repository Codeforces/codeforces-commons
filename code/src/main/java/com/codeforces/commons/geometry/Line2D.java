package com.codeforces.commons.geometry;

import com.codeforces.commons.text.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.sqrt;

/**
 * ax + by + c = 0
 *
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 22.07.13
 */
@SuppressWarnings("StandardVariableNames")
public class Line2D {
    public static final double DEFAULT_EPSILON = 1.0E-6D;

    private double a;
    private double b;
    private double c;

    public Line2D(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getC() {
        return c;
    }

    public void setC(double c) {
        this.c = c;
    }

    public double getX(double y) {
        return a == 0.0D ? Double.NaN : -(b * y + c) / a;
    }

    public double getY(double x) {
        return b == 0.0D ? Double.NaN : -(a * x + c) / b;
    }

    public Line2D[] getParallelLines(double range) {
        double shift = range * sqrt(a * a + b * b);
        return new Line2D[]{new Line2D(a, b, c + shift), new Line2D(a, b, c - shift)};
    }

    public double getDistanceFrom(double x, double y) {
        return abs((a * x + b * y + c) / sqrt(a * a + b * b));
    }

    public Point2D getUnitNormalFrom(double x, double y) {
        double length = sqrt(a * a + b * b);
        return getDistanceFrom(x + a, y + b) < getDistanceFrom(x - a, y - b)
                ? new Point2D(a / length, b / length)
                : new Point2D(-a / length, -b / length);
    }

    public Point2D getProjectionOf(double x, double y) {
        Point2D unitNormal = getUnitNormalFrom(x, y);
        double distance = getDistanceFrom(x, y);
        return new Point2D(x + unitNormal.getX() * distance, y + unitNormal.getY() * distance);
    }

    /**
     * Get intersection point or {@code null} if lines are parallel.
     *
     * @param line    to intersect with
     * @param epsilon to check if lines are parallel
     * @return intersection point or {@code null} if lines are parallel
     */
    @Nullable
    public Point2D getIntersectionPoint(@Nonnull Line2D line, double epsilon) {
        double d = a * line.b - line.a * b;
        return abs(d) <= abs(epsilon)
                ? null
                : new Point2D((b * line.c - line.b * c) / d, (line.a * c - a * line.c) / d);
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

    @Override
    public String toString() {
        return StringUtil.toString(this, false, "a", "b", "c");
    }

    @Nonnull
    public static Line2D getLineByTwoPoints(double x1, double y1, double x2, double y2) {
        return new Line2D(y2 - y1, x1 - x2, (y1 - y2) * x1 + (x2 - x1) * y1);
    }

    @Nonnull
    public static Line2D getLineByTwoPoints(Point2D point1, Point2D point2) {
        return getLineByTwoPoints(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }
}
