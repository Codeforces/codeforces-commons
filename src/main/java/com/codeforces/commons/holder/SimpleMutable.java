package com.codeforces.commons.holder;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 06.03.13
 */
public class SimpleMutable<T> extends Mutable<T> {
    private T value;

    public SimpleMutable() {
    }

    public SimpleMutable(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public T set(T value) {
        return this.value = value;
    }
}
