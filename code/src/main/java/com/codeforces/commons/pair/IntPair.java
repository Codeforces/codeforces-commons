package com.codeforces.commons.pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 11.07.13
 */
public class IntPair extends Pair<Integer, Integer> {
    public IntPair() {
    }

    public IntPair(@Nullable Integer first, @Nullable Integer second) {
        super(first, second);
    }

    public IntPair(@Nonnull SimplePair<Integer, Integer> pair) {
        super(pair);
    }
}
