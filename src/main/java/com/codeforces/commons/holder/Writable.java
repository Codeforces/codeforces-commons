package com.codeforces.commons.holder;

import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 07.03.13
 */
public interface Writable<T> {
    @Contract("null -> null; !null -> !null")
    @Nullable
    T set(@Nullable T value);
}
