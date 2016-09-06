package com.codeforces.commons.geometry;

import com.codeforces.commons.math.NumberUtil;
import com.codeforces.commons.pair.DoublePair;
import com.codeforces.commons.text.StringUtil;
import org.apache.commons.math3.util.MathArrays;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.codeforces.commons.math.Math.*;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 22.07.2013
 */
public class Vector2D extends DoublePair {
    public static final double DEFAULT_EPSILON = Line2D.DEFAULT_EPSILON;
    public static final Vector2D[] EMPTY_VECTOR_ARRAY = {};

    public Vector2D(double x, double y) {
        super(x, y);
    }

    public Vector2D(double x1, double y1, double x2, double y2) {
        super(x2 - x1, y2 - y1);
    }

    public Vector2D(@Nonnull Point2D point1, @Nonnull Point2D point2) {
        super(point2.getX() - point1.getX(), point2.getY() - point1.getY());
    }

    public Vector2D(@Nonnull Point2D point1, double x2, double y2) {
        super(x2 - point1.getX(), y2 - point1.getY());
    }

    public Vector2D(double x1, double y1, @Nonnull Point2D point2) {
        super(point2.getX() - x1, point2.getY() - y1);
    }

    public Vector2D(@Nonnull Vector2D vector) {
        super(vector.getX(), vector.getY());
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
    public Vector2D add(@Nonnull Vector2D vector) {
        setX(getX() + vector.getX());
        setY(getY() + vector.getY());
        return this;
    }

    @Nonnull
    public Vector2D add(double x, double y) {
        setX(getX() + x);
        setY(getY() + y);
        return this;
    }

    @Nonnull
    public Point2D add(@Nonnull Point2D point) {
        return new Point2D(point.getX() + getX(), point.getY() + getY());
    }

    @Nonnull
    public Vector2D subtract(@Nonnull Vector2D vector) {
        setX(getX() - vector.getX());
        setY(getY() - vector.getY());
        return this;
    }

    @Nonnull
    public Vector2D subtract(double x, double y) {
        setX(getX() - x);
        setY(getY() - y);
        return this;
    }

    @Nonnull
    public Vector2D multiply(double factor) {
        setX(factor * getX());
        setY(factor * getY());
        return this;
    }

    @Nonnull
    public Vector2D rotate(double angle) {
        double cos = cos(angle);
        double sin = sin(angle);

        double x = getX();
        double y = getY();

        setX(x * cos - y * sin);
        setY(x * sin + y * cos);

        return this;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Nonnull
    public Vector2D rotateHalfPi() {
        double x = getX();
        double y = getY();

        setX(-y);
        setY(x);

        return this;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Nonnull
    public Vector2D rotateMinusHalfPi() {
        double x = getX();
        double y = getY();

        setX(y);
        setY(-x);

        return this;
    }

    public double dotProduct(@Nonnull Vector2D vector) {
        return MathArrays.linearCombination(getX(), vector.getX(), getY(), vector.getY());
    }

    @Nonnull
    public Vector2D negate() {
        setX(-getX());
        setY(-getY());
        return this;
    }

    @Nonnull
    public Vector2D normalize() {
        double length = getLength();
        if (length == 0.0D) {
            throw new IllegalStateException("Can't set angle of zero-width vector.");
        }
        setX(getX() / length);
        setY(getY() / length);
        return this;
    }

    public double getAngle() {
        return atan2(getY(), getX());
    }

    @Nonnull
    public Vector2D setAngle(double angle) {
        double length = getLength();
        if (length == 0.0D) {
            throw new IllegalStateException("Can't set angle of zero-width vector.");
        }
        setX(cos(angle) * length);
        setY(sin(angle) * length);
        return this;
    }

    public double getAngle(Vector2D vector) {
        return org.apache.commons.math3.geometry.euclidean.twod.Vector2D.angle(
                new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(getX(), getY()),
                new org.apache.commons.math3.geometry.euclidean.twod.Vector2D(vector.getX(), vector.getY())
        );
    }

    public double getLength() {
        return hypot(getX(), getY());
    }

    @Nonnull
    public Vector2D setLength(double length) {
        double currentLength = getLength();
        if (currentLength == 0.0D) {
            throw new IllegalStateException("Can't resize zero-width vector.");
        }
        return multiply(length / currentLength);
    }

    public double getSquaredLength() {
        return getX() * getX() + getY() * getY();
    }

    @Nonnull
    public Vector2D setSquaredLength(double squaredLength) {
        double currentSquaredLength = getSquaredLength();
        if (currentSquaredLength == 0.0D) {
            throw new IllegalStateException("Can't resize zero-width vector.");
        }
        return multiply(sqrt(squaredLength / currentSquaredLength));
    }

    @Contract(value = "-> !null", pure = true)
    @Nonnull
    public Vector2D copy() {
        return new Vector2D(this);
    }

    @Contract(value = "-> !null", pure = true)
    @Nonnull
    public Vector2D copyNegate() {
        return new Vector2D(-getX(), -getY());
    }

    @Contract("null, _ -> false")
    public boolean nearlyEquals(@Nullable Vector2D vector, double epsilon) {
        return vector != null
                && NumberUtil.nearlyEquals(getX(), vector.getX(), epsilon)
                && NumberUtil.nearlyEquals(getY(), vector.getY(), epsilon);
    }

    @Contract("null -> false")
    public boolean nearlyEquals(@Nullable Vector2D vector) {
        return nearlyEquals(vector, DEFAULT_EPSILON);
    }

    public boolean nearlyEquals(double x, double y, double epsilon) {
        return NumberUtil.nearlyEquals(getX(), x, epsilon)
                && NumberUtil.nearlyEquals(getY(), y, epsilon);
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
