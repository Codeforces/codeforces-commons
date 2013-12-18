package com.codeforces.commons.cache;

import com.codeforces.commons.math.RandomUtil;
import com.codeforces.commons.process.ThreadUtil;
import junit.framework.Assert;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 29.12.12
 */
final class CacheTestUtil {
    private CacheTestUtil() {
        throw new UnsupportedOperationException();
    }

    public static void checkStoringOneValue(ByteCache cache, CachePath cachePath, int valueLength) {
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

    public static void checkStoringOneValueWithLifetime(ByteCache cache, CachePath cachePath, int valueLength,
                                                        long valueLifetimeMillis, long valueCheckIntervalMillis) {
        String section = cachePath.getSection();
        String key = cachePath.getKey();

        byte[] value = RandomUtil.getRandomBytes(valueLength);

        cache.put(section, key, value, valueLifetimeMillis);
        Assert.assertTrue(
                "Restored value (with lifetime) does not equal to original value.",
                Arrays.equals(value, cache.get(section, key))
        );

        ThreadUtil.sleep(valueLifetimeMillis - valueCheckIntervalMillis);
        Assert.assertTrue(
                "Restored value (with lifetime) does not equal to original value after sleeping some time.",
                Arrays.equals(value, cache.get(section, key))
        );

        ThreadUtil.sleep(3L * valueCheckIntervalMillis);
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

    public static BlockingQueue<CachePath> getCachePaths(int sectionCount, int keyPerSectionCount, int totalKeyCount) {
        Map<String, List<String>> keysBySection = new HashMap<>();

        for (int sectionIndex = 0; sectionIndex < sectionCount; ++sectionIndex) {
            String section;
            do {
                section = RandomUtil.getRandomToken();
            } while (keysBySection.containsKey(section));

            Set<String> keys = new HashSet<>(keyPerSectionCount);

            for (int keyIndex = 0; keyIndex < keyPerSectionCount; ++keyIndex) {
                String key;
                do {
                    key = RandomUtil.getRandomToken();
                } while (keys.contains(key));

                keys.add(key);
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

        return new LinkedBlockingQueue<>(cachePaths);
    }

    public static void determineOperationTime(String operationName, Runnable operation) {
        long startTime = System.nanoTime();
        operation.run();
        long finishTime = System.nanoTime();
        System.out.printf(
                "Operation '%s' takes %.3f ms.%n",
                operationName, (finishTime - startTime) / 1000000.0
        );
        System.out.flush();
    }
}
