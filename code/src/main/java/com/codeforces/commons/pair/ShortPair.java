package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 13.05.2016
 */
public class ShortPair implements Comparable<ShortPair> {
    private short first;
    private short second;

    public ShortPair() {
    }

    public ShortPair(short first, short second) {
        this.first = first;
        this.second = second;
    }

    public ShortPair(@Nonnull ShortPair pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    public short getFirst() {
        return first;
    }

    public void setFirst(short first) {
        this.first = first;
    }

    public short getSecond() {
        return second;
    }

    public void setSecond(short second) {
        this.second = second;
    }

    @SuppressWarnings({"ObjectEquality", "CompareToUsesNonFinalVariable"})
    @Override
    public int compareTo(@Nonnull ShortPair pair) {
        int comparisonResult = Short.compare(first, pair.first);
        return comparisonResult == 0 ? Short.compare(second, pair.second) : comparisonResult;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Contract(pure = true)
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ShortPair)) {
            return false;
        }

        ShortPair pair = (ShortPair) o;

        return first == pair.first && second == pair.second;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return 31 * Short.hashCode(first) + Short.hashCode(second);
    }

    @Override
    public String toString() {
        return toString(this);
    }

    @Nonnull
    public static String toString(@Nullable ShortPair pair) {
        return toString(ShortPair.class, pair);
    }

    @Nonnull
    public static <T extends ShortPair> String toString(@Nonnull Class<T> pairClass, @Nullable T pair) {
        return StringUtil.toString(pairClass, pair, false, "first", "second");
    }
}
