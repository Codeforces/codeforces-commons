package com.codeforces.commons.lang;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 11.09.2017
 */
public final class Flow {
    private Flow() {
        throw new UnsupportedOperationException();
    }

    public static void ifTrue(boolean condition, @Nonnull Runnable action) {
        if (condition) {
            action.run();
        }
    }

    public static void ifTrue(@Nonnull BooleanSupplier condition, @Nonnull Runnable action) {
        if (condition.getAsBoolean()) {
            action.run();
        }
    }

    public static void ifTrue(@Nonnull AtomicBoolean condition, @Nonnull Runnable action) {
        if (condition.get()) {
            action.run();
        }
    }

    public static void ifFalse(boolean condition, @Nonnull Runnable action) {
        if (!condition) {
            action.run();
        }
    }

    public static void ifFalse(@Nonnull BooleanSupplier condition, @Nonnull Runnable action) {
        if (!condition.getAsBoolean()) {
            action.run();
        }
    }

    public static void ifFalse(@Nonnull AtomicBoolean condition, @Nonnull Runnable action) {
        if (!condition.get()) {
            action.run();
        }
    }

    public static void ifElse(boolean condition, @Nonnull Runnable trueAction, @Nonnull Runnable falseAction) {
        (condition ? trueAction : falseAction).run();
    }

    public static void ifElse(@Nonnull BooleanSupplier condition,
                              @Nonnull Runnable trueAction, @Nonnull Runnable falseAction) {
        (condition.getAsBoolean() ? trueAction : falseAction).run();
    }

    public static void ifElse(@Nonnull AtomicBoolean condition,
                              @Nonnull Runnable trueAction, @Nonnull Runnable falseAction) {
        (condition.get() ? trueAction : falseAction).run();
    }

    public static <T> void forLoop(@Nonnull Supplier<T> initialValue, @Nonnull Predicate<T> condition,
                                   @Nonnull UnaryOperator<T> valueUpdate, @Nonnull Consumer<T> action) {
        for (T value = initialValue.get(); condition.test(value); value = valueUpdate.apply(value)) {
            action.accept(value);
        }
    }

    public static void forInt(@Nonnull IntSupplier initialValue, @Nonnull IntPredicate condition,
                              @Nonnull IntUnaryOperator valueUpdate, @Nonnull IntConsumer action) {
        for (int value = initialValue.getAsInt(); condition.test(value); value = valueUpdate.applyAsInt(value)) {
            action.accept(value);
        }
    }

    public static void forLong(@Nonnull LongSupplier initialValue, @Nonnull LongPredicate condition,
                               @Nonnull LongUnaryOperator valueUpdate, @Nonnull LongConsumer action) {
        for (long value = initialValue.getAsLong(); condition.test(value); value = valueUpdate.applyAsLong(value)) {
            action.accept(value);
        }
    }
}
