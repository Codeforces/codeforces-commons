package com.codeforces.commons.holder;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 07.03.13
 */
public abstract class Mutable<T> implements Readable<T>, Writable<T> {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Mutable)) {
            return false;
        }

        T value = get();
        return value == null ? ((Readable) o).get() == null : value.equals(((Readable) o).get());
    }

    @Override
    public int hashCode() {
        T value = get();
        return value == null ? 0 : value.hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(get());
    }
}
