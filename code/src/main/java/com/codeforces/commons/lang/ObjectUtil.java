package com.codeforces.commons.lang;

import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.*;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 10.03.2017
 */
@SuppressWarnings("WeakerAccess")
public class ObjectUtil {
    private ObjectUtil() {
        throw new UnsupportedOperationException();
    }

    @Contract("null, null -> fail")
    @Nonnull
    public static <T> T toNotNull(@Nullable T value, @Nullable T nullReplacement) {
        return value == null ? Objects.requireNonNull(nullReplacement) : value;
    }

    @Contract("null, null, null -> fail")
    @Nonnull
    public static <T> T toNotNull(@Nullable T value, @Nullable T nullReplacementA, @Nullable T nullReplacementB) {
        return value == null ? toNotNull(nullReplacementA, nullReplacementB) : value;
    }

    @Contract("null, null -> fail")
    @Nonnull
    public static <T> T toNotNull(@Nullable T value, @Nonnull Supplier<T> nullReplacement) {
        return value == null ? Objects.requireNonNull(nullReplacement.get()) : value;
    }

    @Contract("null, null, _ -> fail")
    @Nonnull
    public static <T> T toNotNull(
            @Nullable T value, @Nonnull Supplier<T> nullReplacementA, @Nonnull Supplier<T> nullReplacementB) {
        return value == null ? toNotNull(nullReplacementA.get(), nullReplacementB) : value;
    }

    @Contract("null, null, _ -> fail")
    @Nonnull
    public static <T> T toNotNull(
            @Nullable T value, @Nonnull Supplier<T> nullReplacementA, @Nullable T nullReplacementB) {
        return value == null ? toNotNull(nullReplacementA.get(), nullReplacementB) : value;
    }

    @Contract("null, null, null -> fail")
    @Nonnull
    public static <T> T toNotNull(
            @Nullable T value, @Nullable T nullReplacementA, @Nonnull Supplier<T> nullReplacementB) {
        return value == null ? toNotNull(nullReplacementA, nullReplacementB) : value;
    }

    @Contract("null, null, null, null -> fail")
    @Nonnull
    public static <T> T toNotNull(
            @Nullable T value, @Nullable T nullReplacementA, @Nullable T nullReplacementB,
            @Nullable T nullReplacementC) {
        return value == null ? toNotNull(nullReplacementA, nullReplacementB, nullReplacementC) : value;
    }

    @Contract("null, null, null, null -> fail")
    @Nonnull
    public static <T> T toNotNull(
            @Nullable T value, @Nullable T nullReplacementA, @Nullable T nullReplacementB,
            @Nonnull Supplier<T> nullReplacementC) {
        return value == null ? toNotNull(nullReplacementA, nullReplacementB, nullReplacementC) : value;
    }

    @Contract("null, null, _, _ -> fail")
    @Nonnull
    public static <T> T toNotNull(
            @Nullable T value, @Nonnull Supplier<T> nullReplacementA, @Nonnull Supplier<T> nullReplacementB,
            @Nullable T nullReplacementC) {
        return value == null ? toNotNull(nullReplacementA.get(), nullReplacementB, nullReplacementC) : value;
    }

    @Contract("null, null, _, _ -> fail")
    @Nonnull
    public static <T> T toNotNull(
            @Nullable T value, @Nonnull Supplier<T> nullReplacementA, @Nonnull Supplier<T> nullReplacementB,
            @Nonnull Supplier<T> nullReplacementC) {
        return value == null ? toNotNull(nullReplacementA.get(), nullReplacementB, nullReplacementC) : value;
    }

    @Contract("null, null, _, _ -> fail")
    @Nonnull
    public static <T> T toNotNull(
            @Nullable T value, @Nonnull Supplier<T> nullReplacementA, @Nullable T nullReplacementB,
            @Nullable T nullReplacementC) {
        return value == null ? toNotNull(nullReplacementA.get(), nullReplacementB, nullReplacementC) : value;
    }

    @Contract("null, null, _, _ -> fail")
    @Nonnull
    public static <T> T toNotNull(
            @Nullable T value, @Nonnull Supplier<T> nullReplacementA, @Nullable T nullReplacementB,
            @Nonnull Supplier<T> nullReplacementC) {
        return value == null ? toNotNull(nullReplacementA.get(), nullReplacementB, nullReplacementC) : value;
    }

    @Contract("null, null, null, _ -> fail")
    @Nonnull
    public static <T> T toNotNull(
            @Nullable T value, @Nullable T nullReplacementA, @Nonnull Supplier<T> nullReplacementB,
            @Nullable T nullReplacementC) {
        return value == null ? toNotNull(nullReplacementA, nullReplacementB, nullReplacementC) : value;
    }

    @Contract("null, null, null, _ -> fail")
    @Nonnull
    public static <T> T toNotNull(
            @Nullable T value, @Nullable T nullReplacementA, @Nonnull Supplier<T> nullReplacementB,
            @Nonnull Supplier<T> nullReplacementC) {
        return value == null ? toNotNull(nullReplacementA, nullReplacementB, nullReplacementC) : value;
    }

    @Contract("null, null -> null; !null, _ -> !null; _, !null -> !null")
    @Nullable
    public static <T> T defaultIfNull(@Nullable T value, @Nullable T nullReplacement) {
        return value == null ? nullReplacement : value;
    }

    @Contract("null, null, null -> null; !null, _, _ -> !null; _, !null, _ -> !null; _, _ , !null -> !null")
    @Nullable
    public static <T> T defaultIfNull(@Nullable T value, @Nullable T nullReplacementA, @Nullable T nullReplacementB) {
        return value == null ? defaultIfNull(nullReplacementA, nullReplacementB) : value;
    }

    @Contract("null, null, null, null -> null; !null, _, _, _ -> !null; _, !null, _, _ -> !null; _, _ , !null, _ -> !null; _, _ , _, !null -> !null")
    @Nullable
    public static <T> T defaultIfNull(
            @Nullable T value, @Nullable T nullReplacementA, @Nullable T nullReplacementB,
            @Nullable T nullReplacementC) {
        return value == null ? defaultIfNull(nullReplacementA, nullReplacementB, nullReplacementC) : value;
    }

    @Contract("null, null -> fail; !null, _ -> !null")
    @Nullable
    public static <T> T computeIfNull(@Nullable T value, @Nonnull Supplier<T> nullReplacement) {
        return value == null ? nullReplacement.get() : value;
    }

    @Contract("null, null, _ -> fail; !null, _, _ -> !null")
    @Nullable
    public static <T> T computeIfNull(
            @Nullable T value, @Nonnull Supplier<T> nullReplacementA, @Nonnull Supplier<T> nullReplacementB) {
        return value == null ? computeIfNull(nullReplacementA.get(), nullReplacementB) : value;
    }

    @Contract("null, null, _, _ -> fail; !null, _, _, _ -> !null")
    @Nullable
    public static <T> T computeIfNull(
            @Nullable T value, @Nonnull Supplier<T> nullReplacementA, @Nonnull Supplier<T> nullReplacementB,
            @Nonnull Supplier<T> nullReplacementC) {
        return value == null ? computeIfNull(nullReplacementA.get(), nullReplacementB, nullReplacementC) : value;
    }

    @Contract("!null, null -> fail")
    public static <T> void ifNotNull(@Nullable T value, @Nonnull Consumer<T> valueConsumer) {
        if (value != null) {
            valueConsumer.accept(value);
        }
    }

    @Contract("null, _ -> null; !null, null -> fail")
    @Nullable
    public static <T, R> R mapNotNull(@Nullable T value, @Nonnull Function<T, R> valueMapper) {
        return value == null ? null : valueMapper.apply(value);
    }

    @SuppressWarnings("unchecked")
    @Contract("null, _ -> null; !null, null -> fail")
    @Nullable
    public static <T, R extends T> R as(@Nullable T value, @Nonnull Class<R> targetClass) {
        return targetClass.isInstance(value) ? (R) value : null;
    }

    @SuppressWarnings({"ObjectEquality", "StandardVariableNames"})
    @Contract(value = "null, null -> true; null, !null -> false; !null, null -> false", pure = true)
    public static boolean referenceEquals(@Nullable Object a, @Nullable Object b) {
        return a == b;
    }
}
