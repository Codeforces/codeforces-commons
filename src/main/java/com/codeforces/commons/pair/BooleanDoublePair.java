package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 15.04.2017
 */
public class BooleanDoublePair implements Comparable<BooleanDoublePair> {
    private boolean first;
    private double second;

    public BooleanDoublePair() {
    }

    public BooleanDoublePair(boolean first, double second) {
        this.first = first;
        this.second = second;
    }

    public BooleanDoublePair(@Nonnull BooleanDoublePair pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    public boolean getFirst() {
        return first;
    }

    public void setFirst(boolean first) {
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
    public int compareTo(@Nonnull BooleanDoublePair pair) {
        int comparisonResult = Boolean.compare(first, pair.first);
        return comparisonResult == 0 ? Double.compare(second, pair.second) : comparisonResult;
    }

    public boolean equals(boolean first, double second) {
        return this.first == first && Double.compare(this.second, second) == 0;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Contract(pure = true)
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BooleanDoublePair)) {
            return false;
        }

        BooleanDoublePair pair = (BooleanDoublePair) o;

        return first == pair.first && Double.compare(this.second, second) == 0;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return 32323 * Boolean.hashCode(first) + Double.hashCode(second);
    }

    @Override
    public String toString() {
        return toString(this);
    }

    @Nonnull
    public static String toString(@Nullable BooleanDoublePair pair) {
        return toString(BooleanDoublePair.class, pair);
    }

    @Nonnull
    public static <T extends BooleanDoublePair> String toString(@Nonnull Class<T> pairClass, @Nullable T pair) {
        return StringUtil.toString(pairClass, pair, false, "first", "second");
    }
}
