package com.codeforces.commons.lang;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 11.09.2017
 */
public final class FlowUtil {
    private FlowUtil() {
        throw new UnsupportedOperationException();
    }

    public static void ifThen(boolean condition, @Nonnull Runnable action) {
        if (condition) {
            action.run();
        }
    }

    public static void ifThen(@Nonnull BooleanSupplier condition, @Nonnull Runnable action) {
        if (condition.getAsBoolean()) {
            action.run();
        }
    }

    public static void ifElse(boolean condition, @Nonnull Runnable trueAction, @Nonnull Runnable falseAction) {
        (condition ? trueAction : falseAction).run();
    }

    public static void ifElse(
            @Nonnull BooleanSupplier condition, @Nonnull Runnable trueAction, @Nonnull Runnable falseAction) {
        (condition.getAsBoolean() ? trueAction : falseAction).run();
    }
}
