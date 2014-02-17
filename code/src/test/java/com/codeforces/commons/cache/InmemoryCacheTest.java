package com.codeforces.commons.cache;

import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.process.ThreadUtil;
import com.google.common.primitives.Ints;
import junit.framework.TestCase;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 26.01.12
 */
@SuppressWarnings({"JUnitTestMethodWithNoAssertions", "ThrowableResultOfMethodCallIgnored", "ErrorNotRethrown"})
public class InmemoryCacheTest extends TestCase {
    private static final int THREAD_COUNT = 10 * Runtime.getRuntime().availableProcessors();
    private static final int SLEEPING_THREAD_COUNT = 100 * Runtime.getRuntime().availableProcessors();

    private static final int SECTION_COUNT = 50;
    private static final int KEY_PER_SECTION_COUNT = 50;
    private static final int TOTAL_KEY_COUNT = SECTION_COUNT * KEY_PER_SECTION_COUNT;

    private static final long VALUE_LIFETIME_MILLIS = 500;
    private static final long VALUE_CHECK_INTERVAL_MILLIS = 250;

    private static final int VALUE_LENGTH = Ints.checkedCast(FileUtil.BYTES_PER_KB);

    public void testStoringOfValues() throws Exception {
        final InmemoryByteCache cache = new InmemoryByteCache();
        final BlockingQueue<CachePath> cachePaths = getCachePaths();

        CacheTestUtil.determineOperationTime("testStoringOfValues", new Runnable() {
            @Override
            public void run() {
                for (int pathIndex = 0; pathIndex < TOTAL_KEY_COUNT; ++pathIndex) {
                    checkStoringOneValue(cache, cachePaths.poll());
                }
            }
        });
    }

    public void testConcurrentStoringOfValues() throws Exception {
        final InmemoryByteCache cache = new InmemoryByteCache();
        final BlockingQueue<CachePath> cachePaths = getCachePaths();
        final AtomicReference<AssertionError> assertionError = new AtomicReference<>();
        final AtomicReference<Throwable> unexpectedThrowable = new AtomicReference<>();

        CacheTestUtil.determineOperationTime("testConcurrentStoringOfValues", new Runnable() {
            @Override
            public void run() {
                ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT, ThreadUtil.getCustomPoolThreadFactory(new ThreadUtil.ThreadCustomizer() {
                    @Override
                    public void customize(Thread thread) {
                        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                            @Override
                            public void uncaughtException(Thread t, Throwable e) {
                                unexpectedThrowable.set(e);
                            }
                        });
                    }
                }));

                final AtomicInteger pathIndex = new AtomicInteger();

                for (int threadIndex = 0; threadIndex < THREAD_COUNT; ++threadIndex) {
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            while (assertionError.get() == null
                                    && pathIndex.getAndIncrement() < TOTAL_KEY_COUNT) {
                                try {
                                    checkStoringOneValue(cache, cachePaths.poll());
                                } catch (AssertionError error) {
                                    assertionError.set(error);
                                }
                            }
                        }
                    });
                }

                executorService.shutdown();
                try {
                    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ignored) {
                    // No operations.
                }

                if (assertionError.get() != null) {
                    throw assertionError.get();
                }
            }
        });

        if (unexpectedThrowable.get() != null) {
            throw new AssertionError("Got unexpected exception in thread pool.", unexpectedThrowable.get());
        }
    }

    public void testConcurrentStoringOfValuesWithLifetime() throws Exception {
        final InmemoryByteCache cache = new InmemoryByteCache();
        final BlockingQueue<CachePath> cachePaths = getCachePaths();
        final AtomicReference<AssertionError> assertionError = new AtomicReference<>();
        final AtomicReference<Throwable> unexpectedThrowable = new AtomicReference<>();

        CacheTestUtil.determineOperationTime("testConcurrentStoringOfValuesWithLifetime", new Runnable() {
            @Override
            public void run() {
                ExecutorService executorService = Executors.newFixedThreadPool(SLEEPING_THREAD_COUNT, ThreadUtil.getCustomPoolThreadFactory(new ThreadUtil.ThreadCustomizer() {
                    @Override
                    public void customize(Thread thread) {
                        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                            @Override
                            public void uncaughtException(Thread t, Throwable e) {
                                unexpectedThrowable.set(e);
                            }
                        });
                    }
                }));

                final AtomicInteger pathIndex = new AtomicInteger();

                for (int threadIndex = 0; threadIndex < SLEEPING_THREAD_COUNT; ++threadIndex) {
                    final long threadSleepTime = 10L * threadIndex;

                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            ThreadUtil.sleep(threadSleepTime);

                            while (assertionError.get() == null
                                    && pathIndex.getAndIncrement() < TOTAL_KEY_COUNT) {
                                try {
                                    checkStoringOneValueWithLifetime(cache, cachePaths.poll());
                                } catch (AssertionError error) {
                                    assertionError.set(error);
                                }
                            }
                        }
                    });
                }

                executorService.shutdown();
                try {
                    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ignored) {
                    // No operations.
                }

                if (assertionError.get() != null) {
                    throw assertionError.get();
                }
            }
        });

        if (unexpectedThrowable.get() != null) {
            throw new AssertionError("Got unexpected exception in thread pool.", unexpectedThrowable.get());
        }
    }

    private static void checkStoringOneValue(ByteCache cache, CachePath cachePath) {
        CacheTestUtil.checkStoringOneValue(cache, cachePath, VALUE_LENGTH);
    }

    private static void checkStoringOneValueWithLifetime(ByteCache cache, CachePath cachePath) {
        CacheTestUtil.checkStoringOneValueWithLifetime(
                cache, cachePath, VALUE_LENGTH, VALUE_LIFETIME_MILLIS, VALUE_CHECK_INTERVAL_MILLIS
        );
    }

    private static BlockingQueue<CachePath> getCachePaths() {
        return CacheTestUtil.getCachePaths(KEY_PER_SECTION_COUNT, KEY_PER_SECTION_COUNT, TOTAL_KEY_COUNT);
    }
}
