package com.codeforces.commons.collection;

import com.codeforces.commons.annotation.NonnullElements;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.*;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 04.11.12
 */
@SuppressWarnings("WeakerAccess")
public class ArrayUtil {
    private ArrayUtil() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @SafeVarargs
    @Nonnull
    public static <T> T[] join(@Nonnull T[] array, @Nonnull @NonnullElements T[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = array.length;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        Class<?> componentType = array.getClass().getComponentType();
        @SuppressWarnings("unchecked") T[] joinedArray = (T[]) Array.newInstance(componentType, joinedLength);

        System.arraycopy(array, 0, joinedArray, 0, array.length);
        int joinedIndex = array.length;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            T[] otherArray = arrays[arrayIndex];
            System.arraycopy(otherArray, 0, joinedArray, joinedIndex, otherArray.length);
            joinedIndex += otherArray.length;
        }

        return joinedArray;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @Nonnull
    public static float[] join(@Nonnull float[] array, @Nonnull @NonnullElements float[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = array.length;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        float[] joinedArray = new float[joinedLength];

        System.arraycopy(array, 0, joinedArray, 0, array.length);
        int joinedIndex = array.length;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            float[] otherArray = arrays[arrayIndex];
            System.arraycopy(otherArray, 0, joinedArray, joinedIndex, otherArray.length);
            joinedIndex += otherArray.length;
        }

        return joinedArray;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @Nonnull
    public static double[] join(@Nonnull double[] array, @Nonnull @NonnullElements double[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = array.length;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        double[] joinedArray = new double[joinedLength];

        System.arraycopy(array, 0, joinedArray, 0, array.length);
        int joinedIndex = array.length;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            double[] otherArray = arrays[arrayIndex];
            System.arraycopy(otherArray, 0, joinedArray, joinedIndex, otherArray.length);
            joinedIndex += otherArray.length;
        }

        return joinedArray;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @Nonnull
    public static byte[] join(@Nonnull byte[] array, @Nonnull @NonnullElements byte[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = array.length;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        byte[] joinedArray = new byte[joinedLength];

        System.arraycopy(array, 0, joinedArray, 0, array.length);
        int joinedIndex = array.length;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            byte[] otherArray = arrays[arrayIndex];
            System.arraycopy(otherArray, 0, joinedArray, joinedIndex, otherArray.length);
            joinedIndex += otherArray.length;
        }

        return joinedArray;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @Nonnull
    public static short[] join(@Nonnull short[] array, @Nonnull @NonnullElements short[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = array.length;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        short[] joinedArray = new short[joinedLength];

        System.arraycopy(array, 0, joinedArray, 0, array.length);
        int joinedIndex = array.length;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            short[] otherArray = arrays[arrayIndex];
            System.arraycopy(otherArray, 0, joinedArray, joinedIndex, otherArray.length);
            joinedIndex += otherArray.length;
        }

        return joinedArray;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @Nonnull
    public static int[] join(@Nonnull int[] array, @Nonnull @NonnullElements int[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = array.length;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        int[] joinedArray = new int[joinedLength];

        System.arraycopy(array, 0, joinedArray, 0, array.length);
        int joinedIndex = array.length;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            int[] otherArray = arrays[arrayIndex];
            System.arraycopy(otherArray, 0, joinedArray, joinedIndex, otherArray.length);
            joinedIndex += otherArray.length;
        }

        return joinedArray;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @Nonnull
    public static long[] join(@Nonnull long[] array, @Nonnull @NonnullElements long[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = array.length;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        long[] joinedArray = new long[joinedLength];

        System.arraycopy(array, 0, joinedArray, 0, array.length);
        int joinedIndex = array.length;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            long[] otherArray = arrays[arrayIndex];
            System.arraycopy(otherArray, 0, joinedArray, joinedIndex, otherArray.length);
            joinedIndex += otherArray.length;
        }

        return joinedArray;
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

    public static void shuffle(byte[] array, Random random) {
        int count;
        if (array != null && (count = array.length) > 1) {
            int index = count;
            while (--index > 0) {
                int newIndex = random.nextInt(index + 1);
                byte temp = array[index];
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

    public static byte[] sort(byte[] array) {
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
