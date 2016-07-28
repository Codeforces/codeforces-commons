package com.codeforces.commons.pair;

import com.codeforces.commons.text.StringUtil;

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

    public boolean equals(@Nullable F first, @Nullable S second) {
        return (this.first == null ? first == null : this.first.equals(first))
                && (this.second == null ? second == null : this.second.equals(second));
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof SimplePair)) {
            return false;
        }

        SimplePair pair = (SimplePair) o;

        return (first == null ? pair.first == null : first.equals(pair.first))
                && (second == null ? pair.second == null : second.equals(pair.second));
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        int result = first == null ? 0 : first.hashCode();
        result = 32323 * result + (second == null ? 0 : second.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return StringUtil.toString(this, false, "first", "second");
    }
}
