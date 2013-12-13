package com.codeforces.commons.geometry;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.sqrt;

/**
 * ax + by + c = 0
 *
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 22.07.13
 */
@SuppressWarnings("StandardVariableNames")
public class Line {
    private double a;
    private double b;
    private double c;

    public Line(double a, double b, double c) {
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

    public Line[] getParallelLines(double range) {
        double shift = range * sqrt(a * a + b * b);
        return new Line[]{new Line(a, b, c + shift), new Line(a, b, c - shift)};
    }

    public double getDistanceFrom(double x, double y) {
        return abs((a * x + b * y + c) / sqrt(a * a + b * b));
    }

    public Point getUnitNormalFrom(double x, double y) {
        double length = sqrt(a * a + b * b);
        return getDistanceFrom(x + a, y + b) < getDistanceFrom(x - a, y - b)
                ? new Point(a / length, b / length)
                : new Point(-a / length, -b / length);
    }

    public Point getProjectionOf(double x, double y) {
        Point unitNormal = getUnitNormalFrom(x, y);
        double distance = getDistanceFrom(x, y);
        return new Point(x + unitNormal.getX() * distance, y + unitNormal.getY() * distance);
    }

    public static Line getLineByTwoPoints(double x1, double y1, double x2, double y2) {
        return new Line(y2 - y1, x1 - x2, (y1 - y2) * x1 + (x2 - x1) * y1);
    }

    public static Line getLineByTwoPoints(Point point1, Point point2) {
        return getLineByTwoPoints(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }
}
