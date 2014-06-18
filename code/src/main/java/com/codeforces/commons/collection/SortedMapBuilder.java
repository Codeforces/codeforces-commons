package com.codeforces.commons.collection;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 27.12.13
 */
@SuppressWarnings({"ReturnOfThis", "ReturnOfCollectionOrArrayField, AssignmentToCollectionOrArrayFieldFromParameter"})
public class SortedMapBuilder<K, V> {
    private final SortedMap<K, V> sortedMap;

    public SortedMapBuilder() {
        this.sortedMap = new TreeMap<>();
    }

    public SortedMapBuilder(SortedMap<K, V> sortedMap) {
        this.sortedMap = sortedMap;
    }

    public SortedMapBuilder<K, V> put(K key, V value) {
        sortedMap.put(key, value);
        return this;
    }

    public <A extends K, B extends V> SortedMapBuilder<K, V> putAll(Map<A, B> map) {
        this.sortedMap.putAll(map);
        return this;
    }

    public <A extends K, B extends V> SortedMapBuilder<K, V> putAll(Map<A, B> map, EntryFilter<A, B> filter) {
        for (Map.Entry<A, B> entry : map.entrySet()) {
            if (filter.matches(entry.getKey(), entry.getValue())) {
                this.sortedMap.put(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    public SortedMap<K, V> build() {
        return sortedMap;
    }

    public SortedMap<K, V> buildUnmodifiable() {
        return Collections.unmodifiableSortedMap(sortedMap);
    }
}
