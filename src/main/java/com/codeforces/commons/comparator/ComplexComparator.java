package com.codeforces.commons.comparator;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 05.10.11
 */
public final class ComplexComparator<T> implements Comparator<T> {
    private final int comparatorCount;

    @Nonnull
    private final Comparator<T>[] comparators;

    @SuppressWarnings("unchecked")
    public ComplexComparator(@Nonnull List<Comparator<T>> comparators) {
        this.comparatorCount = comparators.size();
        this.comparators = comparators.toArray(new Comparator[comparatorCount]);
    }

    @SuppressWarnings("unchecked")
    public ComplexComparator(@Nonnull Comparator<T> comparator) {
        this.comparatorCount = 1;
        this.comparators = new Comparator[comparatorCount];
        this.comparators[0] = comparator;
    }

    @SuppressWarnings("unchecked")
    public ComplexComparator(@Nonnull Comparator<T> comparator1, @Nonnull Comparator<T> comparator2) {
        this.comparatorCount = 2;
        this.comparators = new Comparator[comparatorCount];
        this.comparators[0] = comparator1;
        this.comparators[1] = comparator2;
    }

    @SuppressWarnings("unchecked")
    public ComplexComparator(@Nonnull Comparator<T> comparator1, @Nonnull Comparator<T> comparator2,
                             @Nonnull Comparator<T> comparator3) {
        this.comparatorCount = 3;
        this.comparators = new Comparator[comparatorCount];
        this.comparators[0] = comparator1;
        this.comparators[1] = comparator2;
        this.comparators[2] = comparator3;
    }

    @SuppressWarnings("unchecked")
    public ComplexComparator(@Nonnull Comparator<T> comparator1, @Nonnull Comparator<T> comparator2,
                             @Nonnull Comparator<T> comparator3, @Nonnull Comparator<T> comparator4) {
        this.comparatorCount = 4;
        this.comparators = new Comparator[comparatorCount];
        this.comparators[0] = comparator1;
        this.comparators[1] = comparator2;
        this.comparators[2] = comparator3;
        this.comparators[3] = comparator4;
    }

    @SuppressWarnings("unchecked")
    public ComplexComparator(@Nonnull Comparator<T> comparator1, @Nonnull Comparator<T> comparator2,
                             @Nonnull Comparator<T> comparator3, @Nonnull Comparator<T> comparator4,
                             @Nonnull Comparator<T> comparator5) {
        this.comparatorCount = 5;
        this.comparators = new Comparator[comparatorCount];
        this.comparators[0] = comparator1;
        this.comparators[1] = comparator2;
        this.comparators[2] = comparator3;
        this.comparators[3] = comparator4;
        this.comparators[4] = comparator5;
    }

    @Override
    public int compare(T o1, T o2) {
        int comparisonResult = 0;

        for (int comparatorIndex = 0; comparatorIndex < comparatorCount; ++comparatorIndex) {
            if ((comparisonResult = comparators[comparatorIndex].compare(o1, o2)) != 0) {
                return comparisonResult;
            }
        }

        return comparisonResult;
    }
}
