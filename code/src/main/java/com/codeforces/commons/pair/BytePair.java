package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 02.06.2015
 */
public class BytePair implements Comparable<BytePair> {
    private byte first;
    private byte second;

    public BytePair() {
    }

    public BytePair(byte first, byte second) {
        this.first = first;
        this.second = second;
    }

    public BytePair(@Nonnull BytePair pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    public byte getFirst() {
        return first;
    }

    public void setFirst(byte first) {
        this.first = first;
    }

    public byte getSecond() {
        return second;
    }

    public void setSecond(byte second) {
        this.second = second;
    }

    @SuppressWarnings("CompareToUsesNonFinalVariable")
    @Override
    public int compareTo(@Nonnull BytePair pair) {
        int comparisonResult = Byte.compare(first, pair.first);
        return comparisonResult == 0 ? Byte.compare(second, pair.second) : comparisonResult;
    }

    public boolean equals(byte first, byte second) {
        return this.first == first && this.second == second;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Contract(pure = true)
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BytePair)) {
            return false;
        }

        BytePair pair = (BytePair) o;

        return first == pair.first && second == pair.second;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return 32323 * Byte.hashCode(first) + Byte.hashCode(second);
    }

    @Override
    public String toString() {
        return toString(this);
    }

    @Nonnull
    public static String toString(@Nullable BytePair pair) {
        return toString(BytePair.class, pair);
    }

    @Nonnull
    public static <T extends BytePair> String toString(@Nonnull Class<T> pairClass, @Nullable T pair) {
        return StringUtil.toString(pairClass, pair, false, "first", "second");
    }
}
