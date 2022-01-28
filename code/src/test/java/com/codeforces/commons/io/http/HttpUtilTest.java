package com.codeforces.commons.io.http;

import com.codeforces.commons.exception.ExceptionUtil;
import com.codeforces.commons.io.*;
import com.codeforces.commons.math.NumberUtil;
import com.codeforces.commons.math.RandomUtil;
import com.codeforces.commons.process.ThreadUtil;
import com.codeforces.commons.text.StringUtil;
import fi.iki.elonen.NanoHTTPD;
import junit.framework.TestCase;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
@SuppressWarnings({"CallToPrintStackTrace", "MessageMissingOnJUnitAssertion", "deprecation", "SameParameterValue", "ResultOfMethodCallIgnored"})
public class HttpUtilTest extends TestCase {
    private static final String BASE_TESTING_URL_WO_PORT = "http://127.0.0.1:";
    private static final AtomicInteger port = new AtomicInteger(8080);
    private static String baseTestingUrl;

    private static final int CONCURRENCY_LEVEL = 20;
    private static final int REQUEST_COUNT = 500;

    private static final int DEFAULT_RESPONSE_SIZE = 1024;
    private static final int LARGE_RESPONSE_SIZE = 100000;

    private static final String POST_DATA = "Trololo Трололо №\"!?#@'`/\\,.()&^%$*<> ёыъьяю ™šœ "
            + RandomUtil.getRandomAlphanumeric(LARGE_RESPONSE_SIZE);

    private static final boolean VERBOSE = false;

    private NanoHTTPD server;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        int port = HttpUtilTest.port.incrementAndGet();
        baseTestingUrl = BASE_TESTING_URL_WO_PORT + port;
        server = new HttpRequestTestServer(port);
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

    public void testManyConcurrentGets() throws InterruptedException {
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY_LEVEL);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; ++i) {
            pool.submit(() -> {
                try {
                    byte[] bytes = HttpUtil.executeGetRequestAndReturnResponse(
                            100000, baseTestingUrl, "size", LARGE_RESPONSE_SIZE
                    ).getBytes();

                    assertEquals(LARGE_RESPONSE_SIZE, ArrayUtils.getLength(bytes));

                    if (VERBOSE) {
                        println("HttpUtilTest.testManyConcurrentGets: done " + count.incrementAndGet());
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    exceptions.add(e);
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(1L, TimeUnit.DAYS);

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size() = " + exceptions.size());
        }

        printf("Done 'HttpUtilTest.testManyConcurrentGets' in %d ms.%n", System.currentTimeMillis() - startTimeMillis);
    }

    public void testManyConcurrentPosts() throws InterruptedException {
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY_LEVEL);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; ++i) {
            pool.submit(() -> {
                try {
                    byte[] bytes = HttpUtil.executePostRequestAndReturnResponse(
                            100000, baseTestingUrl, "size", LARGE_RESPONSE_SIZE
                    ).getBytes();

                    assertEquals(LARGE_RESPONSE_SIZE, ArrayUtils.getLength(bytes));

                    if (VERBOSE) {
                        println("HttpUtilTest.testManyConcurrentPosts: done " + count.incrementAndGet());
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    exceptions.add(e);
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(1L, TimeUnit.DAYS);

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size() = " + exceptions.size());
        }

        printf("Done 'HttpUtilTest.testManyConcurrentPosts' in %d ms.%n", System.currentTimeMillis() - startTimeMillis);
    }

    public void testManyConcurrentDoGets() throws InterruptedException {
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY_LEVEL);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; ++i) {
            pool.submit(() -> {
                try {
                    byte[] bytes = doGet(baseTestingUrl + "?size=" + LARGE_RESPONSE_SIZE);

                    assertEquals(LARGE_RESPONSE_SIZE, bytes.length);

                    if (VERBOSE) {
                        println("HttpUtilTest.testManyConcurrentDoGets: done " + count.incrementAndGet());
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    exceptions.add(e);
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(1L, TimeUnit.DAYS);

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size() = " + exceptions.size());
        }

        printf("Done 'HttpUtilTest.testManyConcurrentDoGets' in %d ms.%n", System.currentTimeMillis() - startTimeMillis);
    }

    public void testManyNotTimedOutPosts() throws InterruptedException {
        int concurrency = 5;
        int requestCount = 5 * concurrency;

        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(concurrency);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < requestCount; ++i) {
            pool.submit(() -> {
                try {
                    HttpResponse response = HttpUtil.executePostRequestAndReturnResponse(
                            1500, baseTestingUrl + "?delay=1000"
                    );

                    assertEquals(
                            getIllegalResponseLengthMessage(response, DEFAULT_RESPONSE_SIZE),
                            DEFAULT_RESPONSE_SIZE, ArrayUtils.getLength(response.getBytes())
                    );

                    if (VERBOSE) {
                        println("HttpUtilTest.testManyNotTimedOutPosts: done " + count.incrementAndGet());
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    exceptions.add(e);
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(1L, TimeUnit.DAYS);

        if (!exceptions.isEmpty()) {
            throw new RuntimeException("exceptions.size() = " + exceptions.size(), exceptions.get(0));
        }

        printf("Done 'HttpUtilTest.testManyNotTimedOutPosts' in %d ms.%n", System.currentTimeMillis() - startTimeMillis);
    }

    public void testManyTimedOutPosts() throws InterruptedException {
        int concurrency = 5;
        int requestCount = 5 * concurrency;

        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger count = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(concurrency);

        long startTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < requestCount; ++i) {
            pool.submit(() -> {
                try {
                    HttpResponse response = HttpUtil.executePostRequestAndReturnResponse(
                            950, baseTestingUrl + "?delay=1000"
                    );

                    assertEquals(
                            getIllegalResponseLengthMessage(response, DEFAULT_RESPONSE_SIZE),
                            DEFAULT_RESPONSE_SIZE, ArrayUtils.getLength(response.getBytes())
                    );

                    if (VERBOSE) {
                        println("HttpUtilTest.testManyTimedOutPosts: done " + count.incrementAndGet());
                    }
                } catch (Throwable e) {
                    exceptions.add(e);
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(1L, TimeUnit.DAYS);

        if (exceptions.size() != requestCount) {
            throw new RuntimeException("exceptions.size() = " + exceptions.size());
        }

        printf("Done 'HttpUtilTest.testManyTimedOutPosts' in %d ms.%n", System.currentTimeMillis() - startTimeMillis);
    }

    public void testTimedOutPost() {
        long startTimeMillis = System.currentTimeMillis();

        HttpUtil.executePostRequestAndReturnResponse(1000, baseTestingUrl + "?delay=1000000");

        assertTrue(
                "Response with 1000 ms timeout takes more than 2050 ms.",
                (System.currentTimeMillis() - startTimeMillis) < 2050
        );

        println("Done 'HttpUtilTest.testTimedOutPost' in " + (System.currentTimeMillis() - startTimeMillis) + " ms.");
    }

    public void testPostWithBinaryEntity_HttpClientUtil() throws IOException {
        long startTimeMillis = System.currentTimeMillis();

        CloseableHttpClient httpClient = null;
        HttpPost request = null;
        org.apache.http.HttpResponse response = null;

        try {
            httpClient = HttpClientUtil.newHttpClient(20000, 20000);
            request = new HttpPost(baseTestingUrl);

            byte[] bytes = POST_DATA.getBytes(StandardCharsets.UTF_8);
            HttpEntity entity = new InputStreamEntity(new ByteArrayInputStream(bytes), bytes.length);
            request.setEntity(entity);

            response = httpClient.execute(request);
            assertEquals(String.format(
                    "Got unexpected response code %d.", response.getStatusLine().getStatusCode()
            ), HttpCode.OK, response.getStatusLine().getStatusCode());
        } finally {
            HttpClientUtil.closeQuietly(httpClient, request, response);
        }

        printf(
                "Done 'HttpUtilTest.testPostWithBinaryEntity_HttpClientUtil' in %d ms.%n",
                System.currentTimeMillis() - startTimeMillis
        );
    }

    public void testPostWithBinaryEntity() {
        long startTimeMillis = System.currentTimeMillis();

        HttpResponse response = HttpRequest.create(baseTestingUrl)
                .setMethod(HttpMethod.POST)
                .setBinaryEntity(POST_DATA.getBytes(StandardCharsets.UTF_8))
                .setTimeoutMillis(20000)
                .executeAndReturnResponse();

        response.getHeadersByNameMap();

        assertEquals(NanoHTTPD.Response.Status.BAD_REQUEST.getRequestStatus(), response.getCode());

        printf("Done 'HttpUtilTest.testPostWithBinaryEntity' in %d ms.%n", System.currentTimeMillis() - startTimeMillis);
    }

    public void testPostWithGzippedBinaryEntity_HttpClientUtil() throws IOException {
        long startTimeMillis = System.currentTimeMillis();

        CloseableHttpClient httpClient = null;
        HttpPost request = null;
        org.apache.http.HttpResponse response = null;

        try {
            httpClient = HttpClientUtil.newHttpClient(20000, 20000);
            request = new HttpPost(baseTestingUrl);

            HttpEntity entity;
            ByteArrayOutputStream gzippedOutputStream = null;
            OutputStream gzipOutputStream = null;
            InputStream entityInputStream = null;

            try {
                byte[] bytes = POST_DATA.getBytes(StandardCharsets.UTF_8);

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

        printf(
                "Done 'HttpUtilTest.testPostWithGzippedBinaryEntity_HttpClientUtil' in %d ms.%n",
                System.currentTimeMillis() - startTimeMillis
        );
    }

    public void testPostWithGzippedBinaryEntity() {
        long startTimeMillis = System.currentTimeMillis();

        HttpResponse response = HttpRequest.create(baseTestingUrl)
                .setMethod(HttpMethod.POST)
                .setBinaryEntity(POST_DATA.getBytes(StandardCharsets.UTF_8))
                .setTimeoutMillis(20000)
                .setGzip(true)
                .executeAndReturnResponse();

        assertEquals(
                getIllegalResponseLengthMessage(response, DEFAULT_RESPONSE_SIZE),
                DEFAULT_RESPONSE_SIZE, ArrayUtils.getLength(response.getBytes())
        );

        assertEquals(String.format(
                "Got unexpected response code %d.", response.getCode()
        ), HttpCode.OK, response.getCode());

        printf(
                "Done 'HttpUtilTest.testPostWithGzippedBinaryEntity' in %d ms.%n",
                System.currentTimeMillis() - startTimeMillis
        );
    }

    public void testPostWithGzippedParameters() {
        long startTimeMillis = System.currentTimeMillis();

        HttpResponse response = HttpRequest.create(baseTestingUrl)
                .setMethod(HttpMethod.POST)
                .appendParameter("size", LARGE_RESPONSE_SIZE)
                .setTimeoutMillis(20000)
                .setGzip(true)
                .executeAndReturnResponse();

        assertEquals(
                getIllegalResponseLengthMessage(response, LARGE_RESPONSE_SIZE),
                LARGE_RESPONSE_SIZE, ArrayUtils.getLength(response.getBytes())
        );

        assertEquals(String.format(
                "Got unexpected response code %d.", response.getCode()
        ), HttpCode.OK, response.getCode());

        printf(
                "Done 'HttpUtilTest.testPostWithGzippedParameters' in %d ms.%n",
                System.currentTimeMillis() - startTimeMillis
        );
    }

    public void testPostWithoutReadingResponse() {
        assertEquals(HttpCode.OK, HttpRequest.create(baseTestingUrl).setMethod(HttpMethod.POST).execute());
    }

    private static String getIllegalResponseLengthMessage(HttpResponse response, int expectedLength) {
        return String.format("Expected response length: %d. %s.", expectedLength, response);
    }

    private static void println(String line) {
        System.out.println(line);
        System.out.flush();
    }

    private static void printf(String format, Object... arguments) {
        System.out.printf(format, arguments);
        System.out.flush();
    }

    private static final class HttpRequestTestServer extends NanoHTTPD {
        private static final int TRUE_RANDOM_PART_LENGTH = 50;

        private final String randomString1024 = getRandomString(DEFAULT_RESPONSE_SIZE - 2 * TRUE_RANDOM_PART_LENGTH);
        private final String randomString100000 = getRandomString(LARGE_RESPONSE_SIZE - 2 * TRUE_RANDOM_PART_LENGTH);

        public HttpRequestTestServer(int port) {
            super(port);
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

            return newFixedLengthResponse(Response.Status.OK, MimeUtil.Type.TEXT_PLAIN, responseBody);
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

                        files.put("postData", new String(bytes, StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        return newFixedLengthResponse(
                                Response.Status.INTERNAL_ERROR, MimeUtil.Type.TEXT_PLAIN, ExceptionUtil.toString(e)
                        );
                    }
                } else {
                    try {
                        session.parseBody(files);
                    } catch (IOException e) {
                        return newFixedLengthResponse(
                                Response.Status.INTERNAL_ERROR, MimeUtil.Type.TEXT_PLAIN, ExceptionUtil.toString(e)
                        );
                    } catch (ResponseException e) {
                        return newFixedLengthResponse(e.getStatus(), MimeUtil.Type.TEXT_PLAIN, ExceptionUtil.toString(e));
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
                    return newFixedLengthResponse(
                            Response.Status.BAD_REQUEST, MimeUtil.Type.TEXT_PLAIN, "Received illegal POST data."
                    );
                }
            } else if (MimeUtil.Type.APPLICATION_X_WWW_FORM_URLENCODED.equalsIgnoreCase(contentType)) {
                for (String postParameter : StringUtil.split(postData, '&')) {
                    if (StringUtil.isBlank(postParameter)) {
                        continue;
                    }

                    String[] postParameterParts = StringUtil.split(postParameter, '=');
                    if (postParameterParts.length != 2) {
                        continue;
                    }

                    String parameterName = postParameterParts[0];

                    if (parameterValueByName.containsKey(parameterName)) {
                        return newFixedLengthResponse(
                                Response.Status.BAD_REQUEST, MimeUtil.Type.TEXT_PLAIN,
                                "Received duplicate parameter '" + parameterName + "'."
                        );
                    }

                    parameterValueByName.put(parameterName, postParameterParts[1]);
                }
            }

            return null;
        }

        @Nonnull
        private static String getRandomString(int length) {
            return RandomUtil.getRandomAlphanumeric(length);
        }
    }
}
