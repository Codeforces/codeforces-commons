package com.codeforces.commons.cache;

import com.codeforces.commons.cache.annotation.*;
import com.codeforces.commons.collection.CollectionUtil;
import com.codeforces.commons.process.ReadWriteEvent;
import com.codeforces.commons.process.ThreadUtil;
import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import gnu.trove.map.TObjectIntMap;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.naming.ConfigurationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.*;

import static com.codeforces.commons.math.Math.min;

/**
 * Important! Any method should lock section in first case
 * and only then lock cache (if both locks are needed) to avoid deadlocks.
 *
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 29.03.2012
 */
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
                    CacheEntryExpirationInfo<K, V> expirationInfo = Preconditions.checkNotNull(getFirstExpirationInfo());
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

    @SuppressWarnings("unchecked")
    public static <K, V> InmemoryCache<K, V> newInstance() {
        InmemoryCache inmemoryCache = Guice.createInjector(new InMemoryCacheModule()).getInstance(InmemoryCache.class);
        inmemoryCache.cacheEntryRemovalThread.setDaemon(true);
        inmemoryCache.cacheEntryRemovalThread.start();
        return inmemoryCache;
    }

    /**
     * Do not decrease visibility less than default (package) to avoid conflicts with Guice.
     */
    InmemoryCache() {
    }

    @Contract(pure = true)
    @Override
    public final boolean validate() {
        return !stopBackgroundThreads.get();
    }

    @CacheSectionRead
    @Override
    public boolean contains(@CacheSection @Nonnull String section, @Nonnull K key) {
        return get(section, key) != null;
    }

    @CacheSectionWrite
    @Override
    public void put(@CacheSection @Nonnull String section, @Nonnull K key, @Nonnull V value) {
        ensureAndReturnCacheSection(section).put(key, new CacheEntry<>(value));
    }

    @Override
    public void put(
            @CacheSection @Nonnull String section, @Nonnull K key, @Nonnull V value, long lifetimeMillis) {
        Map<K, CacheEntry<V>> cacheEntryByKey = ensureAndReturnCacheSection(section);
        Queue<CacheEntryExpirationInfo<K, V>> expirationInfos = ensureAndReturnExpirationInfosSection(section);
        addCacheEntryWithLifetime(section, key, value, lifetimeMillis, cacheEntryByKey, expirationInfos);
    }

    @CacheSectionWrite
    @Override
    public void putIfAbsent(@CacheSection @Nonnull String section, @Nonnull K key, @Nonnull V value) {
        Map<K, CacheEntry<V>> cacheEntryByKey = ensureAndReturnCacheSection(section);
        if (!cacheEntryByKey.containsKey(key)) {
            cacheEntryByKey.put(key, new CacheEntry<>(value));
        }
    }

    @Override
    public void putIfAbsent(
            @CacheSection @Nonnull String section, @Nonnull K key, @Nonnull V value, long lifetimeMillis) {
        Map<K, CacheEntry<V>> cacheEntryByKey = ensureAndReturnCacheSection(section);
        if (!cacheEntryByKey.containsKey(key)) {
            addCacheEntryWithLifetimeIfAbsent(section, key, value, lifetimeMillis, cacheEntryByKey);
        }
    }

    @CacheSectionRead
    @Override
    public V get(@CacheSection @Nonnull String section, @Nonnull K key) {
        CacheEntry<V> cacheEntry = ensureAndReturnCacheSection(section).get(key);
        return cacheEntry == null ? null : cacheEntry.getValueOrNull();
    }

    @CacheSectionWrite
    @Override
    public boolean remove(@CacheSection @Nonnull String section, @Nonnull K key) {
        return ensureAndReturnCacheSection(section).remove(key) != null;
    }

    @CacheSectionWrite
    @Override
    public void clearSection(@CacheSection @Nonnull String section) {
        ensureAndReturnCacheSection(section).clear();
        ensureAndReturnExpirationInfosSection(section).clear();
    }

    @CacheWrite
    @Override
    public void clear() {
        cacheEntryExpirationInfosBySection.clear();
        cacheEntryByKeyBySection.clear();
    }

    @Override
    public void close() {
        if (!stopBackgroundThreads.getAndSet(true)) {
            cacheEntryRemovalThread.interrupt();
        }
    }

    @SuppressWarnings("WeakerAccess")
    Map<K, CacheEntry<V>> ensureAndReturnCacheSection(@CacheSection String section) {
        Map<K, CacheEntry<V>> cacheEntryByKey = getCacheSection(section);
        return cacheEntryByKey == null ? createCacheSection(section) : cacheEntryByKey;
    }

    @SuppressWarnings("WeakerAccess")
    @CacheRead
    Map<K, CacheEntry<V>> getCacheSection(@CacheSection String section) {
        return cacheEntryByKeyBySection.get(section);
    }

    @SuppressWarnings("WeakerAccess")
    @CacheWrite
    Map<K, CacheEntry<V>> createCacheSection(@CacheSection String section) {
        return cacheEntryByKeyBySection.computeIfAbsent(section, __ -> new HashMap<>());
    }

    @SuppressWarnings("WeakerAccess")
    Queue<CacheEntryExpirationInfo<K, V>> ensureAndReturnExpirationInfosSection(@CacheSection String section) {
        Queue<CacheEntryExpirationInfo<K, V>> expirationInfos = getExpirationInfosBySection(section);
        return expirationInfos == null ? createExpirationInfosSection(section) : expirationInfos;
    }

    @SuppressWarnings("WeakerAccess")
    @CacheRead
    Queue<CacheEntryExpirationInfo<K, V>> getExpirationInfosBySection(@CacheSection String section) {
        return cacheEntryExpirationInfosBySection.get(section);
    }

    @SuppressWarnings("WeakerAccess")
    @CacheWrite
    Queue<CacheEntryExpirationInfo<K, V>> createExpirationInfosSection(@CacheSection String section) {
        return cacheEntryExpirationInfosBySection.computeIfAbsent(section, __ -> new PriorityQueue<>());
    }

    @SuppressWarnings("WeakerAccess")
    @CacheSectionWrite
    void addCacheEntryWithLifetime(
            @CacheSection String section, K key, V value, long lifetimeMillis,
            Map<K, CacheEntry<V>> valueByKey, Queue<CacheEntryExpirationInfo<K, V>> expirationInfos) {
        long expirationTimeMillis = System.currentTimeMillis() + lifetimeMillis;

        CacheEntry<V> cacheEntry = new CacheEntry<>(value, expirationTimeMillis);
        valueByKey.put(key, cacheEntry);
        expirationInfos.add(new CacheEntryExpirationInfo<>(section, key, cacheEntry, expirationTimeMillis));

        cacheEntryRemovalThread.interrupt();
    }

    @SuppressWarnings("WeakerAccess")
    @CacheSectionWrite
    void addCacheEntryWithLifetimeIfAbsent(
            @CacheSection String section, K key, V value, long lifetimeMillis, Map<K, CacheEntry<V>> cacheEntryByKey) {
        if (!cacheEntryByKey.containsKey(key)) {
            Queue<CacheEntryExpirationInfo<K, V>> expirationInfos = ensureAndReturnExpirationInfosSection(section);
            long expirationTimeMillis = System.currentTimeMillis() + lifetimeMillis;

            CacheEntry<V> cacheEntry = new CacheEntry<>(value, expirationTimeMillis);
            cacheEntryByKey.put(key, cacheEntry);
            expirationInfos.add(new CacheEntryExpirationInfo<>(section, key, cacheEntry, expirationTimeMillis));

            cacheEntryRemovalThread.interrupt();
        }
    }

    @SuppressWarnings("WeakerAccess")
    final boolean hasExpirationInfos() {
        String[] expirationInfoSections = getExpirationInfoSections();
        int sectionIndex = expirationInfoSections.length;

        while (--sectionIndex >= 0) {
            if (hasExpirationInfosInSection(expirationInfoSections[sectionIndex])) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("WeakerAccess")
    @CacheSectionRead
    boolean hasExpirationInfosInSection(@CacheSection String section) {
        return !ensureAndReturnExpirationInfosSection(section).isEmpty();
    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    final CacheEntryExpirationInfo<K, V> getFirstExpirationInfo() {
        CacheEntryExpirationInfo<K, V> firstExpirationInfo = null;

        String[] expirationInfoSections = getExpirationInfoSections();
        int sectionsCount = expirationInfoSections.length;

        for (int sectionIndex = 0; sectionIndex < sectionsCount; ++sectionIndex) {
            String section = expirationInfoSections[sectionIndex];
            CacheEntryExpirationInfo<K, V> expirationInfo = getFirstExpirationInfoInSection(section);

            if (firstExpirationInfo == null || expirationInfo != null
                    && expirationInfo.getExpirationTimeMillis() < firstExpirationInfo.getExpirationTimeMillis()) {
                firstExpirationInfo = expirationInfo;
            }
        }

        return firstExpirationInfo;
    }

    @SuppressWarnings("WeakerAccess")
    @CacheSectionRead
    CacheEntryExpirationInfo<K, V> getFirstExpirationInfoInSection(@CacheSection String section) {
        return ensureAndReturnExpirationInfosSection(section).peek();
    }

    @SuppressWarnings("WeakerAccess")
    @CacheRead
    String[] getExpirationInfoSections() {
        Set<String> sections = cacheEntryExpirationInfosBySection.keySet();
        return sections.toArray(new String[sections.size()]);
    }

    @SuppressWarnings("WeakerAccess")
    @CacheSectionWrite
    final void removeCacheEntryWithLifetimeIfNeeded(
            @CacheSection String section, CacheEntryExpirationInfo<K, V> expirationInfo) {
        Map<K, CacheEntry<V>> cacheEntryByKey = ensureAndReturnCacheSection(section);
        CacheEntry<V> cacheEntry = cacheEntryByKey.get(expirationInfo.getKey());
        if (cacheEntry != null && cacheEntry.getExpirationTimeMillis() != -1
                && cacheEntry.equals(expirationInfo.getEntry())) {
            cacheEntryByKey.remove(expirationInfo.getKey());
        }
        ensureAndReturnExpirationInfosSection(section).poll();
    }

    private ReadWriteEvent getSectionEvent(@CacheSection String section) {
        return eventBySection.computeIfAbsent(section, __ -> new ReadWriteEvent());
    }

    @SuppressWarnings("PackageVisibleInnerClass")
    static final class CacheEntry<V> {
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

    @SuppressWarnings("PackageVisibleInnerClass")
    static final class CacheEntryExpirationInfo<K, V> implements Comparable<CacheEntryExpirationInfo<K, V>> {
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

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    private static final class InMemoryCacheModule extends AbstractModule {
        private static final TObjectIntMap<Method> sectionParameterIndexByMethod = CollectionUtil.newTObjectIntMap();
        private static final ReadWriteLock sectionParameterIndexesLock = new ReentrantReadWriteLock();

        @SuppressWarnings("OverlyLongMethod")
        @Override
        protected void configure() {
            Matcher<Object> classMatcher = Matchers.only(InmemoryCache.class);

            bindInterceptor(classMatcher, Matchers.annotatedWith(CacheSectionRead.class), invocation -> {
                String section = getSectionParameterValue(invocation);
                InmemoryCache inmemoryCache = (InmemoryCache) invocation.getThis();

                return proceedLocked(invocation, inmemoryCache.getSectionEvent(section).getReadLock());
            });

            bindInterceptor(classMatcher, Matchers.annotatedWith(CacheRead.class), invocation -> {
                InmemoryCache inmemoryCache = (InmemoryCache) invocation.getThis();

                return proceedLocked(invocation, inmemoryCache.cacheEvent.getReadLock());
            });

            bindInterceptor(classMatcher, Matchers.annotatedWith(CacheSectionWrite.class), invocation -> {
                String section = getSectionParameterValue(invocation);
                InmemoryCache inmemoryCache = (InmemoryCache) invocation.getThis();

                return proceedLocked(invocation, inmemoryCache.getSectionEvent(section).getWriteLock());
            });

            bindInterceptor(classMatcher, Matchers.annotatedWith(CacheWrite.class), invocation -> {
                InmemoryCache inmemoryCache = (InmemoryCache) invocation.getThis();

                return proceedLocked(invocation, inmemoryCache.cacheEvent.getWriteLock());
            });
        }

        private static Object proceedLocked(MethodInvocation invocation, Lock lock) throws Throwable {
            lock.lock();

            try {
                return invocation.proceed();
            } finally {
                lock.unlock();
            }
        }

        private static String getSectionParameterValue(MethodInvocation invocation) throws ConfigurationException {
            int sectionParameterIndex = getSectionParameterIndex(invocation.getMethod());

            if (sectionParameterIndex == -1) {
                String message = String.format(
                        "Method '%s' has no parameter annotated with '@%s'.", invocation.getMethod(), CacheSection.class
                );
                logger.fatal(message);
                throw new ConfigurationException(message);
            }

            return (String) invocation.getArguments()[sectionParameterIndex];
        }

        @SuppressWarnings("ForLoopWithMissingComponent")
        private static int getSectionParameterIndex(Method method) {
            int sectionParameterIndex;

            Lock readLock = sectionParameterIndexesLock.readLock();
            readLock.lock();
            try {
                sectionParameterIndex = sectionParameterIndexByMethod.get(method);
            } finally {
                readLock.unlock();
            }

            if (sectionParameterIndex == sectionParameterIndexByMethod.getNoEntryValue()) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                sectionParameterIndex = -1;

                int parameterIndex = min(parameterTypes.length, parameterAnnotations.length);

                while (--parameterIndex >= 0) {
                    if (parameterTypes[parameterIndex] != String.class) {
                        continue;
                    }

                    Annotation[] annotations = parameterAnnotations[parameterIndex];
                    boolean sectionAnnotation = false;

                    for (int annotationIndex = annotations.length; --annotationIndex >= 0; ) {
                        if (annotations[annotationIndex].annotationType() == CacheSection.class) {
                            sectionAnnotation = true;
                            break;
                        }
                    }

                    if (sectionAnnotation) {
                        sectionParameterIndex = parameterIndex;
                        break;
                    }
                }

                Lock writeLock = sectionParameterIndexesLock.writeLock();
                writeLock.lock();
                try {
                    sectionParameterIndexByMethod.put(method, sectionParameterIndex);
                } finally {
                    writeLock.unlock();
                }
            }

            return sectionParameterIndex;
        }
    }
}
