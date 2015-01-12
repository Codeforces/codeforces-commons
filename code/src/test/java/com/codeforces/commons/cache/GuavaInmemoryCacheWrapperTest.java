package com.codeforces.commons.cache;

import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.math.RandomUtil;
import com.codeforces.commons.process.ThreadUtil;
import com.google.common.primitives.Ints;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 26.01.12
 */
@SuppressWarnings({"JUnitTestMethodWithNoAssertions", "ThrowableResultOfMethodCallIgnored", "ErrorNotRethrown"})
public class GuavaInmemoryCacheWrapperTest {
    private static final int THREAD_COUNT = 10 * Runtime.getRuntime().availableProcessors();
    private static final int SLEEPING_THREAD_COUNT = 100 * Runtime.getRuntime().availableProcessors();

    private static final int SECTION_COUNT = 50;
    private static final int KEY_PER_SECTION_COUNT = 50;
    private static final int TOTAL_KEY_COUNT = SECTION_COUNT * KEY_PER_SECTION_COUNT;

    private static final long VALUE_LIFETIME_MILLIS = 350L;
    private static final long VALUE_CHECK_INTERVAL_MILLIS = 250L;

    private static final int VALUE_LENGTH = Ints.checkedCast(FileUtil.BYTES_PER_KB);

    @Test
    public void testStoringOfValues() throws Exception {
        final Cache<String, byte[]> cache = new GuavaInmemoryCacheWrapper<>();
        final BlockingQueue<CachePath> cachePaths = getCachePaths();
        final BlockingQueue<CachePath> copiedCachePaths = new LinkedBlockingQueue<>(cachePaths);

        CacheTestUtil.determineOperationTime("GuavaInmemoryCacheWrapperTest.testStoringOfValues", new Runnable() {
            @Override
            public void run() {
                for (int pathIndex = 0; pathIndex < TOTAL_KEY_COUNT; ++pathIndex) {
                    checkStoringOneValue(cache, cachePaths.poll());
                }
            }
        });

        CacheTestUtil.determineOperationTime("GuavaInmemoryCacheWrapperTest.testStoringOfValues (after warm up)", new Runnable() {
            @Override
            public void run() {
                for (int pathIndex = 0; pathIndex < TOTAL_KEY_COUNT; ++pathIndex) {
                    checkStoringOneValue(cache, copiedCachePaths.poll());
                }
            }
        });
    }

    @Test
    public void testOverridingOfValuesWithLifetime() throws Exception {
        final Cache<String, byte[]> cache = new GuavaInmemoryCacheWrapper<>(1000L);

        CacheTestUtil.determineOperationTime("GuavaInmemoryCacheWrapperTest.testOverridingOfValuesWithLifetime", new Runnable() {
            @Override
            public void run() {
                byte[] temporaryBytes = RandomUtil.getRandomBytes(VALUE_LENGTH);
                byte[] finalBytes = RandomUtil.getRandomBytes(VALUE_LENGTH);

                cache.put("S", "K", temporaryBytes, 1000L);
                Assert.assertTrue(
                        "Restored value (with lifetime) does not equal to original value.",
                        Arrays.equals(temporaryBytes, cache.get("S", "K"))
                );

                cache.put("S", "K", finalBytes, 1000L);
                ThreadUtil.sleep(500L);
                Assert.assertNotNull("Value is 'null' after previous value lifetime expiration.", cache.get("S", "K"));
                Assert.assertEquals("Restored value does not equal to original value.", finalBytes, cache.get("S", "K"));

                ThreadUtil.sleep(1000L);
                Assert.assertNull("Value is not 'null' after lifetime expiration.", cache.get("S", "K"));
            }
        });
    }

    @Test
    public void testConcurrentStoringOfValues() throws Exception {
        final Cache<String, byte[]> cache = new GuavaInmemoryCacheWrapper<>();
        final BlockingQueue<CachePath> cachePaths = getCachePaths();
        final BlockingQueue<CachePath> copiedCachePaths = new LinkedBlockingQueue<>(cachePaths);
        final AtomicReference<AssertionError> assertionError = new AtomicReference<>();
        final AtomicReference<Throwable> unexpectedThrowable = new AtomicReference<>();

        CacheTestUtil.determineOperationTime("GuavaInmemoryCacheWrapperTest.testConcurrentStoringOfValues", new Runnable() {
            @Override
            public void run() {
                executeConcurrentStoringOfValues(cache, cachePaths, assertionError, unexpectedThrowable);
            }
        });

        if (unexpectedThrowable.get() != null) {
            throw new AssertionError("Got unexpected exception in thread pool.", unexpectedThrowable.get());
        }

        CacheTestUtil.determineOperationTime("GuavaInmemoryCacheWrapperTest.testConcurrentStoringOfValues (after warm up)", new Runnable() {
            @Override
            public void run() {
                executeConcurrentStoringOfValues(cache, copiedCachePaths, assertionError, unexpectedThrowable);
            }
        });

        if (unexpectedThrowable.get() != null) {
            throw new AssertionError("Got unexpected exception in thread pool.", unexpectedThrowable.get());
        }
    }

    private static void executeConcurrentStoringOfValues(
            final Cache<String, byte[]> cache, final BlockingQueue<CachePath> cachePaths,
            final AtomicReference<AssertionError> assertionError, final AtomicReference<Throwable> unexpectedThrowable) {
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

    @Test
    public void testConcurrentStoringOfValuesWithLifetime() throws Exception {
        final Cache<String, byte[]> cache = new GuavaInmemoryCacheWrapper<>(VALUE_LIFETIME_MILLIS);
        final BlockingQueue<CachePath> cachePaths = getCachePaths();
        final AtomicReference<AssertionError> assertionError = new AtomicReference<>();
        final AtomicReference<Throwable> unexpectedThrowable = new AtomicReference<>();

        CacheTestUtil.determineOperationTime("GuavaInmemoryCacheWrapperTest.testConcurrentStoringOfValuesWithLifetime", new Runnable() {
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
                    final long threadSleepTime = 1L * threadIndex;

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

    private static void checkStoringOneValue(Cache<String, byte[]> cache, CachePath cachePath) {
        CacheTestUtil.checkStoringOneValue(cache, cachePath, VALUE_LENGTH);
    }

    private static void checkStoringOneValueWithLifetime(Cache<String, byte[]> cache, CachePath cachePath) {
        CacheTestUtil.checkStoringOneValueWithLifetime(
                cache, cachePath, VALUE_LENGTH, VALUE_LIFETIME_MILLIS, VALUE_CHECK_INTERVAL_MILLIS
        );
    }

    private static BlockingQueue<CachePath> getCachePaths() {
        return CacheTestUtil.getCachePaths(SECTION_COUNT, KEY_PER_SECTION_COUNT, TOTAL_KEY_COUNT);
    }
}
