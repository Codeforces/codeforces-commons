package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 12.04.2017
 */
public class FloatObjectPair<S> {
    private float first;

    @Nullable
    private S second;

    public FloatObjectPair() {
    }

    public FloatObjectPair(float first, @Nullable S second) {
        this.first = first;
        this.second = second;
    }

    public FloatObjectPair(@Nonnull FloatObjectPair<S> pair) {
        this.first = pair.first;
        this.second = pair.second;
    }

    public float getFirst() {
        return first;
    }

    public void setFirst(float first) {
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

        if (!(o instanceof FloatObjectPair)) {
            return false;
        }

        FloatObjectPair pair = (FloatObjectPair) o;

        return Float.compare(first, pair.first) == 0
                && (second == null ? pair.second == null : second.equals(pair.second));
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return 32323 * Float.hashCode(first) + (second == null ? 0 : second.hashCode());
    }

    @Override
    public String toString() {
        return StringUtil.toString(this, false, "first", "second");
    }
}
