package com.codeforces.commons.collection;

import java.util.*;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 27.12.13
 */
@SuppressWarnings({"ReturnOfThis", "ReturnOfCollectionOrArrayField", "AssignmentToCollectionOrArrayFieldFromParameter"})
public class SortedSetBuilder<E> {
    private final SortedSet<E> sortedSet;

    public SortedSetBuilder() {
        this.sortedSet = new TreeSet<>();
    }

    public SortedSetBuilder(SortedSet<E> sortedSet) {
        this.sortedSet = sortedSet;
    }

    public SortedSetBuilder<E> add(E element) {
        sortedSet.add(element);
        return this;
    }

    public <A extends E> SortedSetBuilder<E> addAll(Collection<A> collection) {
        this.sortedSet.addAll(collection);
        return this;
    }

    public <A extends E> SortedSetBuilder<E> addAll(Enumeration<A> enumeration) {
        while (enumeration.hasMoreElements()) {
            this.sortedSet.add(enumeration.nextElement());
        }
        return this;
    }

    public <A extends E> SortedSetBuilder<E> addAll(Collection<A> collection, ElementFilter<A> filter) {
        for (A element : collection) {
            if (filter.matches(element)) {
                this.sortedSet.add(element);
            }
        }
        return this;
    }

    public <A extends E> SortedSetBuilder<E> addAll(Enumeration<A> enumeration, ElementFilter<A> filter) {
        while (enumeration.hasMoreElements()) {
            A element = enumeration.nextElement();
            if (filter.matches(element)) {
                this.sortedSet.add(element);
            }
        }
        return this;
    }

    public SortedSet<E> build() {
        return sortedSet;
    }

    public SortedSet<E> buildUnmodifiable() {
        return Collections.unmodifiableSortedSet(sortedSet);
    }

    public Enumeration<E> buildEnumeration() {
        return Collections.enumeration(sortedSet);
    }
}
