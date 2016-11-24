package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 24.11.2016
 */
public class IntObjectPair<S> {
    private int first;

    @Nullable
    private S second;

    public IntObjectPair() {
    }

    public IntObjectPair(int first, @Nullable S second) {
        this.first = first;
        this.second = second;
    }

    public IntObjectPair(@Nonnull IntObjectPair<S> pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
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

        if (!(o instanceof IntObjectPair)) {
            return false;
        }

        IntObjectPair pair = (IntObjectPair) o;

        return first == pair.first
                && (second == null ? pair.second == null : second.equals(pair.second));
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return 32323 * first + (second == null ? 0 : second.hashCode());
    }

    @Override
    public String toString() {
        return StringUtil.toString(this, false, "first", "second");
    }
}
