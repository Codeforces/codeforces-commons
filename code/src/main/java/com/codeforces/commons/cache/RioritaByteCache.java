package com.codeforces.commons.cache;

import com.codeforces.riorita.engine.Engine;
import com.codeforces.riorita.engine.RioritaEngine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author MikeMirzayanov (mirzayanovmr@gmail.com)
 */
@SuppressWarnings("unused")
public class RioritaByteCache extends ByteCache {
    private static final long INFINITE_TIME_MILLIS = TimeUnit.DAYS.toMillis(3650);
    private final Engine engine;

    public RioritaByteCache(@Nonnull File directory) {
        //noinspection ResultOfMethodCallIgnored
        directory.mkdirs();
        if (!directory.isDirectory()) {
            throw new RuntimeException("RioritaByteCache expects '" + directory + "' to be a directory.");
        }

        this.engine = new RioritaEngine(directory);
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public boolean contains(@Nonnull String section, @Nonnull String key) {
        return engine.has(section, key);
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        engine.put(section, key, value, INFINITE_TIME_MILLIS, true);
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        engine.put(section, key, value, lifetimeMillis, true);
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        engine.put(section, key, value, INFINITE_TIME_MILLIS, false);
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        engine.put(section, key, value, lifetimeMillis, false);
    }

    @Nullable
    @Override
    public byte[] get(@Nonnull String section, @Nonnull String key) {
        return engine.get(section, key);
    }

    @Override
    public boolean remove(@Nonnull String section, @Nonnull String key) {
        return engine.erase(section, key);
    }

    @Override
    public void clearSection(@Nonnull String section) {
        engine.erase(section);
    }

    @Override
    public void clear() {
        engine.clear();
    }

    @Override
    public void close() {
        // No operations.
    }
}
