package com.codeforces.commons.cache;

import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.math.NumberUtil;
import org.junit.Test;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 26.01.12
 */
@SuppressWarnings({"JUnitTestMethodWithNoAssertions"})
public class InmemoryCacheTest {
    private static final int THREAD_COUNT = 10 * Runtime.getRuntime().availableProcessors();
    private static final int SLEEPING_THREAD_COUNT = 100 * Runtime.getRuntime().availableProcessors();

    private static final int SECTION_COUNT = 50;
    private static final int KEY_PER_SECTION_COUNT = 50;
    private static final int TOTAL_KEY_COUNT = SECTION_COUNT * KEY_PER_SECTION_COUNT;

    private static final long VALUE_LIFETIME_MILLIS = 350L;
    private static final long VALUE_CHECK_INTERVAL_MILLIS = 250L;

    private static final int VALUE_LENGTH = NumberUtil.toInt(FileUtil.BYTES_PER_KB);

    @Test
    public void testStoringOfValues() throws Exception {
        CacheTestUtil.testStoringOfValues(
                InmemoryCacheTest.class, new InmemoryByteCache(),
                SECTION_COUNT, KEY_PER_SECTION_COUNT, TOTAL_KEY_COUNT, VALUE_LENGTH
        );
    }

    @Test
    public void testOverridingOfValuesWithLifetime() throws Exception {
        CacheTestUtil.testOverridingOfValuesWithLifetime(
                InmemoryCacheTest.class, new InmemoryByteCache(),
                VALUE_LENGTH
        );
    }

    @Test
    public void testConcurrentStoringOfValues() throws Exception {
        CacheTestUtil.testConcurrentStoringOfValues(
                InmemoryCacheTest.class, new InmemoryByteCache(),
                SECTION_COUNT, KEY_PER_SECTION_COUNT, TOTAL_KEY_COUNT, VALUE_LENGTH, THREAD_COUNT
        );
    }

    @Test
    public void testConcurrentStoringOfValuesWithLifetime() throws Exception {
        try {
            CacheTestUtil.testConcurrentStoringOfValuesWithLifetime(
                    InmemoryCacheTest.class, new InmemoryByteCache(),
                    SECTION_COUNT, KEY_PER_SECTION_COUNT, TOTAL_KEY_COUNT, VALUE_LENGTH,
                    SLEEPING_THREAD_COUNT, VALUE_LIFETIME_MILLIS, VALUE_CHECK_INTERVAL_MILLIS
            );
        } catch (AssertionError ignoredA) {
            try {
                CacheTestUtil.testConcurrentStoringOfValuesWithLifetime(
                        InmemoryCacheTest.class, new InmemoryByteCache(),
                        SECTION_COUNT, KEY_PER_SECTION_COUNT, TOTAL_KEY_COUNT, VALUE_LENGTH,
                        SLEEPING_THREAD_COUNT, VALUE_LIFETIME_MILLIS * 2L, VALUE_CHECK_INTERVAL_MILLIS * 2L
                );
            } catch (AssertionError ignoredB) {
                CacheTestUtil.testConcurrentStoringOfValuesWithLifetime(
                        InmemoryCacheTest.class, new InmemoryByteCache(),
                        SECTION_COUNT, KEY_PER_SECTION_COUNT, TOTAL_KEY_COUNT, VALUE_LENGTH,
                        SLEEPING_THREAD_COUNT, VALUE_LIFETIME_MILLIS * 4L, VALUE_CHECK_INTERVAL_MILLIS * 4L
                );
            }
        }
    }
}
