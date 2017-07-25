package com.codeforces.commons.collection;

import com.codeforces.commons.annotation.NonnullElements;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.*;

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
            int index = count;
            while (--index > 0) {
                int newIndex = random.nextInt(index + 1);
                T temp = array[index];
                array[index] = array[newIndex];
                array[newIndex] = temp;
            }
        }
    }

    public static void shuffle(float[] array, Random random) {
        int count;
        if (array != null && (count = array.length) > 1) {
            int index = count;
            while (--index > 0) {
                int newIndex = random.nextInt(index + 1);
                float temp = array[index];
                array[index] = array[newIndex];
                array[newIndex] = temp;
            }
        }
    }

    public static void shuffle(double[] array, Random random) {
        int count;
        if (array != null && (count = array.length) > 1) {
            int index = count;
            while (--index > 0) {
                int newIndex = random.nextInt(index + 1);
                double temp = array[index];
                array[index] = array[newIndex];
                array[newIndex] = temp;
            }
        }
    }

    public static void shuffle(short[] array, Random random) {
        int count;
        if (array != null && (count = array.length) > 1) {
            int index = count;
            while (--index > 0) {
                int newIndex = random.nextInt(index + 1);
                short temp = array[index];
                array[index] = array[newIndex];
                array[newIndex] = temp;
            }
        }
    }

    public static void shuffle(int[] array, Random random) {
        int count;
        if (array != null && (count = array.length) > 1) {
            int index = count;
            while (--index > 0) {
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
            int index = count;
            while (--index > 0) {
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

    public static float[] sort(float[] array) {
        Arrays.sort(array);
        return array;
    }

    public static double[] sort(double[] array) {
        Arrays.sort(array);
        return array;
    }

    public static short[] sort(short[] array) {
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

    @Contract(pure = true)
    public static float sum(float[] array) {
        float sum = 0.0f;
        int index = array.length;

        while (--index >= 0) {
            sum += array[index];
        }

        return sum;
    }

    @Contract(pure = true)
    public static double sum(double[] array) {
        double sum = 0.0D;
        int index = array.length;

        while (--index >= 0) {
            sum += array[index];
        }

        return sum;
    }

    @Contract(pure = true)
    public static short sum(short[] array) {
        short sum = 0;
        int index = array.length;

        while (--index >= 0) {
            sum += array[index];
        }

        return sum;
    }

    @Contract(pure = true)
    public static int sum(int[] array) {
        int sum = 0;
        int index = array.length;

        while (--index >= 0) {
            sum += array[index];
        }

        return sum;
    }

    @Contract(pure = true)
    public static long sum(long[] array) {
        long sum = 0L;
        int index = array.length;

        while (--index >= 0) {
            sum += array[index];
        }

        return sum;
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    @SafeVarargs
    public static <T> void forEach(@Nonnull Consumer<? super T> action, @Nonnull @NonnullElements T[]... arrays) {
        for (int arrayIndex = 0, arrayCount = arrays.length; arrayIndex < arrayCount; ++arrayIndex) {
            T[] array = arrays[arrayIndex];

            for (int elementIndex = 0, elementCount = array.length; elementIndex < elementCount; ++elementIndex) {
                action.accept(array[elementIndex]);
            }
        }
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public static void forEach(@Nonnull DoubleConsumer action, @Nonnull @NonnullElements double[]... arrays) {
        for (int arrayIndex = 0, arrayCount = arrays.length; arrayIndex < arrayCount; ++arrayIndex) {
            double[] array = arrays[arrayIndex];

            for (int elementIndex = 0, elementCount = array.length; elementIndex < elementCount; ++elementIndex) {
                action.accept(array[elementIndex]);
            }
        }
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public static void forEach(@Nonnull IntConsumer action, @Nonnull @NonnullElements int[]... arrays) {
        for (int arrayIndex = 0, arrayCount = arrays.length; arrayIndex < arrayCount; ++arrayIndex) {
            int[] array = arrays[arrayIndex];

            for (int elementIndex = 0, elementCount = array.length; elementIndex < elementCount; ++elementIndex) {
                action.accept(array[elementIndex]);
            }
        }
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public static void forEach(@Nonnull LongConsumer action, @Nonnull @NonnullElements long[]... arrays) {
        for (int arrayIndex = 0, arrayCount = arrays.length; arrayIndex < arrayCount; ++arrayIndex) {
            long[] array = arrays[arrayIndex];

            for (int elementIndex = 0, elementCount = array.length; elementIndex < elementCount; ++elementIndex) {
                action.accept(array[elementIndex]);
            }
        }
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
