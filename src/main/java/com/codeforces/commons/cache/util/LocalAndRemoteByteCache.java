package com.codeforces.commons.cache.util;

import com.codeforces.commons.cache.ByteCache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 14.02.11
 */
class LocalAndRemoteByteCache extends ByteCache {
    private final ByteCache localCache;
    private final ByteCache remoteCache;
    private final boolean localCacheOptional;
    private final boolean remoteCacheOptional;

    LocalAndRemoteByteCache(ByteCache localCache, ByteCache remoteCache) {
        this(localCache, remoteCache, false, false);
    }

    LocalAndRemoteByteCache(
            ByteCache localCache, ByteCache remoteCache, boolean localCacheOptional, boolean remoteCacheOptional) {
        this.localCache = localCache;
        this.remoteCache = remoteCache;
        this.localCacheOptional = localCacheOptional;
        this.remoteCacheOptional = remoteCacheOptional;
        if (!validate()) {
            throw new IllegalArgumentException("Can't validate cache.");
        }
    }

    @Override
    public final boolean validate() {
        return internalValidate(localCache, localCacheOptional) && internalValidate(remoteCache, remoteCacheOptional);
    }

    private static boolean internalValidate(ByteCache cache, boolean optional) {
        return optional || cache != null && cache.validate();
    }

    @Override
    public boolean contains(@Nonnull String section, @Nonnull String key) {
        boolean localResult = internalContains(localCache, section, key, localCacheOptional);
        if (localResult) {
            return true;
        } else {
            byte[] remoteValue = internalGet(remoteCache, section, key, remoteCacheOptional);
            if (remoteValue == null) {
                return false;
            } else {
                internalPut(localCache, section, key, remoteValue, localCacheOptional);
                return true;
            }
        }
    }

    private static boolean internalContains(ByteCache cache, String section, String key, boolean optional) {
        try {
            if (cache != null) {
                return cache.contains(section, key);
            } else if (optional) {
                return false;
            } else {
                throw new IllegalStateException("ByteCache is invalid.");
            }
        } catch (RuntimeException e) {
            if (optional) {
                return false;
            } else {
                throw new IllegalStateException("ByteCache is invalid.", e);
            }
        }
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        internalPut(localCache, section, key, value, localCacheOptional);
        internalPut(remoteCache, section, key, value, remoteCacheOptional);
    }

    private static void internalPut(ByteCache cache, String section, String key, byte[] value, boolean optional) {
        try {
            if (cache != null) {
                cache.put(section, key, value);
            } else if (!optional) {
                throw new IllegalStateException("ByteCache is invalid.");
            }
        } catch (RuntimeException e) {
            if (!optional) {
                throw new IllegalStateException("ByteCache is invalid.", e);
            }
        }
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        internalPut(localCache, section, key, value, localCacheOptional, lifetimeMillis);
        internalPut(remoteCache, section, key, value, remoteCacheOptional, lifetimeMillis);
    }

    private static void internalPut(
            ByteCache cache, String section, String key, byte[] value, boolean optional, long lifetimeMillis) {
        try {
            if (cache != null) {
                cache.put(section, key, value, lifetimeMillis);
            } else if (!optional) {
                throw new IllegalStateException("ByteCache is invalid.");
            }
        } catch (RuntimeException e) {
            if (!optional) {
                throw new IllegalStateException("ByteCache is invalid.", e);
            }
        }
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        internalPutIfAbsent(localCache, section, key, value, localCacheOptional);
        internalPutIfAbsent(remoteCache, section, key, value, remoteCacheOptional);
    }

    private static void internalPutIfAbsent(ByteCache cache, String section, String key, byte[] value, boolean optional) {
        try {
            if (cache != null) {
                cache.putIfAbsent(section, key, value);
            } else if (!optional) {
                throw new IllegalStateException("ByteCache is invalid.");
            }
        } catch (RuntimeException e) {
            if (!optional) {
                throw new IllegalStateException("ByteCache is invalid.", e);
            }
        }
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        internalPutIfAbsent(localCache, section, key, value, localCacheOptional, lifetimeMillis);
        internalPutIfAbsent(remoteCache, section, key, value, remoteCacheOptional, lifetimeMillis);
    }

    private static void internalPutIfAbsent(
            ByteCache cache, String section, String key, byte[] value, boolean optional, long lifetimeMillis) {
        try {
            if (cache != null) {
                cache.putIfAbsent(section, key, value, lifetimeMillis);
            } else if (!optional) {
                throw new IllegalStateException("ByteCache is invalid.");
            }
        } catch (RuntimeException e) {
            if (!optional) {
                throw new IllegalStateException("ByteCache is invalid.", e);
            }
        }
    }

    @Nullable
    @Override
    public byte[] get(@Nonnull String section, @Nonnull
    String key) {
        byte[] localValue = internalGet(localCache, section, key, localCacheOptional);
        if (localValue == null) {
            byte[] remoteValue = internalGet(remoteCache, section, key, remoteCacheOptional);
            if (remoteValue != null) {
                internalPut(localCache, section, key, remoteValue, localCacheOptional);
            }
            return remoteValue;
        } else {
            return localValue;
        }
    }

    @Nullable
    private static byte[] internalGet(ByteCache cache, String section, String key, boolean optional) {
        try {
            if (cache != null) {
                return cache.get(section, key);
            } else if (optional) {
                return null;
            } else {
                throw new IllegalStateException("ByteCache is invalid.");
            }
        } catch (RuntimeException e) {
            if (optional) {
                return null;
            } else {
                throw new IllegalStateException("ByteCache is invalid.", e);
            }
        }
    }

    @Override
    public boolean remove(@Nonnull String section, @Nonnull String key) {
        boolean remoteResult = internalRemove(remoteCache, section, key, remoteCacheOptional);
        boolean localResult = internalRemove(localCache, section, key, localCacheOptional);
        return localResult || remoteResult;
    }

    private static boolean internalRemove(ByteCache cache, String section, String key, boolean optional) {
        try {
            if (cache != null) {
                return cache.remove(section, key);
            } else if (optional) {
                return false;
            } else {
                throw new IllegalStateException("ByteCache is invalid.");
            }
        } catch (RuntimeException e) {
            if (optional) {
                return false;
            } else {
                throw new IllegalStateException("ByteCache is invalid.", e);
            }
        }
    }

    @Override
    public void clearSection(@Nonnull String section) {
        remoteCache.clearSection(section);
        localCache.clearSection(section);
    }

    @Override
    public void clear() {
        remoteCache.clear();
        localCache.clear();
    }

    @Override
    public void close() {
        remoteCache.close();
        localCache.close();
    }
}
