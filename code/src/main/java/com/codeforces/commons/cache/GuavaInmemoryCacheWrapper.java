package com.codeforces.commons.cache;

import com.codeforces.commons.text.StringUtil;
import com.google.common.cache.CacheBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 17.12.14
 */
public class GuavaInmemoryCacheWrapper<K, V> extends Cache<K, V> {
    private final com.google.common.cache.Cache<CacheKeyWrapper<K>, V> internalCache;

    private final long lifetimeMillis;
    private final boolean persistent;

    public GuavaInmemoryCacheWrapper() {
        this(-1L, false);
    }

    public GuavaInmemoryCacheWrapper(boolean useSoftReferences) {
        this(-1L, useSoftReferences);
    }

    public GuavaInmemoryCacheWrapper(long lifetimeMillis) {
        this(lifetimeMillis, false);
    }

    public GuavaInmemoryCacheWrapper(long lifetimeMillis, boolean useSoftReferences) {
        this.lifetimeMillis = lifetimeMillis;

        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.<CacheKeyWrapper<K>, V>newBuilder()
                .concurrencyLevel(2 * Runtime.getRuntime().availableProcessors());

        if (lifetimeMillis < 0L) {
            this.persistent = true;
        } else {
            cacheBuilder.expireAfterWrite(lifetimeMillis, TimeUnit.MILLISECONDS);
            this.persistent = false;
        }

        if (useSoftReferences) {
            cacheBuilder.softValues();
        }

        this.internalCache = cacheBuilder.build();
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public boolean contains(@Nonnull String section, @Nonnull K key) {
        return get(section, key) != null;
    }

    @Override
    public void put(@Nonnull String section, @Nonnull K key, @Nonnull V value) {
        if (!persistent) {
            throw new UnsupportedOperationException("Can't put persistent value into non-persistent cache.");
        }

        internalCache.put(new CacheKeyWrapper<>(section, key), value);
    }

    @Override
    public void put(@Nonnull String section, @Nonnull K key, @Nonnull V value, long lifetimeMillis) {
        if (persistent) {
            throw new UnsupportedOperationException("Can't put non-persistent value into persistent cache.");
        }

        if (this.lifetimeMillis != lifetimeMillis) {
            throw new IllegalArgumentException("Argument 'lifetimeMillis' is not equal to cache setting.");
        }

        internalCache.put(new CacheKeyWrapper<>(section, key), value);
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull K key, @Nonnull V value) {
        throw new UnsupportedOperationException("Cache does not support putIfAbsent(section, key, value) operation.");
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull K key, @Nonnull V value, long lifetimeMillis) {
        throw new UnsupportedOperationException(
                "Cache does not support putIfAbsent(section, key, value, lifetimeMillis) operation."
        );
    }

    @Nullable
    @Override
    public V get(@Nonnull String section, @Nonnull K key) {
        return internalCache.getIfPresent(new CacheKeyWrapper<>(section, key));
    }

    @Override
    public boolean remove(@Nonnull String section, @Nonnull K key) {
        return internalCache.asMap().remove(new CacheKeyWrapper<>(section, key)) != null;
    }

    @Override
    public void clearSection(@Nonnull String section) {
        throw new UnsupportedOperationException("Cache does not support clearSection(section) operation.");
    }

    @Override
    public void clear() {
        internalCache.invalidateAll();
    }

    @Override
    public void close() {
        internalCache.invalidateAll();
    }

    private static final class CacheKeyWrapper<K> {
        private final String section;
        private final K key;
        private final int hashCode;

        private CacheKeyWrapper(@Nonnull String section, @Nonnull K key) {
            if (StringUtil.isEmpty(section)) {
                throw new IllegalArgumentException("Argument 'section' can't be 'null' or empty.");
            }

            if (StringUtil.isEmpty(section)) {
                throw new IllegalArgumentException("Argument 'key' can't be 'null' or empty.");
            }

            this.section = section;
            this.key = key;

            int hash = this.section.hashCode();
            hash = 32323 * hash + this.key.hashCode();
            this.hashCode = hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CacheKeyWrapper cacheKeyWrapper = (CacheKeyWrapper) o;

            return key.equals(cacheKeyWrapper.key) && section.equals(cacheKeyWrapper.section);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
