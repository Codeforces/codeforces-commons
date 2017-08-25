package com.codeforces.commons.cache;

import com.codeforces.commons.math.RandomUtil;
import com.codeforces.commons.process.ThreadUtil;
import com.codeforces.commons.time.TimeUtil;
import org.junit.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 29.12.12
 */
@SuppressWarnings({"CallToSystemGC", "ThrowableResultOfMethodCallIgnored", "ErrorNotRethrown"})
final class CacheTestUtil {
    private CacheTestUtil() {
        throw new UnsupportedOperationException();
    }

    public static void testStoringOfValues(
            Class<?> cacheTestClass, Cache<String, byte[]> cache,
            int sectionCount, int keyPerSectionCount, int totalKeyCount, int valueLength) {
        CachePath[] cachePaths = getCachePaths(sectionCount, keyPerSectionCount, totalKeyCount);

        determineOperationTime(cacheTestClass.getSimpleName() + ".testStoringOfValues", () -> {
            for (int pathIndex = 0; pathIndex < totalKeyCount; ++pathIndex) {
                checkStoringOneValue(cache, cachePaths[pathIndex], valueLength);
            }
        });

        System.gc();

        determineOperationTime(cacheTestClass.getSimpleName() + ".testStoringOfValues (after warm up)", () -> {
            for (int pathIndex = 0; pathIndex < totalKeyCount; ++pathIndex) {
                checkStoringOneValue(cache, cachePaths[pathIndex], valueLength);
            }
        });
    }

    public static void testOverridingOfValuesWithLifetime(
            Class<?> cacheTestClass, Cache<String, byte[]> cache, int valueLength) {
        determineOperationTime(cacheTestClass.getSimpleName() + ".testOverridingOfValuesWithLifetime", () -> {
            byte[] temporaryBytes = RandomUtil.getRandomBytes(valueLength);
            byte[] finalBytes = RandomUtil.getRandomBytes(valueLength);

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
        });
    }

    public static void testConcurrentStoringOfValues(
            Class<?> cacheTestClass, Cache<String, byte[]> cache,
            int sectionCount, int keyPerSectionCount, int totalKeyCount, int valueLength, int threadCount) {
        CachePath[] cachePaths = getCachePaths(sectionCount, keyPerSectionCount, totalKeyCount);
        AtomicReference<AssertionError> assertionError = new AtomicReference<>();
        AtomicReference<Throwable> unexpectedThrowable = new AtomicReference<>();

        determineOperationTime(cacheTestClass.getSimpleName() + ".testConcurrentStoringOfValues", () -> executeConcurrentStoringOfValues(
                cache, cachePaths, assertionError, unexpectedThrowable, totalKeyCount, valueLength, threadCount
        ));

        if (unexpectedThrowable.get() != null) {
            throw new AssertionError("Got unexpected exception in thread pool.", unexpectedThrowable.get());
        }

        System.gc();

        determineOperationTime(cacheTestClass.getSimpleName() + ".testConcurrentStoringOfValues (after warm up)", () -> executeConcurrentStoringOfValues(
                cache, cachePaths, assertionError, unexpectedThrowable, totalKeyCount, valueLength, threadCount
        ));

        if (unexpectedThrowable.get() != null) {
            throw new AssertionError("Got unexpected exception in thread pool.", unexpectedThrowable.get());
        }
    }

    public static void testConcurrentStoringOfValuesWithLifetime(
            Class<?> cacheTestClass, Cache<String, byte[]> cache,
            int sectionCount, int keyPerSectionCount, int totalKeyCount, int valueLength,
            int sleepingThreadCount, long valueLifetimeMillis, long valueCheckIntervalMillis) {
        CachePath[] cachePaths = getCachePaths(sectionCount, keyPerSectionCount, totalKeyCount);
        AtomicReference<AssertionError> assertionError = new AtomicReference<>();
        AtomicReference<Throwable> unexpectedThrowable = new AtomicReference<>();

        determineOperationTime(cacheTestClass.getSimpleName() + ".testConcurrentStoringOfValuesWithLifetime", () -> {
            ExecutorService executorService = Executors.newFixedThreadPool(
                    sleepingThreadCount,
                    ThreadUtil.getCustomPoolThreadFactory(
                            thread -> thread.setUncaughtExceptionHandler((t, e) -> unexpectedThrowable.set(e))
                    )
            );

            AtomicInteger pathIndexCounter = new AtomicInteger();

            for (int threadIndex = 0; threadIndex < sleepingThreadCount; ++threadIndex) {
                @SuppressWarnings("PointlessArithmeticExpression") long threadSleepTime = 1L * threadIndex;

                executorService.execute(() -> {
                    ThreadUtil.sleep(threadSleepTime);
                    int pathIndex;

                    while (assertionError.get() == null
                            && (pathIndex = pathIndexCounter.getAndIncrement()) < totalKeyCount) {
                        try {
                            checkStoringOneValueWithLifetime(
                                    cache, cachePaths[pathIndex], valueLength,
                                    valueLifetimeMillis, valueCheckIntervalMillis
                            );
                        } catch (AssertionError error) {
                            assertionError.set(error);
                        }
                    }
                });
            }

            executorService.shutdown();
            try {
                executorService.awaitTermination(1L, TimeUnit.HOURS);
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
    }

    private static void executeConcurrentStoringOfValues(
            Cache<String, byte[]> cache, CachePath[] cachePaths,
            AtomicReference<AssertionError> assertionError, AtomicReference<Throwable> unexpectedThrowable,
            int totalKeyCount, int valueLength, int threadCount) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount, ThreadUtil.getCustomPoolThreadFactory(thread -> thread.setUncaughtExceptionHandler((t, e) -> unexpectedThrowable.set(e))));

        AtomicInteger pathIndexCounter = new AtomicInteger();

        for (int threadIndex = 0; threadIndex < threadCount; ++threadIndex) {
            executorService.execute(() -> {
                int pathIndex;

                while (assertionError.get() == null
                        && (pathIndex = pathIndexCounter.getAndIncrement()) < totalKeyCount) {
                    try {
                        checkStoringOneValue(cache, cachePaths[pathIndex], valueLength);
                    } catch (AssertionError error) {
                        assertionError.set(error);
                    }
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(1L, TimeUnit.HOURS);
        } catch (InterruptedException ignored) {
            // No operations.
        }

        if (assertionError.get() != null) {
            throw assertionError.get();
        }
    }

    public static void checkStoringOneValue(Cache<String, byte[]> cache, CachePath cachePath, int valueLength) {
        String section = cachePath.getSection();
        String key = cachePath.getKey();

        byte[] value = RandomUtil.getRandomBytes(valueLength);

        cache.put(section, key, value);
        Assert.assertTrue(
                "Restored value does not equal to original value.",
                Arrays.equals(value, cache.get(section, key))
        );

        cache.remove(section, key);
        Assert.assertNull("Value is not 'null' after removal.", cache.get(section, key));
    }

    public static void checkStoringOneValueWithLifetime(
            Cache<String, byte[]> cache, CachePath cachePath, int valueLength,
            long valueLifetimeMillis, long valueCheckIntervalMillis) {
        String section = cachePath.getSection();
        String key = cachePath.getKey();

        byte[] value = RandomUtil.getRandomBytes(valueLength);

        long cachePutTime = System.currentTimeMillis();
        cache.put(section, key, value, valueLifetimeMillis);
        Assert.assertTrue(
                "Restored value (with lifetime) does not equal to original value.",
                Arrays.equals(value, cache.get(section, key))
        );

        ThreadUtil.sleep(valueLifetimeMillis - valueCheckIntervalMillis);
        byte[] restoredValue = cache.get(section, key);
        Assert.assertTrue(String.format(
                "Restored value (with lifetime) does not equal to original value after sleeping some time (%s) " +
                        "(restored=%s, expected=%s, section=%s, key=%s, valueLifetime=%s, valueCheckInterval=%s).",
                TimeUtil.formatInterval(System.currentTimeMillis() - cachePutTime),
                toShortString(restoredValue), toShortString(value), section, key,
                TimeUtil.formatInterval(valueLifetimeMillis), TimeUtil.formatInterval(valueCheckIntervalMillis)
        ), Arrays.equals(value, restoredValue));

        ThreadUtil.sleep(2L * valueCheckIntervalMillis);
        Assert.assertNull(
                "Restored value (with lifetime) does not equal to 'null' after lifetime expiration.",
                cache.get(section, key)
        );

        cache.put(section, key, value, valueLifetimeMillis);
        Assert.assertTrue(
                "Restored value (with lifetime) does not equal to original value.",
                Arrays.equals(value, cache.get(section, key))
        );

        cache.remove(section, key);
        Assert.assertNull("Value (with lifetime) is not 'null' after removal.", cache.get(section, key));
    }

    public static CachePath[] getCachePaths(int sectionCount, int keyPerSectionCount, int totalKeyCount) {
        Map<String, List<String>> keysBySection = new HashMap<>(sectionCount);

        for (int sectionIndex = 0; sectionIndex < sectionCount; ++sectionIndex) {
            String section;
            do {
                section = RandomUtil.getRandomToken();
            } while (keysBySection.containsKey(section));

            Set<String> keys = new HashSet<>(keyPerSectionCount);

            while (keys.size() < keyPerSectionCount) {
                keys.add(RandomUtil.getRandomToken());
            }

            keysBySection.put(section, new ArrayList<>(keys));
        }

        List<CachePath> cachePaths = new ArrayList<>(totalKeyCount);

        for (Map.Entry<String, List<String>> sectionEntry : keysBySection.entrySet()) {
            List<String> keys = sectionEntry.getValue();

            for (int keyIndex = 0; keyIndex < keyPerSectionCount; ++keyIndex) {
                cachePaths.add(new CachePath(sectionEntry.getKey(), keys.get(keyIndex)));
            }
        }

        Collections.shuffle(cachePaths);

        return cachePaths.toArray(new CachePath[totalKeyCount]);
    }

    public static void determineOperationTime(String operationName, Runnable operation) {
        System.gc();
        long startTime = System.nanoTime();
        operation.run();
        long finishTime = System.nanoTime();
        System.out.printf(
                "Operation '%s' takes %.3f ms.%n",
                operationName, (finishTime - startTime) / 1000000.0
        );
        System.out.flush();
    }

    @Nonnull
    private static String toShortString(@Nullable byte[] array) {
        if (array == null) {
            return "null";
        }

        int length = array.length;
        if (length == 0) {
            return "[]";
        }

        StringBuilder stringBuilder = new StringBuilder("[").append(array[0]);

        if (length <= 7) {
            for (int i = 1; i < length; ++i) {
                stringBuilder.append(", ").append(array[i]);
            }
        } else {
            for (int i = 1; i < 3; ++i) {
                stringBuilder.append(", ").append(array[i]);
            }

            stringBuilder
                    .append(", ... ").append(array[length - 3])
                    .append(", ").append(array[length - 2])
                    .append(", ").append(array[length - 1]);
        }

        stringBuilder.append(']');
        return stringBuilder.toString();
    }
}
