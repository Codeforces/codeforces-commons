package com.codeforces.commons.io.http;

import com.codeforces.commons.exception.ExceptionUtil;
import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.io.IoUtil;
import com.codeforces.commons.io.LimitedByteArrayOutputStream;
import com.codeforces.commons.io.MimeUtil;
import com.codeforces.commons.math.NumberUtil;
import com.codeforces.commons.process.ThreadUtil;
import com.codeforces.commons.text.Patterns;
import com.codeforces.commons.text.StringUtil;
import fi.iki.elonen.NanoHTTPD;
import junit.framework.TestCase;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.junit.Ignore;

import javax.annotation.Nullable;
import java.io.*;
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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Mike Mirzayanov (mirzayanovmr@gmail.com)
 * @author Maxim Shipko (sladethe@gmail.com)
 */
@SuppressWarnings({"CallToPrintStackTrace", "MessageMissingOnJUnitAssertion"})
public class HttpUtilTest extends TestCase {
    private static final String BASE_TESTING_URL = "http://127.0.0.1:8081";
    //private static final String BASE_TESTING_URL = "http://polygon-api.codeforces.com/httpLoad";

    private static final int CONCURRENCY_LEVEL = 20;
    private static final int REQUEST_COUNT = 500;

    private static final int DEFAULT_RESPONSE_SIZE = 1024;
    private static final int LARGE_RESPONSE_SIZE = 100000;

    private static final String POST_DATA = "Trololo Трололо №\"!?#@'`/\\,.()&^%$*<> ёыъьяю ™šœ "
            + RandomStringUtils.randomAlphanumeric(LARGE_RESPONSE_SIZE);

    private static final boolean VERBOSE = false;

    private final NanoHTTPD server = new HttpRequestTestServer();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        server.start();
    }

    @Override
    public void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

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

    @Ignore
    public void _testManyConcurrentGets_HttpClientUtil() throws InterruptedException, IOException {
        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY_LEVEL);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; ++i) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = IoUtil.toByteArray(HttpClientUtil.executeGetRequestAndReturnResponse(
                                100000, BASE_TESTING_URL, "size", LARGE_RESPONSE_SIZE
                        ).getInputStream());

                        assertEquals(LARGE_RESPONSE_SIZE, bytes.length);

                        if (VERBOSE) {
                            System.out.println("testManyConcurrentGets_HttpClientUtil: done " + count.incrementAndGet());
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

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size() = " + exceptions.size());
        }

        System.out.println("Done 'testManyConcurrentGets_HttpClientUtil' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testManyConcurrentGets() throws InterruptedException, IOException {
        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY_LEVEL);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; ++i) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = HttpUtil.executeGetRequestAndReturnResponse(
                                100000, BASE_TESTING_URL, "size", LARGE_RESPONSE_SIZE
                        ).getBytes();

                        assertEquals(LARGE_RESPONSE_SIZE, bytes.length);

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

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size() = " + exceptions.size());
        }

        System.out.println("Done 'testManyConcurrentGets' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    @Ignore
    public void _testManyConcurrentPosts_HttpClientUtil() throws InterruptedException, IOException {
        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY_LEVEL);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; ++i) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = IoUtil.toByteArray(HttpClientUtil.executePostRequestAndReturnResponse(
                                100000, BASE_TESTING_URL, "size", LARGE_RESPONSE_SIZE
                        ).getInputStream());

                        assertEquals(LARGE_RESPONSE_SIZE, bytes.length);

                        if (VERBOSE) {
                            System.out.println("testManyConcurrentPosts_HttpClientUtil: done " + count.incrementAndGet());
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

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size() = " + exceptions.size());
        }

        System.out.println("Done 'testManyConcurrentPosts_HttpClientUtil' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testManyConcurrentPosts() throws InterruptedException, IOException {
        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY_LEVEL);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; ++i) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = HttpUtil.executePostRequestAndReturnResponse(
                                100000, BASE_TESTING_URL, "size", LARGE_RESPONSE_SIZE
                        ).getBytes();

                        assertEquals(LARGE_RESPONSE_SIZE, bytes.length);

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

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size() = " + exceptions.size());
        }

        System.out.println("Done 'testManyConcurrentPosts' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testManyConcurrentDoGets() throws InterruptedException, IOException {
        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY_LEVEL);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; ++i) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] bytes = doGet(BASE_TESTING_URL + "?size=" + LARGE_RESPONSE_SIZE);

                        assertEquals(LARGE_RESPONSE_SIZE, bytes.length);

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

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size() = " + exceptions.size());
        }

        System.out.println("Done 'testManyConcurrentDoGets' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testManyNotTimedOutPosts() throws InterruptedException, IOException {
        int concurrency = 5;
        int requestCount = 5 * concurrency;

        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(concurrency);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < requestCount; ++i) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpResponse response = HttpUtil.executePostRequestAndReturnResponse(
                                1500, BASE_TESTING_URL + "?delay=1000"
                        );

                        assertEquals(getIllegalResponseLengthMessage(response, DEFAULT_RESPONSE_SIZE), DEFAULT_RESPONSE_SIZE, response.getBytes().length);

                        if (VERBOSE) {
                            System.out.println("testManyNotTimedOutPosts: done " + count.incrementAndGet());
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

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size() = " + exceptions.size(), exceptions.get(0));
        }

        System.out.println("Done 'testManyNotTimedOutPosts' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testManyTimedOutPosts() throws InterruptedException, IOException {
        int concurrency = 5;
        int requestCount = 5 * concurrency;

        final List<Throwable> exceptions = new ArrayList<>();
        final AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(concurrency);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < requestCount; ++i) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpResponse response = HttpUtil.executePostRequestAndReturnResponse(
                                950, BASE_TESTING_URL + "?delay=1000"
                        );

                        assertEquals(getIllegalResponseLengthMessage(response, DEFAULT_RESPONSE_SIZE), DEFAULT_RESPONSE_SIZE, response.getBytes().length);

                        if (VERBOSE) {
                            System.out.println("testManyTimedOutPosts: done " + count.incrementAndGet());
                        }
                    } catch (Throwable e) {
                        exceptions.add(e);
                    }
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(1L, TimeUnit.DAYS);

        if (exceptions.size() != requestCount) {
            throw new RuntimeException("exceptions.size() = " + exceptions.size());
        }

        System.out.println("Done 'testManyTimedOutPosts' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testTimedOutPost() throws InterruptedException, IOException {
        long startTimeMillis = System.currentTimeMillis();

        HttpUtil.executePostRequestAndReturnResponse(1000, BASE_TESTING_URL + "?delay=1000000");

        assertTrue(
                "Response with 1000 ms timeout takes more than 2050 ms.",
                (System.currentTimeMillis() - startTimeMillis) < 2050
        );

        System.out.println("Done 'testTimedOutPost' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testPostWithBinaryEntity_HttpClientUtil() throws IOException {
        long startTimeMillis = System.currentTimeMillis();

        CloseableHttpClient httpClient = null;
        HttpPost request = null;
        org.apache.http.HttpResponse response = null;

        try {
            httpClient = HttpClientUtil.newHttpClient(20000, 20000);
            request = new HttpPost(BASE_TESTING_URL);

            byte[] bytes = POST_DATA.getBytes(Charsets.UTF_8);
            HttpEntity entity = new InputStreamEntity(new ByteArrayInputStream(bytes), bytes.length);
            request.setEntity(entity);

            response = httpClient.execute(request);
            assertEquals(String.format(
                    "Got unexpected response code %d.", response.getStatusLine().getStatusCode()
            ), HttpCode.OK, response.getStatusLine().getStatusCode());
        } finally {
            HttpClientUtil.closeQuietly(httpClient, request, response);
        }

        System.out.println("Done 'testPostWithBinaryEntity_HttpClientUtil' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testPostWithBinaryEntity() throws IOException {
        long startTimeMillis = System.currentTimeMillis();

        HttpResponse response = HttpRequest.create(BASE_TESTING_URL)
                .setMethod(HttpMethod.POST)
                .setBinaryEntity(POST_DATA.getBytes(Charsets.UTF_8))
                .setTimeoutMillis(20000)
                .executeAndReturnResponse();

        response.getHeadersByNameMap();

        assertEquals(getIllegalResponseLengthMessage(response, DEFAULT_RESPONSE_SIZE), DEFAULT_RESPONSE_SIZE, response.getBytes().length);

        assertEquals(String.format(
                "Got unexpected response code %d.", response.getCode()
        ), HttpCode.OK, response.getCode());

        System.out.println("Done 'testPostWithBinaryEntity' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testPostWithGzippedBinaryEntity_HttpClientUtil() throws IOException {
        long startTimeMillis = System.currentTimeMillis();

        CloseableHttpClient httpClient = null;
        HttpPost request = null;
        org.apache.http.HttpResponse response = null;

        try {
            httpClient = HttpClientUtil.newHttpClient(20000, 20000);
            request = new HttpPost(BASE_TESTING_URL);

            HttpEntity entity;
            ByteArrayOutputStream gzippedOutputStream = null;
            OutputStream gzipOutputStream = null;
            InputStream entityInputStream = null;

            try {
                byte[] bytes = POST_DATA.getBytes(Charsets.UTF_8);

                gzippedOutputStream = new ByteArrayOutputStream();
                gzipOutputStream = new GZIPOutputStream(gzippedOutputStream);
                gzipOutputStream.write(bytes);
                gzipOutputStream.close();
                entityInputStream = new ByteArrayInputStream(gzippedOutputStream.toByteArray());

                entity = new InputStreamEntity(entityInputStream, gzippedOutputStream.size());
            } finally {
                IoUtil.closeQuietly(gzippedOutputStream, gzipOutputStream, entityInputStream);
            }

            request.addHeader(new BasicHeader("Content-Encoding", "gzip"));
            request.setEntity(entity);

            response = httpClient.execute(request);
            assertEquals(String.format(
                    "Got unexpected response code %d.", response.getStatusLine().getStatusCode()
            ), HttpCode.OK, response.getStatusLine().getStatusCode());
        } finally {
            HttpClientUtil.closeQuietly(httpClient, request, response);
        }

        System.out.println("Done 'testPostWithGzippedBinaryEntity_HttpClientUtil' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testPostWithGzippedBinaryEntity() throws IOException {
        long startTimeMillis = System.currentTimeMillis();

        HttpResponse response = HttpRequest.create(BASE_TESTING_URL)
                .setMethod(HttpMethod.POST)
                .setBinaryEntity(POST_DATA.getBytes(Charsets.UTF_8))
                .setTimeoutMillis(20000)
                .setGzip(true)
                .executeAndReturnResponse();

        assertEquals(getIllegalResponseLengthMessage(response, DEFAULT_RESPONSE_SIZE), DEFAULT_RESPONSE_SIZE, response.getBytes().length);

        assertEquals(String.format(
                "Got unexpected response code %d.", response.getCode()
        ), HttpCode.OK, response.getCode());

        System.out.println("Done 'testPostWithGzippedBinaryEntity' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testPostWithGzippedParameters() throws IOException {
        long startTimeMillis = System.currentTimeMillis();

        HttpResponse response = HttpRequest.create(BASE_TESTING_URL)
                .setMethod(HttpMethod.POST)
                .appendParameter("size", LARGE_RESPONSE_SIZE)
                .setTimeoutMillis(20000)
                .setGzip(true)
                .executeAndReturnResponse();

        assertEquals(getIllegalResponseLengthMessage(response, LARGE_RESPONSE_SIZE), LARGE_RESPONSE_SIZE, response.getBytes().length);

        assertEquals(String.format(
                "Got unexpected response code %d.", response.getCode()
        ), HttpCode.OK, response.getCode());

        System.out.println("Done 'testPostWithGzippedParameters' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    private static String getIllegalResponseLengthMessage(HttpResponse response, int expectedLength) {
        return String.format("Expected response length: %d. %s.", expectedLength, response);
    }

    private static final class HttpRequestTestServer extends NanoHTTPD {
        private static final int TRUE_RANDOM_PART_LENGTH = 50;

        private final String randomString1024 = getRandomString(DEFAULT_RESPONSE_SIZE - 2 * TRUE_RANDOM_PART_LENGTH);
        private final String randomString100000 = getRandomString(LARGE_RESPONSE_SIZE - 2 * TRUE_RANDOM_PART_LENGTH);

        private HttpRequestTestServer() {
            super(8081);
        }

        @SuppressWarnings({"RefusedBequest", "OverlyLongMethod"})
        @Override
        public Response serve(IHTTPSession session) {
            Map<String, String> files = new HashMap<>();

            @Nullable Response response = parseRequest(session, files);
            if (response != null) {
                return response;
            }

            Map<String, String> parameterValueByName = new HashMap<>(session.getParms());

            response = validatePostDataAndUpdateParameters(session, files, parameterValueByName);
            if (response != null) {
                return response;
            }

            String delayString = parameterValueByName.get("delay");
            if (delayString != null) {
                ThreadUtil.sleep(NumberUtil.toInt(delayString));
            }

            String sizeString = parameterValueByName.get("size");
            int size = sizeString == null ? DEFAULT_RESPONSE_SIZE : NumberUtil.toInt(sizeString);

            String randomPrefixPart = getRandomString(TRUE_RANDOM_PART_LENGTH);
            String randomPostfixPart = getRandomString(TRUE_RANDOM_PART_LENGTH);

            String responseBody;
            if (size == DEFAULT_RESPONSE_SIZE) {
                responseBody = randomPrefixPart + randomString1024 + randomPostfixPart;
            } else if (size == LARGE_RESPONSE_SIZE) {
                responseBody = randomPrefixPart + randomString100000 + randomPostfixPart;
            } else {
                throw new IllegalArgumentException(String.format("Unsupported size %d.", size));
            }

            return new Response(Response.Status.OK, MimeUtil.Type.TEXT_PLAIN, responseBody);
        }

        /**
         * Parses HTTP request.
         *
         * @param session all-in-one HTTP session and request
         * @param files   map to store parsed file data
         * @return {@code null} if succeeded and {@code {@link Response Response}} in case of some error
         */
        @Nullable
        private static Response parseRequest(IHTTPSession session, Map<String, String> files) {
            Map<String, String> headerValueByName = session.getHeaders();

            if (session.getMethod() == Method.PUT || session.getMethod() == Method.POST) {
                if ("gzip".equalsIgnoreCase(headerValueByName.get("Content-Encoding".toLowerCase()))) {
                    try {
                        String contentLengthString = headerValueByName.get("Content-Length".toLowerCase());
                        InputStream inputStream = session.getInputStream();
                        ByteArrayOutputStream outputStream;

                        if (StringUtil.isBlank(contentLengthString)) {
                            outputStream = new ByteArrayOutputStream();
                            IoUtil.copy(inputStream, outputStream, false, true, NumberUtil.toInt(FileUtil.BYTES_PER_GB));
                        } else {
                            int contentLength = NumberUtil.toInt(contentLengthString);
                            outputStream = new LimitedByteArrayOutputStream(contentLength, true);
                            IoUtil.copy(inputStream, outputStream, false, true, contentLength);
                        }

                        byte[] bytes = outputStream.toByteArray();
                        bytes = IoUtil.toByteArray(new GZIPInputStream(new ByteArrayInputStream(bytes)));

                        files.put("postData", new String(bytes, Charsets.UTF_8));
                    } catch (IOException e) {
                        return new Response(
                                Response.Status.INTERNAL_ERROR, MimeUtil.Type.TEXT_PLAIN, ExceptionUtil.toString(e)
                        );
                    }
                } else {
                    try {
                        session.parseBody(files);
                    } catch (IOException e) {
                        return new Response(
                                Response.Status.INTERNAL_ERROR, MimeUtil.Type.TEXT_PLAIN, ExceptionUtil.toString(e)
                        );
                    } catch (ResponseException e) {
                        return new Response(e.getStatus(), MimeUtil.Type.TEXT_PLAIN, ExceptionUtil.toString(e));
                    }
                }
            }

            return null;
        }

        @Nullable
        private static Response validatePostDataAndUpdateParameters(
                IHTTPSession session, Map<String, String> files, Map<String, String> parameterValueByName) {
            String postData = files.get("postData");
            if (postData == null) {
                return null;
            }

            Map<String, String> headerValueByName = session.getHeaders();
            String contentType = headerValueByName.get("Content-Type".toLowerCase());

            if (MimeUtil.Type.APPLICATION_OCTET_STREAM.equalsIgnoreCase(contentType)) {
                if (!POST_DATA.equals(postData)) {
                    return new Response(
                            Response.Status.BAD_REQUEST, MimeUtil.Type.TEXT_PLAIN, "Received illegal POST data."
                    );
                }
            } else if (MimeUtil.Type.APPLICATION_X_WWW_FORM_URLENCODED.equalsIgnoreCase(contentType)) {
                for (String postParameter : Patterns.AMP_PATTERN.split(postData)) {
                    if (StringUtil.isBlank(postParameter)) {
                        continue;
                    }

                    String[] postParameterParts = Patterns.EQ_PATTERN.split(postParameter);
                    if (postParameterParts.length != 2) {
                        continue;
                    }

                    String parameterName = postParameterParts[0];

                    if (parameterValueByName.containsKey(parameterName)) {
                        return new Response(
                                Response.Status.BAD_REQUEST, MimeUtil.Type.TEXT_PLAIN,
                                "Received duplicate parameter '" + parameterName + "'."
                        );
                    }

                    parameterValueByName.put(parameterName, postParameterParts[1]);
                }
            }

            return null;
        }

        private static String getRandomString(int length) {
            return RandomStringUtils.randomAlphanumeric(length);
        }
    }
}
