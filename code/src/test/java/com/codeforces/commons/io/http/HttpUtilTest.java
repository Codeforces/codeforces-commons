package com.codeforces.commons.io.http;

import com.codeforces.commons.io.IoUtil;
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
    //private static final String BASE_TESTING_URL = "http://127.0.0.1/httpLoad.php";

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
        final int REQUEST_COUNT = 200;
        final int RESPONSE_SIZE = 100000;

        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = HttpUtil.executeGetRequestAndReturnResponse(100000, BASE_TESTING_URL, "size", RESPONSE_SIZE).getBytes();
                        assertEquals(RESPONSE_SIZE, bytes.length);
                        //System.out.println("Done " + (count.incrementAndGet()));
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

        System.out.println("Done 'testManyConcurrentGets' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testManyConcurrentPosts() throws InterruptedException {
        final int CONCURRENCY = 10;
        final int REQUEST_COUNT = 200;
        final int RESPONSE_SIZE = 100000;

        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = HttpUtil.executePostRequestAndReturnResponse(100000, BASE_TESTING_URL, "size", RESPONSE_SIZE).getBytes();
                        assertEquals(RESPONSE_SIZE, bytes.length);
                        //System.out.println("Done " + (count.incrementAndGet()));
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

        System.out.println("Done 'testManyConcurrentPosts' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testManyConcurrentDoGets() throws InterruptedException {
        final int CONCURRENCY = 10;
        final int REQUEST_COUNT = 200;
        final int RESPONSE_SIZE = 100000;

        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);

        long s = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = doGet(BASE_TESTING_URL + "?size=" + RESPONSE_SIZE);
                        assertEquals(RESPONSE_SIZE, bytes.length);
                        //System.out.println("Done " + (count.incrementAndGet()));
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

        System.out.println("Done 'testManyConcurrentDoGets' in " + (System.currentTimeMillis() - s) + " ms.");
    }

    public void testManyTimedOutPosts() throws InterruptedException {
        final int CONCURRENCY = 5;
        final int REQUEST_COUNT = 10 * CONCURRENCY;
        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);

        long s = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpResponse response = HttpUtil.executePostRequestAndReturnResponse(2000, BASE_TESTING_URL + "?delay=1000");
                        assertEquals(1024, response.getBytes().length);
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
            throw new RuntimeException("exceptions.size()=" + exceptions.size(), exceptions.get(0));
        }

        System.out.println("Done 'testManyTimedOutPosts' in " + (System.currentTimeMillis() - s) + " ms.");
    }
}
