package com.codeforces.commons.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 02.06.14
 */
public class AtomicUtil {
    public static void invert(AtomicBoolean value) {
        boolean previousValue;
        do {
            previousValue = value.get();
        } while (!value.compareAndSet(previousValue, !previousValue));
    }

    public static void increment(AtomicInteger value, int maxValue) {
        int previousValue;
        int newValue;
        do {
            previousValue = value.get();
            newValue = normalizeValue(previousValue + 1, maxValue);
        } while (!value.compareAndSet(previousValue, newValue));
    }

    public static void decrement(AtomicInteger value, int maxValue) {
        int previousValue;
        int newValue;
        do {
            previousValue = value.get();
            newValue = normalizeValue(previousValue - 1, maxValue);
        } while (!value.compareAndSet(previousValue, newValue));
    }

    private static int normalizeValue(int value, int maxValue) {
        while (value > maxValue) {
            value -= maxValue + 1;
        }
        while (value < 0) {
            value += maxValue + 1;
        }
        return value;
    }

    public static void increment(AtomicLong value, long maxValue) {
        long previousValue;
        long newValue;
        do {
            previousValue = value.get();
            newValue = normalizeValue(previousValue + 1L, maxValue);
        } while (!value.compareAndSet(previousValue, newValue));
    }

    public static void decrement(AtomicLong value, long maxValue) {
        long previousValue;
        long newValue;
        do {
            previousValue = value.get();
            newValue = normalizeValue(previousValue - 1L, maxValue);
        } while (!value.compareAndSet(previousValue, newValue));
    }

    private static long normalizeValue(long value, long maxValue) {
        while (value > maxValue) {
            value -= maxValue + 1L;
        }
        while (value < 0L) {
            value += maxValue + 1L;
        }
        return value;
    }

    private AtomicUtil() {
        throw new UnsupportedOperationException();
    }
}
