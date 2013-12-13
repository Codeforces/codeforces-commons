package com.codeforces.commons.cache;

import com.codeforces.commons.cache.annotations.*;
import com.codeforces.commons.process.ReadWriteEvent;
import com.codeforces.commons.process.ThreadUtil;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.naming.ConfigurationException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 29.03.12
 */

/**
 * Important! Any method should lock section in first case
 * and only then lock cache (if both locks are needed) to avoid deadlocks.
 */
public class InmemoryCache<K, V> extends Cache<K, V> {
    private static final Logger logger = Logger.getLogger(InmemoryCache.class);

    private final ReadWriteEvent cacheEvent = new ReadWriteEvent();
    private final ConcurrentMap<String, ReadWriteEvent> eventBySection = new ConcurrentHashMap<String, ReadWriteEvent>();

    private final Map<String, Map<K, CacheEntry<V>>> cacheEntryByKeyBySection = new HashMap<String, Map<K, CacheEntry<V>>>();
    private final Map<String, Queue<CacheEntryExpirationInfo<K>>> cacheEntryExpirationInfosBySection = new HashMap<String, Queue<CacheEntryExpirationInfo<K>>>();

    private final AtomicBoolean stopBackgroundThreads = new AtomicBoolean();

    private final Thread cacheEntryRemovalThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!stopBackgroundThreads.get()) {
                /*TODO Lock cacheWriteLock = cacheEvent.getWriteLock();
                cacheWriteLock.lock();*/

                try {
                    if (hasExpirationInfos()) {
                        CacheEntryExpirationInfo<K> expirationInfo = getFirstExpirationInfo();
                        long currentTimeMillis = System.currentTimeMillis();

                        if (currentTimeMillis < expirationInfo.getExpirationTimeMillis()) {
                            //cacheEvent.await(expirationInfo.getExpirationTimeMillis() - currentTimeMillis);
                            ThreadUtil.sleep(expirationInfo.getExpirationTimeMillis() - currentTimeMillis);
                        } else {
                            removeCacheEntryWithLifetimeIfNeeded(
                                    expirationInfo.getSection(), expirationInfo.getKey(),
                                    expirationInfo.getExpirationTimeMillis()
                            );
                        }
                    } else {
                        //cacheEvent.await(Long.MAX_VALUE);
                        ThreadUtil.sleep(Long.MAX_VALUE);
                    }
                } catch (RuntimeException e) {
                    logger.error(String.format(
                            "Got unexpected exception while removing expired entries in '%s' #%d.",
                            InmemoryCache.class, getIndex()
                    ), e);
                } finally {
                    /*cacheWriteLock.unlock();*/
                }
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
        ensureAndReturnCacheSection(section).put(key, new CacheEntry<V>(value));
    }

    @Override
    public void put(
            @CacheSection @Nonnull String section, @Nonnull K key, @Nonnull V value, long lifetimeMillis) {
        Map<K, CacheEntry<V>> cacheEntryByKey = ensureAndReturnCacheSection(section);
        Queue<CacheEntryExpirationInfo<K>> expirationInfos = ensureAndReturnExpirationInfosSection(section);
        addCacheEntryWithLifetime(section, key, value, lifetimeMillis, cacheEntryByKey, expirationInfos);
    }

    @CacheSectionWrite
    @Override
    public void putIfAbsent(@CacheSection @Nonnull String section, @Nonnull K key, @Nonnull V value) {
        Map<K, CacheEntry<V>> cacheEntryByKey = ensureAndReturnCacheSection(section);
        if (!cacheEntryByKey.containsKey(key)) {
            cacheEntryByKey.put(key, new CacheEntry<V>(value));
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
        if (cacheEntry == null || cacheEntry.getExpirationTimeMillis() != -1
                && cacheEntry.getExpirationTimeMillis() < System.currentTimeMillis()) {
            return null;
        } else {
            return cacheEntry.getValue();
        }
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
            /*cacheEvent.getWriteLock().lock();
            try {
                cacheEvent.signalAll();
            } finally {
                cacheEvent.getWriteLock().unlock();
            }*/
            cacheEntryRemovalThread.interrupt();
        }
    }

    Map<K, CacheEntry<V>> ensureAndReturnCacheSection(@CacheSection String section) {
        Map<K, CacheEntry<V>> cacheEntryByKey = getCacheSection(section);
        if (cacheEntryByKey == null) {
            return createCacheSection(section);
        } else {
            return cacheEntryByKey;
        }
    }

    @CacheRead
    Map<K, CacheEntry<V>> getCacheSection(@CacheSection String section) {
        return cacheEntryByKeyBySection.get(section);
    }

    @CacheWrite
    Map<K, CacheEntry<V>> createCacheSection(@CacheSection String section) {
        Map<K, CacheEntry<V>> cacheEntryByKey = cacheEntryByKeyBySection.get(section);

        if (cacheEntryByKey == null) {
            cacheEntryByKey = new HashMap<K, CacheEntry<V>>();
            cacheEntryByKeyBySection.put(section, cacheEntryByKey);
        }

        return cacheEntryByKey;
    }

    Queue<CacheEntryExpirationInfo<K>> ensureAndReturnExpirationInfosSection(@CacheSection String section) {
        Queue<CacheEntryExpirationInfo<K>> expirationInfos = getExpirationInfosBySection(section);
        if (expirationInfos == null) {
            return createExpirationInfosSection(section);
        } else {
            return expirationInfos;
        }
    }

    @CacheRead
    Queue<CacheEntryExpirationInfo<K>> getExpirationInfosBySection(@CacheSection String section) {
        return cacheEntryExpirationInfosBySection.get(section);
    }

    @CacheWrite
    Queue<CacheEntryExpirationInfo<K>> createExpirationInfosSection(@CacheSection String section) {
        Queue<CacheEntryExpirationInfo<K>> expirationInfos = cacheEntryExpirationInfosBySection.get(section);

        if (expirationInfos == null) {
            expirationInfos = new PriorityQueue<CacheEntryExpirationInfo<K>>();
            cacheEntryExpirationInfosBySection.put(section, expirationInfos);
        }

        return expirationInfos;
    }

    @CacheSectionWrite
    void addCacheEntryWithLifetime(
            @CacheSection String section, K key, V value, long lifetimeMillis,
            Map<K, CacheEntry<V>> valueByKey, Queue<CacheEntryExpirationInfo<K>> expirationInfos) {
        long expirationTimeMillis = System.currentTimeMillis() + lifetimeMillis;

        valueByKey.put(key, new CacheEntry<V>(value, expirationTimeMillis));
        expirationInfos.add(new CacheEntryExpirationInfo<K>(section, key, expirationTimeMillis));

        /*cacheEvent.getWriteLock().lock();
        try {
            cacheEvent.signalAll();
        } finally {
            cacheEvent.getWriteLock().unlock();
        }*/
        cacheEntryRemovalThread.interrupt();
    }

    @CacheSectionWrite
    void addCacheEntryWithLifetimeIfAbsent(
            @CacheSection String section, K key, V value, long lifetimeMillis, Map<K, CacheEntry<V>> cacheEntryByKey) {
        if (!cacheEntryByKey.containsKey(key)) {
            Queue<CacheEntryExpirationInfo<K>> expirationInfos = ensureAndReturnExpirationInfosSection(section);
            long expirationTimeMillis = System.currentTimeMillis() + lifetimeMillis;

            cacheEntryByKey.put(key, new CacheEntry<V>(value, expirationTimeMillis));
            expirationInfos.add(new CacheEntryExpirationInfo<K>(section, key, expirationTimeMillis));

            /*cacheEvent.getWriteLock().lock();
            try {
                cacheEvent.signalAll();
            } finally {
                cacheEvent.getWriteLock().unlock();
            }*/
            cacheEntryRemovalThread.interrupt();
        }
    }

    boolean hasExpirationInfos() {
        String[] expirationInfoSections = getExpirationInfoSections();
        int sectionsCount = expirationInfoSections.length;

        for (int sectionIndex = 0; sectionIndex < sectionsCount; ++sectionIndex) {
            if (hasExpirationInfosInSection(expirationInfoSections[sectionIndex])) {
                return true;
            }
        }

        return false;
    }

    @CacheSectionRead
    boolean hasExpirationInfosInSection(@CacheSection String section) {
        return !ensureAndReturnExpirationInfosSection(section).isEmpty();
    }

    CacheEntryExpirationInfo<K> getFirstExpirationInfo() {
        CacheEntryExpirationInfo<K> firstExpirationInfo = null;

        String[] expirationInfoSections = getExpirationInfoSections();
        int sectionsCount = expirationInfoSections.length;

        for (int sectionIndex = 0; sectionIndex < sectionsCount; ++sectionIndex) {
            String section = expirationInfoSections[sectionIndex];
            CacheEntryExpirationInfo<K> expirationInfo = getFirstExpirationInfoInSection(section);

            if (firstExpirationInfo == null || expirationInfo != null
                    && expirationInfo.getExpirationTimeMillis() < firstExpirationInfo.getExpirationTimeMillis()) {
                firstExpirationInfo = expirationInfo;
            }
        }

        return firstExpirationInfo;
    }

    @CacheSectionRead
    CacheEntryExpirationInfo<K> getFirstExpirationInfoInSection(@CacheSection String section) {
        return ensureAndReturnExpirationInfosSection(section).peek();
    }


    @CacheRead
    String[] getExpirationInfoSections() {
        Set<String> sections = cacheEntryExpirationInfosBySection.keySet();
        return sections.toArray(new String[sections.size()]);
    }

    @CacheSectionWrite
    void removeCacheEntryWithLifetimeIfNeeded(@CacheSection String section, K key, long expirationTimeMillis) {
        Map<K, CacheEntry<V>> cacheEntryByKey = ensureAndReturnCacheSection(section);
        CacheEntry<V> cacheEntry = cacheEntryByKey.get(key);
        if (cacheEntry != null && cacheEntry.getExpirationTimeMillis() != -1
                && cacheEntry.getExpirationTimeMillis() <= expirationTimeMillis) {
            cacheEntryByKey.remove(key);
        }
        ensureAndReturnExpirationInfosSection(section).poll();
    }

    private ReadWriteEvent getSectionEvent(@CacheSection String section) {
        ReadWriteEvent sectionEvent = eventBySection.get(section);

        if (sectionEvent == null) {
            eventBySection.putIfAbsent(section, new ReadWriteEvent());
            sectionEvent = eventBySection.get(section);
        }

        return sectionEvent;
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

        public V getValue() {
            return value;
        }

        public long getExpirationTimeMillis() {
            return expirationTimeMillis;
        }
    }

    @SuppressWarnings("PackageVisibleInnerClass")
    static final class CacheEntryExpirationInfo<K> implements Comparable<CacheEntryExpirationInfo<K>> {
        private final String section;
        private final K key;
        private final long expirationTimeMillis;

        private CacheEntryExpirationInfo(@Nonnull String section, @Nonnull K key, long expirationTimeMillis) {
            this.section = section;
            this.key = key;
            this.expirationTimeMillis = expirationTimeMillis;
        }

        public String getSection() {
            return section;
        }

        public K getKey() {
            return key;
        }

        public long getExpirationTimeMillis() {
            return expirationTimeMillis;
        }

        @Override
        public int compareTo(CacheEntryExpirationInfo<K> o) {
            if (expirationTimeMillis > o.expirationTimeMillis) {
                return 1;
            }

            if (expirationTimeMillis < o.expirationTimeMillis) {
                return -1;
            }

            return 0;
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
                    && expirationTimeMillis == cacheEntryExpirationInfo.expirationTimeMillis;
        }

        @Override
        public int hashCode() {
            int result = Long.valueOf(expirationTimeMillis).hashCode();
            result = 31 * result + section.hashCode();
            result = 31 * result + key.hashCode();
            return result;
        }
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    private static final class InMemoryCacheModule extends AbstractModule {
        @Override
        protected void configure() {
            bindInterceptor(
                    Matchers.only(InmemoryCache.class), Matchers.annotatedWith(CacheSectionRead.class),
                    new MethodInterceptor() {
                        @Override
                        public Object invoke(MethodInvocation invocation) throws Throwable {
                            String section = getSectionParameterValue(invocation);
                            InmemoryCache inmemoryCache = (InmemoryCache) invocation.getThis();
                            ReadWriteEvent sectionEvent = inmemoryCache.getSectionEvent(section);

                            Lock sectionReadLock = sectionEvent.getReadLock();
                            sectionReadLock.lock();

                            try {
                                return invocation.proceed();
                            } finally {
                                sectionReadLock.unlock();
                            }
                        }
                    }
            );

            bindInterceptor(
                    Matchers.only(InmemoryCache.class), Matchers.annotatedWith(CacheRead.class),
                    new MethodInterceptor() {
                        @Override
                        public Object invoke(MethodInvocation invocation) throws Throwable {
                            InmemoryCache inmemoryCache = (InmemoryCache) invocation.getThis();
                            Lock cacheReadLock = inmemoryCache.cacheEvent.getReadLock();
                            cacheReadLock.lock();

                            try {
                                return invocation.proceed();
                            } finally {
                                cacheReadLock.unlock();
                            }
                        }
                    }
            );

            bindInterceptor(
                    Matchers.only(InmemoryCache.class), Matchers.annotatedWith(CacheSectionWrite.class),
                    new MethodInterceptor() {
                        @Override
                        public Object invoke(MethodInvocation invocation) throws Throwable {
                            String section = getSectionParameterValue(invocation);
                            InmemoryCache inmemoryCache = (InmemoryCache) invocation.getThis();
                            ReadWriteEvent sectionEvent = inmemoryCache.getSectionEvent(section);

                            Lock sectionWriteLock = sectionEvent.getWriteLock();
                            sectionWriteLock.lock();

                            try {
                                return invocation.proceed();
                            } finally {
                                sectionWriteLock.unlock();
                            }
                        }
                    }
            );

            bindInterceptor(
                    Matchers.only(InmemoryCache.class), Matchers.annotatedWith(CacheWrite.class),
                    new MethodInterceptor() {
                        @Override
                        public Object invoke(MethodInvocation invocation) throws Throwable {
                            InmemoryCache inmemoryCache = (InmemoryCache) invocation.getThis();
                            Lock cacheWriteLock = inmemoryCache.cacheEvent.getWriteLock();
                            cacheWriteLock.lock();

                            try {
                                return invocation.proceed();
                            } finally {
                                cacheWriteLock.unlock();
                            }
                        }
                    }
            );
        }

        private static String getSectionParameterValue(MethodInvocation invocation) throws ConfigurationException {
            Annotation[][] parameterAnnotations = invocation.getMethod().getParameterAnnotations();
            int parameterCount = parameterAnnotations.length;
            Integer sectionParameterIndex = null;

            for (int parameterIndex = 0; parameterIndex < parameterCount; ++parameterIndex) {
                Annotation[] annotations = parameterAnnotations[parameterIndex];
                int annotationCount = annotations.length;
                boolean sectionAnnotation = false;

                for (int annotationIndex = 0; annotationIndex < annotationCount; ++annotationIndex) {
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

            if (sectionParameterIndex == null) {
                String message = String.format(
                        "Method '%s' has no parameter annotated with '@%s'.",
                        invocation.getMethod(), CacheSection.class
                );
                logger.fatal(message);
                throw new ConfigurationException(message);
            }

            return (String) invocation.getArguments()[sectionParameterIndex];
        }
    }
}
