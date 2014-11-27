package com.codeforces.commons.io;

import com.codeforces.commons.io.http.HttpResponse;
import com.codeforces.commons.io.http.HttpUtil;
import junit.framework.TestCase;
import org.junit.Ignore;

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
@Ignore
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
                        byte[] bytes = HttpUtil.executeGetRequestAndReturnResponse(1000, BASE_TESTING_URL, "size", RESPONSE_SIZE).getBytes();
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

        System.out.println("Done in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
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
                        byte[] bytes = HttpUtil.executePostRequestAndReturnResponse(1000, BASE_TESTING_URL, "size", RESPONSE_SIZE).getBytes();
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

        System.out.println("Done in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
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

        System.out.println("Done in " + (System.currentTimeMillis() - s) + " ms.");
    }

    @Ignore
    public void testManyConcurrentHttpUtilHttpClientGets() throws InterruptedException {
        final int CONCURRENCY = 10;
        final int REQUEST_COUNT = 200;
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

        System.out.println("Done in " + (System.currentTimeMillis() - s) + " ms.");
    }

    public void testManyTimedOutConcurrentHttpUtilHttpClientPosts() throws InterruptedException {
        final int CONCURRENCY = 5;
        final int REQUEST_COUNT = 10 * CONCURRENCY;
        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);

        long s = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            System.out.println(i);
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    long startTimeMillis = System.currentTimeMillis();
                    try {
                        //System.out.println("Waiting...");
                        byte[] bytes = HttpUtil_HttpClient.executePostRequestAndReturnResponseBytes(2000, BASE_TESTING_URL + "?delay=500");
                        assertEquals(1024, bytes.length);
                        //System.out.println("Done " + (count.incrementAndGet()) + " in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
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

        System.out.println("Done in " + (System.currentTimeMillis() - s) + " ms.");
    }

    public void testManyTimedOutPosts() throws InterruptedException {
        final int CONCURRENCY = 5;
        final int REQUEST_COUNT = 10 * CONCURRENCY;
        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);

        long s = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            System.out.println(i);
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    long startTimeMillis = System.currentTimeMillis();
                    try {
                        //System.out.println("Waiting...");
                        HttpResponse response = HttpUtil.executePostRequestAndReturnResponse(1000, BASE_TESTING_URL + "?delay=1800");
                        if (response.getBytes() == null) {
                            System.out.println(1);
                        }
                        assertEquals(1024, response.getBytes().length);
                        //System.out.println("Done " + (count.incrementAndGet()) + " in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
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

        System.out.println("Done in " + (System.currentTimeMillis() - s) + " ms.");
    }

    public static void main(String[] args) throws IOException {
        System.out.println(doGet(BASE_TESTING_URL + "?delay=1").length);
    }
}
