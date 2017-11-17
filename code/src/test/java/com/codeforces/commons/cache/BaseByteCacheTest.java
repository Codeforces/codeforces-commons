package com.codeforces.commons.cache;

import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.process.ThreadUtil;
import com.google.common.primitives.Ints;
import junit.framework.TestCase;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 29.12.12
 */
@SuppressWarnings({"JUnitTestMethodWithNoAssertions", "ThrowableResultOfMethodCallIgnored", "ErrorNotRethrown"})
public abstract class BaseByteCacheTest extends TestCase {
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
            ByteCache cache = newByteCache(tempDir);
            CachePath cachePath = new CachePath("testStoringOfLargeValues", "value");
            CacheTestUtil.checkStoringOneValue(cache, cachePath, Ints.checkedCast(32 * FileUtil.BYTES_PER_MB));
        } finally {
            FileUtil.deleteTotally(tempDir);
        }
    }

    public void testStoringOfValues() throws Exception {
        File tempDir = FileUtil.createTemporaryDirectory("file-system-cache");
        try {
            ByteCache cache = newByteCache(tempDir);
            BlockingQueue<CachePath> cachePaths = getCachePaths();

            CacheTestUtil.determineOperationTime("BaseByteCacheTest.testStoringOfValues", () -> {
                for (int pathIndex = 0; pathIndex < TOTAL_KEY_COUNT; ++pathIndex) {
                    checkStoringOneValue(cache, cachePaths.poll());
                }
            });
        } finally {
            FileUtil.deleteTotally(tempDir);
        }
    }

    protected abstract ByteCache newByteCache(File tempDir);

    @SuppressWarnings("Duplicates")
    public void testConcurrentStoringOfValues() throws Exception {
        File tempDir = FileUtil.createTemporaryDirectory("file-system-cache");
        try {
            ByteCache cache = newByteCache(tempDir);
            BlockingQueue<CachePath> cachePaths = getCachePaths();
            AtomicReference<AssertionError> assertionError = new AtomicReference<>();
            AtomicReference<Throwable> unexpectedThrowable = new AtomicReference<>();

            CacheTestUtil.determineOperationTime("BaseByteCacheTest.testConcurrentStoringOfValues", () -> {
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
                try {
                    internalTestConcurrentStoringOfValuesWithLifetime(
                            VALUE_LIFETIME_MILLIS * 4L, VALUE_CHECK_INTERVAL_MILLIS * 4L
                    );
                } catch (AssertionError ignoredC) {
                    internalTestConcurrentStoringOfValuesWithLifetime(
                            VALUE_LIFETIME_MILLIS * 8L, VALUE_CHECK_INTERVAL_MILLIS * 8L
                    );
                }
            }
        }
    }

    @SuppressWarnings("Duplicates")
    private void internalTestConcurrentStoringOfValuesWithLifetime(
            long valueLifetimeMillis, long valueCheckIntervalMillis) throws IOException {
        File tempDir = FileUtil.createTemporaryDirectory("file-system-cache");
        try {
            ByteCache cache = newByteCache(tempDir);
            BlockingQueue<CachePath> cachePaths = getCachePaths();
            AtomicReference<AssertionError> assertionError = new AtomicReference<>();
            AtomicReference<Throwable> unexpectedThrowable = new AtomicReference<>();

            CacheTestUtil.determineOperationTime("BaseByteCacheTest.testConcurrentStoringOfValuesWithLifetime", () -> {
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

    private String generateRandomString(Random random, Set<String> set) {
        while (true) {
            int length = random.nextInt(10) + 1;
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < length; i++) {
                s.append((char) ('a' + random.nextInt(5)));
            }
            String ss = s.toString();
            if (!set.contains(ss)) {
                set.add(ss);
                return ss;
            }
        }
    }

    private List<String> generateRandomStrings(Random random, int size) {
        Set<String> set = new HashSet<>();
        List<String> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(generateRandomString(random, set));
        }
        return result;
    }


    public void _testTypicalPerformance() throws IOException {
        long startTimeMillis = System.currentTimeMillis();
        File tempDir = FileUtil.createTemporaryDirectory("file-system-cache");
        try {
            ByteCache cache = newByteCache(tempDir);

            int SECTION_COUNT = 10;
            int KEY_COUNT = 50000;
            int AVG_VALUE_LENGTH = 100;

            Map<String, Long> lengths = new HashMap<>();

            Random random = new Random(11);
            List<String> sections = generateRandomStrings(random, SECTION_COUNT);
            random = new Random(91);
            List<String> keys = generateRandomStrings(random, KEY_COUNT);

            random = new Random(31);
            for (int i = 0; i < SECTION_COUNT * KEY_COUNT; i++) {
                if (i % 1000 == 0) {
                    System.out.println(i);
                }
                String section = sections.get(random.nextInt(sections.size()));
                String key = keys.get(random.nextInt(keys.size()));
                String sectionKey = section + (char)(1) + key;

                if (random.nextBoolean()) {
                    byte[] value = cache.get(section, key);
                    if (value == null && !lengths.containsKey(sectionKey)) {
                        continue;
                    }
                    if (value == null || !lengths.containsKey(sectionKey)) {
                        throw new RuntimeException("Expected both nulls or both nonnuls.");
                    }
                    Assert.assertEquals(value.length, lengths.get(sectionKey).longValue());
                } else {
                    int size = 0;
                    for (int j = 0; j < 10; j++) {
                        size += random.nextInt(AVG_VALUE_LENGTH * 2 / 10);
                    }
                    size = Math.max(size, 1);
                    byte[] bytes = new byte[size];
                    for (int j = 0; j < size; j++) {
                        bytes[j] = (byte) ('a' + random.nextInt(26));
                    }
                    cache.put(section, key, bytes);
                    lengths.put(sectionKey, (long) bytes.length);
                }
            }
        } finally {
            System.out.println("Test processed in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
            System.out.println(FileUtil.getDirectorySize(tempDir));
            FileUtil.deleteTotally(tempDir);
        }
    }
}
