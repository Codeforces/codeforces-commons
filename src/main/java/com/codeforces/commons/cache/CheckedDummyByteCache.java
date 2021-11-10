package com.codeforces.commons.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 02.11.13
 */
public class CheckedDummyByteCache extends ByteCache {
    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public boolean contains(@Nonnull String section, @Nonnull String key) {
        ensureCacheSectionName(section);
        ensureCacheKeyName(key);
        return false;
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        ensureCacheSectionName(section);
        ensureCacheKeyName(key);
        ensureValue(section, key, value);
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        ensureCacheSectionName(section);
        ensureCacheKeyName(key);
        ensureValue(section, key, value);
        ensureLifetimeMillis(section, key, lifetimeMillis);
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        ensureCacheSectionName(section);
        ensureCacheKeyName(key);
        ensureValue(section, key, value);
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        ensureCacheSectionName(section);
        ensureCacheKeyName(key);
        ensureValue(section, key, value);
        ensureLifetimeMillis(section, key, lifetimeMillis);
    }

    @Nullable
    @Override
    public byte[] get(@Nonnull String section, @Nonnull String key) {
        ensureCacheSectionName(section);
        ensureCacheKeyName(key);
        return null;
    }

    @Override
    public boolean remove(@Nonnull String section, @Nonnull String key) {
        ensureCacheSectionName(section);
        ensureCacheKeyName(key);
        return false;
    }

    @Override
    public void clearSection(@Nonnull String section) {
        ensureCacheSectionName(section);
    }

    @Override
    public void clear() {
        // No operations.
    }

    @Override
    public void close() {
        // No operations.
    }

    private static void ensureValue(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException(String.format(
                    "Argument 'value' can't be 'null' (section='%s', key='%s').", section, key
            ));
        }
    }

    private static void ensureLifetimeMillis(@Nonnull String section, @Nonnull String key, long lifetimeMillis) {
        if (lifetimeMillis < 1) {
            throw new IllegalArgumentException(String.format(
                    "Argument 'lifetimeMillis' must be a positive long integer (section='%s', key='%s').", section, key
            ));
        }
    }
}
