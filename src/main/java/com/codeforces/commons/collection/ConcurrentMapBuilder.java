package com.codeforces.commons.collection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 27.12.13
 */
@SuppressWarnings({"ReturnOfThis", "ReturnOfCollectionOrArrayField, AssignmentToCollectionOrArrayFieldFromParameter"})
public class ConcurrentMapBuilder<K, V> {
    private final ConcurrentMap<K, V> concurrentMap;

    public ConcurrentMapBuilder() {
        this.concurrentMap = new ConcurrentHashMap<>();
    }

    public ConcurrentMapBuilder(ConcurrentMap<K, V> concurrentMap) {
        this.concurrentMap = concurrentMap;
    }

    public ConcurrentMapBuilder<K, V> put(K key, V value) {
        concurrentMap.put(key, value);
        return this;
    }

    public <A extends K, B extends V> ConcurrentMapBuilder<K, V> putAll(Map<A, B> map) {
        this.concurrentMap.putAll(map);
        return this;
    }

    public <A extends K, B extends V> ConcurrentMapBuilder<K, V> putAll(Map<A, B> map, EntryFilter<A, B> filter) {
        for (Map.Entry<A, B> entry : map.entrySet()) {
            if (filter.matches(entry.getKey(), entry.getValue())) {
                this.concurrentMap.put(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    public ConcurrentMap<K, V> build() {
        return concurrentMap;
    }
}
