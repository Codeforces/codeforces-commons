package com.codeforces.commons.geometry;

import com.codeforces.commons.text.StringUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;

/**
 * x^2 + y^2 + ax + by + c = 0
 *
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 30.06.2015
 */
@SuppressWarnings("StandardVariableNames")
public class Circle2D {
    public static final double DEFAULT_EPSILON = Line2D.DEFAULT_EPSILON;

    private final double a;
    private final double b;
    private final double c;

    public Circle2D(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public Circle2D(@Nonnull Circle2D circle) {
        this.a = circle.a;
        this.b = circle.b;
        this.c = circle.c;
    }

    public double getA() {
        return a;
    }

    @Contract(pure = true)
    public Circle2D setA(double a) {
        return new Circle2D(a, b, c);
    }

    public double getB() {
        return b;
    }

    @Contract(pure = true)
    public Circle2D setB(double b) {
        return new Circle2D(a, b, c);
    }

    public double getC() {
        return c;
    }

    @Contract(pure = true)
    public Circle2D setC(double c) {
        return new Circle2D(a, b, c);
    }

    @Nonnull
    public Circle2D copy() {
        return new Circle2D(this);
    }

    @Override
    public String toString() {
        return StringUtil.toString(this, false, "a", "b", "c");
    }
}
