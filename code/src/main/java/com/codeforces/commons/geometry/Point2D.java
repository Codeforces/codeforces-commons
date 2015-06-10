package com.codeforces.commons.geometry;

import com.codeforces.commons.math.NumberUtil;
import com.codeforces.commons.pair.DoublePair;
import com.codeforces.commons.text.StringUtil;
import org.apache.commons.math3.util.FastMath;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 22.07.13
 */
public class Point2D extends DoublePair {
    public static final double DEFAULT_EPSILON = 1.0E-6D;

    public Point2D(double x, double y) {
        super(x, y);
    }

    public Point2D(Point2D point) {
        super(point.getX(), point.getY());
    }

    public double getX() {
        return getFirst();
    }

    public void setX(double x) {
        setFirst(x);
    }

    public double getY() {
        return getSecond();
    }

    public void setY(double y) {
        setSecond(y);
    }

    public Point2D add(Vector2D vector) {
        setX(getX() + vector.getX());
        setY(getY() + vector.getY());
        return this;
    }

    public Point2D subtract(Vector2D vector) {
        setX(getX() - vector.getX());
        setY(getY() - vector.getY());
        return this;
    }

    public double getDistanceTo(Point2D point) {
        return FastMath.hypot(getX() - point.getX(), getY() - point.getY());
    }

    public double getDistanceTo(double x, double y) {
        return FastMath.hypot(getX() - x, getY() - y);
    }

    public double getSquaredDistanceTo(Point2D point) {
        return NumberUtil.sumSqr(getX() - point.getX(), getY() - point.getY());
    }

    public double getSquaredDistanceTo(double x, double y) {
        return NumberUtil.sumSqr(getX() - x, getY() - y);
    }

    public Point2D copy() {
        return new Point2D(this);
    }

    public boolean nearlyEquals(Point2D point, double epsilon) {
        return point != null
                && NumberUtil.nearlyEquals(getX(), point.getX(), epsilon)
                && NumberUtil.nearlyEquals(getY(), point.getY(), epsilon);
    }

    public boolean nearlyEquals(Point2D point) {
        return nearlyEquals(point, DEFAULT_EPSILON);
    }

    public boolean nearlyEquals(double x, double y, double epsilon) {
        return NumberUtil.nearlyEquals(getX(), x, epsilon)
                && NumberUtil.nearlyEquals(getY(), y, epsilon);
    }

    public boolean nearlyEquals(double x, double y) {
        return nearlyEquals(x, y, DEFAULT_EPSILON);
    }

    @Override
    public String toString() {
        return StringUtil.toString(this, false, "x", "y");
    }
}
