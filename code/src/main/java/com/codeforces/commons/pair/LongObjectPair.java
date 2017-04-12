package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 12.04.2017
 */
public class LongObjectPair<S> {
    private long first;

    @Nullable
    private S second;

    public LongObjectPair() {
    }

    public LongObjectPair(long first, @Nullable S second) {
        this.first = first;
        this.second = second;
    }

    public LongObjectPair(@Nonnull LongObjectPair<S> pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    public long getFirst() {
        return first;
    }

    public void setFirst(long first) {
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

        if (!(o instanceof LongObjectPair)) {
            return false;
        }

        LongObjectPair pair = (LongObjectPair) o;

        return first == pair.first
                && (second == null ? pair.second == null : second.equals(pair.second));
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return 32323 * Long.hashCode(first) + (second == null ? 0 : second.hashCode());
    }

    @Override
    public String toString() {
        return StringUtil.toString(this, false, "first", "second");
    }
}
