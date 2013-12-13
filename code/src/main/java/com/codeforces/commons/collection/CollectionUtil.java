package com.codeforces.commons.collection;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

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
}
