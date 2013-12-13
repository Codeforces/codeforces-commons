package com.codeforces.commons.pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 11.07.13
 */
public class StringPair extends Pair<String, String> {
    public StringPair() {
    }

    public StringPair(@Nullable String first, @Nullable String second) {
        super(first, second);
    }

    public StringPair(@Nonnull SimplePair<String, String> pair) {
        super(pair);
    }
}
