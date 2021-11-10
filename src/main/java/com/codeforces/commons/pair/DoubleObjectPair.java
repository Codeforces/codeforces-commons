package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 12.04.2017
 */
public class DoubleObjectPair<S> {
    private double first;

    @Nullable
    private S second;

    public DoubleObjectPair() {
    }

    public DoubleObjectPair(double first, @Nullable S second) {
        this.first = first;
        this.second = second;
    }

    public DoubleObjectPair(@Nonnull DoubleObjectPair<S> pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    public double getFirst() {
        return first;
    }

    public void setFirst(double first) {
        this.first = first;
    }

    @Nullable
    public S getSecond() {
        return second;
    }

    public void setSecond(@Nullable S second) {
        this.second = second;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof DoubleObjectPair)) {
            return false;
        }

        DoubleObjectPair pair = (DoubleObjectPair) o;

        return Double.compare(first, pair.first) == 0
                && (second == null ? pair.second == null : second.equals(pair.second));
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return 32323 * Double.hashCode(first) + (second == null ? 0 : second.hashCode());
    }

    @Override
    public String toString() {
        return StringUtil.toString(this, false, "first", "second");
    }
}
