package com.codeforces.commons.collection;

import gnu.trove.map.*;
import gnu.trove.map.hash.*;
import gnu.trove.set.*;
import gnu.trove.set.hash.*;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static gnu.trove.impl.Constants.DEFAULT_CAPACITY;
import static gnu.trove.impl.Constants.DEFAULT_LOAD_FACTOR;

/**
 * @author Edvard Davtyan (homo_sapiens@xakep.ru)
 */
public class CollectionUtil {
    private CollectionUtil() {
        throw new UnsupportedOperationException();
    }

    @Contract("null -> true")
    public static boolean isEmpty(@Nullable Collection collection) {
        return collection == null || collection.isEmpty();
    }

    @Contract("null, null -> true; null, !null -> false; !null, null -> false")
    public static boolean equals(@Nullable Collection collectionA, @Nullable Collection collectionB) {
        return collectionA == null ? collectionB == null : collectionA.equals(collectionB);
    }

    @Contract("null, null -> true")
    public static boolean equalsOrEmpty(@Nullable Collection collectionA, @Nullable Collection collectionB) {
        return isEmpty(collectionA) ? isEmpty(collectionB) : collectionA.equals(collectionB);
    }

    @Nonnull
    public static String toString(@Nullable Collection<?> collection) {
        StringBuilder result = new StringBuilder();

        if (collection == null) {
            result.append("<null>");
        } else {
            result.append('[');
            for (Object item : collection) {
                if (result.length() > "[".length()) {
                    result.append(", ");
                }
                result.append(item == null ? "<null>" : item.toString());
            }
            result.append(']');
        }

        return result.toString();
    }

    @Nonnull
    public static String toString(@Nullable Map<?, ?> map) {
        StringBuilder result = new StringBuilder();

        if (map == null) {
            result.append("<null>");
        } else {
            result.append('[');
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (result.length() > "[".length()) {
                    result.append(", ");
                }

                String key = e.getKey() == null ? "<null>" : e.getKey().toString();
                String value = e.getValue() == null ? "<null>" : e.getValue().toString();
                result.append(key).append("->").append(value);
            }
            result.append(']');
        }

        return result.toString();
    }

    public static <T1, T2> List<T2> convert(List<T1> list, Function<T1, T2> converter) {
        int count = list.size();
        List<T2> newList = new ArrayList<>(count);

        if (list instanceof RandomAccess) {
            for (int i = 0; i < count; ++i) {
                newList.add(converter.apply(list.get(i)));
            }
        } else {
            Iterator<T1> iterator = list.iterator();
            for (int i = 0; i < count; ++i) {
                newList.add(converter.apply(iterator.next()));
            }
        }

        return newList;
    }

    public static <T1, T2> Collection<T2> convert(Collection<T1> collection, Function<T1, T2> converter) {
        int count = collection.size();
        Collection<T2> newCollection = new ArrayList<>(count);

        Iterator<T1> iterator = collection.iterator();
        for (int i = 0; i < count; ++i) {
            newCollection.add(converter.apply(iterator.next()));
        }

        return newCollection;
    }

    public static <K1, T1, K2, T2> Map<K2, T2> convert(
            Map<K1, T1> map, Function<K1, K2> keyConverter, Function<T1, T2> valueConverter) {
        int count = map.size();
        Map<K2, T2> newMap = new LinkedHashMap<>(count);

        Iterator<Map.Entry<K1, T1>> iterator = map.entrySet().iterator();
        for (int i = 0; i < count; ++i) {
            Map.Entry<K1, T1> entry = iterator.next();
            newMap.put(keyConverter.apply(entry.getKey()), valueConverter.apply(entry.getValue()));
        }

        return newMap;
    }

    public static <T1, T2> List<T2> convertQuietly(List<T1> list, Function<T1, T2> converter) {
        int count = list.size();
        List<T2> newList = new ArrayList<>(count);

        if (list instanceof RandomAccess) {
            for (int i = 0; i < count; ++i) {
                T1 value = list.get(i);
                try {
                    newList.add(converter.apply(value));
                } catch (RuntimeException ignored) {
                    // No operations.
                }
            }
        } else {
            Iterator<T1> iterator = list.iterator();
            for (int i = 0; i < count; ++i) {
                T1 value = iterator.next();
                try {
                    newList.add(converter.apply(value));
                } catch (RuntimeException ignored) {
                    // No operations.
                }
            }
        }

        return newList;
    }

    public static <T1, T2> Collection<T2> convertQuietly(Collection<T1> collection, Function<T1, T2> converter) {
        int count = collection.size();
        Collection<T2> newCollection = new ArrayList<>(count);

        Iterator<T1> iterator = collection.iterator();
        for (int i = 0; i < count; ++i) {
            T1 value = iterator.next();
            try {
                newCollection.add(converter.apply(value));
            } catch (RuntimeException ignored) {
                // No operations.
            }
        }

        return newCollection;
    }

    public static <K1, T1, K2, T2> Map<K2, T2> convertQuietly(
            Map<K1, T1> map, Function<K1, K2> keyConverter, Function<T1, T2> valueConverter) {
        int count = map.size();
        Map<K2, T2> newMap = new LinkedHashMap<>(count);

        Iterator<Map.Entry<K1, T1>> iterator = map.entrySet().iterator();
        for (int i = 0; i < count; ++i) {
            Map.Entry<K1, T1> entry = iterator.next();
            try {
                newMap.put(keyConverter.apply(entry.getKey()), valueConverter.apply(entry.getValue()));
            } catch (RuntimeException ignored) {
                // No operations.
            }
        }

        return newMap;
    }

    public static <E> boolean addAll(Collection<E> collection, E[] array) {
        boolean modified = false;
        for (int i = 0, length = array.length; i < length; ++i) {
            modified |= collection.add(array[i]);
        }
        return modified;
    }

    public static <T> void swap(List<T> list, int indexA, int indexB) {
        T temp = list.get(indexA);
        list.set(indexA, list.get(indexB));
        list.set(indexB, temp);
    }

    @Nonnull
    public static TCharSet newTCharSet() {
        return new TCharHashSet(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Character.MIN_VALUE);
    }

    @Nonnull
    public static TCharSet newTCharSet(int capacity) {
        return new TCharHashSet(capacity, DEFAULT_LOAD_FACTOR, Character.MIN_VALUE);
    }

    @Nonnull
    public static TByteSet newTByteSet() {
        return new TByteHashSet(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TByteSet newTByteSet(int capacity) {
        return new TByteHashSet(capacity, DEFAULT_LOAD_FACTOR, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TShortSet newTShortSet() {
        return new TShortHashSet(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Short.MIN_VALUE);
    }

    @Nonnull
    public static TShortSet newTShortSet(int capacity) {
        return new TShortHashSet(capacity, DEFAULT_LOAD_FACTOR, Short.MIN_VALUE);
    }

    @Nonnull
    public static TIntSet newTIntSet() {
        return new TIntHashSet(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TIntSet newTIntSet(int capacity) {
        return new TIntHashSet(capacity, DEFAULT_LOAD_FACTOR, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TLongSet newTLongSet() {
        return new TLongHashSet(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Long.MIN_VALUE);
    }

    @Nonnull
    public static TLongSet newTLongSet(int capacity) {
        return new TLongHashSet(capacity, DEFAULT_LOAD_FACTOR, Long.MIN_VALUE);
    }

    @Nonnull
    public static TFloatSet newTFloatSet() {
        return new TFloatHashSet(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Float.NaN);
    }

    @Nonnull
    public static TFloatSet newTFloatSet(int capacity) {
        return new TFloatHashSet(capacity, DEFAULT_LOAD_FACTOR, Float.NaN);
    }

    @Nonnull
    public static TDoubleSet newTDoubleSet() {
        return new TDoubleHashSet(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Double.NaN);
    }

    @Nonnull
    public static TDoubleSet newTDoubleSet(int capacity) {
        return new TDoubleHashSet(capacity, DEFAULT_LOAD_FACTOR, Double.NaN);
    }

    @Nonnull
    public static TCharCharMap newTCharCharMap() {
        return new TCharCharHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Character.MIN_VALUE, Character.MIN_VALUE);
    }

    @Nonnull
    public static TCharCharMap newTCharCharMap(int capacity) {
        return new TCharCharHashMap(capacity, DEFAULT_LOAD_FACTOR, Character.MIN_VALUE, Character.MIN_VALUE);
    }

    @Nonnull
    public static <V> TCharObjectMap<V> newTCharObjectMap() {
        return new TCharObjectHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Character.MIN_VALUE);
    }

    @Nonnull
    public static <V> TCharObjectMap<V> newTCharObjectMap(int capacity) {
        return new TCharObjectHashMap<>(capacity, DEFAULT_LOAD_FACTOR, Character.MIN_VALUE);
    }

    @Nonnull
    public static <V> TObjectCharMap<V> newTObjectCharMap() {
        return new TObjectCharHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Character.MIN_VALUE);
    }

    @Nonnull
    public static <V> TObjectCharMap<V> newTObjectCharMap(int capacity) {
        return new TObjectCharHashMap<>(capacity, DEFAULT_LOAD_FACTOR, Character.MIN_VALUE);
    }

    @Nonnull
    public static TByteByteMap newTByteByteMap() {
        return new TByteByteHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Byte.MIN_VALUE, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TByteByteMap newTByteByteMap(int capacity) {
        return new TByteByteHashMap(capacity, DEFAULT_LOAD_FACTOR, Byte.MIN_VALUE, Byte.MIN_VALUE);
    }

    @Nonnull
    public static <V> TByteObjectMap<V> newTByteObjectMap() {
        return new TByteObjectHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Byte.MIN_VALUE);
    }

    @Nonnull
    public static <V> TByteObjectMap<V> newTByteObjectMap(int capacity) {
        return new TByteObjectHashMap<>(capacity, DEFAULT_LOAD_FACTOR, Byte.MIN_VALUE);
    }

    @Nonnull
    public static <V> TObjectByteMap<V> newTObjectByteMap() {
        return new TObjectByteHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Byte.MIN_VALUE);
    }

    @Nonnull
    public static <V> TObjectByteMap<V> newTObjectByteMap(int capacity) {
        return new TObjectByteHashMap<>(capacity, DEFAULT_LOAD_FACTOR, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TShortShortMap newTShortShortMap() {
        return new TShortShortHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Short.MIN_VALUE, Short.MIN_VALUE);
    }

    @Nonnull
    public static TShortShortMap newTShortShortMap(int capacity) {
        return new TShortShortHashMap(capacity, DEFAULT_LOAD_FACTOR, Short.MIN_VALUE, Short.MIN_VALUE);
    }

    @Nonnull
    public static <V> TShortObjectMap<V> newTShortObjectMap() {
        return new TShortObjectHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Short.MIN_VALUE);
    }

    @Nonnull
    public static <V> TShortObjectMap<V> newTShortObjectMap(int capacity) {
        return new TShortObjectHashMap<>(capacity, DEFAULT_LOAD_FACTOR, Short.MIN_VALUE);
    }

    @Nonnull
    public static <V> TObjectShortMap<V> newTObjectShortMap() {
        return new TObjectShortHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Short.MIN_VALUE);
    }

    @Nonnull
    public static <V> TObjectShortMap<V> newTObjectShortMap(int capacity) {
        return new TObjectShortHashMap<>(capacity, DEFAULT_LOAD_FACTOR, Short.MIN_VALUE);
    }

    @Nonnull
    public static TIntIntMap newTIntIntMap() {
        return new TIntIntHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TIntIntMap newTIntIntMap(int capacity) {
        return new TIntIntHashMap(capacity, DEFAULT_LOAD_FACTOR, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Nonnull
    public static <V> TIntObjectMap<V> newTIntObjectMap() {
        return new TIntObjectHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Integer.MIN_VALUE);
    }

    @Nonnull
    public static <V> TIntObjectMap<V> newTIntObjectMap(int capacity) {
        return new TIntObjectHashMap<>(capacity, DEFAULT_LOAD_FACTOR, Integer.MIN_VALUE);
    }

    @Nonnull
    public static <V> TObjectIntMap<V> newTObjectIntMap() {
        return new TObjectIntHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Integer.MIN_VALUE);
    }

    @Nonnull
    public static <V> TObjectIntMap<V> newTObjectIntMap(int capacity) {
        return new TObjectIntHashMap<>(capacity, DEFAULT_LOAD_FACTOR, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TLongLongMap newTLongLongMap() {
        return new TLongLongHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Long.MIN_VALUE, Long.MIN_VALUE);
    }

    @Nonnull
    public static TLongLongMap newTLongLongMap(int capacity) {
        return new TLongLongHashMap(capacity, DEFAULT_LOAD_FACTOR, Long.MIN_VALUE, Long.MIN_VALUE);
    }

    @Nonnull
    public static <V> TLongObjectMap<V> newTLongObjectMap() {
        return new TLongObjectHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Long.MIN_VALUE);
    }

    @Nonnull
    public static <V> TLongObjectMap<V> newTLongObjectMap(int capacity) {
        return new TLongObjectHashMap<>(capacity, DEFAULT_LOAD_FACTOR, Long.MIN_VALUE);
    }

    @Nonnull
    public static <V> TObjectLongMap<V> newTObjectLongMap() {
        return new TObjectLongHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Long.MIN_VALUE);
    }

    @Nonnull
    public static <V> TObjectLongMap<V> newTObjectLongMap(int capacity) {
        return new TObjectLongHashMap<>(capacity, DEFAULT_LOAD_FACTOR, Long.MIN_VALUE);
    }

    @Nonnull
    public static TFloatFloatMap newTFloatFloatMap() {
        return new TFloatFloatHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Float.NaN, Float.NaN);
    }

    @Nonnull
    public static TFloatFloatMap newTFloatFloatMap(int capacity) {
        return new TFloatFloatHashMap(capacity, DEFAULT_LOAD_FACTOR, Float.NaN, Float.NaN);
    }

    @Nonnull
    public static <V> TFloatObjectMap<V> newTFloatObjectMap() {
        return new TFloatObjectHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Float.NaN);
    }

    @Nonnull
    public static <V> TFloatObjectMap<V> newTFloatObjectMap(int capacity) {
        return new TFloatObjectHashMap<>(capacity, DEFAULT_LOAD_FACTOR, Float.NaN);
    }

    @Nonnull
    public static <V> TObjectFloatMap<V> newTObjectFloatMap() {
        return new TObjectFloatHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Float.NaN);
    }

    @Nonnull
    public static <V> TObjectFloatMap<V> newTObjectFloatMap(int capacity) {
        return new TObjectFloatHashMap<>(capacity, DEFAULT_LOAD_FACTOR, Float.NaN);
    }

    @Nonnull
    public static TDoubleDoubleMap newTDoubleDoubleMap() {
        return new TDoubleDoubleHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Double.NaN, Double.NaN);
    }

    @Nonnull
    public static TDoubleDoubleMap newTDoubleDoubleMap(int capacity) {
        return new TDoubleDoubleHashMap(capacity, DEFAULT_LOAD_FACTOR, Double.NaN, Double.NaN);
    }

    @Nonnull
    public static <V> TDoubleObjectMap<V> newTDoubleObjectMap() {
        return new TDoubleObjectHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Double.NaN);
    }

    @Nonnull
    public static <V> TDoubleObjectMap<V> newTDoubleObjectMap(int capacity) {
        return new TDoubleObjectHashMap<>(capacity, DEFAULT_LOAD_FACTOR, Double.NaN);
    }

    @Nonnull
    public static <V> TObjectDoubleMap<V> newTObjectDoubleMap() {
        return new TObjectDoubleHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Double.NaN);
    }

    @Nonnull
    public static <V> TObjectDoubleMap<V> newTObjectDoubleMap(int capacity) {
        return new TObjectDoubleHashMap<>(capacity, DEFAULT_LOAD_FACTOR, Double.NaN);
    }

    public static class CollectionComparator<T extends Comparable<T>> implements Comparator<Collection<T>> {
        @Override
        public int compare(Collection<T> o1, Collection<T> o2) {
            Iterator<T> iterator1 = o1.iterator();
            Iterator<T> iterator2 = o2.iterator();
            while (iterator1.hasNext() && iterator2.hasNext()) {
                int compareResult = iterator1.next().compareTo(iterator2.next());
                if (compareResult != 0) {
                    return compareResult;
                }
            }
            if (iterator1.hasNext()) {
                return 1;
            }
            if (iterator2.hasNext()) {
                return -1;
            }
            return 0;
        }
    }
}
