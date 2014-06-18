package com.codeforces.commons.collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 27.12.13
 */
@SuppressWarnings({"ReturnOfThis", "ReturnOfCollectionOrArrayField, AssignmentToCollectionOrArrayFieldFromParameter"})
public class MapBuilder<K, V> {
    private final Map<K, V> map;

    public MapBuilder() {
        this.map = new HashMap<>();
    }

    public MapBuilder(Map<K, V> map) {
        this.map = map;
    }

    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public <A extends K, B extends V> MapBuilder<K, V> putAll(Map<A, B> map) {
        this.map.putAll(map);
        return this;
    }

    public <A extends K, B extends V> MapBuilder<K, V> putAll(Map<A, B> map, EntryFilter<A, B> filter) {
        for (Map.Entry<A, B> entry : map.entrySet()) {
            if (filter.matches(entry.getKey(), entry.getValue())) {
                this.map.put(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    public Map<K, V> build() {
        return map;
    }

    public Map<K, V> buildUnmodifiable() {
        return Collections.unmodifiableMap(map);
    }
}
