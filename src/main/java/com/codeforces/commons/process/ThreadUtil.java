package com.codeforces.commons.process;

import com.codeforces.commons.exception.ExceptionUtil;
import org.apache.log4j.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 02.03.11
 */
public class ThreadUtil {
    private static final Logger logger = Logger.getLogger(ThreadUtil.class);

    private ThreadUtil() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    public static Thread newThread(
            @Nullable String name, @Nonnull Runnable runnable,
            @Nullable Thread.UncaughtExceptionHandler uncaughtExceptionHandler, @Nonnegative long stackSize,
            boolean daemon) {
        if (name == null) {
            name = String.format(
                    "Unnamed thread by %s running %s at %s.",
                    Thread.currentThread(), runnable.getClass().getName(), new Date()
            );
        }

        Thread thread = stackSize > 0L ? new Thread(null, runnable, name, stackSize) : new Thread(null, runnable, name);

        if (daemon) {
            thread.setDaemon(true);
        }

        if (uncaughtExceptionHandler != null || Thread.getDefaultUncaughtExceptionHandler() == null) {
            if (uncaughtExceptionHandler == null) {
                uncaughtExceptionHandler = (t, e) -> {
                    System.out.printf(
                            "Unexpected exception %s (%s) in %s:%n%s%n",
                            e.getClass(), e.getMessage(), t.getName(), ExceptionUtil.toString(e)
                    );
                    logger.error(String.format(
                            "Unexpected exception %s (%s) in %s.", e.getClass(), e.getMessage(), t.getName()
                    ), e);
                };
            }

            thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        }

        return thread;
    }

    public static Thread newThread(
            @Nullable String name, @Nonnull Runnable runnable,
            @Nullable Thread.UncaughtExceptionHandler uncaughtExceptionHandler, @Nonnegative long stackSize) {
        return newThread(name, runnable, uncaughtExceptionHandler, stackSize, false);
    }

    public static Thread newThread(
            @Nullable String name, @Nonnull Runnable runnable,
            @Nullable Thread.UncaughtExceptionHandler uncaughtExceptionHandler, boolean daemon) {
        return newThread(name, runnable, uncaughtExceptionHandler, 0L, daemon);
    }

    public static Thread newThread(
            @Nullable String name, @Nonnull Runnable runnable,
            @Nullable Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        return newThread(name, runnable, uncaughtExceptionHandler, 0L, false);
    }

    public static Thread newThread(@Nullable String name, @Nonnull Runnable runnable, boolean daemon) {
        return newThread(name, runnable, null, 0L, daemon);
    }

    public static Thread newThread(@Nullable String name, @Nonnull Runnable runnable) {
        return newThread(name, runnable, null, 0L, false);
    }

    public static Thread newThread(
            @Nonnull Runnable runnable, @Nullable Thread.UncaughtExceptionHandler uncaughtExceptionHandler,
            @Nonnegative long stackSize, boolean daemon) {
        return newThread(null, runnable, uncaughtExceptionHandler, stackSize, daemon);
    }

    public static Thread newThread(
            @Nonnull Runnable runnable, @Nullable Thread.UncaughtExceptionHandler uncaughtExceptionHandler,
            @Nonnegative long stackSize) {
        return newThread(null, runnable, uncaughtExceptionHandler, stackSize, false);
    }

    public static Thread newThread(
            @Nonnull Runnable runnable, @Nullable Thread.UncaughtExceptionHandler uncaughtExceptionHandler,
            boolean daemon) {
        return newThread(null, runnable, uncaughtExceptionHandler, 0L, daemon);
    }

    public static Thread newThread(
            @Nonnull Runnable runnable, @Nullable Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        return newThread(null, runnable, uncaughtExceptionHandler, 0L, false);
    }

    public static Thread newThread(@Nonnull Runnable runnable, boolean daemon) {
        return newThread(null, runnable, null, 0L, daemon);
    }

    public static Thread newThread(@Nonnull Runnable runnable) {
        return newThread(null, runnable, null, 0L, false);
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            // No operations.
        }
    }

    @Nullable
    public static <T> T execute(Operation<T> operation, int attemptCount, ExecutionStrategy strategy) throws Throwable {
        ensureArguments(operation, attemptCount, strategy);

        for (int attemptIndex = 1; attemptIndex <= attemptCount; ++attemptIndex) {
            try {
                return operation.run();
            } catch (Throwable t) {
                if (strategy.getUnsuccessHandler() != null) {
                    strategy.getUnsuccessHandler().handle(attemptIndex, t);
                }

                if (attemptIndex < attemptCount) {
                    if (attemptIndex == 1) {
                        logger.info("Iteration #1 has been failed: " + t.getMessage(), t);
                    } else {
                        logger.warn("Iteration #" + attemptIndex + " has been failed: " + t.getMessage(), t);
                    }
                    sleep(strategy.getDelayTimeMillis(attemptIndex));
                } else {
                    logger.error("Iteration #" + attemptIndex + " has been failed: " + t.getMessage(), t);
                    throw t;
                }
            }
        }

        throw new RuntimeException("This line shouldn't be executed.");
    }

    public static boolean join(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException ignored) {
            // No operations.
        }

        if (thread.isAlive()) {
            logger.warn(String.format("Can't join thread '%s'.", thread.getName()));
            return false;
        } else {
            logger.info(String.format("Successfully joined thread '%s'.", thread.getName()));
            return true;
        }
    }

    public static boolean join(Thread thread, long timeoutMillis) {
        try {
            thread.join(timeoutMillis);
        } catch (InterruptedException ignored) {
            // No operations.
        }

        if (thread.isAlive()) {
            logger.warn(String.format("Can't join thread '%s' in %d ms.", thread.getName(), timeoutMillis));
            return false;
        } else {
            logger.info(String.format("Successfully joined thread '%s'.", thread.getName()));
            return true;
        }
    }

    public static ThreadFactory getCustomPoolThreadFactory(ThreadCustomizer threadCustomizer) {
        return new ThreadFactory() {
            private final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

            @Nonnull
            @Override
            public Thread newThread(@Nonnull Runnable task) {
                Thread thread = defaultThreadFactory.newThread(task);
                threadCustomizer.customize(thread);
                return thread;
            }
        };
    }

    private static <T> void ensureArguments(Operation<T> operation, int attemptCount, ExecutionStrategy strategy) {
        if (operation == null) {
            throw new IllegalArgumentException("Argument 'operation' can't be 'null'.");
        }

        if (attemptCount < 1) {
            throw new IllegalArgumentException("Argument 'attemptCount' should be positive.");
        }

        if (strategy == null) {
            throw new IllegalArgumentException("Argument 'strategy' can't be 'null'.");
        }
    }

    public interface Operation<T> {
        @Nullable
        T run() throws Throwable;
    }

    /**
     * Action to be executed after each unsuccessful attempt to execute operation.
     */
    @SuppressWarnings("InterfaceNeverImplemented")
    public interface UnsuccessHandler {
        void handle(int attemptIndex, Throwable t);
    }

    public static class ExecutionStrategy {
        private final long delayTimeMillis;
        private final Type type;

        @Nullable
        private final UnsuccessHandler unsuccessHandler;

        public ExecutionStrategy(long delayTimeMillis, Type type) {
            this(delayTimeMillis, type, null);
        }

        public ExecutionStrategy(long delayTimeMillis, Type type, @Nullable UnsuccessHandler unsuccessHandler) {
            ensureArguments(delayTimeMillis, type);

            this.delayTimeMillis = delayTimeMillis;
            this.type = type;
            this.unsuccessHandler = unsuccessHandler;
        }

        private static void ensureArguments(long delayTimeMillis, Type type) {
            if (delayTimeMillis < 1) {
                throw new IllegalArgumentException("Argument 'delayTimeMillis' should be positive.");
            }

            if (type == null) {
                throw new IllegalArgumentException("Argument 'type' can't be 'null'.");
            }
        }

        public long getDelayTimeMillis() {
            return delayTimeMillis;
        }

        /**
         * @param attemptIndex 1-based attempt index.
         * @return Delay time according to attempt index and strategy type.
         */
        public long getDelayTimeMillis(int attemptIndex) {
            if (attemptIndex < 1) {
                throw new IllegalArgumentException("Argument 'attemptNumber' should be positive.");
            }

            switch (type) {
                case CONSTANT:
                    return delayTimeMillis;
                case LINEAR:
                    return delayTimeMillis * attemptIndex;
                case SQUARE:
                    return delayTimeMillis * attemptIndex * attemptIndex;
                default:
                    throw new IllegalArgumentException("Unknown strategy type '" + type + "'.");
            }
        }

        /**
         * @return Returns action to be executed after each unsuccessful attempt to execute operation.
         */
        @Nullable
        public UnsuccessHandler getUnsuccessHandler() {
            return unsuccessHandler;
        }

        public Type getType() {
            return type;
        }

        public enum Type {
            /**
             * Same delay interval between execution attempts.
             */
            CONSTANT,

            /**
             * Delay interval grows from attempt to attempt with a linear law.
             */
            LINEAR,

            /**
             * Delay interval grows from attempt to attempt with a square law.
             */
            SQUARE
        }
    }

    public interface ThreadCustomizer {
        void customize(Thread thread);
    }
}
