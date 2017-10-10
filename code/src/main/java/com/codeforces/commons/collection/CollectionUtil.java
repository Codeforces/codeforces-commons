package com.codeforces.commons.collection;

import com.google.common.collect.*;
import gnu.trove.map.*;
import gnu.trove.map.hash.*;
import gnu.trove.set.*;
import gnu.trove.set.hash.*;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static gnu.trove.impl.Constants.*;

/**
 * @author Edvard Davtyan (homo_sapiens@xakep.ru)
 */
@SuppressWarnings("ForLoopWithMissingComponent")
public class CollectionUtil {
    private static final Class unmodifiableCollectionClass = Collections.unmodifiableCollection(new ArrayList<>()).getClass();

    private static final Class unmodifiableRandomAccessListClass = Collections.unmodifiableList(new ArrayList<>()).getClass();
    private static final Class unmodifiableListClass = Collections.unmodifiableList(new LinkedList<>()).getClass();

    private static final Class unmodifiableSetClass = Collections.unmodifiableSet(new HashSet<>()).getClass();
    private static final Class unmodifiableNavigableSetClass = Collections.unmodifiableNavigableSet(new TreeSet<>()).getClass();
    private static final Class unmodifiableSortedSetClass = Collections.unmodifiableSortedSet(new TreeSet<>()).getClass();

    private static final Class unmodifiableMapClass = Collections.unmodifiableMap(new HashMap<>()).getClass();
    private static final Class unmodifiableNavigableMapClass = Collections.unmodifiableNavigableMap(new TreeMap<>()).getClass();
    private static final Class unmodifiableSortedMapClass = Collections.unmodifiableSortedMap(new TreeMap<>()).getClass();

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
            for (int i = count; --i >= 0; ) {
                newList.add(converter.apply(iterator.next()));
            }
        }

        return newList;
    }

    public static <T1, T2> List<T2> convert(Collection<T1> collection, Function<T1, T2> converter) {
        int count = collection.size();
        List<T2> newCollection = new ArrayList<>(count);

        Iterator<T1> iterator = collection.iterator();
        for (int i = count; --i >= 0; ) {
            newCollection.add(converter.apply(iterator.next()));
        }

        return newCollection;
    }

    public static <K1, T1, K2, T2> Map<K2, T2> convert(
            Map<K1, T1> map, Function<K1, K2> keyConverter, Function<T1, T2> valueConverter) {
        int count = map.size();
        Map<K2, T2> newMap = new LinkedHashMap<>(count);

        Iterator<Map.Entry<K1, T1>> iterator = map.entrySet().iterator();
        for (int i = count; --i >= 0; ) {
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
            for (int i = count; --i >= 0; ) {
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
        for (int i = count; --i >= 0; ) {
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
        for (int i = count; --i >= 0; ) {
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

    @Contract("null -> false")
    public static boolean isUnmodifiable(@Nullable Collection collection) {
        if (collection == null) {
            return false;
        }

        Class collectionClass = collection.getClass();

        return collectionClass == unmodifiableCollectionClass || collectionClass == unmodifiableRandomAccessListClass
                || collectionClass == unmodifiableListClass || collectionClass == unmodifiableSetClass
                || collectionClass == unmodifiableNavigableSetClass || collectionClass == unmodifiableSortedSetClass
                || ImmutableList.class.isAssignableFrom(collectionClass)
                || ImmutableSet.class.isAssignableFrom(collectionClass);
    }

    @Contract("null -> false")
    public static boolean isUnmodifiable(@Nullable List list) {
        if (list == null) {
            return false;
        }

        Class listClass = list.getClass();

        return listClass == unmodifiableRandomAccessListClass || listClass == unmodifiableListClass
                || ImmutableList.class.isAssignableFrom(listClass);
    }

    @Contract("null -> false")
    public static boolean isUnmodifiable(@Nullable Set set) {
        if (set == null) {
            return false;
        }

        Class setClass = set.getClass();

        return setClass == unmodifiableSetClass || setClass == unmodifiableNavigableSetClass
                || setClass == unmodifiableSortedSetClass || ImmutableSet.class.isAssignableFrom(setClass);
    }

    @Contract("null -> false")
    public static boolean isUnmodifiable(@Nullable Map map) {
        if (map == null) {
            return false;
        }

        Class mapClass = map.getClass();

        return mapClass == unmodifiableMapClass || mapClass == unmodifiableNavigableMapClass
                || mapClass == unmodifiableSortedMapClass || ImmutableMap.class.isAssignableFrom(mapClass);
    }

    @Nonnull
    public static TCharSet newTCharSet() {
        return new TCharHashSet(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (char) 0);
    }

    @Nonnull
    public static TCharSet newTCharSet(int capacity) {
        return new TCharHashSet(capacity, DEFAULT_LOAD_FACTOR, (char) 0);
    }

    @Nonnull
    public static TByteSet newTByteSet() {
        return new TByteHashSet(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (byte) 0);
    }

    @Nonnull
    public static TByteSet newTByteSet(int capacity) {
        return new TByteHashSet(capacity, DEFAULT_LOAD_FACTOR, (byte) 0);
    }

    @Nonnull
    public static TShortSet newTShortSet() {
        return new TShortHashSet(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (short) 0);
    }

    @Nonnull
    public static TShortSet newTShortSet(int capacity) {
        return new TShortHashSet(capacity, DEFAULT_LOAD_FACTOR, (short) 0);
    }

    @Nonnull
    public static TIntSet newTIntSet() {
        return new TIntHashSet(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0);
    }

    @Nonnull
    public static TIntSet newTIntSet(int capacity) {
        return new TIntHashSet(capacity, DEFAULT_LOAD_FACTOR, 0);
    }

    @Nonnull
    public static TLongSet newTLongSet() {
        return new TLongHashSet(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0L);
    }

    @Nonnull
    public static TLongSet newTLongSet(int capacity) {
        return new TLongHashSet(capacity, DEFAULT_LOAD_FACTOR, 0L);
    }

    @Nonnull
    public static TFloatSet newTFloatSet() {
        return new TFloatHashSet(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0f);
    }

    @Nonnull
    public static TFloatSet newTFloatSet(int capacity) {
        return new TFloatHashSet(capacity, DEFAULT_LOAD_FACTOR, 0.0f);
    }

    @Nonnull
    public static TDoubleSet newTDoubleSet() {
        return new TDoubleHashSet(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0D);
    }

    @Nonnull
    public static TDoubleSet newTDoubleSet(int capacity) {
        return new TDoubleHashSet(capacity, DEFAULT_LOAD_FACTOR, 0.0D);
    }

    @Nonnull
    public static <V> TCharObjectMap<V> newTCharObjectMap() {
        return new TCharObjectHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (char) 0);
    }

    @Nonnull
    public static <V> TCharObjectMap<V> newTCharObjectMap(int capacity) {
        return new TCharObjectHashMap<>(capacity, DEFAULT_LOAD_FACTOR, (char) 0);
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
    public static TCharCharMap newTCharCharMap() {
        return new TCharCharHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (char) 0, Character.MIN_VALUE);
    }

    @Nonnull
    public static TCharCharMap newTCharCharMap(int capacity) {
        return new TCharCharHashMap(capacity, DEFAULT_LOAD_FACTOR, (char) 0, Character.MIN_VALUE);
    }

    @Nonnull
    public static TCharByteMap newTCharByteMap() {
        return new TCharByteHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (char) 0, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TCharByteMap newTCharByteMap(int capacity) {
        return new TCharByteHashMap(capacity, DEFAULT_LOAD_FACTOR, (char) 0, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TCharShortMap newTCharShortMap() {
        return new TCharShortHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (char) 0, Short.MIN_VALUE);
    }

    @Nonnull
    public static TCharShortMap newTCharShortMap(int capacity) {
        return new TCharShortHashMap(capacity, DEFAULT_LOAD_FACTOR, (char) 0, Short.MIN_VALUE);
    }

    @Nonnull
    public static TCharIntMap newTCharIntMap() {
        return new TCharIntHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (char) 0, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TCharIntMap newTCharIntMap(int capacity) {
        return new TCharIntHashMap(capacity, DEFAULT_LOAD_FACTOR, (char) 0, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TCharLongMap newTCharLongMap() {
        return new TCharLongHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (char) 0, Long.MIN_VALUE);
    }

    @Nonnull
    public static TCharLongMap newTCharLongMap(int capacity) {
        return new TCharLongHashMap(capacity, DEFAULT_LOAD_FACTOR, (char) 0, Long.MIN_VALUE);
    }

    @Nonnull
    public static TCharFloatMap newTCharFloatMap() {
        return new TCharFloatHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (char) 0, Float.NaN);
    }

    @Nonnull
    public static TCharFloatMap newTCharFloatMap(int capacity) {
        return new TCharFloatHashMap(capacity, DEFAULT_LOAD_FACTOR, (char) 0, Float.NaN);
    }

    @Nonnull
    public static TCharDoubleMap newTCharDoubleMap() {
        return new TCharDoubleHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (char) 0, Double.NaN);
    }

    @Nonnull
    public static TCharDoubleMap newTCharDoubleMap(int capacity) {
        return new TCharDoubleHashMap(capacity, DEFAULT_LOAD_FACTOR, (char) 0, Double.NaN);
    }

    @Nonnull
    public static <V> TByteObjectMap<V> newTByteObjectMap() {
        return new TByteObjectHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (byte) 0);
    }

    @Nonnull
    public static <V> TByteObjectMap<V> newTByteObjectMap(int capacity) {
        return new TByteObjectHashMap<>(capacity, DEFAULT_LOAD_FACTOR, (byte) 0);
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
    public static TByteCharMap newTByteCharMap() {
        return new TByteCharHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (byte) 0, Character.MIN_VALUE);
    }

    @Nonnull
    public static TByteCharMap newTByteCharMap(int capacity) {
        return new TByteCharHashMap(capacity, DEFAULT_LOAD_FACTOR, (byte) 0, Character.MIN_VALUE);
    }

    @Nonnull
    public static TByteByteMap newTByteByteMap() {
        return new TByteByteHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (byte) 0, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TByteByteMap newTByteByteMap(int capacity) {
        return new TByteByteHashMap(capacity, DEFAULT_LOAD_FACTOR, (byte) 0, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TByteShortMap newTByteShortMap() {
        return new TByteShortHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (byte) 0, Short.MIN_VALUE);
    }

    @Nonnull
    public static TByteShortMap newTByteShortMap(int capacity) {
        return new TByteShortHashMap(capacity, DEFAULT_LOAD_FACTOR, (byte) 0, Short.MIN_VALUE);
    }

    @Nonnull
    public static TByteIntMap newTByteIntMap() {
        return new TByteIntHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (byte) 0, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TByteIntMap newTByteIntMap(int capacity) {
        return new TByteIntHashMap(capacity, DEFAULT_LOAD_FACTOR, (byte) 0, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TByteLongMap newTByteLongMap() {
        return new TByteLongHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (byte) 0, Long.MIN_VALUE);
    }

    @Nonnull
    public static TByteLongMap newTByteLongMap(int capacity) {
        return new TByteLongHashMap(capacity, DEFAULT_LOAD_FACTOR, (byte) 0, Long.MIN_VALUE);
    }

    @Nonnull
    public static TByteFloatMap newTByteFloatMap() {
        return new TByteFloatHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (byte) 0, Float.NaN);
    }

    @Nonnull
    public static TByteFloatMap newTByteFloatMap(int capacity) {
        return new TByteFloatHashMap(capacity, DEFAULT_LOAD_FACTOR, (byte) 0, Float.NaN);
    }

    @Nonnull
    public static TByteDoubleMap newTByteDoubleMap() {
        return new TByteDoubleHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (byte) 0, Double.NaN);
    }

    @Nonnull
    public static TByteDoubleMap newTByteDoubleMap(int capacity) {
        return new TByteDoubleHashMap(capacity, DEFAULT_LOAD_FACTOR, (byte) 0, Double.NaN);
    }

    @Nonnull
    public static <V> TShortObjectMap<V> newTShortObjectMap() {
        return new TShortObjectHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (short) 0);
    }

    @Nonnull
    public static <V> TShortObjectMap<V> newTShortObjectMap(int capacity) {
        return new TShortObjectHashMap<>(capacity, DEFAULT_LOAD_FACTOR, (short) 0);
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
    public static TShortCharMap newTShortCharMap() {
        return new TShortCharHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (short) 0, Character.MIN_VALUE);
    }

    @Nonnull
    public static TShortCharMap newTShortCharMap(int capacity) {
        return new TShortCharHashMap(capacity, DEFAULT_LOAD_FACTOR, (short) 0, Character.MIN_VALUE);
    }

    @Nonnull
    public static TShortByteMap newTShortByteMap() {
        return new TShortByteHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (short) 0, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TShortByteMap newTShortByteMap(int capacity) {
        return new TShortByteHashMap(capacity, DEFAULT_LOAD_FACTOR, (short) 0, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TShortShortMap newTShortShortMap() {
        return new TShortShortHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (short) 0, Short.MIN_VALUE);
    }

    @Nonnull
    public static TShortShortMap newTShortShortMap(int capacity) {
        return new TShortShortHashMap(capacity, DEFAULT_LOAD_FACTOR, (short) 0, Short.MIN_VALUE);
    }

    @Nonnull
    public static TShortIntMap newTShortIntMap() {
        return new TShortIntHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (short) 0, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TShortIntMap newTShortIntMap(int capacity) {
        return new TShortIntHashMap(capacity, DEFAULT_LOAD_FACTOR, (short) 0, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TShortLongMap newTShortLongMap() {
        return new TShortLongHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (short) 0, Long.MIN_VALUE);
    }

    @Nonnull
    public static TShortLongMap newTShortLongMap(int capacity) {
        return new TShortLongHashMap(capacity, DEFAULT_LOAD_FACTOR, (short) 0, Long.MIN_VALUE);
    }

    @Nonnull
    public static TShortFloatMap newTShortFloatMap() {
        return new TShortFloatHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (short) 0, Float.NaN);
    }

    @Nonnull
    public static TShortFloatMap newTShortFloatMap(int capacity) {
        return new TShortFloatHashMap(capacity, DEFAULT_LOAD_FACTOR, (short) 0, Float.NaN);
    }

    @Nonnull
    public static TShortDoubleMap newTShortDoubleMap() {
        return new TShortDoubleHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, (short) 0, Double.NaN);
    }

    @Nonnull
    public static TShortDoubleMap newTShortDoubleMap(int capacity) {
        return new TShortDoubleHashMap(capacity, DEFAULT_LOAD_FACTOR, (short) 0, Double.NaN);
    }

    @Nonnull
    public static <V> TIntObjectMap<V> newTIntObjectMap() {
        return new TIntObjectHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0);
    }

    @Nonnull
    public static <V> TIntObjectMap<V> newTIntObjectMap(int capacity) {
        return new TIntObjectHashMap<>(capacity, DEFAULT_LOAD_FACTOR, 0);
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
    public static TIntCharMap newTIntCharMap() {
        return new TIntCharHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0, Character.MIN_VALUE);
    }

    @Nonnull
    public static TIntCharMap newTIntCharMap(int capacity) {
        return new TIntCharHashMap(capacity, DEFAULT_LOAD_FACTOR, 0, Character.MIN_VALUE);
    }

    @Nonnull
    public static TIntByteMap newTIntByteMap() {
        return new TIntByteHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TIntByteMap newTIntByteMap(int capacity) {
        return new TIntByteHashMap(capacity, DEFAULT_LOAD_FACTOR, 0, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TIntShortMap newTIntShortMap() {
        return new TIntShortHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0, Short.MIN_VALUE);
    }

    @Nonnull
    public static TIntShortMap newTIntShortMap(int capacity) {
        return new TIntShortHashMap(capacity, DEFAULT_LOAD_FACTOR, 0, Short.MIN_VALUE);
    }

    @Nonnull
    public static TIntIntMap newTIntIntMap() {
        return new TIntIntHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TIntIntMap newTIntIntMap(int capacity) {
        return new TIntIntHashMap(capacity, DEFAULT_LOAD_FACTOR, 0, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TIntLongMap newTIntLongMap() {
        return new TIntLongHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0, Long.MIN_VALUE);
    }

    @Nonnull
    public static TIntLongMap newTIntLongMap(int capacity) {
        return new TIntLongHashMap(capacity, DEFAULT_LOAD_FACTOR, 0, Long.MIN_VALUE);
    }

    @Nonnull
    public static TIntFloatMap newTIntFloatMap() {
        return new TIntFloatHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0, Float.NaN);
    }

    @Nonnull
    public static TIntFloatMap newTIntFloatMap(int capacity) {
        return new TIntFloatHashMap(capacity, DEFAULT_LOAD_FACTOR, 0, Float.NaN);
    }

    @Nonnull
    public static TIntDoubleMap newTIntDoubleMap() {
        return new TIntDoubleHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0, Double.NaN);
    }

    @Nonnull
    public static TIntDoubleMap newTIntDoubleMap(int capacity) {
        return new TIntDoubleHashMap(capacity, DEFAULT_LOAD_FACTOR, 0, Double.NaN);
    }

    @Nonnull
    public static <V> TLongObjectMap<V> newTLongObjectMap() {
        return new TLongObjectHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0L);
    }

    @Nonnull
    public static <V> TLongObjectMap<V> newTLongObjectMap(int capacity) {
        return new TLongObjectHashMap<>(capacity, DEFAULT_LOAD_FACTOR, 0L);
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
    public static TLongCharMap newTLongCharMap() {
        return new TLongCharHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0L, Character.MIN_VALUE);
    }

    @Nonnull
    public static TLongCharMap newTLongCharMap(int capacity) {
        return new TLongCharHashMap(capacity, DEFAULT_LOAD_FACTOR, 0L, Character.MIN_VALUE);
    }

    @Nonnull
    public static TLongByteMap newTLongByteMap() {
        return new TLongByteHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0L, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TLongByteMap newTLongByteMap(int capacity) {
        return new TLongByteHashMap(capacity, DEFAULT_LOAD_FACTOR, 0L, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TLongShortMap newTLongShortMap() {
        return new TLongShortHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0L, Short.MIN_VALUE);
    }

    @Nonnull
    public static TLongShortMap newTLongShortMap(int capacity) {
        return new TLongShortHashMap(capacity, DEFAULT_LOAD_FACTOR, 0L, Short.MIN_VALUE);
    }

    @Nonnull
    public static TLongIntMap newTLongIntMap() {
        return new TLongIntHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0L, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TLongIntMap newTLongIntMap(int capacity) {
        return new TLongIntHashMap(capacity, DEFAULT_LOAD_FACTOR, 0L, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TLongLongMap newTLongLongMap() {
        return new TLongLongHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0L, Long.MIN_VALUE);
    }

    @Nonnull
    public static TLongLongMap newTLongLongMap(int capacity) {
        return new TLongLongHashMap(capacity, DEFAULT_LOAD_FACTOR, 0L, Long.MIN_VALUE);
    }

    @Nonnull
    public static TLongFloatMap newTLongFloatMap() {
        return new TLongFloatHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0L, Float.NaN);
    }

    @Nonnull
    public static TLongFloatMap newTLongFloatMap(int capacity) {
        return new TLongFloatHashMap(capacity, DEFAULT_LOAD_FACTOR, 0L, Float.NaN);
    }

    @Nonnull
    public static TLongDoubleMap newTLongDoubleMap() {
        return new TLongDoubleHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0L, Double.NaN);
    }

    @Nonnull
    public static TLongDoubleMap newTLongDoubleMap(int capacity) {
        return new TLongDoubleHashMap(capacity, DEFAULT_LOAD_FACTOR, 0L, Double.NaN);
    }

    @Nonnull
    public static <V> TFloatObjectMap<V> newTFloatObjectMap() {
        return new TFloatObjectHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0f);
    }

    @Nonnull
    public static <V> TFloatObjectMap<V> newTFloatObjectMap(int capacity) {
        return new TFloatObjectHashMap<>(capacity, DEFAULT_LOAD_FACTOR, 0.0f);
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
    public static TFloatCharMap newTFloatCharMap() {
        return new TFloatCharHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0f, Character.MIN_VALUE);
    }

    @Nonnull
    public static TFloatCharMap newTFloatCharMap(int capacity) {
        return new TFloatCharHashMap(capacity, DEFAULT_LOAD_FACTOR, 0.0f, Character.MIN_VALUE);
    }

    @Nonnull
    public static TFloatByteMap newTFloatByteMap() {
        return new TFloatByteHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0f, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TFloatByteMap newTFloatByteMap(int capacity) {
        return new TFloatByteHashMap(capacity, DEFAULT_LOAD_FACTOR, 0.0f, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TFloatShortMap newTFloatShortMap() {
        return new TFloatShortHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0f, Short.MIN_VALUE);
    }

    @Nonnull
    public static TFloatShortMap newTFloatShortMap(int capacity) {
        return new TFloatShortHashMap(capacity, DEFAULT_LOAD_FACTOR, 0.0f, Short.MIN_VALUE);
    }

    @Nonnull
    public static TFloatIntMap newTFloatIntMap() {
        return new TFloatIntHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0f, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TFloatIntMap newTFloatIntMap(int capacity) {
        return new TFloatIntHashMap(capacity, DEFAULT_LOAD_FACTOR, 0.0f, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TFloatLongMap newTFloatLongMap() {
        return new TFloatLongHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0f, Long.MIN_VALUE);
    }

    @Nonnull
    public static TFloatLongMap newTFloatLongMap(int capacity) {
        return new TFloatLongHashMap(capacity, DEFAULT_LOAD_FACTOR, 0.0f, Long.MIN_VALUE);
    }

    @Nonnull
    public static TFloatFloatMap newTFloatFloatMap() {
        return new TFloatFloatHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0f, Float.NaN);
    }

    @Nonnull
    public static TFloatFloatMap newTFloatFloatMap(int capacity) {
        return new TFloatFloatHashMap(capacity, DEFAULT_LOAD_FACTOR, 0.0f, Float.NaN);
    }

    @Nonnull
    public static TFloatDoubleMap newTFloatDoubleMap() {
        return new TFloatDoubleHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0f, Double.NaN);
    }

    @Nonnull
    public static TFloatDoubleMap newTFloatDoubleMap(int capacity) {
        return new TFloatDoubleHashMap(capacity, DEFAULT_LOAD_FACTOR, 0.0f, Double.NaN);
    }

    @Nonnull
    public static <V> TDoubleObjectMap<V> newTDoubleObjectMap() {
        return new TDoubleObjectHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0D);
    }

    @Nonnull
    public static <V> TDoubleObjectMap<V> newTDoubleObjectMap(int capacity) {
        return new TDoubleObjectHashMap<>(capacity, DEFAULT_LOAD_FACTOR, 0.0D);
    }

    @Nonnull
    public static <V> TObjectDoubleMap<V> newTObjectDoubleMap() {
        return new TObjectDoubleHashMap<>(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, Double.NaN);
    }

    @Nonnull
    public static <V> TObjectDoubleMap<V> newTObjectDoubleMap(int capacity) {
        return new TObjectDoubleHashMap<>(capacity, DEFAULT_LOAD_FACTOR, Double.NaN);
    }

    @Nonnull
    public static TDoubleCharMap newTDoubleCharMap() {
        return new TDoubleCharHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0D, Character.MIN_VALUE);
    }

    @Nonnull
    public static TDoubleCharMap newTDoubleCharMap(int capacity) {
        return new TDoubleCharHashMap(capacity, DEFAULT_LOAD_FACTOR, 0.0D, Character.MIN_VALUE);
    }

    @Nonnull
    public static TDoubleByteMap newTDoubleByteMap() {
        return new TDoubleByteHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0D, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TDoubleByteMap newTDoubleByteMap(int capacity) {
        return new TDoubleByteHashMap(capacity, DEFAULT_LOAD_FACTOR, 0.0D, Byte.MIN_VALUE);
    }

    @Nonnull
    public static TDoubleShortMap newTDoubleShortMap() {
        return new TDoubleShortHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0D, Short.MIN_VALUE);
    }

    @Nonnull
    public static TDoubleShortMap newTDoubleShortMap(int capacity) {
        return new TDoubleShortHashMap(capacity, DEFAULT_LOAD_FACTOR, 0.0D, Short.MIN_VALUE);
    }

    @Nonnull
    public static TDoubleIntMap newTDoubleIntMap() {
        return new TDoubleIntHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0D, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TDoubleIntMap newTDoubleIntMap(int capacity) {
        return new TDoubleIntHashMap(capacity, DEFAULT_LOAD_FACTOR, 0.0D, Integer.MIN_VALUE);
    }

    @Nonnull
    public static TDoubleLongMap newTDoubleLongMap() {
        return new TDoubleLongHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0D, Long.MIN_VALUE);
    }

    @Nonnull
    public static TDoubleLongMap newTDoubleLongMap(int capacity) {
        return new TDoubleLongHashMap(capacity, DEFAULT_LOAD_FACTOR, 0.0D, Long.MIN_VALUE);
    }

    @Nonnull
    public static TDoubleFloatMap newTDoubleFloatMap() {
        return new TDoubleFloatHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0D, Float.NaN);
    }

    @Nonnull
    public static TDoubleFloatMap newTDoubleFloatMap(int capacity) {
        return new TDoubleFloatHashMap(capacity, DEFAULT_LOAD_FACTOR, 0.0D, Float.NaN);
    }

    @Nonnull
    public static TDoubleDoubleMap newTDoubleDoubleMap() {
        return new TDoubleDoubleHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, 0.0D, Double.NaN);
    }

    @Nonnull
    public static TDoubleDoubleMap newTDoubleDoubleMap(int capacity) {
        return new TDoubleDoubleHashMap(capacity, DEFAULT_LOAD_FACTOR, 0.0D, Double.NaN);
    }

    public static class CollectionComparator<T extends Comparable<T>> implements Comparator<Collection<T>> {
        @Override
        public int compare(Collection<T> collectionA, Collection<T> collectionB) {
            Iterator<T> iteratorA = collectionA.iterator();
            Iterator<T> iteratorB = collectionB.iterator();

            while (iteratorA.hasNext() && iteratorB.hasNext()) {
                int result = iteratorA.next().compareTo(iteratorB.next());
                if (result != 0) {
                    return result;
                }
            }

            return iteratorA.hasNext() ? 1 : iteratorB.hasNext() ? -1 : 0;
        }
    }
}
