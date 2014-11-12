package com.codeforces.commons.io;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mike Mirzayanov (mirzayanovmr@gmail.com)
 */
public class HttpUtilTest extends TestCase {
    private static final String BASE_TESTING_URL = "http://polygon-api.codeforces.com/httpLoad";

    public static byte[] doGet(String s) throws IOException {
        URL url = new URL(s);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(15000);
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.connect();
        byte[] result = IoUtil.toByteArray(connection.getInputStream());
        connection.disconnect();
        return result;
    }

    public void testManyConcurrentGets() throws InterruptedException {
        final int CONCURRENCY = 10;
        final int REQUEST_COUNT = 100;
        final int SIZE = 100000;

        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);

        long s = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = HttpUtil.executePostRequestAndReturnResponse(2000, BASE_TESTING_URL, "size", SIZE).getBytes();
                        assertEquals(SIZE, bytes.length);
                        System.out.println("Done " + (count.incrementAndGet()));
                    } catch (Throwable e) {
                        e.printStackTrace();
                        exceptions.add(e);
                    }
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.DAYS);

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size()=" + exceptions.size());
        }

        System.out.println("Done in " + (System.currentTimeMillis() - s) + " ms.");
    }

    public void testManyConcurrentDoGets() throws InterruptedException {
        final int CONCURRENCY = 2;
        final int REQUEST_COUNT = 400;
        final int SIZE = 1000;

        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);

        long s = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = doGet(BASE_TESTING_URL + "?size=" + SIZE);
                        assertEquals(SIZE, bytes.length);
                        System.out.println("Done " + (count.incrementAndGet()));
                    } catch (Throwable e) {
                        e.printStackTrace();
                        exceptions.add(e);
                    }
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.DAYS);

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size()=" + exceptions.size());
        }

        System.out.println("Done in " + (System.currentTimeMillis() - s) + " ms.");
    }

    public void testManyConcurrentHttpUtilHttpClientGets() throws InterruptedException {
        final int CONCURRENCY = 10;
        final int REQUEST_COUNT = 100;
        final int SIZE = 100000;

        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);

        long s = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = HttpUtil_HttpClient.executePostRequestAndReturnResponseBytes(BASE_TESTING_URL, "size", SIZE);
                        assertEquals(SIZE, bytes.length);
                        System.out.println("Done " + (count.incrementAndGet()));
                    } catch (Throwable e) {
                        e.printStackTrace();
                        exceptions.add(e);
                    }
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.DAYS);

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size()=" + exceptions.size());
        }

        System.out.println("Done in " + (System.currentTimeMillis() - s) + " ms.");
    }

    public void testManyTimedOutConcurrentGets() throws InterruptedException {
        final int CONCURRENCY = 5;
        final int REQUEST_COUNT = 5 * CONCURRENCY;
        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);


        System.out.println("Start");
        for (int i = 0; i < REQUEST_COUNT; i++) {
            System.out.println(i);
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    long startTimeMillis = System.currentTimeMillis();
                    try {
                        System.out.println("Waiting...");
                        byte[] bytes = HttpUtil_HttpClient.executePostRequestAndReturnResponseBytes(8000, BASE_TESTING_URL + "?delay=3");
                        assertEquals(1024, bytes.length);
                        System.out.println("Done " + (count.incrementAndGet()) + " in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
                    } catch (Throwable e) {
                        e.printStackTrace();
                        exceptions.add(e);
                    }
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.DAYS);

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size()=" + exceptions.size());
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(doGet(BASE_TESTING_URL + "?delay=1").length);
    }
}
