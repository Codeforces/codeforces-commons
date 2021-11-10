package com.codeforces.commons.collection;

import com.codeforces.commons.annotation.NonnullElements;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnegative;
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

    @Nonnull
    public static <T> T[] join(@Nonnull IntFunction<T[]> generator, @Nonnull @NonnullElements Iterable<T[]> arrays) {
        int joinedLength = 0;

        for (T[] array : arrays) {
            joinedLength += array.length;
        }

        T[] joinedArray = generator.apply(joinedLength);
        int joinedIndex = 0;

        for (T[] array : arrays) {
            System.arraycopy(array, 0, joinedArray, joinedIndex, array.length);
            joinedIndex += array.length;
        }

        return joinedArray;
    }

    @Nonnull
    public static <T> T[] join(@Nonnull Class<T> elementClass, @Nonnull @NonnullElements Iterable<T[]> arrays) {
        int joinedLength = 0;

        for (T[] array : arrays) {
            joinedLength += array.length;
        }

        @SuppressWarnings("unchecked") T[] joinedArray = (T[]) Array.newInstance(elementClass, joinedLength);
        int joinedIndex = 0;

        for (T[] array : arrays) {
            System.arraycopy(array, 0, joinedArray, joinedIndex, array.length);
            joinedIndex += array.length;
        }

        return joinedArray;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @SafeVarargs
    @Nonnull
    public static <T> T[] join(@Nonnull IntFunction<T[]> generator, @Nonnull @NonnullElements T[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = 0;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        T[] joinedArray = generator.apply(joinedLength);
        int joinedIndex = 0;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            T[] array = arrays[arrayIndex];
            System.arraycopy(array, 0, joinedArray, joinedIndex, array.length);
            joinedIndex += array.length;
        }

        return joinedArray;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @SafeVarargs
    @Nonnull
    public static <T> T[] join(@Nonnull Class<T> elementClass, @Nonnull @NonnullElements T[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = 0;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        @SuppressWarnings("unchecked") T[] joinedArray = (T[]) Array.newInstance(elementClass, joinedLength);
        int joinedIndex = 0;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            T[] array = arrays[arrayIndex];
            System.arraycopy(array, 0, joinedArray, joinedIndex, array.length);
            joinedIndex += array.length;
        }

        return joinedArray;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @Nonnull
    public static float[] join(@Nonnull @NonnullElements float[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = 0;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        float[] joinedArray = new float[joinedLength];
        int joinedIndex = 0;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            float[] array = arrays[arrayIndex];
            System.arraycopy(array, 0, joinedArray, joinedIndex, array.length);
            joinedIndex += array.length;
        }

        return joinedArray;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @Nonnull
    public static double[] join(@Nonnull @NonnullElements double[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = 0;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        double[] joinedArray = new double[joinedLength];
        int joinedIndex = 0;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            double[] array = arrays[arrayIndex];
            System.arraycopy(array, 0, joinedArray, joinedIndex, array.length);
            joinedIndex += array.length;
        }

        return joinedArray;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @Nonnull
    public static byte[] join(@Nonnull @NonnullElements byte[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = 0;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        byte[] joinedArray = new byte[joinedLength];
        int joinedIndex = 0;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            byte[] array = arrays[arrayIndex];
            System.arraycopy(array, 0, joinedArray, joinedIndex, array.length);
            joinedIndex += array.length;
        }

        return joinedArray;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @Nonnull
    public static short[] join(@Nonnull @NonnullElements short[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = 0;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        short[] joinedArray = new short[joinedLength];
        int joinedIndex = 0;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            short[] array = arrays[arrayIndex];
            System.arraycopy(array, 0, joinedArray, joinedIndex, array.length);
            joinedIndex += array.length;
        }

        return joinedArray;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @Nonnull
    public static int[] join(@Nonnull @NonnullElements int[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = 0;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        int[] joinedArray = new int[joinedLength];
        int joinedIndex = 0;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            int[] array = arrays[arrayIndex];
            System.arraycopy(array, 0, joinedArray, joinedIndex, array.length);
            joinedIndex += array.length;
        }

        return joinedArray;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @Nonnull
    public static long[] join(@Nonnull @NonnullElements long[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = 0;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        long[] joinedArray = new long[joinedLength];
        int joinedIndex = 0;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            long[] array = arrays[arrayIndex];
            System.arraycopy(array, 0, joinedArray, joinedIndex, array.length);
            joinedIndex += array.length;
        }

        return joinedArray;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @Nonnull
    public static char[] join(@Nonnull @NonnullElements char[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = 0;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        char[] joinedArray = new char[joinedLength];
        int joinedIndex = 0;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            char[] array = arrays[arrayIndex];
            System.arraycopy(array, 0, joinedArray, joinedIndex, array.length);
            joinedIndex += array.length;
        }

        return joinedArray;
    }

    @SuppressWarnings({"ForLoopWithMissingComponent", "OverloadedVarargsMethod"})
    @Nonnull
    public static String[] join(@Nonnull @NonnullElements String[]... arrays) {
        int arrayCount = arrays.length;
        int joinedLength = 0;

        for (int arrayIndex = arrayCount; --arrayIndex >= 0; ) {
            joinedLength += arrays[arrayIndex].length;
        }

        String[] joinedArray = new String[joinedLength];
        int joinedIndex = 0;

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
            String[] array = arrays[arrayIndex];
            System.arraycopy(array, 0, joinedArray, joinedIndex, array.length);
            joinedIndex += array.length;
        }

        return joinedArray;
    }

    @Nonnull
    public static <T> T[] trim(@Nonnull T[] array, @Nonnegative int length) {
        if (array.length == length) {
            return array;
        }

        @SuppressWarnings("unchecked") T[] prefix = (T[]) Array.newInstance(array.getClass().getComponentType(), length);
        System.arraycopy(array, 0, prefix, 0, length);
        return prefix;
    }

    @Nonnull
    public static <T> T[] trim(@Nonnull T[] array, @Nonnegative int length, @Nonnull IntFunction<T[]> generator) {
        if (array.length == length) {
            return array;
        }

        T[] prefix = generator.apply(length);
        System.arraycopy(array, 0, prefix, 0, length);
        return prefix;
    }

    @Nonnull
    public static float[] trim(@Nonnull float[] array, @Nonnegative int length) {
        if (length == 0) {
            return ArrayUtils.EMPTY_FLOAT_ARRAY;
        }

        if (array.length == length) {
            return array;
        }

        float[] prefix = new float[length];
        System.arraycopy(array, 0, prefix, 0, length);
        return prefix;
    }

    @Nonnull
    public static double[] trim(@Nonnull double[] array, @Nonnegative int length) {
        if (length == 0) {
            return ArrayUtils.EMPTY_DOUBLE_ARRAY;
        }

        if (array.length == length) {
            return array;
        }

        double[] prefix = new double[length];
        System.arraycopy(array, 0, prefix, 0, length);
        return prefix;
    }

    @Nonnull
    public static byte[] trim(@Nonnull byte[] array, @Nonnegative int length) {
        if (length == 0) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }

        if (array.length == length) {
            return array;
        }

        byte[] prefix = new byte[length];
        System.arraycopy(array, 0, prefix, 0, length);
        return prefix;
    }

    @Nonnull
    public static short[] trim(@Nonnull short[] array, @Nonnegative int length) {
        if (length == 0) {
            return ArrayUtils.EMPTY_SHORT_ARRAY;
        }

        if (array.length == length) {
            return array;
        }

        short[] prefix = new short[length];
        System.arraycopy(array, 0, prefix, 0, length);
        return prefix;
    }

    @Nonnull
    public static int[] trim(@Nonnull int[] array, @Nonnegative int length) {
        if (length == 0) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }

        if (array.length == length) {
            return array;
        }

        int[] prefix = new int[length];
        System.arraycopy(array, 0, prefix, 0, length);
        return prefix;
    }

    @Nonnull
    public static long[] trim(@Nonnull long[] array, @Nonnegative int length) {
        if (length == 0) {
            return ArrayUtils.EMPTY_LONG_ARRAY;
        }

        if (array.length == length) {
            return array;
        }

        long[] prefix = new long[length];
        System.arraycopy(array, 0, prefix, 0, length);
        return prefix;
    }

    @Nonnull
    public static char[] trim(@Nonnull char[] array, @Nonnegative int length) {
        if (length == 0) {
            return ArrayUtils.EMPTY_CHAR_ARRAY;
        }

        if (array.length == length) {
            return array;
        }

        char[] prefix = new char[length];
        System.arraycopy(array, 0, prefix, 0, length);
        return prefix;
    }

    @Nonnull
    public static String[] trim(@Nonnull String[] array, @Nonnegative int length) {
        if (length == 0) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }

        if (array.length == length) {
            return array;
        }

        String[] prefix = new String[length];
        System.arraycopy(array, 0, prefix, 0, length);
        return prefix;
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

    public static void shuffle(char[] array, Random random) {
        int count;
        if (array != null && (count = array.length) > 1) {
            int index = count;
            while (--index > 0) {
                int newIndex = random.nextInt(index + 1);
                char temp = array[index];
                array[index] = array[newIndex];
                array[newIndex] = temp;
            }
        }
    }

    public static <T extends Comparable<T>> T[] sort(T[] array) {
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

    public static char[] sort(char[] array) {
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
