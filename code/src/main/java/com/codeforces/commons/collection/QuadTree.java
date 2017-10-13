package com.codeforces.commons.collection;

import com.codeforces.commons.annotation.NonnullElements;
import com.google.common.base.Preconditions;

import javax.annotation.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import static com.codeforces.commons.math.Math.*;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 19.09.2017
 */
@SuppressWarnings("WeakerAccess")
public class QuadTree<T> {
    public static final double DEFAULT_EPSILON = 1.0E-6D;

    private final Node<T> root = new Node<>();

    @Nonnull
    private final ToDoubleFunction<T> xExtractor;

    @Nonnull
    private final ToDoubleFunction<T> yExtractor;

    private final double left;
    private final double top;
    private final double right;
    private final double bottom;

    @Nonnegative
    private final double epsilon;

    public QuadTree(
            @Nonnull ToDoubleFunction<T> xExtractor, @Nonnull ToDoubleFunction<T> yExtractor,
            double left, double top, double right, double bottom, @Nonnegative double epsilon
    ) {
        Preconditions.checkArgument(Double.isFinite(left) && Double.isFinite(right) && left < right);
        Preconditions.checkArgument(Double.isFinite(top) && Double.isFinite(bottom) && top < bottom);
        Preconditions.checkArgument(Double.isFinite(epsilon) && epsilon >= 0.0D);

        this.xExtractor = Objects.requireNonNull(xExtractor);
        this.yExtractor = Objects.requireNonNull(yExtractor);

        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;

        this.epsilon = epsilon;
    }

    public QuadTree(
            @Nonnull ToDoubleFunction<T> xExtractor, @Nonnull ToDoubleFunction<T> yExtractor,
            double left, double top, double right, double bottom
    ) {
        this(xExtractor, yExtractor, left, top, right, bottom, DEFAULT_EPSILON);
    }

    @Nonnull
    public ToDoubleFunction<T> getXExtractor() {
        return xExtractor;
    }

    @Nonnull
    public ToDoubleFunction<T> getYExtractor() {
        return yExtractor;
    }

    public double getLeft() {
        return left;
    }

    public double getTop() {
        return top;
    }

    public double getRight() {
        return right;
    }

    public double getBottom() {
        return bottom;
    }

    @Nonnegative
    public double getEpsilon() {
        return epsilon;
    }

    public void add(@Nonnull T value) {
        double x = xExtractor.applyAsDouble(value);
        double y = yExtractor.applyAsDouble(value);

        if (!Double.isFinite(x) || !Double.isFinite(y) || x < left || y < top || x > right || y > bottom) {
            throw new IllegalArgumentException(String.format(
                    "The point (x=%s, y=%s) is outside of the bounding box (left=%s, top=%s, right=%s, bottom=%s).",
                    x, y, left, top, right, bottom
            ));
        }

        add(value, x, y, root, left, top, right, bottom);
    }

    public void addAll(@Nonnull @NonnullElements T[] values) {
        for (T value : values) {
            add(value);
        }
    }

    public void addAll(@Nonnull @NonnullElements Iterable<T> values) {
        for (T value : values) {
            add(value);
        }
    }

    @SuppressWarnings({"AssignmentToMethodParameter", "OverlyLongMethod", "OverlyComplexMethod"})
    private void add(
            @Nonnull T value, double x, double y, @Nonnull Node<T> node,
            double left, double top, double right, double bottom
    ) {
        T currentValue = node.value;

        if (currentValue == null) {
            if (node.hasValueBelow) {
                addAsChild(value, x, y, node, left, top, right, bottom);
            } else {
                node.value = value;
            }
        } else {
            double currentX = xExtractor.applyAsDouble(currentValue);
            double currentY = yExtractor.applyAsDouble(currentValue);

            if (abs(x - currentX) <= epsilon && abs(y - currentY) <= epsilon) {
                return;
            }

            node.value = null;
            node.hasValueBelow = true;
            node.initializeChildren();

            addAsChild(currentValue, currentX, currentY, node, left, top, right, bottom);
            addAsChild(value, x, y, node, left, top, right, bottom);
        }
    }

    private void addAsChild(
            @Nonnull T value, double x, double y, @Nonnull Node<T> node,
            double left, double top, double right, double bottom
    ) {
        double centerX = (left + right) / 2.0D;
        double centerY = (top + bottom) / 2.0D;

        if (x < centerX) {
            if (y < centerY) {
                add(value, x, y, node.leftTop, left, top, centerX, centerY);
            } else {
                add(value, x, y, node.leftBottom, left, centerY, centerX, bottom);
            }
        } else {
            if (y < centerY) {
                add(value, x, y, node.rightTop, centerX, top, right, centerY);
            } else {
                add(value, x, y, node.rightBottom, centerX, centerY, right, bottom);
            }
        }
    }

    @Nullable
    public T findNearest(@Nonnull T value) {
        return findNearest(xExtractor.applyAsDouble(value), yExtractor.applyAsDouble(value));
    }

    @Nullable
    public T findNearest(double x, double y) {
        return findNearest(x, y, root, left, top, right, bottom);
    }

    @Nullable
    public T findNearest(@Nonnull T value, @Nonnull Predicate<T> matcher) {
        return findNearest(xExtractor.applyAsDouble(value), yExtractor.applyAsDouble(value), matcher);
    }

    @Nullable
    public T findNearest(double x, double y, @Nonnull Predicate<T> matcher) {
        return findNearest(x, y, root, left, top, right, bottom, matcher);
    }

    // Equal to call of findNearest(..., Predicate<T> matcher) with (value -> true), but copied for performance reason
    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    @Nullable
    private T findNearest(
            double x, double y, @Nonnull Node<T> node, double left, double top, double right, double bottom
    ) {
        if (node.value != null) {
            return node.value;
        }

        if (!node.hasValueBelow) {
            return null;
        }

        double centerX = (left + right) / 2.0D;
        double centerY = (top + bottom) / 2.0D;

        if (x < centerX) {
            if (y < centerY) {
                T nearestValue = findNearest(x, y, node.leftTop, left, top, centerX, centerY);
                double nearestSquaredDistance = getSquaredDistanceTo(nearestValue, x, y);

                if (nearestSquaredDistance + epsilon >= sqr(centerX - x)) {
                    T otherValue = findNearest(x, y, node.rightTop, centerX, top, right, centerY);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                        nearestSquaredDistance = otherSquaredDistance;
                    }
                }

                if (nearestSquaredDistance + epsilon >= sqr(centerY - y)) {
                    T otherValue = findNearest(x, y, node.leftBottom, left, centerY, centerX, bottom);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                        nearestSquaredDistance = otherSquaredDistance;
                    }
                }

                if (nearestSquaredDistance + epsilon >= sumSqr(centerX - x, centerY - y)) {
                    T otherValue = findNearest(x, y, node.rightBottom, centerX, centerY, right, bottom);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                    }
                }

                return nearestValue;
            } else {
                T nearestValue = findNearest(x, y, node.leftBottom, left, centerY, centerX, bottom);
                double nearestSquaredDistance = getSquaredDistanceTo(nearestValue, x, y);

                if (nearestSquaredDistance + epsilon >= sqr(centerX - x)) {
                    T otherValue = findNearest(x, y, node.rightBottom, centerX, centerY, right, bottom);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                        nearestSquaredDistance = otherSquaredDistance;
                    }
                }

                if (nearestSquaredDistance + epsilon > sqr(y - centerY)) {
                    T otherValue = findNearest(x, y, node.leftTop, left, top, centerX, centerY);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                        nearestSquaredDistance = otherSquaredDistance;
                    }
                }

                if (nearestSquaredDistance + epsilon >= sumSqr(centerX - x, y - centerY)) {
                    T otherValue = findNearest(x, y, node.rightTop, centerX, top, right, centerY);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                    }
                }

                return nearestValue;
            }
        } else {
            if (y < centerY) {
                T nearestValue = findNearest(x, y, node.rightTop, centerX, top, right, centerY);
                double nearestSquaredDistance = getSquaredDistanceTo(nearestValue, x, y);

                if (nearestSquaredDistance + epsilon > sqr(x - centerX)) {
                    T otherValue = findNearest(x, y, node.leftTop, left, top, centerX, centerY);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                        nearestSquaredDistance = otherSquaredDistance;
                    }
                }

                if (nearestSquaredDistance + epsilon >= sqr(centerY - y)) {
                    T otherValue = findNearest(x, y, node.rightBottom, centerX, centerY, right, bottom);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                        nearestSquaredDistance = otherSquaredDistance;
                    }
                }

                if (nearestSquaredDistance + epsilon >= sumSqr(x - centerX, centerY - y)) {
                    T otherValue = findNearest(x, y, node.leftBottom, left, centerY, centerX, bottom);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                    }
                }

                return nearestValue;
            } else {
                T nearestValue = findNearest(x, y, node.rightBottom, centerX, centerY, right, bottom);
                double nearestSquaredDistance = getSquaredDistanceTo(nearestValue, x, y);

                if (nearestSquaredDistance + epsilon > sqr(x - centerX)) {
                    T otherValue = findNearest(x, y, node.leftBottom, left, centerY, centerX, bottom);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                        nearestSquaredDistance = otherSquaredDistance;
                    }
                }

                if (nearestSquaredDistance + epsilon > sqr(y - centerY)) {
                    T otherValue = findNearest(x, y, node.rightTop, centerX, top, right, centerY);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                        nearestSquaredDistance = otherSquaredDistance;
                    }
                }

                if (nearestSquaredDistance + epsilon > sumSqr(x - centerX, y - centerY)) {
                    T otherValue = findNearest(x, y, node.leftTop, left, top, centerX, centerY);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                    }
                }

                return nearestValue;
            }
        }
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    @Nullable
    private T findNearest(
            double x, double y, @Nonnull Node<T> node,
            double left, double top, double right, double bottom, @Nonnull Predicate<T> matcher
    ) {
        if (node.value != null) {
            return matcher.test(node.value) ? node.value : null;
        }

        if (!node.hasValueBelow) {
            return null;
        }

        double centerX = (left + right) / 2.0D;
        double centerY = (top + bottom) / 2.0D;

        if (x < centerX) {
            if (y < centerY) {
                T nearestValue = findNearest(x, y, node.leftTop, left, top, centerX, centerY, matcher);
                double nearestSquaredDistance = getSquaredDistanceTo(nearestValue, x, y);

                if (nearestSquaredDistance + epsilon >= sqr(centerX - x)) {
                    T otherValue = findNearest(x, y, node.rightTop, centerX, top, right, centerY, matcher);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                        nearestSquaredDistance = otherSquaredDistance;
                    }
                }

                if (nearestSquaredDistance + epsilon >= sqr(centerY - y)) {
                    T otherValue = findNearest(x, y, node.leftBottom, left, centerY, centerX, bottom, matcher);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                        nearestSquaredDistance = otherSquaredDistance;
                    }
                }

                if (nearestSquaredDistance + epsilon >= sumSqr(centerX - x, centerY - y)) {
                    T otherValue = findNearest(x, y, node.rightBottom, centerX, centerY, right, bottom, matcher);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                    }
                }

                return nearestValue;
            } else {
                T nearestValue = findNearest(x, y, node.leftBottom, left, centerY, centerX, bottom, matcher);
                double nearestSquaredDistance = getSquaredDistanceTo(nearestValue, x, y);

                if (nearestSquaredDistance + epsilon >= sqr(centerX - x)) {
                    T otherValue = findNearest(x, y, node.rightBottom, centerX, centerY, right, bottom, matcher);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                        nearestSquaredDistance = otherSquaredDistance;
                    }
                }

                if (nearestSquaredDistance + epsilon > sqr(y - centerY)) {
                    T otherValue = findNearest(x, y, node.leftTop, left, top, centerX, centerY, matcher);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                        nearestSquaredDistance = otherSquaredDistance;
                    }
                }

                if (nearestSquaredDistance + epsilon >= sumSqr(centerX - x, y - centerY)) {
                    T otherValue = findNearest(x, y, node.rightTop, centerX, top, right, centerY, matcher);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                    }
                }

                return nearestValue;
            }
        } else {
            if (y < centerY) {
                T nearestValue = findNearest(x, y, node.rightTop, centerX, top, right, centerY, matcher);
                double nearestSquaredDistance = getSquaredDistanceTo(nearestValue, x, y);

                if (nearestSquaredDistance + epsilon > sqr(x - centerX)) {
                    T otherValue = findNearest(x, y, node.leftTop, left, top, centerX, centerY, matcher);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                        nearestSquaredDistance = otherSquaredDistance;
                    }
                }

                if (nearestSquaredDistance + epsilon >= sqr(centerY - y)) {
                    T otherValue = findNearest(x, y, node.rightBottom, centerX, centerY, right, bottom, matcher);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                        nearestSquaredDistance = otherSquaredDistance;
                    }
                }

                if (nearestSquaredDistance + epsilon >= sumSqr(x - centerX, centerY - y)) {
                    T otherValue = findNearest(x, y, node.leftBottom, left, centerY, centerX, bottom, matcher);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                    }
                }

                return nearestValue;
            } else {
                T nearestValue = findNearest(x, y, node.rightBottom, centerX, centerY, right, bottom, matcher);
                double nearestSquaredDistance = getSquaredDistanceTo(nearestValue, x, y);

                if (nearestSquaredDistance + epsilon > sqr(x - centerX)) {
                    T otherValue = findNearest(x, y, node.leftBottom, left, centerY, centerX, bottom, matcher);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                        nearestSquaredDistance = otherSquaredDistance;
                    }
                }

                if (nearestSquaredDistance + epsilon > sqr(y - centerY)) {
                    T otherValue = findNearest(x, y, node.rightTop, centerX, top, right, centerY, matcher);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                        nearestSquaredDistance = otherSquaredDistance;
                    }
                }

                if (nearestSquaredDistance + epsilon > sumSqr(x - centerX, y - centerY)) {
                    T otherValue = findNearest(x, y, node.leftTop, left, top, centerX, centerY, matcher);
                    double otherSquaredDistance = getSquaredDistanceTo(otherValue, x, y);

                    if (otherSquaredDistance < nearestSquaredDistance) {
                        nearestValue = otherValue;
                    }
                }

                return nearestValue;
            }
        }
    }

    @Nonnull
    public List<T> findAllNearby(@Nonnull T value, double squaredDistance) {
        return findAllNearby(xExtractor.applyAsDouble(value), yExtractor.applyAsDouble(value), squaredDistance);
    }

    @Nonnull
    public List<T> findAllNearby(double x, double y, double squaredDistance) {
        List<T> values = new ArrayList<>();
        findAllNearby(x, y, squaredDistance, values, root, left, top, right, bottom);
        return values;
    }

    @Nonnull
    public List<T> findAllNearby(@Nonnull T value, double squaredDistance, @Nonnull Predicate<T> matcher) {
        return findAllNearby(xExtractor.applyAsDouble(value), yExtractor.applyAsDouble(value), squaredDistance, matcher);
    }

    @Nonnull
    public List<T> findAllNearby(double x, double y, double squaredDistance, @Nonnull Predicate<T> matcher) {
        List<T> values = new ArrayList<>();
        findAllNearby(x, y, squaredDistance, values, root, left, top, right, bottom, matcher);
        return values;
    }

    // Equal to call of findAllNearby(..., Predicate<T> matcher) with (value -> true), but copied for performance reason
    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    private void findAllNearby(
            double x, double y, double squaredDistance, List<T> values, @Nonnull Node<T> node,
            double left, double top, double right, double bottom
    ) {
        if (node.value != null) {
            if (getSquaredDistanceTo(node.value, x, y) <= squaredDistance) {
                values.add(node.value);
            }
            return;
        }

        if (!node.hasValueBelow) {
            return;
        }

        double centerX = (left + right) / 2.0D;
        double centerY = (top + bottom) / 2.0D;

        if (x < centerX) {
            if (y < centerY) {
                findAllNearby(x, y, squaredDistance, values, node.leftTop, left, top, centerX, centerY);

                if (squaredDistance + epsilon >= sqr(centerX - x)) {
                    findAllNearby(x, y, squaredDistance, values, node.rightTop, centerX, top, right, centerY);
                }

                if (squaredDistance + epsilon >= sqr(centerY - y)) {
                    findAllNearby(x, y, squaredDistance, values, node.leftBottom, left, centerY, centerX, bottom);
                }

                if (squaredDistance + epsilon >= sumSqr(centerX - x, centerY - y)) {
                    findAllNearby(x, y, squaredDistance, values, node.rightBottom, centerX, centerY, right, bottom);
                }
            } else {
                findAllNearby(x, y, squaredDistance, values, node.leftBottom, left, centerY, centerX, bottom);

                if (squaredDistance + epsilon >= sqr(centerX - x)) {
                    findAllNearby(x, y, squaredDistance, values, node.rightBottom, centerX, centerY, right, bottom);
                }

                if (squaredDistance + epsilon > sqr(y - centerY)) {
                    findAllNearby(x, y, squaredDistance, values, node.leftTop, left, top, centerX, centerY);
                }

                if (squaredDistance + epsilon >= sumSqr(centerX - x, y - centerY)) {
                    findAllNearby(x, y, squaredDistance, values, node.rightTop, centerX, top, right, centerY);
                }
            }
        } else {
            if (y < centerY) {
                findAllNearby(x, y, squaredDistance, values, node.rightTop, centerX, top, right, centerY);

                if (squaredDistance + epsilon > sqr(x - centerX)) {
                    findAllNearby(x, y, squaredDistance, values, node.leftTop, left, top, centerX, centerY);
                }

                if (squaredDistance + epsilon >= sqr(centerY - y)) {
                    findAllNearby(x, y, squaredDistance, values, node.rightBottom, centerX, centerY, right, bottom);
                }

                if (squaredDistance + epsilon >= sumSqr(x - centerX, centerY - y)) {
                    findAllNearby(x, y, squaredDistance, values, node.leftBottom, left, centerY, centerX, bottom);
                }
            } else {
                findAllNearby(x, y, squaredDistance, values, node.rightBottom, centerX, centerY, right, bottom);

                if (squaredDistance + epsilon > sqr(x - centerX)) {
                    findAllNearby(x, y, squaredDistance, values, node.leftBottom, left, centerY, centerX, bottom);
                }

                if (squaredDistance + epsilon > sqr(y - centerY)) {
                    findAllNearby(x, y, squaredDistance, values, node.rightTop, centerX, top, right, centerY);
                }

                if (squaredDistance + epsilon > sumSqr(x - centerX, y - centerY)) {
                    findAllNearby(x, y, squaredDistance, values, node.leftTop, left, top, centerX, centerY);
                }
            }
        }
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    private void findAllNearby(
            double x, double y, double squaredDistance, List<T> values, @Nonnull Node<T> node,
            double left, double top, double right, double bottom, @Nonnull Predicate<T> matcher
    ) {
        if (node.value != null) {
            if (getSquaredDistanceTo(node.value, x, y) <= squaredDistance && matcher.test(node.value)) {
                values.add(node.value);
            }
            return;
        }

        if (!node.hasValueBelow) {
            return;
        }

        double centerX = (left + right) / 2.0D;
        double centerY = (top + bottom) / 2.0D;

        if (x < centerX) {
            if (y < centerY) {
                findAllNearby(x, y, squaredDistance, values, node.leftTop, left, top, centerX, centerY, matcher);

                if (squaredDistance + epsilon >= sqr(centerX - x)) {
                    findAllNearby(x, y, squaredDistance, values, node.rightTop, centerX, top, right, centerY, matcher);
                }

                if (squaredDistance + epsilon >= sqr(centerY - y)) {
                    findAllNearby(x, y, squaredDistance, values, node.leftBottom, left, centerY, centerX, bottom, matcher);
                }

                if (squaredDistance + epsilon >= sumSqr(centerX - x, centerY - y)) {
                    findAllNearby(x, y, squaredDistance, values, node.rightBottom, centerX, centerY, right, bottom, matcher);
                }
            } else {
                findAllNearby(x, y, squaredDistance, values, node.leftBottom, left, centerY, centerX, bottom, matcher);

                if (squaredDistance + epsilon >= sqr(centerX - x)) {
                    findAllNearby(x, y, squaredDistance, values, node.rightBottom, centerX, centerY, right, bottom, matcher);
                }

                if (squaredDistance + epsilon > sqr(y - centerY)) {
                    findAllNearby(x, y, squaredDistance, values, node.leftTop, left, top, centerX, centerY, matcher);
                }

                if (squaredDistance + epsilon >= sumSqr(centerX - x, y - centerY)) {
                    findAllNearby(x, y, squaredDistance, values, node.rightTop, centerX, top, right, centerY, matcher);
                }
            }
        } else {
            if (y < centerY) {
                findAllNearby(x, y, squaredDistance, values, node.rightTop, centerX, top, right, centerY, matcher);

                if (squaredDistance + epsilon > sqr(x - centerX)) {
                    findAllNearby(x, y, squaredDistance, values, node.leftTop, left, top, centerX, centerY, matcher);
                }

                if (squaredDistance + epsilon >= sqr(centerY - y)) {
                    findAllNearby(x, y, squaredDistance, values, node.rightBottom, centerX, centerY, right, bottom, matcher);
                }

                if (squaredDistance + epsilon >= sumSqr(x - centerX, centerY - y)) {
                    findAllNearby(x, y, squaredDistance, values, node.leftBottom, left, centerY, centerX, bottom, matcher);
                }
            } else {
                findAllNearby(x, y, squaredDistance, values, node.rightBottom, centerX, centerY, right, bottom, matcher);

                if (squaredDistance + epsilon > sqr(x - centerX)) {
                    findAllNearby(x, y, squaredDistance, values, node.leftBottom, left, centerY, centerX, bottom, matcher);
                }

                if (squaredDistance + epsilon > sqr(y - centerY)) {
                    findAllNearby(x, y, squaredDistance, values, node.rightTop, centerX, top, right, centerY, matcher);
                }

                if (squaredDistance + epsilon > sumSqr(x - centerX, y - centerY)) {
                    findAllNearby(x, y, squaredDistance, values, node.leftTop, left, top, centerX, centerY, matcher);
                }
            }
        }
    }

    public boolean hasNearby(@Nonnull T value, double squaredDistance) {
        return hasNearby(xExtractor.applyAsDouble(value), yExtractor.applyAsDouble(value), squaredDistance);
    }

    public boolean hasNearby(double x, double y, double squaredDistance) {
        return hasNearby(x, y, squaredDistance, root, left, top, right, bottom);
    }

    public boolean hasNearby(@Nonnull T value, double squaredDistance, @Nonnull Predicate<T> matcher) {
        return hasNearby(xExtractor.applyAsDouble(value), yExtractor.applyAsDouble(value), squaredDistance, matcher);
    }

    public boolean hasNearby(double x, double y, double squaredDistance, @Nonnull Predicate<T> matcher) {
        return hasNearby(x, y, squaredDistance, root, left, top, right, bottom, matcher);
    }

    // Equal to call of hasNearby(..., Predicate<T> matcher) with (value -> true), but copied for performance reason
    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    private boolean hasNearby(
            double x, double y, double squaredDistance, @Nonnull Node<T> node,
            double left, double top, double right, double bottom
    ) {
        if (node.value != null) {
            return getSquaredDistanceTo(node.value, x, y) <= squaredDistance;
        }

        if (!node.hasValueBelow) {
            return false;
        }

        double centerX = (left + right) / 2.0D;
        double centerY = (top + bottom) / 2.0D;

        if (x < centerX) {
            if (y < centerY) {
                if (hasNearby(x, y, squaredDistance, node.leftTop, left, top, centerX, centerY)) {
                    return true;
                }

                if (squaredDistance + epsilon >= sqr(centerX - x)) {
                    if (hasNearby(x, y, squaredDistance, node.rightTop, centerX, top, right, centerY)) {
                        return true;
                    }
                }

                if (squaredDistance + epsilon >= sqr(centerY - y)) {
                    if (hasNearby(x, y, squaredDistance, node.leftBottom, left, centerY, centerX, bottom)) {
                        return true;
                    }
                }

                if (squaredDistance + epsilon >= sumSqr(centerX - x, centerY - y)) {
                    if (hasNearby(x, y, squaredDistance, node.rightBottom, centerX, centerY, right, bottom)) {
                        return true;
                    }
                }

                return false;
            } else {
                if (hasNearby(x, y, squaredDistance, node.leftBottom, left, centerY, centerX, bottom)) {
                    return true;
                }

                if (squaredDistance + epsilon >= sqr(centerX - x)) {
                    if (hasNearby(x, y, squaredDistance, node.rightBottom, centerX, centerY, right, bottom)) {
                        return true;
                    }
                }

                if (squaredDistance + epsilon > sqr(y - centerY)) {
                    if (hasNearby(x, y, squaredDistance, node.leftTop, left, top, centerX, centerY)) {
                        return true;
                    }
                }

                if (squaredDistance + epsilon >= sumSqr(centerX - x, y - centerY)) {
                    if (hasNearby(x, y, squaredDistance, node.rightTop, centerX, top, right, centerY)) {
                        return true;
                    }
                }

                return false;
            }
        } else {
            if (y < centerY) {
                if (hasNearby(x, y, squaredDistance, node.rightTop, centerX, top, right, centerY)) {
                    return true;
                }

                if (squaredDistance + epsilon > sqr(x - centerX)) {
                    if (hasNearby(x, y, squaredDistance, node.leftTop, left, top, centerX, centerY)) {
                        return true;
                    }
                }

                if (squaredDistance + epsilon >= sqr(centerY - y)) {
                    if (hasNearby(x, y, squaredDistance, node.rightBottom, centerX, centerY, right, bottom)) {
                        return true;
                    }
                }

                if (squaredDistance + epsilon >= sumSqr(x - centerX, centerY - y)) {
                    if (hasNearby(x, y, squaredDistance, node.leftBottom, left, centerY, centerX, bottom)) {
                        return true;
                    }
                }

                return false;
            } else {
                if (hasNearby(x, y, squaredDistance, node.rightBottom, centerX, centerY, right, bottom)) {
                    return true;
                }

                if (squaredDistance + epsilon > sqr(x - centerX)) {
                    if (hasNearby(x, y, squaredDistance, node.leftBottom, left, centerY, centerX, bottom)) {
                        return true;
                    }
                }

                if (squaredDistance + epsilon > sqr(y - centerY)) {
                    if (hasNearby(x, y, squaredDistance, node.rightTop, centerX, top, right, centerY)) {
                        return true;
                    }
                }

                if (squaredDistance + epsilon > sumSqr(x - centerX, y - centerY)) {
                    if (hasNearby(x, y, squaredDistance, node.leftTop, left, top, centerX, centerY)) {
                        return true;
                    }
                }

                return false;
            }
        }
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    private boolean hasNearby(
            double x, double y, double squaredDistance, @Nonnull Node<T> node,
            double left, double top, double right, double bottom, @Nonnull Predicate<T> matcher
    ) {
        if (node.value != null) {
            return getSquaredDistanceTo(node.value, x, y) <= squaredDistance && matcher.test(node.value);
        }

        if (!node.hasValueBelow) {
            return false;
        }

        double centerX = (left + right) / 2.0D;
        double centerY = (top + bottom) / 2.0D;

        if (x < centerX) {
            if (y < centerY) {
                if (hasNearby(x, y, squaredDistance, node.leftTop, left, top, centerX, centerY, matcher)) {
                    return true;
                }

                if (squaredDistance + epsilon >= sqr(centerX - x)) {
                    if (hasNearby(x, y, squaredDistance, node.rightTop, centerX, top, right, centerY, matcher)) {
                        return true;
                    }
                }

                if (squaredDistance + epsilon >= sqr(centerY - y)) {
                    if (hasNearby(x, y, squaredDistance, node.leftBottom, left, centerY, centerX, bottom, matcher)) {
                        return true;
                    }
                }

                if (squaredDistance + epsilon >= sumSqr(centerX - x, centerY - y)) {
                    if (hasNearby(x, y, squaredDistance, node.rightBottom, centerX, centerY, right, bottom, matcher)) {
                        return true;
                    }
                }

                return false;
            } else {
                if (hasNearby(x, y, squaredDistance, node.leftBottom, left, centerY, centerX, bottom, matcher)) {
                    return true;
                }

                if (squaredDistance + epsilon >= sqr(centerX - x)) {
                    if (hasNearby(x, y, squaredDistance, node.rightBottom, centerX, centerY, right, bottom, matcher)) {
                        return true;
                    }
                }

                if (squaredDistance + epsilon > sqr(y - centerY)) {
                    if (hasNearby(x, y, squaredDistance, node.leftTop, left, top, centerX, centerY, matcher)) {
                        return true;
                    }
                }

                if (squaredDistance + epsilon >= sumSqr(centerX - x, y - centerY)) {
                    if (hasNearby(x, y, squaredDistance, node.rightTop, centerX, top, right, centerY, matcher)) {
                        return true;
                    }
                }

                return false;
            }
        } else {
            if (y < centerY) {
                if (hasNearby(x, y, squaredDistance, node.rightTop, centerX, top, right, centerY, matcher)) {
                    return true;
                }

                if (squaredDistance + epsilon > sqr(x - centerX)) {
                    if (hasNearby(x, y, squaredDistance, node.leftTop, left, top, centerX, centerY, matcher)) {
                        return true;
                    }
                }

                if (squaredDistance + epsilon >= sqr(centerY - y)) {
                    if (hasNearby(x, y, squaredDistance, node.rightBottom, centerX, centerY, right, bottom, matcher)) {
                        return true;
                    }
                }

                if (squaredDistance + epsilon >= sumSqr(x - centerX, centerY - y)) {
                    if (hasNearby(x, y, squaredDistance, node.leftBottom, left, centerY, centerX, bottom, matcher)) {
                        return true;
                    }
                }

                return false;
            } else {
                if (hasNearby(x, y, squaredDistance, node.rightBottom, centerX, centerY, right, bottom, matcher)) {
                    return true;
                }

                if (squaredDistance + epsilon > sqr(x - centerX)) {
                    if (hasNearby(x, y, squaredDistance, node.leftBottom, left, centerY, centerX, bottom, matcher)) {
                        return true;
                    }
                }

                if (squaredDistance + epsilon > sqr(y - centerY)) {
                    if (hasNearby(x, y, squaredDistance, node.rightTop, centerX, top, right, centerY, matcher)) {
                        return true;
                    }
                }

                if (squaredDistance + epsilon > sumSqr(x - centerX, y - centerY)) {
                    if (hasNearby(x, y, squaredDistance, node.leftTop, left, top, centerX, centerY, matcher)) {
                        return true;
                    }
                }

                return false;
            }
        }
    }

    public void clear() {
        clear(root);
    }

    private static <T> void clear(@Nonnull Node<T> node) {
        node.value = null;

        if (node.hasValueBelow) {
            node.hasValueBelow = false;

            clear(node.leftTop);
            clear(node.rightTop);
            clear(node.leftBottom);
            clear(node.rightBottom);
        }
    }

    private double getSquaredDistanceTo(@Nullable T value, double x, double y) {
        return value == null ? Double.POSITIVE_INFINITY : sumSqr(
                xExtractor.applyAsDouble(value) - x, yExtractor.applyAsDouble(value) - y
        );
    }

    private static final class Node<T> {
        @Nullable
        private T value;

        private boolean hasValueBelow;

        private Node<T> leftTop;
        private Node<T> rightTop;
        private Node<T> leftBottom;
        private Node<T> rightBottom;

        public void initializeChildren() {
            if (leftTop == null) {
                leftTop = new Node<>();
                rightTop = new Node<>();
                leftBottom = new Node<>();
                rightBottom = new Node<>();
            }
        }
    }
}
