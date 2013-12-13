package com.codeforces.commons.pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 11.07.13
 */
public class Pair<F extends Comparable<F>, S extends Comparable<S>>
        extends SimplePair<F, S> implements Comparable<Pair<F, S>> {
    public Pair() {
    }

    public Pair(@Nullable F first, @Nullable S second) {
        super(first, second);
    }

    public Pair(@Nonnull SimplePair<F, S> pair) {
        super(pair);
    }

    @SuppressWarnings("ObjectEquality")
    @Override
    public int compareTo(Pair<F, S> pair) {
        if (getFirst() != pair.getFirst()) {
            if (getFirst() == null) {
                return -1;
            }

            if (pair.getFirst() == null) {
                return 1;
            }

            int comparisonResult = getFirst().compareTo(pair.getFirst());
            if (comparisonResult != 0) {
                return comparisonResult;
            }
        }

        if (getSecond() != pair.getSecond()) {
            if (getSecond() == null) {
                return -1;
            }

            if (pair.getSecond() == null) {
                return 1;
            }

            int comparisonResult = getSecond().compareTo(pair.getSecond());
            if (comparisonResult != 0) {
                return comparisonResult;
            }
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return toString(this);
    }

    public static String toString(@Nullable Pair pair) {
        return toString(Pair.class, pair);
    }
}
