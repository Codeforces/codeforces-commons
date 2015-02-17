package com.codeforces.commons.holder;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 17.02.15
 */
public class DummyMutable<T> extends Mutable<T> {
    private static final DummyMutable<?> INSTANCE = new DummyMutable<>();

    @SuppressWarnings("unchecked")
    public static <T> DummyMutable<T> getInstance() {
        return (DummyMutable<T>) INSTANCE;
    }

    private DummyMutable() {
    }

    @Override
    public T get() {
        return null;
    }

    @Override
    public T set(T value) {
        return value;
    }
}
