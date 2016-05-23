package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 13.05.2016
 */
public class FloatPair implements Comparable<FloatPair> {
    private float first;
    private float second;

    public FloatPair() {
    }

    public FloatPair(float first, float second) {
        this.first = first;
        this.second = second;
    }

    public FloatPair(@Nonnull FloatPair pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    public float getFirst() {
        return first;
    }

    public void setFirst(float first) {
        this.first = first;
    }

    public float getSecond() {
        return second;
    }

    public void setSecond(float second) {
        this.second = second;
    }

    @SuppressWarnings("CompareToUsesNonFinalVariable")
    @Override
    public int compareTo(@Nonnull FloatPair pair) {
        int comparisonResult = Float.compare(first, pair.first);
        return comparisonResult == 0 ? Float.compare(second, pair.second) : comparisonResult;
    }

    public boolean equals(float first, float second) {
        return Float.compare(this.first, first) == 0 && Float.compare(this.second, second) == 0;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Contract(pure = true)
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof FloatPair)) {
            return false;
        }

        FloatPair pair = (FloatPair) o;

        return Float.compare(first, pair.first) == 0 && Float.compare(second, pair.second) == 0;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return 31 * Float.hashCode(first) + Float.hashCode(second);
    }

    @Override
    public String toString() {
        return toString(this);
    }

    @Nonnull
    public static String toString(@Nullable FloatPair pair) {
        return toString(FloatPair.class, pair);
    }

    @Nonnull
    public static <T extends FloatPair> String toString(@Nonnull Class<T> pairClass, @Nullable T pair) {
        return StringUtil.toString(pairClass, pair, false, "first", "second");
    }
}
