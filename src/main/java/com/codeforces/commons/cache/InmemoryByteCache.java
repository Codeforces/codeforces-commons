package com.codeforces.commons.cache;

import javax.annotation.Nonnull;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 29.03.12
 */
public class InmemoryByteCache extends ByteCache {
    private final Cache<String, byte[]> internalCache = InmemoryCache.newInstance();

    @Override
    public final boolean validate() {
        return internalCache.validate();
    }

    @Override
    public boolean contains(@Nonnull String section, @Nonnull String key) {
        ensureCacheSectionName(section);
        ensureCacheKeyName(key);
        return internalCache.contains(section, key);
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        ensureCacheSectionName(section);
        ensureCacheKeyName(key);
        internalCache.put(section, key, value);
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        ensureCacheSectionName(section);
        ensureCacheKeyName(key);
        internalCache.put(section, key, value, lifetimeMillis);
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        ensureCacheSectionName(section);
        ensureCacheKeyName(key);
        internalCache.putIfAbsent(section, key, value);
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        ensureCacheSectionName(section);
        ensureCacheKeyName(key);
        internalCache.putIfAbsent(section, key, value, lifetimeMillis);
    }

    @Override
    public byte[] get(@Nonnull String section, @Nonnull String key) {
        ensureCacheSectionName(section);
        ensureCacheKeyName(key);
        return internalCache.get(section, key);
    }

    @Override
    public boolean remove(@Nonnull String section, @Nonnull String key) {
        ensureCacheSectionName(section);
        ensureCacheKeyName(key);
        return internalCache.remove(section, key);
    }

    @Override
    public void clearSection(@Nonnull String section) {
        ensureCacheSectionName(section);
        internalCache.clearSection(section);
    }

    @Override
    public void clear() {
        internalCache.clear();
    }

    @Override
    public void close() {
        internalCache.close();
    }
}
