package com.codeforces.commons.collection;

import java.util.*;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 27.12.13
 */
@SuppressWarnings({"ReturnOfThis", "ReturnOfCollectionOrArrayField", "AssignmentToCollectionOrArrayFieldFromParameter"})
public class SetBuilder<E> {
    private final Set<E> set;

    public SetBuilder() {
        this.set = new HashSet<>();
    }

    public SetBuilder(Set<E> set) {
        this.set = set;
    }

    public SetBuilder<E> add(E element) {
        set.add(element);
        return this;
    }

    public <A extends E> SetBuilder<E> addAll(Collection<A> collection) {
        this.set.addAll(collection);
        return this;
    }

    public <A extends E> SetBuilder<E> addAll(Enumeration<A> enumeration) {
        while (enumeration.hasMoreElements()) {
            this.set.add(enumeration.nextElement());
        }
        return this;
    }

    public <A extends E> SetBuilder<E> addAll(Collection<A> collection, ElementFilter<A> filter) {
        for (A element : collection) {
            if (filter.matches(element)) {
                this.set.add(element);
            }
        }
        return this;
    }

    public <A extends E> SetBuilder<E> addAll(Enumeration<A> enumeration, ElementFilter<A> filter) {
        while (enumeration.hasMoreElements()) {
            A element = enumeration.nextElement();
            if (filter.matches(element)) {
                this.set.add(element);
            }
        }
        return this;
    }

    public Set<E> build() {
        return set;
    }

    public Set<E> buildUnmodifiable() {
        return Collections.unmodifiableSet(set);
    }

    public Enumeration<E> buildEnumeration() {
        return Collections.enumeration(set);
    }
}
