package com.codeforces.commons.cache.util;

import com.codeforces.commons.cache.ByteCache;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class SilentByteCache extends ByteCache {
    private static final Logger logger = Logger.getLogger(SilentByteCache.class);

    private final ByteCache byteCache;
    private final boolean logErrors;

    SilentByteCache(@Nonnull ByteCache byteCache, boolean logErrors) {
        this.byteCache = byteCache;
        this.logErrors = logErrors;
    }

    SilentByteCache(@Nonnull ByteCache byteCache) {
        this(byteCache, true);
    }

    @Override
    public boolean validate() {
        try {
            return byteCache.validate();
        } catch (RuntimeException e) {
            if (logErrors) {
                logger.warn("Can't execute validate().", e);
            }
            return false;
        }
    }

    @Override
    public boolean contains(@Nonnull String section, @Nonnull String key) {
        try {
            return byteCache.contains(section, key);
        } catch (RuntimeException e) {
            if (logErrors) {
                logger.warn(String.format("Can't execute contains('%s', '%s').", section, key), e);
            }
            return false;
        }
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        try {
            byteCache.put(section, key, value);
        } catch (RuntimeException e) {
            if (logErrors) {
                logger.warn(String.format("Can't execute put('%s', '%s', size=%d).", section, key, value.length), e);
            }
        }
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        try {
            byteCache.put(section, key, value, lifetimeMillis);
        } catch (RuntimeException e) {
            if (logErrors) {
                logger.warn(String.format("Can't execute put('%s', '%s', size=%d, %d).", section, key, value.length, lifetimeMillis), e);
            }
        }
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        try {
            byteCache.putIfAbsent(section, key, value);
        } catch (RuntimeException e) {
            if (logErrors) {
                logger.warn(String.format("Can't execute putIfAbsent('%s', '%s', size=%d).", section, key, value.length), e);
            }
        }
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        try {
            byteCache.putIfAbsent(section, key, value, lifetimeMillis);
        } catch (RuntimeException e) {
            if (logErrors) {
                logger.warn(String.format("Can't execute putIfAbsent('%s', '%s', size=%d, %d).", section, key, value.length, lifetimeMillis), e);
            }
        }
    }

    @Nullable
    @Override
    public byte[] get(@Nonnull String section, @Nonnull String key) {
        try {
            return byteCache.get(section, key);
        } catch (RuntimeException e) {
            if (logErrors) {
                logger.warn(String.format("Can't execute get('%s', '%s').", section, key), e);
            }
        }

        return null;
    }

    @Override
    public boolean remove(@Nonnull String section, @Nonnull String key) {
        return byteCache.remove(section, key);
    }

    @Override
    public void clearSection(@Nonnull String section) {
        byteCache.clearSection(section);
    }

    @Override
    public void clear() {
        byteCache.clear();
    }

    @Override
    public void close() {
        try {
            byteCache.close();
            byteCache = null;
        } catch (RuntimeException e) {
            if (logErrors) {
                logger.warn("Can't execute close().", e);
            }
        }
    }
}
