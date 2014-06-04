package com.codeforces.commons.concurrent;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 02.06.14
 */
public class AtomicUtilTest extends TestCase {
    private static final int THREAD_COUNT = 1000;
    private static final int ITERATION_COUNT = 1000;

    public void testInvert() throws Exception {
        final AtomicBoolean value = new AtomicBoolean();
        List<Thread> threads = new ArrayList<>(THREAD_COUNT);

        for (int threadIndex = 0; threadIndex < THREAD_COUNT; ++threadIndex) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int iterationIndex = 0; iterationIndex < ITERATION_COUNT; ++iterationIndex) {
                        AtomicUtil.invert(value);
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (int threadIndex = 0; threadIndex < THREAD_COUNT; ++threadIndex) {
            threads.get(threadIndex).join();
        }

        assertEquals("AtomicUtil.invert(AtomicBoolean) failed.", false, value.get());
    }

    public void testIncrement() throws Exception {
        final AtomicInteger value = new AtomicInteger(3);
        List<Thread> threads = new ArrayList<>(THREAD_COUNT);

        for (int threadIndex = 0; threadIndex < THREAD_COUNT; ++threadIndex) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int iterationIndex = 0; iterationIndex < ITERATION_COUNT; ++iterationIndex) {
                        AtomicUtil.increment(value, 9);
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (int threadIndex = 0; threadIndex < THREAD_COUNT; ++threadIndex) {
            threads.get(threadIndex).join();
        }

        assertEquals("AtomicUtil.increment(AtomicInteger, int) failed.", 3, value.get());
    }

    public void testDecrement() throws Exception {
        final AtomicInteger value = new AtomicInteger(7);
        List<Thread> threads = new ArrayList<>(THREAD_COUNT);

        for (int threadIndex = 0; threadIndex < THREAD_COUNT; ++threadIndex) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int iterationIndex = 0; iterationIndex < ITERATION_COUNT; ++iterationIndex) {
                        AtomicUtil.decrement(value, 9);
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (int threadIndex = 0; threadIndex < THREAD_COUNT; ++threadIndex) {
            threads.get(threadIndex).join();
        }

        assertEquals("AtomicUtil.decrement(AtomicInteger, int) failed.", 7, value.get());
    }
}
