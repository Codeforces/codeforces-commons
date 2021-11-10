package com.codeforces.commons.holder;

import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 07.03.13
 */
public interface Readable<T> {
    @Nullable
    T get();
}
