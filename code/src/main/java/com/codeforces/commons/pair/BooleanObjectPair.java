package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 14.04.2017
 */
public class BooleanObjectPair<S> {
    private boolean first;

    @Nullable
    private S second;

    public BooleanObjectPair() {
    }

    public BooleanObjectPair(boolean first, @Nullable S second) {
        this.first = first;
        this.second = second;
    }

    public BooleanObjectPair(@Nonnull BooleanObjectPair<S> pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    public boolean getFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    @Nullable
    public S getSecond() {
        return second;
    }

    public void setSecond(@Nullable S second) {
        this.second = second;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BooleanObjectPair)) {
            return false;
        }

        BooleanObjectPair pair = (BooleanObjectPair) o;

        return first == pair.first && (second == null ? pair.second == null : second.equals(pair.second));
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return 32323 * Boolean.hashCode(first) + (second == null ? 0 : second.hashCode());
    }

    @Override
    public String toString() {
        return StringUtil.toString(this, false, "first", "second");
    }
}
