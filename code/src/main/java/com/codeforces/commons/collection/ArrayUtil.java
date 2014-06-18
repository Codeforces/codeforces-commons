package com.codeforces.commons.collection;

import javax.annotation.Nullable;
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

    /**
     * @return dimension count or {@code 0} if {@code arrayClass} is not representing array
     */
    public static int getDimensionCount(@Nullable Class arrayClass) {
        int dimensionCount = 0;

        while (arrayClass.isArray()) {
            ++dimensionCount;
            arrayClass = arrayClass.getComponentType();
        }

        return dimensionCount;
    }

    /**
     * @return dimension count or {@code 0} if {@code arrayObject} is not representing array
     */
    public static int getDimensionCount(@Nullable Object arrayObject) {
        return getDimensionCount(arrayObject.getClass());
    }
}
