package com.codeforces.commons.holder;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 07.03.13
 */
public abstract class Mutable<T> implements Readable<T>, Writable<T> {
    public T computeIfAbsent(@Nonnull Readable<T> supplier) {
        T value = get();
        return value == null ? set(supplier.get()) : value;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Mutable && Objects.equals(get(), ((Readable) o).get());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(get());
    }

    @Override
    public String toString() {
        return String.valueOf(get());
    }
}
