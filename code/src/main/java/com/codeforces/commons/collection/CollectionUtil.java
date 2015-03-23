package com.codeforces.commons.collection;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Edvard Davtyan (homo_sapiens@xakep.ru)
 */
public class CollectionUtil {
    private CollectionUtil() {
        throw new UnsupportedOperationException();
    }

    public static boolean isEmpty(@Nullable Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean equals(@Nullable Collection collectionA, @Nullable Collection collectionB) {
        return collectionA == null ? collectionB == null : collectionA.equals(collectionB);
    }

    public static boolean equalsOrEmpty(@Nullable Collection collectionA, @Nullable Collection collectionB) {
        return isEmpty(collectionA) ? isEmpty(collectionB) : collectionA.equals(collectionB);
    }

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

    public static <T1, T2> List<T2> convert(List<T1> list, Converter<T1, T2> converter) {
        int count = list.size();
        List<T2> newList = new ArrayList<>(count);

        if (list instanceof RandomAccess) {
            for (int i = 0; i < count; ++i) {
                newList.add(converter.convert(list.get(i)));
            }
        } else {
            Iterator<T1> iterator = list.iterator();
            for (int i = 0; i < count; ++i) {
                newList.add(converter.convert(iterator.next()));
            }
        }

        return newList;
    }

    public static <T1, T2> Collection<T2> convert(Collection<T1> collection, Converter<T1, T2> converter) {
        int count = collection.size();
        Collection<T2> newCollection = new ArrayList<>(count);

        Iterator<T1> iterator = collection.iterator();
        for (int i = 0; i < count; ++i) {
            newCollection.add(converter.convert(iterator.next()));
        }

        return newCollection;
    }

    public static <K1, T1, K2, T2> Map<K2, T2> convert(
            Map<K1, T1> map, Converter<K1, K2> keyConverter, Converter<T1, T2> valueConverter) {
        int count = map.size();
        Map<K2, T2> newMap = new LinkedHashMap<>(count);

        Iterator<Map.Entry<K1, T1>> iterator = map.entrySet().iterator();
        for (int i = 0; i < count; ++i) {
            Map.Entry<K1, T1> entry = iterator.next();
            newMap.put(keyConverter.convert(entry.getKey()), valueConverter.convert(entry.getValue()));
        }

        return newMap;
    }

    public static <T1, T2> List<T2> convertQuietly(List<T1> list, Converter<T1, T2> converter) {
        int count = list.size();
        List<T2> newList = new ArrayList<>(count);

        if (list instanceof RandomAccess) {
            for (int i = 0; i < count; ++i) {
                T1 value = list.get(i);
                try {
                    newList.add(converter.convert(value));
                } catch (RuntimeException ignored) {
                    // No operations.
                }
            }
        } else {
            Iterator<T1> iterator = list.iterator();
            for (int i = 0; i < count; ++i) {
                T1 value = iterator.next();
                try {
                    newList.add(converter.convert(value));
                } catch (RuntimeException ignored) {
                    // No operations.
                }
            }
        }

        return newList;
    }

    public static <T1, T2> Collection<T2> convertQuietly(Collection<T1> collection, Converter<T1, T2> converter) {
        int count = collection.size();
        Collection<T2> newCollection = new ArrayList<>(count);

        Iterator<T1> iterator = collection.iterator();
        for (int i = 0; i < count; ++i) {
            T1 value = iterator.next();
            try {
                newCollection.add(converter.convert(value));
            } catch (RuntimeException ignored) {
                // No operations.
            }
        }

        return newCollection;
    }

    public static <K1, T1, K2, T2> Map<K2, T2> convertQuietly(
            Map<K1, T1> map, Converter<K1, K2> keyConverter, Converter<T1, T2> valueConverter) {
        int count = map.size();
        Map<K2, T2> newMap = new LinkedHashMap<>(count);

        Iterator<Map.Entry<K1, T1>> iterator = map.entrySet().iterator();
        for (int i = 0; i < count; ++i) {
            Map.Entry<K1, T1> entry = iterator.next();
            try {
                newMap.put(keyConverter.convert(entry.getKey()), valueConverter.convert(entry.getValue()));
            } catch (RuntimeException ignored) {
                // No operations.
            }
        }

        return newMap;
    }

    public interface Converter<T1, T2> {
        T2 convert(T1 value);
    }

    public static final class PreservingConverter<T> implements Converter<T, T> {
        @Override
        public T convert(T value) {
            return value;
        }
    }
}
