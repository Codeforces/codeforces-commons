package com.codeforces.commons.concurrent;

import com.codeforces.commons.annotation.NullableElements;
import com.codeforces.commons.time.TimeUtil;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 13.11.2016
 */
public final class ExecutorUtil {
    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod", "WeakerAccess"})
    public static void shutdownQuietly(long timeoutMillis, @Nullable @NullableElements ExecutorService... executors) {
        if (executors == null) {
            return;
        }

        for (int executorIndex = executors.length; --executorIndex >= 0; ) {
            ExecutorService executor = executors[executorIndex];
            if (executor != null) {
                executor.shutdown();
            }
        }

        for (int executorIndex = executors.length; --executorIndex >= 0; ) {
            ExecutorService executor = executors[executorIndex];
            if (executor != null) {
                try {
                    executor.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ignored) {
                    // No operations.
                }
            }
        }
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public static void shutdownQuietly(@Nullable @NullableElements ExecutorService... executors) {
        shutdownQuietly(TimeUtil.MILLIS_PER_WEEK, executors);
    }

    private ExecutorUtil() {
        throw new UnsupportedOperationException();
    }
}
