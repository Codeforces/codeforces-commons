package com.codeforces.commons.pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 28.07.2016
 */
@SuppressWarnings("ProtectedField")
public class SimplePair<F, S> {
    @Nullable
    protected F first;

    @Nullable
    protected S second;

    public SimplePair() {
    }

    public SimplePair(@Nullable F first, @Nullable S second) {
        this.first = first;
        this.second = second;
    }

    public SimplePair(@Nonnull SimplePair<F, S> pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    @Nullable
    public F getFirst() {
        return first;
    }

    public void setFirst(@Nullable F first) {
        this.first = first;
    }

    @Nullable
    public S getSecond() {
        return second;
    }

    public void setSecond(@Nullable S second) {
        this.second = second;
    }
}
