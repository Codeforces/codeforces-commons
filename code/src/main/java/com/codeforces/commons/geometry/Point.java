package com.codeforces.commons.geometry;

import com.codeforces.commons.pair.DoublePair;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 22.07.13
 */
public class Point extends DoublePair {
    public Point(double x, double y) {
        super(x, y);
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
}
