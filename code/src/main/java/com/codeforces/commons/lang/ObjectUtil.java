package com.codeforces.commons.lang;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    public static <T> T toNonNull(@Nullable T value, @Nonnull T nullReplacement) {
        return value == null ? Preconditions.checkNotNull(nullReplacement) : value;
    }

    @Nonnull
    public static <T> T toNonNull(@Nullable T value, @Nonnull Supplier<T> nullReplacement) {
        return value == null ? Preconditions.checkNotNull(nullReplacement.get()) : value;
    }
}
