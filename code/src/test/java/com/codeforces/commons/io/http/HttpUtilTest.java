package com.codeforces.commons.io.http;

import com.codeforces.commons.exception.ExceptionUtil;
import com.codeforces.commons.io.IoUtil;
import com.codeforces.commons.io.MimeUtil;
import com.codeforces.commons.math.NumberUtil;
import com.codeforces.commons.process.ThreadUtil;
import fi.iki.elonen.NanoHTTPD;
import junit.framework.TestCase;
import org.apache.commons.lang.RandomStringUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mike Mirzayanov (mirzayanovmr@gmail.com)
 * @author Maxim Shipko (sladethe@gmail.com)
 */
@SuppressWarnings({"CallToPrintStackTrace", "MessageMissingOnJUnitAssertion"})
public class HttpUtilTest extends TestCase {
    private static final String BASE_TESTING_URL = "http://127.0.0.1:8081";
    private static final boolean VERBOSE = false;

    private static byte[] doGet(String s) throws IOException {
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

    public void testManyConcurrentGets() throws InterruptedException, IOException {
        int CONCURRENCY = 10;
        int REQUEST_COUNT = 200;
        final int RESPONSE_SIZE = 100000;

        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        NanoHTTPD server = new HttpRequestTestServer();
        server.start();
        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; ++i) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = HttpUtil.executeGetRequestAndReturnResponse(
                                100000, BASE_TESTING_URL, "size", RESPONSE_SIZE
                        ).getBytes();

                        assertEquals(RESPONSE_SIZE, bytes.length);

                        if (VERBOSE) {
                            System.out.println("testManyConcurrentGets: done " + count.incrementAndGet());
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        exceptions.add(e);
                    }
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(1L, TimeUnit.DAYS);
        server.stop();

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size() = " + exceptions.size());
        }

        System.out.println("Done 'testManyConcurrentGets' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testManyConcurrentPosts() throws InterruptedException, IOException {
        int CONCURRENCY = 10;
        int REQUEST_COUNT = 200;
        final int RESPONSE_SIZE = 100000;

        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        NanoHTTPD server = new HttpRequestTestServer();
        server.start();
        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; ++i) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = HttpUtil.executePostRequestAndReturnResponse(
                                100000, BASE_TESTING_URL, "size", RESPONSE_SIZE
                        ).getBytes();

                        assertEquals(RESPONSE_SIZE, bytes.length);

                        if (VERBOSE) {
                            System.out.println("testManyConcurrentPosts: done " + count.incrementAndGet());
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        exceptions.add(e);
                    }
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(1L, TimeUnit.DAYS);
        server.stop();

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size() = " + exceptions.size());
        }

        System.out.println("Done 'testManyConcurrentPosts' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testManyConcurrentDoGets() throws InterruptedException, IOException {
        int CONCURRENCY = 10;
        int REQUEST_COUNT = 200;
        final int RESPONSE_SIZE = 100000;

        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        NanoHTTPD server = new HttpRequestTestServer();
        server.start();
        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; ++i) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = doGet(BASE_TESTING_URL + "?size=" + RESPONSE_SIZE);

                        assertEquals(RESPONSE_SIZE, bytes.length);

                        if (VERBOSE) {
                            System.out.println("testManyConcurrentDoGets: done " + count.incrementAndGet());
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        exceptions.add(e);
                    }
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(1L, TimeUnit.DAYS);
        server.stop();

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size() = " + exceptions.size());
        }

        System.out.println("Done 'testManyConcurrentDoGets' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testManyTimedOutPosts() throws InterruptedException, IOException {
        int CONCURRENCY = 5;
        int REQUEST_COUNT = 10 * CONCURRENCY;

        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        NanoHTTPD server = new HttpRequestTestServer();
        server.start();
        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; ++i) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpResponse response = HttpUtil.executePostRequestAndReturnResponse(
                                2000, BASE_TESTING_URL + "?delay=1000"
                        );

                        assertEquals(1024, response.getBytes().length);

                        if (VERBOSE) {
                            System.out.println("testManyTimedOutPosts: done " + count.incrementAndGet());
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        exceptions.add(e);
                    }
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(1L, TimeUnit.DAYS);
        server.stop();

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size() = " + exceptions.size(), exceptions.get(0));
        }

        System.out.println("Done 'testManyTimedOutPosts' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    private static final class HttpRequestTestServer extends NanoHTTPD {
        private HttpRequestTestServer() {
            super(8081);
        }

        @SuppressWarnings("RefusedBequest")
        @Override
        public Response serve(IHTTPSession session) {
            if (session.getMethod() == Method.PUT || session.getMethod() == Method.POST) {
                try {
                    session.parseBody(new HashMap<String, String>());
                } catch (IOException e) {
                    return new Response(
                            Response.Status.INTERNAL_ERROR, MimeUtil.Type.TEXT_PLAIN, ExceptionUtil.toString(e)
                    );
                } catch (ResponseException e) {
                    return new Response(e.getStatus(), MimeUtil.Type.TEXT_PLAIN, ExceptionUtil.toString(e));
                }
            }

            Map<String, String> parameterValueByName = session.getParms();

            String delayString = parameterValueByName.get("delay");
            if (delayString != null) {
                ThreadUtil.sleep(NumberUtil.toInt(delayString));
            }

            String sizeString = parameterValueByName.get("size");
            int size = sizeString == null ? 1024 : NumberUtil.toInt(sizeString);

            return new Response(
                    Response.Status.OK, MimeUtil.Type.TEXT_PLAIN, RandomStringUtils.randomAlphanumeric(size)
            );
        }
    }
}
