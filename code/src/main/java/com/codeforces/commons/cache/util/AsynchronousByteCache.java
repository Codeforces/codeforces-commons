package com.codeforces.commons.cache.util;

import com.codeforces.commons.cache.ByteCache;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 26.01.12
 */
class AsynchronousByteCache extends ByteCache {
    private static final Logger logger = Logger.getLogger(AsynchronousByteCache.class);

    private static final int THREAD_COUNT = 2 * Runtime.getRuntime().availableProcessors();

    private final ByteCache cache;
    private final long validationTimeoutMillis;
    private final long disableOnFailMillis;

    private final AtomicLong lastValidationFail = new AtomicLong();

    private final ExecutorService validationService = new ThreadPoolExecutor(0, THREAD_COUNT, 1L, TimeUnit.MINUTES,
            new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
        private final AtomicLong threadIndex = new AtomicLong();

        @Nonnull
        @Override
        public Thread newThread(@Nonnull Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(cache.getClass().getSimpleName()
                    + '#' + AsynchronousByteCache.class.getSimpleName() + '-' + getIndex()
                    + "#ValidationThread-" + threadIndex.incrementAndGet()
            );
            return thread;
        }
    });

    private final ExecutorService executionService = Executors.newSingleThreadExecutor(new ThreadFactory() {
        private final AtomicInteger threadIndex = new AtomicInteger();

        @Nonnull
        @Override
        public Thread newThread(@Nonnull Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(cache.getClass().getSimpleName()
                    + '#' + AsynchronousByteCache.class.getSimpleName() + '-' + getIndex()
                    + "#ExecutionThread-" + threadIndex.incrementAndGet()
            );
            return thread;
        }
    });

    AsynchronousByteCache(ByteCache cache, long validationTimeoutMillis, long disableOnFailMillis) {
        ensureArguments(cache, validationTimeoutMillis, disableOnFailMillis);

        this.cache = cache;
        this.validationTimeoutMillis = validationTimeoutMillis;
        this.disableOnFailMillis = disableOnFailMillis;
    }

    private static void ensureArguments(ByteCache cache, long validationTimeoutMillis, long disableOnFailMillis) {
        if (cache == null) {
            throw new IllegalArgumentException("Argument 'cache' is 'null'.");
        }

        if (validationTimeoutMillis < 0) {
            throw new IllegalArgumentException("Argument 'validationTimeoutMillis' is less than zero.");
        }

        if (disableOnFailMillis < 0) {
            throw new IllegalArgumentException("Argument 'disableOnFailMillis' is less than zero.");
        }
    }

    @Override
    public boolean validate() {
        if (disableOnFailMillis != 0
                && System.currentTimeMillis() - lastValidationFail.get() < disableOnFailMillis) {
            logger.warn("AsynchronousByteCache disabled since "
                    + lastValidationFail.get() + " [" + new Date(lastValidationFail.get()) + "].");
            return false;
        }

        if (internalValidate()) {
            return true;
        } else {
            if (disableOnFailMillis != 0) {
                lastValidationFail.set(System.currentTimeMillis());
                logger.warn("AsynchronousByteCache can't be validated "
                        + lastValidationFail.get() + " [" + new Date(lastValidationFail.get()) + "].");
            }
            return false;
        }
    }

    private boolean internalValidate() {
        if (validationTimeoutMillis == 0) {
            return cache.validate();
        } else {
            Future<Boolean> future = validationService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return cache.validate();
                }
            });

            try {
                return future.get(validationTimeoutMillis, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                logger.error("Unexpected exception while validating.", e);
                return false;
            }
        }
    }

    @Override
    public boolean contains(@Nonnull String section, @Nonnull String key) {
        return cache.contains(section, key);
    }

    @Override
    public void put(@Nonnull final String section, @Nonnull final String key, @Nonnull final byte[] value) {
        executionService.execute(new Runnable() {
            @Override
            public void run() {
                cache.put(section, key, value);
            }
        });
    }

    @Override
    public void put(@Nonnull final String section, @Nonnull final String key,
                    @Nonnull final byte[] value, final long lifetimeMillis) {
        executionService.execute(new Runnable() {
            @Override
            public void run() {
                cache.put(section, key, value, lifetimeMillis);
            }
        });
    }

    @Override
    public void putIfAbsent(@Nonnull final String section, @Nonnull final String key, @Nonnull final byte[] value) {
        executionService.execute(new Runnable() {
            @Override
            public void run() {
                cache.putIfAbsent(section, key, value);
            }
        });
    }

    @Override
    public void putIfAbsent(@Nonnull final String section, @Nonnull final String key,
                            @Nonnull final byte[] value, final long lifetimeMillis) {
        executionService.execute(new Runnable() {
            @Override
            public void run() {
                cache.putIfAbsent(section, key, value, lifetimeMillis);
            }
        });
    }

    @Nullable
    @Override
    public byte[] get(@Nonnull String section, @Nonnull String key) {
        return cache.get(section, key);
    }

    @Override
    public boolean remove(@Nonnull String section, @Nonnull String key) {
        return cache.remove(section, key);
    }

    @Override
    public void clearSection(@Nonnull String section) {
        cache.clearSection(section);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public void close() {
        cache.close();

        validationService.shutdown();
        executionService.shutdown();

        try {
            validationService.awaitTermination(30L, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            // No operations.
        }

        try {
            executionService.awaitTermination(30L, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            // No operations.
        }
    }
}
