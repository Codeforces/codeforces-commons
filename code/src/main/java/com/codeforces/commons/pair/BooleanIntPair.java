package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 15.04.2017
 */
public class BooleanIntPair implements Comparable<BooleanIntPair> {
    private boolean first;
    private int second;

    public BooleanIntPair() {
    }

    public BooleanIntPair(boolean first, int second) {
        this.first = first;
        this.second = second;
    }

    public BooleanIntPair(@Nonnull BooleanIntPair pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    public boolean getFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    @SuppressWarnings("CompareToUsesNonFinalVariable")
    @Override
    public int compareTo(@Nonnull BooleanIntPair pair) {
        int comparisonResult = Boolean.compare(first, pair.first);
        return comparisonResult == 0 ? Integer.compare(second, pair.second) : comparisonResult;
    }

    public boolean equals(boolean first, int second) {
        return this.first == first && this.second == second;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Contract(pure = true)
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BooleanIntPair)) {
            return false;
        }

        BooleanIntPair pair = (BooleanIntPair) o;

        return first == pair.first && second == pair.second;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return 32323 * Boolean.hashCode(first) + second;
    }

    @Override
    public String toString() {
        return toString(this);
    }

    @Nonnull
    public static String toString(@Nullable BooleanIntPair pair) {
        return toString(BooleanIntPair.class, pair);
    }

    @Nonnull
    public static <T extends BooleanIntPair> String toString(@Nonnull Class<T> pairClass, @Nullable T pair) {
        return StringUtil.toString(pairClass, pair, false, "first", "second");
    }
}
