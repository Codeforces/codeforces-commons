package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 11.07.13
 */
public class IntPair implements Comparable<IntPair> {
    private int first;
    private int second;

    public IntPair() {
    }

    public IntPair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public IntPair(@Nonnull IntPair pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
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
    public int compareTo(@Nonnull IntPair pair) {
        int comparisonResult = Integer.compare(first, pair.first);
        return comparisonResult == 0 ? Integer.compare(second, pair.second) : comparisonResult;
    }

    public boolean equals(int first, int second) {
        return this.first == first && this.second == second;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Contract(pure = true)
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof IntPair)) {
            return false;
        }

        IntPair pair = (IntPair) o;

        return first == pair.first && second == pair.second;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return 31 * first + second;
    }

    @Override
    public String toString() {
        return toString(this);
    }

    @Nonnull
    public static String toString(@Nullable IntPair pair) {
        return toString(IntPair.class, pair);
    }

    @Nonnull
    public static <T extends IntPair> String toString(@Nonnull Class<T> pairClass, @Nullable T pair) {
        return StringUtil.toString(pairClass, pair, false, "first", "second");
    }
}
