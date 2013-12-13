package com.codeforces.commons.holder;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 07.03.13
 */
public abstract class Mutable<T> implements Readable<T>, Writable<T> {
    @Override
    public boolean equals(Object o) {
        return this == o
                || o instanceof Mutable
                && (get() == null ? ((Readable) o).get() == null : get().equals(((Readable) o).get()));
    }

    @Override
    public int hashCode() {
        return get() == null ? 0 : get().hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(get());
    }
}
