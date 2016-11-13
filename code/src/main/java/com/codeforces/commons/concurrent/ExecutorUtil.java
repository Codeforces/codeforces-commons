package com.codeforces.commons.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 13.11.2016
 */
public final class ExecutorUtil {
    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    public static void shutdownQuietly(long timeoutMillis, ExecutorService... executors) {
        for (int executorIndex = executors.length; --executorIndex >= 0; ) {
            executors[executorIndex].shutdown();
        }

        for (int executorIndex = executors.length; --executorIndex >= 0; ) {
            try {
                executors[executorIndex].awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
                // No operations.
            }
        }
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    public static void shutdownQuietly(ExecutorService... executors) {
        for (int executorIndex = executors.length; --executorIndex >= 0; ) {
            executors[executorIndex].shutdown();
        }

        for (int executorIndex = executors.length; --executorIndex >= 0; ) {
            try {
                executors[executorIndex].awaitTermination(1L, TimeUnit.DAYS);
            } catch (InterruptedException ignored) {
                // No operations.
            }
        }
    }

    private ExecutorUtil() {
        throw new UnsupportedOperationException();
    }
}
