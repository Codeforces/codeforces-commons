package com.codeforces.commons.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 27.12.13
 */
@SuppressWarnings({"ReturnOfThis", "ReturnOfCollectionOrArrayField", "AssignmentToCollectionOrArrayFieldFromParameter"})
public class CollectionBuilder<E> {
    private final Collection<E> collection;

    public CollectionBuilder() {
        this.collection = new LinkedList<>();
    }

    public CollectionBuilder(Collection<E> collection) {
        this.collection = collection;
    }

    public CollectionBuilder<E> add(E element) {
        collection.add(element);
        return this;
    }

    public <A extends E> CollectionBuilder<E> addAll(Collection<A> collection) {
        this.collection.addAll(collection);
        return this;
    }

    public Collection<E> build() {
        return collection;
    }

    public Collection<E> buildUnmodifiable() {
        return Collections.unmodifiableCollection(collection);
    }
}
