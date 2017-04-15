package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 15.04.2017
 */
public class BooleanLongPair implements Comparable<BooleanLongPair> {
    private boolean first;
    private long second;

    public BooleanLongPair() {
    }

    public BooleanLongPair(boolean first, long second) {
        this.first = first;
        this.second = second;
    }

    public BooleanLongPair(@Nonnull BooleanLongPair pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    public boolean getFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public long getSecond() {
        return second;
    }

    public void setSecond(long second) {
        this.second = second;
    }

    @SuppressWarnings("CompareToUsesNonFinalVariable")
    @Override
    public int compareTo(@Nonnull BooleanLongPair pair) {
        int comparisonResult = Boolean.compare(first, pair.first);
        return comparisonResult == 0 ? Long.compare(second, pair.second) : comparisonResult;
    }

    public boolean equals(boolean first, long second) {
        return this.first == first && this.second == second;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Contract(pure = true)
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BooleanLongPair)) {
            return false;
        }

        BooleanLongPair pair = (BooleanLongPair) o;

        return first == pair.first && second == pair.second;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return 32323 * Boolean.hashCode(first) + Long.hashCode(second);
    }

    @Override
    public String toString() {
        return toString(this);
    }

    @Nonnull
    public static String toString(@Nullable BooleanLongPair pair) {
        return toString(BooleanLongPair.class, pair);
    }

    @Nonnull
    public static <T extends BooleanLongPair> String toString(@Nonnull Class<T> pairClass, @Nullable T pair) {
        return StringUtil.toString(pairClass, pair, false, "first", "second");
    }
}
