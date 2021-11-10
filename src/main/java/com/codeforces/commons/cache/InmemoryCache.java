package com.codeforces.commons.cache;

import com.codeforces.commons.process.ReadWriteEvent;
import com.codeforces.commons.process.ThreadUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Important! Any method should lock section in first case
 * and only then lock cache (if both locks are needed) to avoid deadlocks.
 *
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 29.03.2012
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class InmemoryCache<K, V> extends Cache<K, V> {
    private static final Logger logger = Logger.getLogger(InmemoryCache.class);

    private final ReadWriteEvent cacheEvent = new ReadWriteEvent();
    private final ConcurrentMap<String, ReadWriteEvent> eventBySection = new ConcurrentHashMap<>();

    private final Map<String, Map<K, CacheEntry<V>>> cacheEntryByKeyBySection = new HashMap<>();
    private final Map<String, Queue<CacheEntryExpirationInfo<K, V>>> cacheEntryExpirationInfosBySection = new HashMap<>();

    private final AtomicBoolean stopBackgroundThreads = new AtomicBoolean();

    private final Thread cacheEntryRemovalThread = new Thread(() -> {
        while (!stopBackgroundThreads.get()) {
            try {
                if (hasExpirationInfos()) {
                    CacheEntryExpirationInfo<K, V> expirationInfo = Objects.requireNonNull(getFirstExpirationInfo());
                    long currentTimeMillis = System.currentTimeMillis();
                    long expirationTimeMillis = expirationInfo.getExpirationTimeMillis();

                    if (currentTimeMillis < expirationTimeMillis) {
                        ThreadUtil.sleep(expirationTimeMillis - currentTimeMillis);
                    } else {
                        removeCacheEntryWithLifetimeIfNeeded(expirationInfo.getSection(), expirationInfo);
                    }
                } else {
                    ThreadUtil.sleep(Long.MAX_VALUE);
                }
            } catch (RuntimeException e) {
                logger.error(String.format(
                        "Got unexpected exception while removing expired entries in '%s' #%d.",
                        InmemoryCache.class, getIndex()
                ), e);
            }
        }
    });

    public static <K, V> InmemoryCache<K, V> newInstance() {
        InmemoryCache<K, V> inmemoryCache = new InmemoryCache<>();
        inmemoryCache.cacheEntryRemovalThread.setDaemon(true);
        inmemoryCache.cacheEntryRemovalThread.start();
        return inmemoryCache;
    }

    private InmemoryCache() {
    }

    @Contract(pure = true)
    @Override
    public final boolean validate() {
        return !stopBackgroundThreads.get();
    }

    @Override
    public boolean contains(@Nonnull String section, @Nonnull K key) {
        return readLockSectionAndReturnResult(section, () -> get(section, key) != null);
    }

    @Override
    public void put(@Nonnull String section, @Nonnull K key, @Nonnull V value) {
        writeLockSectionAndExecute(section, () -> ensureAndReturnCacheSection(section).put(
                key, new CacheEntry<>(value)
        ));
    }

    @Override
    public void put(@Nonnull String section, @Nonnull K key, @Nonnull V value, long lifetimeMillis) {
        Map<K, CacheEntry<V>> cacheEntryByKey = ensureAndReturnCacheSection(section);
        Queue<CacheEntryExpirationInfo<K, V>> expirationInfos = ensureAndReturnExpirationInfosSection(section);
        addCacheEntryWithLifetime(section, key, value, lifetimeMillis, cacheEntryByKey, expirationInfos);
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull K key, @Nonnull V value) {
        writeLockSectionAndExecute(section, () -> {
            Map<K, CacheEntry<V>> cacheEntryByKey = ensureAndReturnCacheSection(section);
            if (!cacheEntryByKey.containsKey(key)) {
                cacheEntryByKey.put(key, new CacheEntry<>(value));
            }
        });
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull K key, @Nonnull V value, long lifetimeMillis) {
        Map<K, CacheEntry<V>> cacheEntryByKey = ensureAndReturnCacheSection(section);
        if (!cacheEntryByKey.containsKey(key)) {
            addCacheEntryWithLifetimeIfAbsent(section, key, value, lifetimeMillis, cacheEntryByKey);
        }
    }

    @Override
    public V get(@Nonnull String section, @Nonnull K key) {
        return readLockSectionAndReturnResult(section, () -> {
            CacheEntry<V> cacheEntry = ensureAndReturnCacheSection(section).get(key);
            return cacheEntry == null ? null : cacheEntry.getValueOrNull();
        });
    }

    @Override
    public boolean remove(@Nonnull String section, @Nonnull K key) {
        return writeLockSectionAndReturnResult(section, () -> ensureAndReturnCacheSection(section).remove(key) != null);
    }

    @Override
    public void clearSection(@Nonnull String section) {
        writeLockSectionAndExecute(section, () -> {
            ensureAndReturnCacheSection(section).clear();
            ensureAndReturnExpirationInfosSection(section).clear();
        });
    }

    @Override
    public void clear() {
        writeLockCacheAndExecute(() -> {
            cacheEntryExpirationInfosBySection.clear();
            cacheEntryByKeyBySection.clear();
        });
    }

    @Override
    public void close() {
        if (!stopBackgroundThreads.getAndSet(true)) {
            cacheEntryRemovalThread.interrupt();
        }
    }

    private Map<K, CacheEntry<V>> ensureAndReturnCacheSection(String section) {
        Map<K, CacheEntry<V>> cacheEntryByKey = getCacheSection(section);
        return cacheEntryByKey == null ? createCacheSection(section) : cacheEntryByKey;
    }

    private Map<K, CacheEntry<V>> getCacheSection(String section) {
        return readLockCacheAndReturnResult(() -> cacheEntryByKeyBySection.get(section));
    }

    private Map<K, CacheEntry<V>> createCacheSection(String section) {
        return writeLockCacheAndReturnResult(() -> cacheEntryByKeyBySection.computeIfAbsent(
                section, __ -> new HashMap<>()
        ));
    }

    private Queue<CacheEntryExpirationInfo<K, V>> ensureAndReturnExpirationInfosSection(String section) {
        Queue<CacheEntryExpirationInfo<K, V>> expirationInfos = getExpirationInfosBySection(section);
        return expirationInfos == null ? createExpirationInfosSection(section) : expirationInfos;
    }

    private Queue<CacheEntryExpirationInfo<K, V>> getExpirationInfosBySection(String section) {
        return readLockCacheAndReturnResult(() -> cacheEntryExpirationInfosBySection.get(section));
    }

    private Queue<CacheEntryExpirationInfo<K, V>> createExpirationInfosSection(String section) {
        return writeLockCacheAndReturnResult(() -> cacheEntryExpirationInfosBySection.computeIfAbsent(
                section, __ -> new PriorityQueue<>()
        ));
    }

    private void addCacheEntryWithLifetime(
            String section, K key, V value, long lifetimeMillis,
            Map<K, CacheEntry<V>> valueByKey, Queue<CacheEntryExpirationInfo<K, V>> expirationInfos) {
        writeLockSectionAndExecute(section, () -> {
            long expirationTimeMillis = System.currentTimeMillis() + lifetimeMillis;

            CacheEntry<V> cacheEntry = new CacheEntry<>(value, expirationTimeMillis);
            valueByKey.put(key, cacheEntry);
            expirationInfos.add(new CacheEntryExpirationInfo<>(section, key, cacheEntry, expirationTimeMillis));

            cacheEntryRemovalThread.interrupt();
        });
    }

    private void addCacheEntryWithLifetimeIfAbsent(
            String section, K key, V value, long lifetimeMillis, Map<K, CacheEntry<V>> cacheEntryByKey) {
        writeLockSectionAndExecute(section, () -> {
            if (!cacheEntryByKey.containsKey(key)) {
                Queue<CacheEntryExpirationInfo<K, V>> expirationInfos = ensureAndReturnExpirationInfosSection(section);
                long expirationTimeMillis = System.currentTimeMillis() + lifetimeMillis;

                CacheEntry<V> cacheEntry = new CacheEntry<>(value, expirationTimeMillis);
                cacheEntryByKey.put(key, cacheEntry);
                expirationInfos.add(new CacheEntryExpirationInfo<>(section, key, cacheEntry, expirationTimeMillis));

                cacheEntryRemovalThread.interrupt();
            }
        });
    }

    private boolean hasExpirationInfos() {
        String[] expirationInfoSections = getExpirationInfoSections();
        int sectionIndex = expirationInfoSections.length;

        while (--sectionIndex >= 0) {
            if (hasExpirationInfosInSection(expirationInfoSections[sectionIndex])) {
                return true;
            }
        }

        return false;
    }

    private boolean hasExpirationInfosInSection(String section) {
        return readLockSectionAndReturnResult(section, () -> !ensureAndReturnExpirationInfosSection(section).isEmpty());
    }

    @Nullable
    private CacheEntryExpirationInfo<K, V> getFirstExpirationInfo() {
        CacheEntryExpirationInfo<K, V> firstExpirationInfo = null;

        String[] expirationInfoSections = getExpirationInfoSections();
        for (String section : expirationInfoSections) {
            CacheEntryExpirationInfo<K, V> expirationInfo = getFirstExpirationInfoInSection(section);

            if (firstExpirationInfo == null || expirationInfo != null
                    && expirationInfo.getExpirationTimeMillis() < firstExpirationInfo.getExpirationTimeMillis()) {
                firstExpirationInfo = expirationInfo;
            }
        }

        return firstExpirationInfo;
    }

    private CacheEntryExpirationInfo<K, V> getFirstExpirationInfoInSection(String section) {
        return readLockSectionAndReturnResult(section, () -> ensureAndReturnExpirationInfosSection(section).peek());
    }

    private String[] getExpirationInfoSections() {
        return readLockCacheAndReturnResult(() -> {
            Set<String> sections = cacheEntryExpirationInfosBySection.keySet();
            return sections.toArray(new String[0]);
        });
    }

    private void removeCacheEntryWithLifetimeIfNeeded(String section, CacheEntryExpirationInfo<K, V> expirationInfo) {
        writeLockSectionAndExecute(section, () -> {
            Map<K, CacheEntry<V>> cacheEntryByKey = ensureAndReturnCacheSection(section);
            CacheEntry<V> cacheEntry = cacheEntryByKey.get(expirationInfo.getKey());
            if (cacheEntry != null && cacheEntry.getExpirationTimeMillis() != -1
                    && cacheEntry.equals(expirationInfo.getEntry())) {
                cacheEntryByKey.remove(expirationInfo.getKey());
            }
            ensureAndReturnExpirationInfosSection(section).poll();
        });
    }

    private ReadWriteEvent getSectionEvent(String section) {
        return eventBySection.computeIfAbsent(section, __ -> new ReadWriteEvent());
    }

    private void readLockSectionAndExecute(String section, Runnable invocation) {
        lockAndExecute(invocation, getSectionEvent(section).getReadLock());
    }

    private void writeLockSectionAndExecute(String section, Runnable invocation) {
        lockAndExecute(invocation, getSectionEvent(section).getWriteLock());
    }

    private <T> T readLockSectionAndReturnResult(String section, Supplier<T> invocation) {
        return lockAndReturnResult(invocation, getSectionEvent(section).getReadLock());
    }

    private <T> T writeLockSectionAndReturnResult(String section, Supplier<T> invocation) {
        return lockAndReturnResult(invocation, getSectionEvent(section).getWriteLock());
    }

    private boolean readLockSectionAndReturnResult(String section, BooleanSupplier invocation) {
        return lockAndReturnResult(invocation, getSectionEvent(section).getReadLock());
    }

    private boolean writeLockSectionAndReturnResult(String section, BooleanSupplier invocation) {
        return lockAndReturnResult(invocation, getSectionEvent(section).getWriteLock());
    }

    private void readLockCacheAndExecute(Runnable invocation) {
        lockAndExecute(invocation, cacheEvent.getReadLock());
    }

    private void writeLockCacheAndExecute(Runnable invocation) {
        lockAndExecute(invocation, cacheEvent.getWriteLock());
    }

    private <T> T readLockCacheAndReturnResult(Supplier<T> invocation) {
        return lockAndReturnResult(invocation, cacheEvent.getReadLock());
    }

    private <T> T writeLockCacheAndReturnResult(Supplier<T> invocation) {
        return lockAndReturnResult(invocation, cacheEvent.getWriteLock());
    }

    private static void lockAndExecute(Runnable invocation, Lock lock) {
        lock.lock();

        try {
            invocation.run();
        } finally {
            lock.unlock();
        }
    }

    private static <T> T lockAndReturnResult(Supplier<T> invocation, Lock lock) {
        lock.lock();

        try {
            return invocation.get();
        } finally {
            lock.unlock();
        }
    }

    private static boolean lockAndReturnResult(BooleanSupplier invocation, Lock lock) {
        lock.lock();

        try {
            return invocation.getAsBoolean();
        } finally {
            lock.unlock();
        }
    }

    private static final class CacheEntry<V> {
        private final V value;

        /**
         * Expiration time in milliseconds or {@code -1} if entry is permanent.
         */
        private final long expirationTimeMillis;

        private CacheEntry(@Nonnull V value) {
            this(value, -1);
        }

        private CacheEntry(@Nonnull V value, long expirationTimeMillis) {
            this.value = value;
            this.expirationTimeMillis = expirationTimeMillis;
        }

        @Contract(pure = true)
        public V getValue() {
            return value;
        }

        @Contract(pure = true)
        public long getExpirationTimeMillis() {
            return expirationTimeMillis;
        }

        @Nullable
        public V getValueOrNull() {
            @SuppressWarnings("LocalVariableHidesMemberVariable") long expirationTimeMillis = this.expirationTimeMillis;
            return expirationTimeMillis == -1 || expirationTimeMillis >= System.currentTimeMillis() ? value : null;
        }
    }

    private static final class CacheEntryExpirationInfo<K, V> implements Comparable<CacheEntryExpirationInfo<K, V>> {
        private final String section;
        private final K key;
        private final CacheEntry<V> entry;
        private final long expirationTimeMillis;

        private CacheEntryExpirationInfo(
                @Nonnull String section, @Nonnull K key, CacheEntry<V> entry, long expirationTimeMillis) {
            this.section = section;
            this.key = key;
            this.entry = entry;
            this.expirationTimeMillis = expirationTimeMillis;
        }

        public String getSection() {
            return section;
        }

        public K getKey() {
            return key;
        }

        CacheEntry<V> getEntry() {
            return entry;
        }

        public long getExpirationTimeMillis() {
            return expirationTimeMillis;
        }

        @Contract(pure = true)
        @Override
        public int compareTo(@Nonnull CacheEntryExpirationInfo<K, V> o) {
            return Long.compare(expirationTimeMillis, o.expirationTimeMillis);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CacheEntryExpirationInfo cacheEntryExpirationInfo = (CacheEntryExpirationInfo) o;

            return section.equals(cacheEntryExpirationInfo.section)
                    && key.equals(cacheEntryExpirationInfo.key)
                    && entry.equals(cacheEntryExpirationInfo.entry)
                    && expirationTimeMillis == cacheEntryExpirationInfo.expirationTimeMillis;
        }

        @Override
        public int hashCode() {
            int result = Long.hashCode(expirationTimeMillis);
            result = 32323 * result + entry.hashCode();
            result = 32323 * result + section.hashCode();
            result = 32323 * result + key.hashCode();
            return result;
        }
    }
}
