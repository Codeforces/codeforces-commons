package com.codeforces.commons.collection;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 10.06.14
 */
public interface EntryFilter<K, V> {
    boolean matches(K key, V value);
}
