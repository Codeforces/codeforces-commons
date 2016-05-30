package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 11.07.13
 */
public class Pair<F extends Comparable<? super F>, S extends Comparable<? super S>> implements Comparable<Pair<F, S>> {
    @Nullable
    private F first;

    @Nullable
    private S second;

    public Pair() {
    }

    public Pair(@Nullable F first, @Nullable S second) {
        this.first = first;
        this.second = second;
    }

    public Pair(@Nonnull Pair<F, S> pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    @Nullable
    public F getFirst() {
        return first;
    }

    public void setFirst(@Nullable F first) {
        this.first = first;
    }

    @Nullable
    public S getSecond() {
        return second;
    }

    public void setSecond(@Nullable S second) {
        this.second = second;
    }

    @SuppressWarnings({"ObjectEquality", "CompareToUsesNonFinalVariable"})
    @Override
    public int compareTo(@Nonnull Pair<F, S> pair) {
        if (first != pair.first) {
            if (first == null) {
                return -1;
            }

            if (pair.first == null) {
                return 1;
            }

            int comparisonResult = first.compareTo(pair.first);
            if (comparisonResult != 0) {
                return comparisonResult;
            }
        }

        if (second != pair.second) {
            if (second == null) {
                return -1;
            }

            if (pair.second == null) {
                return 1;
            }

            int comparisonResult = second.compareTo(pair.second);
            if (comparisonResult != 0) {
                return comparisonResult;
            }
        }

        return 0;
    }

    public boolean equals(@Nullable F first, @Nullable S second) {
        return (this.first == null ? first == null : this.first.equals(first))
                && (this.second == null ? second == null : this.second.equals(second));
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Pair)) {
            return false;
        }

        Pair pair = (Pair) o;

        return (first == null ? pair.first == null : first.equals(pair.first))
                && (second == null ? pair.second == null : second.equals(pair.second));
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        int result = first == null ? 0 : first.hashCode();
        result = 32323 * result + (second == null ? 0 : second.hashCode());
        return result;
    }


    @Override
    public String toString() {
        return toString(this);
    }

    @Nonnull
    public static String toString(@Nullable Pair pair) {
        return toString(Pair.class, pair);
    }

    @Nonnull
    public static <T extends Pair> String toString(@Nonnull Class<T> pairClass, @Nullable T pair) {
        return StringUtil.toString(pairClass, pair, false, "first", "second");
    }
}
