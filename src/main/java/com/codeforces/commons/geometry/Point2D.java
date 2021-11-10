package com.codeforces.commons.geometry;

import com.codeforces.commons.math.NumberUtil;
import com.codeforces.commons.pair.DoublePair;
import com.codeforces.commons.text.StringUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.*;

import static com.codeforces.commons.math.Math.*;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 22.07.2013
 */
@SuppressWarnings("WeakerAccess")
public class Point2D extends DoublePair {
    public static final double DEFAULT_EPSILON = Line2D.DEFAULT_EPSILON;
    public static final Point2D[] EMPTY_POINT_ARRAY = {};

    public Point2D(double x, double y) {
        super(x, y);
    }

    public Point2D(@Nonnull Point2D point) {
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

    @Nonnull
    public Point2D add(@Nonnull Vector2D vector) {
        setX(getX() + vector.getX());
        setY(getY() + vector.getY());
        return this;
    }

    @Nonnull
    public Point2D add(double x, double y) {
        setX(getX() + x);
        setY(getY() + y);
        return this;
    }

    @Nonnull
    public Point2D subtract(@Nonnull Vector2D vector) {
        setX(getX() - vector.getX());
        setY(getY() - vector.getY());
        return this;
    }

    @Nonnull
    public Point2D subtract(double x, double y) {
        setX(getX() - x);
        setY(getY() - y);
        return this;
    }

    @Nonnull
    public Vector2D subtract(@Nonnull Point2D point) {
        return new Vector2D(getX() - point.getX(), getY() - point.getY());
    }

    @Nonnegative
    public double getDistanceTo(@Nonnull Point2D point) {
        return hypot(getX() - point.getX(), getY() - point.getY());
    }

    @Nonnegative
    public double getDistanceTo(double x, double y) {
        return hypot(getX() - x, getY() - y);
    }

    @Nonnegative
    public double getSquaredDistanceTo(@Nonnull Point2D point) {
        return sumSqr(getX() - point.getX(), getY() - point.getY());
    }

    @Nonnegative
    public double getSquaredDistanceTo(double x, double y) {
        return sumSqr(getX() - x, getY() - y);
    }

    @Contract(value = "-> !null", pure = true)
    @Nonnull
    public Point2D copy() {
        return new Point2D(this);
    }

    @Contract("null, _ -> false")
    public boolean nearlyEquals(@Nullable Point2D point, @Nonnegative double epsilon) {
        return point != null && nearlyEquals(point.getX(), point.getY(), epsilon);
    }

    @Contract("null -> false")
    public boolean nearlyEquals(@Nullable Point2D point) {
        return nearlyEquals(point, DEFAULT_EPSILON);
    }

    public boolean nearlyEquals(double x, double y, @Nonnegative double epsilon) {
        return NumberUtil.nearlyEquals(getX(), x, epsilon) && NumberUtil.nearlyEquals(getY(), y, epsilon);
    }

    public boolean nearlyEquals(double x, double y) {
        return nearlyEquals(x, y, DEFAULT_EPSILON);
    }

    @Nonnull
    @Override
    public String toString() {
        return StringUtil.toString(this, false, "x", "y");
    }
}
