package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 11.07.13
 */
public class DoublePair implements Comparable<DoublePair> {
    private double first;
    private double second;

    public DoublePair() {
    }

    public DoublePair(double first, double second) {
        this.first = first;
        this.second = second;
    }

    public DoublePair(@Nonnull DoublePair pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    public double getFirst() {
        return first;
    }

    public void setFirst(double first) {
        this.first = first;
    }

    public double getSecond() {
        return second;
    }

    public void setSecond(double second) {
        this.second = second;
    }

    @SuppressWarnings("CompareToUsesNonFinalVariable")
    @Override
    public int compareTo(@Nonnull DoublePair pair) {
        int comparisonResult = Double.compare(first, pair.first);
        return comparisonResult == 0 ? Double.compare(second, pair.second) : comparisonResult;
    }

    public boolean equals(double first, double second) {
        return Double.compare(this.first, first) == 0 && Double.compare(this.second, second) == 0;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Contract(pure = true)
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof DoublePair)) {
            return false;
        }

        DoublePair pair = (DoublePair) o;

        return Double.compare(first, pair.first) == 0 && Double.compare(second, pair.second) == 0;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return 32323 * Double.hashCode(first) + Double.hashCode(second);
    }

    @Override
    public String toString() {
        return toString(this);
    }

    @Nonnull
    public static String toString(@Nullable DoublePair pair) {
        return toString(DoublePair.class, pair);
    }

    @Nonnull
    public static <T extends DoublePair> String toString(@Nonnull Class<T> pairClass, @Nullable T pair) {
        return StringUtil.toString(pairClass, pair, false, "first", "second");
    }
}
