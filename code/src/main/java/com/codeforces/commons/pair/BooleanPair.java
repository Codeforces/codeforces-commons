package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 02.06.2015
 */
public class BooleanPair implements Comparable<BooleanPair> {
    private boolean first;
    private boolean second;

    public BooleanPair() {
    }

    public BooleanPair(boolean first, boolean second) {
        this.first = first;
        this.second = second;
    }

    public BooleanPair(@Nonnull BooleanPair pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    public boolean getFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean getSecond() {
        return second;
    }

    public void setSecond(boolean second) {
        this.second = second;
    }

    @SuppressWarnings("CompareToUsesNonFinalVariable")
    @Override
    public int compareTo(@Nonnull BooleanPair pair) {
        int comparisonResult = Boolean.compare(first, pair.first);
        return comparisonResult == 0 ? Boolean.compare(second, pair.second) : comparisonResult;
    }

    public boolean equals(boolean first, boolean second) {
        return this.first == first && this.second == second;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Contract(pure = true)
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BooleanPair)) {
            return false;
        }

        BooleanPair pair = (BooleanPair) o;

        return first == pair.first && second == pair.second;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return 32323 * Boolean.hashCode(first) + Boolean.hashCode(second);
    }

    @Override
    public String toString() {
        return toString(this);
    }

    @Nonnull
    public static String toString(@Nullable BooleanPair pair) {
        return toString(BooleanPair.class, pair);
    }

    @Nonnull
    public static <T extends BooleanPair> String toString(@Nonnull Class<T> pairClass, @Nullable T pair) {
        return StringUtil.toString(pairClass, pair, false, "first", "second");
    }
}
