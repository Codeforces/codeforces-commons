package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 02.06.2015
 */
public class LongPair implements Comparable<LongPair> {
    private long first;
    private long second;

    public LongPair() {
    }

    public LongPair(long first, long second) {
        this.first = first;
        this.second = second;
    }

    public LongPair(@Nonnull LongPair pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    public long getFirst() {
        return first;
    }

    public void setFirst(long first) {
        this.first = first;
    }

    public long getSecond() {
        return second;
    }

    public void setSecond(long second) {
        this.second = second;
    }

    @SuppressWarnings({"ObjectEquality", "CompareToUsesNonFinalVariable"})
    @Override
    public int compareTo(@Nonnull LongPair pair) {
        int comparisonResult = Long.compare(first, pair.first);
        return comparisonResult == 0 ? Long.compare(second, pair.second) : comparisonResult;
    }

    public boolean equals(long first, long second) {
        return this.first == first && this.second == second;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Contract(pure = true)
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof LongPair)) {
            return false;
        }

        LongPair pair = (LongPair) o;

        return first == pair.first && second == pair.second;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return 31 * Long.hashCode(first) + Long.hashCode(second);
    }

    @Override
    public String toString() {
        return toString(this);
    }

    @Nonnull
    public static String toString(@Nullable LongPair pair) {
        return toString(LongPair.class, pair);
    }

    @Nonnull
    public static <T extends LongPair> String toString(@Nonnull Class<T> pairClass, @Nullable T pair) {
        return StringUtil.toString(pairClass, pair, false, "first", "second");
    }
}
