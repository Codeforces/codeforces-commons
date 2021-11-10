package com.codeforces.commons.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 02.11.13
 */
public class DummyByteCache extends ByteCache {
    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public boolean contains(@Nonnull String section, @Nonnull String key) {
        return false;
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        // No operations.
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        // No operations.
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        // No operations.
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        // No operations.
    }

    @Nullable
    @Override
    public byte[] get(@Nonnull String section, @Nonnull String key) {
        return null;
    }

    @Override
    public boolean remove(@Nonnull String section, @Nonnull String key) {
        return false;
    }

    @Override
    public void clearSection(@Nonnull String section) {
        // No operations.
    }

    @Override
    public void clear() {
        // No operations.
    }

    @Override
    public void close() {
        // No operations.
    }
}
