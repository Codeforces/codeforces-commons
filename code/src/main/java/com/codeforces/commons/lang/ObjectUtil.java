package com.codeforces.commons.lang;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 10.03.2017
 */
public class ObjectUtil {
    private ObjectUtil() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    public static <T> T toNotNull(@Nullable T value, @Nonnull T nullReplacement) {
        return value == null ? Preconditions.checkNotNull(nullReplacement) : value;
    }

    @Nonnull
    public static <T> T toNotNull(@Nullable T value, @Nonnull Supplier<T> nullReplacement) {
        return value == null ? Preconditions.checkNotNull(nullReplacement.get()) : value;
    }

    public static <T> void ifNotNull(@Nullable T value, @Nonnull Consumer<T> valueConsumer) {
        if (value != null) {
            valueConsumer.accept(value);
        }
    }

    @Contract("null, _ -> null")
    @Nullable
    public static <T, R> R mapNotNull(@Nullable T value, @Nonnull Function<T, R> valueMapper) {
        return value == null ? null : valueMapper.apply(value);
    }
}
