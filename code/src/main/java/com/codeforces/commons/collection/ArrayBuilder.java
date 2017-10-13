package com.codeforces.commons.collection;

import com.google.common.base.Preconditions;
import org.apache.commons.collections4.EnumerationUtils;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 17.04.2017
 */
@SuppressWarnings("WeakerAccess")
@NotThreadSafe
public class ArrayBuilder<E> {
    @Nonnull
    private E[] array;

    @SuppressWarnings("unchecked")
    public ArrayBuilder(@Nonnull Class<E> elementClass) {
        try {
            this.array = (E[]) Array.newInstance(elementClass, 0);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(String.format(
                    "Can not instantiate %s using the provided element class '%s'.",
                    ArrayBuilder.class.getSimpleName(), elementClass
            ), e);
        }
    }

    @SuppressWarnings({"unchecked", "OverloadedVarargsMethod"})
    @SafeVarargs
    public ArrayBuilder(@Nonnull E... elements) {
        int length = elements.length;
        this.array = (E[]) Array.newInstance(elements.getClass().getComponentType(), length);
        System.arraycopy(elements, 0, this.array, 0, length);
    }

    public ArrayBuilder<E> add(E element) {
        int length = array.length;
        array = expandArray(length, length + 1);
        array[length] = element;
        return this;
    }

    public <A extends E> ArrayBuilder<E> addAll(@Nonnull Collection<A> collection) {
        int length = array.length;
        int newLength = length + collection.size();
        E[] newArray = expandArray(length, newLength);

        Iterator<A> iterator = collection.iterator();

        for (int i = length; i < newLength; ++i) {
            newArray[i] = iterator.next();
        }

        array = newArray;
        return this;
    }

    public <A extends E> ArrayBuilder<E> addAll(@Nonnull Enumeration<A> enumeration) {
        addAll(EnumerationUtils.toList(enumeration));
        return this;
    }

    public <A extends E> ArrayBuilder<E> addAll(@Nonnull Collection<A> collection, @Nonnull ElementFilter<A> filter) {
        addAll(collection.stream().filter(filter::matches).collect(Collectors.toList()));
        return this;
    }

    public <A extends E> ArrayBuilder<E> addAll(@Nonnull Enumeration<A> enumeration, @Nonnull ElementFilter<A> filter) {
        addAll(EnumerationUtils.toList(enumeration).stream().filter(filter::matches).collect(Collectors.toList()));
        return this;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @SafeVarargs
    public final ArrayBuilder<E> addAll(@Nonnull E... elements) {
        int elementCount = elements.length;
        if (elementCount == 0) {
            return this;
        }

        int length = array.length;
        int newLength = length + elementCount;
        E[] newArray = expandArray(length, newLength);

        for (int i = elementCount; --i >= 0; ) {
            newArray[length + i] = elements[i];
        }

        array = newArray;
        return this;
    }

    public ArrayBuilder<E> addMany(E element, @Nonnegative int count) {
        if (count == 0) {
            return this;
        }

        Preconditions.checkArgument(count > 0, "The count %s is negative.");

        int length = array.length;
        int newLength = length + count;
        E[] newArray = expandArray(length, newLength);

        for (int i = length; i < newLength; ++i) {
            newArray[i] = element;
        }

        array = newArray;
        return this;
    }

    public ArrayBuilder<E> set(int index, E element) {
        if (index < 0) {
            array[array.length + index] = element;
        } else {
            array[index] = element;
        }
        return this;
    }

    public ArrayBuilder<E> setMany(int index, E element, @Nonnegative int count) {
        if (count == 0) {
            return this;
        }

        Preconditions.checkArgument(count > 0, "The count %s is negative.");

        @SuppressWarnings("LocalVariableHidesMemberVariable") E[] array = this.array;

        if (index < 0) {
            int length = array.length;
            int minIndex = length + index;
            int i = minIndex + count;

            while (--i >= minIndex) {
                array[i] = element;
            }
        } else {
            int i = index + count;

            while (--i >= index) {
                array[i] = element;
            }
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    private E[] expandArray(@Nonnegative int length, @Nonnegative int newLength) {
        Preconditions.checkArgument(
                newLength >= length, "The new length %s is less than current length %s.", newLength, length
        );

        E[] newArray = (E[]) Array.newInstance(array.getClass().getComponentType(), newLength);
        System.arraycopy(array, 0, newArray, 0, length);
        return newArray;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    @Nonnull
    public E[] build() {
        return array;
    }

    @SuppressWarnings({"unchecked", "ReturnOfCollectionOrArrayField"})
    @Nonnull
    public E[] build(boolean forceCopy) {
        if (forceCopy) {
            int length = array.length;
            E[] copiedArray = (E[]) Array.newInstance(array.getClass().getComponentType(), length);
            System.arraycopy(array, 0, copiedArray, 0, length);
            return copiedArray;
        }

        return array;
    }
}
