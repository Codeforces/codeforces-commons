package com.codeforces.commons.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 14.02.11
 */

/**
 * All methods of this class throws {@code {@link IllegalArgumentException }} if either section or key is {@code null}.
 */
//TODO timed entries in in-memory cache
public abstract class Cache<K, V> implements Closeable {
    private static final AtomicLong cacheIndex = new AtomicLong();

    private final long index;

    protected Cache() {
        this.index = cacheIndex.incrementAndGet();
    }

    protected long getIndex() {
        return index;
    }

    /**
     * Makes sure that storage is operational and returns its status:
     * {@code true} if cache is operational or {@code false} otherwise.
     *
     * @return cache status
     */
    public abstract boolean validate();

    /**
     * Checks that the storage has value for the given section and key.
     *
     * @param section storage section
     * @param key     storage key (unique for each section)
     * @return {@code true} iff sought-for value is presented in the storage
     */
    public abstract boolean contains(@Nonnull String section, @Nonnull K key);

    /**
     * Puts value into the storage using given section and key.
     * Replaces old value if exists.
     *
     * @param section storage section
     * @param key     storage key (unique for each section)
     * @param value   value to store
     * @throws IllegalArgumentException if value is {@code null}
     */
    public abstract void put(@Nonnull String section, @Nonnull K key, @Nonnull V value);

    /**
     * Puts value into the storage using given section and key.
     * Replaces old value if exists.
     * Value will be considered outdated
     * ({@code {@link #get(String, K) Cache.get(section, key)}} will return {@code null})
     * after {@code lifetimeMillis}.
     *
     * @param section        storage section
     * @param key            storage key (unique for each section)
     * @param value          value to store
     * @param lifetimeMillis value lifetime
     * @throws IllegalArgumentException if value is {@code null}
     */
    public abstract void put(@Nonnull String section, @Nonnull K key, @Nonnull V value, long lifetimeMillis);

    /**
     * Puts value into the storage using given section and key.
     * Does not overwrite existing value.
     *
     * @param section storage section
     * @param key     storage key (unique for each section)
     * @param value   value to store
     * @throws IllegalArgumentException if value is {@code null}
     */
    public abstract void putIfAbsent(@Nonnull String section, @Nonnull K key, @Nonnull V value);

    /**
     * Puts value into the storage using given section and key.
     * Does not overwrite existing value.
     * Value will be considered outdated
     * ({@code {@link #get(String, K) Cache.get(section, key)}} will return {@code null})
     * after {@code lifetimeMillis}.
     *
     * @param section        storage section
     * @param key            storage key (unique for each section)
     * @param value          value to store
     * @param lifetimeMillis value lifetime
     * @throws IllegalArgumentException if value is {@code null}
     */
    public abstract void putIfAbsent(
            @Nonnull String section, @Nonnull K key, @Nonnull V value, long lifetimeMillis);

    /**
     * Returns the value from the storage using given section and key.
     *
     * @param section storage section
     * @param key     storage key (unique for each section)
     * @return value iff it's presented in the storage and is consistent, otherwise returns {@code null}
     */
    @Nullable
    public abstract V get(@Nonnull String section, @Nonnull K key);

    /**
     * Removes value from the storage using given section and key.
     *
     * @param section storage section
     * @param key     storage key (unique for each section)
     * @return {@code true} iff storage contained specified value and value has been successfully deleted,
     *         otherwise returns {@code false}
     */
    public abstract boolean remove(@Nonnull String section, @Nonnull K key);

    /**
     * Removes all values from the specified section of the storage.
     *
     * @param section storage section
     */
    public abstract void clearSection(@Nonnull String section);

    /**
     * Removes all values from the storage.
     */
    public abstract void clear();

    /**
     * Closes this cache and releases any resources associated with it.
     * If the cache is already closed then invoking this method has no effect.
     */
    @Override
    public abstract void close();

    @Override
    protected final void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
