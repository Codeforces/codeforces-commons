package com.codeforces.commons.collection;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 04.11.12
 */
public class ArrayUtil {
    private ArrayUtil() {
        throw new UnsupportedOperationException();
    }

    public static <T> void shuffle(T[] array, Random random) {
        int count;
        if (array != null && (count = array.length) > 1) {
            for (int index = count - 1; index > 0; --index) {
                int newIndex = random.nextInt(index + 1);
                T temp = array[index];
                array[index] = array[newIndex];
                array[newIndex] = temp;
            }
        }
    }

    public static void shuffle(double[] array, Random random) {
        int count;
        if (array != null && (count = array.length) > 1) {
            for (int index = count - 1; index > 0; --index) {
                int newIndex = random.nextInt(index + 1);
                double temp = array[index];
                array[index] = array[newIndex];
                array[newIndex] = temp;
            }
        }
    }

    public static void shuffle(int[] array, Random random) {
        int count;
        if (array != null && (count = array.length) > 1) {
            for (int index = count - 1; index > 0; --index) {
                int newIndex = random.nextInt(index + 1);
                int temp = array[index];
                array[index] = array[newIndex];
                array[newIndex] = temp;
            }
        }
    }

    public static void shuffle(long[] array, Random random) {
        int count;
        if (array != null && (count = array.length) > 1) {
            for (int index = count - 1; index > 0; --index) {
                int newIndex = random.nextInt(index + 1);
                long temp = array[index];
                array[index] = array[newIndex];
                array[newIndex] = temp;
            }
        }
    }

    public static <T> T[] sort(T[] array) {
        Arrays.sort(array);
        return array;
    }

    public static <T> T[] sort(T[] array, Comparator<T> comparator) {
        Arrays.sort(array, comparator);
        return array;
    }

    public static double[] sort(double[] array) {
        Arrays.sort(array);
        return array;
    }

    public static int[] sort(int[] array) {
        Arrays.sort(array);
        return array;
    }

    public static long[] sort(long[] array) {
        Arrays.sort(array);
        return array;
    }

    public static int getDimensionCount(@Nonnull Class arrayClass) {
        int dimensionCount = 0;

        while (arrayClass.isArray()) {
            ++dimensionCount;
            arrayClass = arrayClass.getComponentType();
        }

        return dimensionCount;
    }

    public static int getDimensionCount(@Nonnull Object arrayObject) {
        if (arrayObject instanceof Class) {
            return getDimensionCount((Class) arrayObject);
        }

        return getDimensionCount(arrayObject.getClass());
    }

    public static Class getComponentType(@Nonnull Class arrayClass) {
        while (arrayClass.isArray()) {
            arrayClass = arrayClass.getComponentType();
        }

        return arrayClass;
    }

    public static Class getComponentType(@Nonnull Object arrayObject) {
        if (arrayObject instanceof Class) {
            return getComponentType((Class) arrayObject);
        }

        return getComponentType(arrayObject.getClass());
    }
}
