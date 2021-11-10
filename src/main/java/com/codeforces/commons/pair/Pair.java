package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 11.07.13
 */
@SuppressWarnings("ComparableImplementedButEqualsNotOverridden")
public class Pair<F extends Comparable<? super F>, S extends Comparable<? super S>>
        extends SimplePair<F, S> implements Comparable<Pair<F, S>> {
    public Pair() {
    }

    public Pair(@Nullable F first, @Nullable S second) {
        super(first, second);
    }

    public Pair(@Nonnull Pair<F, S> pair) {
        super(pair);
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
