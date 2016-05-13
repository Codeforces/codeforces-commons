package com.codeforces.commons.holder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 07.03.13
 */
public class Holders {
    private Holders() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    public static <T> Readable<T> readOnly(@Nonnull Mutable<T> mutable) {
        return new Mutable<T>() {
            @Override
            public T get() {
                return mutable.get();
            }

            @SuppressWarnings("Contract")
            @Nonnull
            @Override
            public T set(T value) {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static <T> void setQuietly(@Nullable Writable<T> writable, @Nullable T value) {
        if (writable != null) {
            try {
                writable.set(value);
            } catch (RuntimeException ignored) {
                // No operations.
            }
        }
    }

    public static <T> void swap(@Nonnull Mutable<T> mutableA, @Nonnull Mutable<T> mutableB) {
        T valueA = mutableA.get();
        mutableA.set(mutableB.get());
        mutableB.set(valueA);
    }

    public static <T> void swapQuietly(@Nullable Mutable<T> mutableA, @Nullable Mutable<T> mutableB) {
        if (mutableA != null && mutableB != null) {
            try {
                swap(mutableA, mutableB);
            } catch (RuntimeException ignored) {
                // No operations.
            }
        }
    }
}
