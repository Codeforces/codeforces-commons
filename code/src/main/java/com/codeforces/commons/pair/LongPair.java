package com.codeforces.commons.pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 02.06.2015
 */
public class LongPair extends Pair<Long, Long> {
    public LongPair() {
    }

    public LongPair(@Nullable Long first, @Nullable Long second) {
        super(first, second);
    }

    public LongPair(@Nonnull SimplePair<Long, Long> pair) {
        super(pair);
    }
}
