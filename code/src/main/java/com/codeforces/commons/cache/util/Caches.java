package com.codeforces.commons.cache.util;

import com.codeforces.commons.cache.ByteCache;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 16.02.11
 */
public final class Caches {
    private Caches() {
        throw new UnsupportedOperationException();
    }

    public static ByteCache newAsynchronousByteCache(ByteCache cache) {
        return new AsynchronousByteCache(cache, 0, 0);
    }

    public static ByteCache newAsynchronousByteCache(
            ByteCache cache, long validationTimeoutMillis, long disableOnFailMillis) {
        return new AsynchronousByteCache(cache, validationTimeoutMillis, disableOnFailMillis);
    }

    public static ByteCache newLocalAndRemoteByteCache(ByteCache localCache, ByteCache remoteCache) {
        return new LocalAndRemoteByteCache(localCache, remoteCache);
    }

    public static ByteCache newSilentByteCache(ByteCache cache) {
        return new SilentByteCache(cache);
    }

    public static ByteCache newLocalAndRemoteByteCache(
            ByteCache localCache, ByteCache remoteCache, boolean localCacheOptional, boolean remoteCacheOptional) {
        return new LocalAndRemoteByteCache(localCache, remoteCache, localCacheOptional, remoteCacheOptional);
    }

    public static ByteCache newLoggingByteCache(ByteCache cache) {
        return LoggingByteCache.newInstance(cache);
    }

    public static ByteCache newSynchronizedByteCache(ByteCache cache) {
        return new SynchronizedByteCache(cache);
    }
}
