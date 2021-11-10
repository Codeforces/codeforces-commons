package com.codeforces.commons.cache;

import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.process.ThreadUtil;
import com.google.common.primitives.Ints;
import junit.framework.TestCase;
import org.junit.Ignore;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 29.12.12
 */
@SuppressWarnings({"JUnitTestMethodWithNoAssertions", "ThrowableResultOfMethodCallIgnored", "ErrorNotRethrown"})
@Ignore
public class RedisByteCacheTest extends TestCase {
    private static final int THREAD_COUNT = 8;
    private static final int SLEEPING_THREAD_COUNT = 125;

    private static final int SECTION_COUNT = 15;
    private static final int KEY_PER_SECTION_COUNT = 50;
    private static final int TOTAL_KEY_COUNT = SECTION_COUNT * KEY_PER_SECTION_COUNT;

    private static final long VALUE_LIFETIME_MILLIS = 500;
    private static final long VALUE_CHECK_INTERVAL_MILLIS = 250;

    private static final int VALUE_LENGTH = Ints.checkedCast(FileUtil.BYTES_PER_KB) / 2;

    public void testStoringOfLargeValues() throws Exception {
        File tempDir = FileUtil.createTemporaryDirectory("file-system-cache");
        try {
            ByteCache cache = new RedisByteCache("127.0.0.1:88");
            CachePath cachePath = new CachePath("testStoringOfLargeValues", "value");
            CacheTestUtil.checkStoringOneValue(cache, cachePath, Ints.checkedCast(32 * FileUtil.BYTES_PER_MB));
        } finally {
            FileUtil.deleteTotally(tempDir);
        }
    }

    public void testStoringOfValues() throws Exception {
        File tempDir = FileUtil.createTemporaryDirectory("file-system-cache");
        try {
            ByteCache cache = new RedisByteCache("127.0.0.1:88");
            BlockingQueue<CachePath> cachePaths = getCachePaths();

            CacheTestUtil.determineOperationTime("RedisByteCacheTest.testStoringOfValues", () -> {
                for (int pathIndex = 0; pathIndex < TOTAL_KEY_COUNT; ++pathIndex) {
                    checkStoringOneValue(cache, cachePaths.poll());
                }
            });
        } finally {
            FileUtil.deleteTotally(tempDir);
        }
    }

    public void testConcurrentStoringOfValues() throws Exception {
        File tempDir = FileUtil.createTemporaryDirectory("file-system-cache");
        try {
            ByteCache cache = new RedisByteCache("127.0.0.1:88");
            BlockingQueue<CachePath> cachePaths = getCachePaths();
            AtomicReference<AssertionError> assertionError = new AtomicReference<>();
            AtomicReference<Throwable> unexpectedThrowable = new AtomicReference<>();

            CacheTestUtil.determineOperationTime("RedisByteCacheTest.testConcurrentStoringOfValues", () -> {
                ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT, ThreadUtil.getCustomPoolThreadFactory(
                        thread -> thread.setUncaughtExceptionHandler(
                                (t, e) -> unexpectedThrowable.set(e)
                        )
                ));

                AtomicInteger pathIndex = new AtomicInteger();

                for (int threadIndex = 0; threadIndex < THREAD_COUNT; ++threadIndex) {
                    executorService.execute(() -> {
                        while (assertionError.get() == null
                                && pathIndex.getAndIncrement() < TOTAL_KEY_COUNT) {
                            try {
                                checkStoringOneValue(cache, cachePaths.poll());
                            } catch (AssertionError error) {
                                assertionError.set(error);
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
            });

            if (unexpectedThrowable.get() != null) {
                throw new AssertionError("Got unexpected exception in thread pool.", unexpectedThrowable.get());
            }
        } finally {
            FileUtil.deleteTotally(tempDir);
        }
    }

    public void testConcurrentStoringOfValuesWithLifetime() throws Exception {
        try {
            internalTestConcurrentStoringOfValuesWithLifetime(VALUE_LIFETIME_MILLIS, VALUE_CHECK_INTERVAL_MILLIS);
        } catch (AssertionError ignoredA) {
            try {
                internalTestConcurrentStoringOfValuesWithLifetime(
                        VALUE_LIFETIME_MILLIS * 2L, VALUE_CHECK_INTERVAL_MILLIS * 2L
                );
            } catch (AssertionError ignoredB) {
                internalTestConcurrentStoringOfValuesWithLifetime(
                        VALUE_LIFETIME_MILLIS * 4L, VALUE_CHECK_INTERVAL_MILLIS * 4L
                );
            }
        }
    }

    private static void internalTestConcurrentStoringOfValuesWithLifetime(
            long valueLifetimeMillis, long valueCheckIntervalMillis) throws IOException {
        File tempDir = FileUtil.createTemporaryDirectory("file-system-cache");
        try {
            ByteCache cache = new RedisByteCache("127.0.0.1:88");
            BlockingQueue<CachePath> cachePaths = getCachePaths();
            AtomicReference<AssertionError> assertionError = new AtomicReference<>();
            AtomicReference<Throwable> unexpectedThrowable = new AtomicReference<>();

            CacheTestUtil.determineOperationTime("RedisByteCacheTest.testConcurrentStoringOfValuesWithLifetime", () -> {
                ExecutorService executorService = Executors.newFixedThreadPool(SLEEPING_THREAD_COUNT, ThreadUtil.getCustomPoolThreadFactory(
                        thread -> thread.setUncaughtExceptionHandler(
                                (t, e) -> unexpectedThrowable.set(e)
                        )
                ));

                AtomicInteger pathIndex = new AtomicInteger();

                for (int threadIndex = 0; threadIndex < SLEEPING_THREAD_COUNT; ++threadIndex) {
                    long threadSleepTime = 10L * threadIndex;

                    executorService.execute(() -> {
                        ThreadUtil.sleep(threadSleepTime);

                        while (assertionError.get() == null
                                && pathIndex.getAndIncrement() < TOTAL_KEY_COUNT) {
                            try {
                                checkStoringOneValueWithLifetime(
                                        cache, cachePaths.poll(), valueLifetimeMillis, valueCheckIntervalMillis
                                );
                            } catch (AssertionError error) {
                                assertionError.set(error);
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
            });

            if (unexpectedThrowable.get() != null) {
                throw new AssertionError("Got unexpected exception in thread pool.", unexpectedThrowable.get());
            }
        } finally {
            FileUtil.deleteTotally(tempDir);
        }
    }

    private static void checkStoringOneValue(ByteCache cache, CachePath cachePath) {
        CacheTestUtil.checkStoringOneValue(cache, cachePath, VALUE_LENGTH);
    }

    private static void checkStoringOneValueWithLifetime(
            ByteCache cache, CachePath cachePath, long valueLifetimeMillis, long valueCheckIntervalMillis) {
        CacheTestUtil.checkStoringOneValueWithLifetime(
                cache, cachePath, VALUE_LENGTH, valueLifetimeMillis, valueCheckIntervalMillis
        );
    }

    private static BlockingQueue<CachePath> getCachePaths() {
        return new LinkedBlockingQueue<>(Arrays.asList(CacheTestUtil.getCachePaths(
                SECTION_COUNT, KEY_PER_SECTION_COUNT, TOTAL_KEY_COUNT
        )));
    }
}
