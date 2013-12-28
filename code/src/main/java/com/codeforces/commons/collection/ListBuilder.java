package com.codeforces.commons.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    public List<E> build() {
        return list;
    }

    public List<E> buildUnmodifiable() {
        return Collections.unmodifiableList(list);
    }
}
