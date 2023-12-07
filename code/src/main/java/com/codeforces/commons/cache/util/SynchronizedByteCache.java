package com.codeforces.commons.cache.util;

import com.codeforces.commons.cache.ByteCache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Mike Mirzayanov
 */
class SynchronizedByteCache extends ByteCache {
    private final ByteCache cache;
    private final Lock lock = new ReentrantLock();

    SynchronizedByteCache(ByteCache cache) {
        ensureArguments(cache);

        this.cache = cache;
    }

    private static void ensureArguments(ByteCache cache) {
        if (cache == null) {
            throw new IllegalArgumentException("Argument 'cache' is 'null'.");
        }
    }

    @Override
    public boolean validate() {
        lock.lock();
        try {
            return cache.validate();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean contains(@Nonnull String section, @Nonnull String key) {
        lock.lock();
        try {
            return cache.contains(section, key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        lock.lock();
        try {
            cache.put(section, key, value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key,
                    @Nonnull byte[] value, long lifetimeMillis) {
        lock.lock();
        try {
            cache.put(section, key, value, lifetimeMillis);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        lock.lock();
        try {
            cache.putIfAbsent(section, key, value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key,
                            @Nonnull byte[] value, long lifetimeMillis) {
        lock.lock();
        try {
            cache.putIfAbsent(section, key, value, lifetimeMillis);
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    @Override
    public byte[] get(@Nonnull String section, @Nonnull String key) {
        lock.lock();
        try {
            return cache.get(section, key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(@Nonnull String section, @Nonnull String key) {
        lock.lock();
        try {
            return cache.remove(section, key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clearSection(@Nonnull String section) {
        lock.lock();
        try {
            cache.clearSection(section);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            cache.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        lock.lock();
        try {
            cache.close();
        } finally {
            lock.unlock();
        }
    }
}
