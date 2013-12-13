package com.codeforces.commons.pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 11.07.13
 */
public class DoublePair extends Pair<Double, Double> {
    public DoublePair() {
    }

    public DoublePair(@Nullable Double first, @Nullable Double second) {
        super(first, second);
    }

    public DoublePair(@Nonnull SimplePair<Double, Double> pair) {
        super(pair);
    }
}
