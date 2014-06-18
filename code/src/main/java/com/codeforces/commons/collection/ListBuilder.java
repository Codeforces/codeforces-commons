package com.codeforces.commons.collection;

import java.util.*;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 27.12.13
 */
@SuppressWarnings({"ReturnOfThis", "ReturnOfCollectionOrArrayField", "AssignmentToCollectionOrArrayFieldFromParameter"})
public class ListBuilder<E> {
    private final List<E> list;

    public ListBuilder() {
        this.list = new ArrayList<>();
    }

    public ListBuilder(List<E> list) {
        this.list = list;
    }

    public ListBuilder<E> add(E element) {
        list.add(element);
        return this;
    }

    public <A extends E> ListBuilder<E> addAll(Collection<A> collection) {
        this.list.addAll(collection);
        return this;
    }

    public <A extends E> ListBuilder<E> addAll(Enumeration<A> enumeration) {
        while (enumeration.hasMoreElements()) {
            this.list.add(enumeration.nextElement());
        }
        return this;
    }

    public <A extends E> ListBuilder<E> addAll(Collection<A> collection, ElementFilter<A> filter) {
        for (A element : collection) {
            if (filter.matches(element)) {
                this.list.add(element);
            }
        }
        return this;
    }

    public <A extends E> ListBuilder<E> addAll(Enumeration<A> enumeration, ElementFilter<A> filter) {
        while (enumeration.hasMoreElements()) {
            A element = enumeration.nextElement();
            if (filter.matches(element)) {
                this.list.add(element);
            }
        }
        return this;
    }

    public List<E> build() {
        return list;
    }

    public List<E> buildUnmodifiable() {
        return Collections.unmodifiableList(list);
    }

    public Enumeration<E> buildEnumeration() {
        return Collections.enumeration(list);
    }
}
