package com.codeforces.commons.io;

import com.codeforces.commons.process.ThreadUtil;
import com.codeforces.commons.properties.internal.CommonsPropertiesUtil;
import com.codeforces.commons.text.StringUtil;
import com.codeforces.commons.text.UrlUtil;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 07.11.11
 */
@SuppressWarnings("OverloadedVarargsMethod")
public class HttpUtil {
    private static final int CONNECTION_POOL_DEFAULT_MAX_SIZE = 50;
    private static final int CONNECTION_POOL_DEFAULT_MAX_SIZE_PER_HOST = 25;

    private static final ExecutorService timedRequestExecutor = new ThreadPoolExecutor(
            0, Short.MAX_VALUE, 5L, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(),
            ThreadUtil.getCustomPoolThreadFactory(new ThreadUtil.ThreadCustomizer() {
                private final AtomicLong threadIndex = new AtomicLong();

                @Override
                public void customize(Thread thread) {
                    thread.setDaemon(true);
                    thread.setName(String.format(
                            "%s#RequestExecutionThread-%d",
                            HttpUtil.class.getSimpleName(), threadIndex.incrementAndGet()
                    ));
                }
            })
    );

    private HttpUtil() {
        throw new UnsupportedOperationException();
    }

    public static void executeGetRequest(
            HttpClient httpClient, boolean encodeParameters, String url, Object... parameters) throws IOException {
        internalExecuteGetRequest(httpClient, encodeParameters, url, parameters);
    }

    public static void executeGetRequest(
            long executionTimeoutMillis, final HttpClient httpClient, final boolean encodeParameters,
            final String url, final Object... parameters) throws IOException {
        internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                executeGetRequest(httpClient, encodeParameters, url, parameters);
                return null;
            }
        });
    }

    public static void executeGetRequest(
            boolean encodeParameters, String url, Object... parameters) throws IOException {
        executeGetRequest(null, encodeParameters, url, parameters);
    }

    public static void executeGetRequest(
            long executionTimeoutMillis, final boolean encodeParameters,
            final String url, final Object... parameters) throws IOException {
        internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                executeGetRequest(encodeParameters, url, parameters);
                return null;
            }
        });
    }

    public static void executeGetRequest(String url, Object... parameters) throws IOException {
        executeGetRequest(false, url, parameters);
    }

    public static void executeGetRequest(
            long executionTimeoutMillis, final String url, final Object... parameters) throws IOException {
        internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                executeGetRequest(url, parameters);
                return null;
            }
        });
    }

    public static byte[] executeGetRequestAndReturnResponseBytes(
            HttpClient httpClient, boolean encodeParameters, String url, Object... parameters) throws IOException {
        HttpResponse httpResponse = internalExecuteGetRequest(httpClient, encodeParameters, url, parameters);
        InputStream inputStream = httpResponse.getEntity().getContent();

        return IoUtil.toByteArray(inputStream);
    }

    public static byte[] executeGetRequestAndReturnResponseBytes(
            long executionTimeoutMillis, final HttpClient httpClient, final boolean encodeParameters,
            final String url, final Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return executeGetRequestAndReturnResponseBytes(httpClient, encodeParameters, url, parameters);
            }
        });
    }

    public static byte[] executeGetRequestAndReturnResponseBytes(
            boolean encodeParameters, String url, Object... parameters) throws IOException {
        return executeGetRequestAndReturnResponseBytes(null, encodeParameters, url, parameters);
    }

    public static byte[] executeGetRequestAndReturnResponseBytes(
            long executionTimeoutMillis, final boolean encodeParameters,
            final String url, final Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return executeGetRequestAndReturnResponseBytes(encodeParameters, url, parameters);
            }
        });
    }

    public static byte[] executeGetRequestAndReturnResponseBytes(String url, Object... parameters) throws IOException {
        return executeGetRequestAndReturnResponseBytes(false, url, parameters);
    }

    public static byte[] executeGetRequestAndReturnResponseBytes(
            long executionTimeoutMillis, final String url, final Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return executeGetRequestAndReturnResponseBytes(url, parameters);
            }
        });
    }

    public static String executeGetRequestAndReturnResponseAsString(
            HttpClient httpClient, boolean encodeParameters, String url, Object... parameters) throws IOException {
        HttpResponse httpResponse = internalExecuteGetRequest(httpClient, encodeParameters, url, parameters);
        HttpEntity httpEntity = httpResponse.getEntity();
        InputStream inputStream = httpEntity.getContent();

        return httpEntity.getContentEncoding() == null
                ? IoUtil.toString(inputStream)
                : IoUtil.toString(inputStream, httpEntity.getContentEncoding().getValue());
    }

    public static String executeGetRequestAndReturnResponseAsString(
            long executionTimeoutMillis, final HttpClient httpClient, final boolean encodeParameters,
            final String url, final Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<String>() {
            @Override
            public String call() throws Exception {
                return executeGetRequestAndReturnResponseAsString(httpClient, encodeParameters, url, parameters);
            }
        });
    }

    public static String executeGetRequestAndReturnResponseAsString(
            boolean encodeParameters, String url, Object... parameters) throws IOException {
        return executeGetRequestAndReturnResponseAsString(null, encodeParameters, url, parameters);
    }

    public static String executeGetRequestAndReturnResponseAsString(
            long executionTimeoutMillis, final boolean encodeParameters,
            final String url, final Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<String>() {
            @Override
            public String call() throws Exception {
                return executeGetRequestAndReturnResponseAsString(encodeParameters, url, parameters);
            }
        });
    }

    public static String executeGetRequestAndReturnResponseAsString(
            String url, Object... parameters) throws IOException {
        return executeGetRequestAndReturnResponseAsString(false, url, parameters);
    }

    public static String executeGetRequestAndReturnResponseAsString(
            long executionTimeoutMillis, final String url, final Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<String>() {
            @Override
            public String call() throws Exception {
                return executeGetRequestAndReturnResponseAsString(url, parameters);
            }
        });
    }

    public static Response executeGetRequestAndReturnResponse(
            HttpClient httpClient, boolean encodeParameters, String url, Object... parameters) throws IOException {
        HttpResponse httpResponse = internalExecuteGetRequest(httpClient, encodeParameters, url, parameters);
        HttpEntity httpEntity = httpResponse.getEntity();

        return new Response(
                httpResponse.getStatusLine().getStatusCode(),
                httpEntity.getContent(),
                httpEntity.getContentEncoding() == null ? null : httpEntity.getContentEncoding().getValue(),
                httpEntity.getContentLength()
        );
    }

    public static Response executeGetRequestAndReturnResponse(
            long executionTimeoutMillis, final HttpClient httpClient, final boolean encodeParameters,
            final String url, final Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return executeGetRequestAndReturnResponse(httpClient, encodeParameters, url, parameters);
            }
        });
    }

    public static Response executeGetRequestAndReturnResponse(
            boolean encodeParameters, String url, Object... parameters) throws IOException {
        return executeGetRequestAndReturnResponse(null, encodeParameters, url, parameters);
    }

    public static Response executeGetRequestAndReturnResponse(
            long executionTimeoutMillis, final boolean encodeParameters,
            final String url, final Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return executeGetRequestAndReturnResponse(encodeParameters, url, parameters);
            }
        });
    }

    public static Response executeGetRequestAndReturnResponse(String url, Object... parameters) throws IOException {
        return executeGetRequestAndReturnResponse(false, url, parameters);
    }

    public static Response executeGetRequestAndReturnResponse(
            long executionTimeoutMillis, final String url, final Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return executeGetRequestAndReturnResponse(url, parameters);
            }
        });
    }

    private static HttpResponse internalExecuteGetRequest(
            @Nullable HttpClient httpClient, boolean encodeParameters,
            String url, Object... parameters) throws IOException {
        parameters = validateAndPreprocessParameters(encodeParameters, url, parameters);

        for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex += 2) {
            url = UrlUtil.appendParameterToUrl(
                    url, (String) parameters[parameterIndex], parameters[parameterIndex + 1].toString()
            );
        }

        httpClient = httpClient == null ? newHttpClient() : httpClient;
        HttpGet request = new HttpGet(url);

        return httpClient.execute(request);
    }

    public static void executePostRequest(HttpClient httpClient, String url, Object... parameters) throws IOException {
        internalExecutePostRequest(httpClient, url, parameters);
    }

    public static void executePostRequest(
            long executionTimeoutMillis, final HttpClient httpClient,
            final String url, final Object... parameters) throws IOException {
        internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                executePostRequest(httpClient, url, parameters);
                return null;
            }
        });
    }

    public static void executePostRequest(String url, Object... parameters) throws IOException {
        executePostRequest(null, url, parameters);
    }

    public static void executePostRequest(
            long executionTimeoutMillis, final String url, final Object... parameters) throws IOException {
        internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                executePostRequest(url, parameters);
                return null;
            }
        });
    }

    @Nullable
    public static byte[] executePostRequestAndReturnResponseBytes(
            HttpClient httpClient, String url, Object... parameters) throws IOException {
        HttpResponse httpResponse = internalExecutePostRequest(httpClient, url, parameters);
        HttpEntity httpEntity = httpResponse.getEntity();
        if (httpEntity == null) {
            return null;
        }

        InputStream inputStream = httpEntity.getContent();
        return IoUtil.toByteArray(inputStream);
    }

    @Nullable
    public static byte[] executePostRequestAndReturnResponseBytes(
            long executionTimeoutMillis, final HttpClient httpClient,
            final String url, final Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return executePostRequestAndReturnResponseBytes(httpClient, url, parameters);
            }
        });
    }

    @Nullable
    public static byte[] executePostRequestAndReturnResponseBytes(String url, Object... parameters) throws IOException {
        return executePostRequestAndReturnResponseBytes(null, url, parameters);
    }

    @Nullable
    public static byte[] executePostRequestAndReturnResponseBytes(
            long executionTimeoutMillis, final String url, final Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return executePostRequestAndReturnResponseBytes(url, parameters);
            }
        });
    }

    @Nullable
    public static String executePostRequestAndReturnResponseAsString(
            HttpClient httpClient, String url, Object... parameters) throws IOException {
        HttpResponse httpResponse = internalExecutePostRequest(httpClient, url, parameters);
        HttpEntity httpEntity = httpResponse.getEntity();
        if (httpEntity == null) {
            return null;
        }

        InputStream inputStream = httpEntity.getContent();
        return httpEntity.getContentEncoding() == null
                ? IoUtil.toString(inputStream)
                : IoUtil.toString(inputStream, httpEntity.getContentEncoding().getValue());
    }

    @Nullable
    public static String executePostRequestAndReturnResponseAsString(
            long executionTimeoutMillis, final HttpClient httpClient,
            final String url, final Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<String>() {
            @Override
            public String call() throws Exception {
                return executePostRequestAndReturnResponseAsString(httpClient, url, parameters);
            }
        });
    }

    @Nullable
    public static String executePostRequestAndReturnResponseAsString(
            String url, Object... parameters) throws IOException {
        return executePostRequestAndReturnResponseAsString(null, url, parameters);
    }

    @Nullable
    public static String executePostRequestAndReturnResponseAsString(
            long executionTimeoutMillis, final String url, final Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<String>() {
            @Override
            public String call() throws Exception {
                return executePostRequestAndReturnResponseAsString(url, parameters);
            }
        });
    }

    public static Response executePostRequestAndReturnResponse(
            HttpClient httpClient, String url, Object... parameters) throws IOException {
        HttpResponse httpResponse = internalExecutePostRequest(httpClient, url, parameters);
        HttpEntity httpEntity = httpResponse.getEntity();

        if (httpEntity == null) {
            return new Response(httpResponse.getStatusLine().getStatusCode(), null, null, 0);
        } else {
            return new Response(
                    httpResponse.getStatusLine().getStatusCode(),
                    httpEntity.getContent(),
                    httpEntity.getContentEncoding() == null ? null : httpEntity.getContentEncoding().getValue(),
                    httpEntity.getContentLength()
            );
        }
    }

    public static Response executePostRequestAndReturnResponse(
            long executionTimeoutMillis, final HttpClient httpClient,
            final String url, final Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return executePostRequestAndReturnResponse(httpClient, url, parameters);
            }
        });
    }

    public static Response executePostRequestAndReturnResponse(String url, Object... parameters) throws IOException {
        return executePostRequestAndReturnResponse(null, url, parameters);
    }

    public static Response executePostRequestAndReturnResponse(
            long executionTimeoutMillis, final String url, final Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return executePostRequestAndReturnResponse(url, parameters);
            }
        });
    }

    private static HttpResponse internalExecutePostRequest(
            @Nullable HttpClient httpClient, String url, Object... parameters) throws IOException {
        parameters = validateAndPreprocessParameters(false, url, parameters);

        httpClient = httpClient == null ? newHttpClient() : httpClient;
        HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> postParameters = new ArrayList<>();

        for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex += 2) {
            postParameters.add(new BasicNameValuePair(
                    (String) parameters[parameterIndex],
                    parameters[parameterIndex + 1].toString()
            ));
        }

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters, "UTF-8");
        httpPost.setEntity(entity);

        return httpClient.execute(httpPost);
    }

    @SuppressWarnings("OverlyComplexMethod")
    private static Object[] validateAndPreprocessParameters(
            boolean encodeParameters, String url, Object... parameters) {
        if (!UrlUtil.isValidUrl(url)) {
            throw new IllegalArgumentException('\'' + url + "' is not valid URL.");
        }

        boolean secureHost;
        try {
            secureHost = CommonsPropertiesUtil.getSecureHosts().contains(new URL(url).getHost());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException('\'' + url + "' is not valid URL.", e);
        }

        int parameterCount = parameters.length;

        if (parameterCount % 2 != 0) {
            throw new IllegalArgumentException("Argument 'parameters' should contain even number of elements, " +
                    "i.e. should consist of key-value pairs."
            );
        }

        List<String> securePasswords = CommonsPropertiesUtil.getSecurePasswords();
        boolean preprocessParameters = encodeParameters || !secureHost && !securePasswords.isEmpty();

        Object[] parameterCopies = preprocessParameters ? new Object[parameterCount] : null;

        for (int parameterIndex = 0; parameterIndex < parameterCount; parameterIndex += 2) {
            Object parameterName = parameters[parameterIndex];
            Object parameterValue = parameters[parameterIndex + 1];

            if (!(parameterName instanceof String) || StringUtil.isBlank((String) parameterName)) {
                throw new IllegalArgumentException(String.format(
                        "Each parameter name should be non-blank string, but found: '%s'.", parameterName
                ));
            }

            if (parameterValue == null) {
                throw new IllegalArgumentException(String.format("Parameter '%s' is 'null'.", parameterName));
            }

            if (preprocessParameters) {
                try {
                    parameterCopies[parameterIndex] = encodeParameters
                            ? URLEncoder.encode((String) parameterName, "UTF-8")
                            : parameterName;

                    parameterCopies[parameterIndex + 1] = securePasswords.contains(parameterValue.toString())
                            ? ""
                            : parameterValue;

                    if (encodeParameters) {
                        parameterCopies[parameterIndex + 1] = URLEncoder.encode(
                                parameterCopies[parameterIndex + 1].toString(), "UTF-8"
                        );
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("UTF-8 is unsupported.", e);
                }
            }
        }

        return preprocessParameters ? parameterCopies : parameters;
    }

    private static <R> R internalExecuteLimitedTimeRequest(
            final long executionTimeoutMillis, String url, Callable<R> httpTask) throws IOException {
        final Future<R> requestFuture = timedRequestExecutor.submit(httpTask);

        try {
            return requestFuture.get(executionTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            requestFuture.cancel(true);
            throw new IOException("Unexpectedly interrupted while executing HTTP request.", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw new IOException("Can't execute HTTP request.", e);
            }
        } catch (TimeoutException e) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ThreadUtil.sleep(executionTimeoutMillis);
                    requestFuture.cancel(true);
                }
            }).start();
            throw new IOException(String.format(
                    "Can't execute HTTP request to '%s' in %d ms.", url, executionTimeoutMillis
            ), e);
        }
    }

    public static CloseableHttpClient newHttpClient() {
        return internalNewHttpClient(getBasicConnectionManagerBuilder());
    }

    public static CloseableHttpClient newHttpClient(int connectionTimeoutMillis, int socketTimeoutMillis) {
        return internalNewHttpClient(connectionTimeoutMillis, socketTimeoutMillis, getBasicConnectionManagerBuilder());
    }

    public static CloseableHttpClient newPoolingHttpClient() {
        return internalNewHttpClient(getPoolingConnectionManagerBuilder(
                CONNECTION_POOL_DEFAULT_MAX_SIZE_PER_HOST, CONNECTION_POOL_DEFAULT_MAX_SIZE
        ));
    }

    public static CloseableHttpClient newPoolingHttpClient(int connectionTimeoutMillis, int socketTimeoutMillis) {
        return newPoolingHttpClient(
                connectionTimeoutMillis, socketTimeoutMillis,
                CONNECTION_POOL_DEFAULT_MAX_SIZE_PER_HOST, CONNECTION_POOL_DEFAULT_MAX_SIZE
        );
    }

    public static CloseableHttpClient newPoolingHttpClient(
            int connectionTimeoutMillis, int socketTimeoutMillis, int maxPoolSizePerHost, int maxPoolSize) {
        return internalNewHttpClient(connectionTimeoutMillis, socketTimeoutMillis, getPoolingConnectionManagerBuilder(
                maxPoolSizePerHost, maxPoolSize
        ));
    }

    private static CloseableHttpClient internalNewHttpClient(
            HttpClientConnectionManagerBuilder connectionManagerBuilder) {
        SocketConfig socketConfig = getSocketConfig();
        RequestConfig requestConfig = getRequestConfig();
        HttpClientConnectionManager connectionManager = connectionManagerBuilder.build(socketConfig);

        return internalNewHttpClient(socketConfig, requestConfig, connectionManager);
    }

    private static CloseableHttpClient internalNewHttpClient(
            int connectionTimeoutMillis, int socketTimeoutMillis,
            HttpClientConnectionManagerBuilder connectionManagerBuilder) {
        SocketConfig socketConfig = getSocketConfig(socketTimeoutMillis);
        RequestConfig requestConfig = getRequestConfig(connectionTimeoutMillis, socketTimeoutMillis);
        HttpClientConnectionManager connectionManager = connectionManagerBuilder.build(socketConfig);

        return internalNewHttpClient(socketConfig, requestConfig, connectionManager);
    }

    private static CloseableHttpClient internalNewHttpClient(
            SocketConfig socketConfig, RequestConfig requestConfig, HttpClientConnectionManager connectionManager) {
        return HttpClientBuilder.create()
                .setDefaultConnectionConfig(HttpClientImmutableFieldHolder.CONNECTION_CONFIG)
                .setDefaultSocketConfig(socketConfig)
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .setRequestExecutor(HttpClientImmutableFieldHolder.HTTP_REQUEST_EXECUTOR)
                .setProxy(HttpClientImmutableFieldHolder.HTTP_PROXY)
                .build();
    }

    private static HttpClientConnectionManagerBuilder getBasicConnectionManagerBuilder() {
        return new HttpClientConnectionManagerBuilder() {
            @Override
            public HttpClientConnectionManager build(SocketConfig socketConfig) {
                BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager();
                connectionManager.setConnectionConfig(HttpClientImmutableFieldHolder.CONNECTION_CONFIG);
                connectionManager.setSocketConfig(socketConfig);
                return connectionManager;
            }
        };
    }

    private static HttpClientConnectionManagerBuilder getPoolingConnectionManagerBuilder(
            final int maxPoolSizePerHost, final int maxPoolSize) {
        return new HttpClientConnectionManagerBuilder() {
            @Override
            public HttpClientConnectionManager build(SocketConfig socketConfig) {
                PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                        1L, TimeUnit.HOURS
                );
                connectionManager.setDefaultMaxPerRoute(maxPoolSizePerHost);
                connectionManager.setMaxTotal(maxPoolSize);
                connectionManager.setDefaultConnectionConfig(HttpClientImmutableFieldHolder.CONNECTION_CONFIG);
                connectionManager.setDefaultSocketConfig(socketConfig);
                return connectionManager;
            }
        };
    }

    private static SocketConfig getSocketConfig() {
        return SocketConfig.DEFAULT;
    }

    private static SocketConfig getSocketConfig(int socketTimeoutMillis) {
        return SocketConfig.copy(SocketConfig.DEFAULT)
                .setSoTimeout(socketTimeoutMillis)
                .build();
    }

    private static RequestConfig getRequestConfig() {
        return RequestConfig.copy(RequestConfig.DEFAULT)
                .setProxy(HttpClientImmutableFieldHolder.HTTP_PROXY)
                .build();
    }

    private static RequestConfig getRequestConfig(int connectionTimeoutMillis, int socketTimeoutMillis) {
        return RequestConfig.copy(RequestConfig.DEFAULT)
                .setConnectTimeout(connectionTimeoutMillis)
                .setConnectionRequestTimeout(connectionTimeoutMillis)
                .setSocketTimeout(socketTimeoutMillis)
                .setProxy(HttpClientImmutableFieldHolder.HTTP_PROXY)
                .build();
    }

    public static void closeQuietly(
            @Nullable CloseableHttpClient httpClient, @Nullable HttpPost request, @Nullable HttpResponse response) {
        closeQuietly(response);
        closeQuietly(request);
        closeQuietly(httpClient);
    }

    /**
     * @deprecated Use {@link #closeQuietly(CloseableHttpClient, HttpPost, HttpResponse)}.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public static void closeQuietly(
            @Nullable HttpClient httpClient, @Nullable HttpPost request, @Nullable HttpResponse response) {
        closeQuietly(response);
        closeQuietly(request);
        closeQuietly(httpClient);
    }

    public static void closeQuietly(@Nullable CloseableHttpClient httpClient) {
        IoUtil.closeQuietly(httpClient);
    }

    /**
     * @deprecated Use {@link #closeQuietly(CloseableHttpClient)}.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public static void closeQuietly(@Nullable HttpClient httpClient) {
        if (httpClient instanceof Closeable) {
            IoUtil.closeQuietly((Closeable) httpClient);
        } else if (httpClient != null) {
            httpClient.getConnectionManager().shutdown();
        }
    }

    public static void closeQuietly(@Nullable HttpPost request) {
        if (request != null) {
            request.abort();
        }
    }

    public static void closeQuietly(@Nullable HttpResponse response) {
        if (response != null) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try {
                    InputStream content = entity.getContent();
                    if (content != null) {
                        content.close();
                    }
                } catch (IllegalStateException | IOException ignored) {
                    // No operations.
                }
            }
        }
    }

    public static final class Response {
        private final int responseCode;
        @Nullable
        private final InputStream inputStream;
        @Nullable
        private final String charsetName;
        private final long contentLength;

        private Response(
                int responseCode, @Nullable InputStream inputStream, @Nullable String charsetName, long contentLength) {
            this.responseCode = responseCode;
            this.inputStream = inputStream;
            this.charsetName = charsetName;
            this.contentLength = contentLength;
        }

        public int getResponseCode() {
            return responseCode;
        }

        @Nullable
        public InputStream getInputStream() {
            return inputStream;
        }

        @Nullable
        public String getCharsetName() {
            return charsetName;
        }

        public long getContentLength() {
            return contentLength;
        }
    }

    public static final class Code {
        /*
        * Server status codes; see RFC 2068.
        */
        /**
         * Status code (100) indicating the client can continue.
         */
        public static final int CONTINUE = 100;
        /**
         * Status code (101) indicating the server is switching protocols
         * according to Upgrade header.
         */
        public static final int SWITCHING_PROTOCOLS = 101;
        /**
         * Status code (200) indicating the request succeeded normally.
         */
        public static final int OK = 200;
        /**
         * Status code (201) indicating the request succeeded and created
         * a new resource on the server.
         */
        public static final int CREATED = 201;
        /**
         * Status code (202) indicating that a request was accepted for
         * processing, but was not completed.
         */
        public static final int ACCEPTED = 202;
        /**
         * Status code (203) indicating that the meta information presented
         * by the client did not originate from the server.
         */
        public static final int NON_AUTHORITATIVE_INFORMATION = 203;
        /**
         * Status code (204) indicating that the request succeeded but that
         * there was no new information to return.
         */
        public static final int NO_CONTENT = 204;
        /**
         * Status code (205) indicating that the agent <em>SHOULD</em> reset
         * the document view which caused the request to be sent.
         */
        public static final int RESET_CONTENT = 205;
        /**
         * Status code (206) indicating that the server has fulfilled
         * the partial GET request for the resource.
         */
        public static final int PARTIAL_CONTENT = 206;
        /**
         * Status code (300) indicating that the requested resource
         * corresponds to any one of a set of representations, each with
         * its own specific location.
         */
        public static final int MULTIPLE_CHOICES = 300;
        /**
         * Status code (301) indicating that the resource has permanently
         * moved to a new location, and that future references should use a
         * new URI with their requests.
         */
        public static final int MOVED_PERMANENTLY = 301;
        /**
         * Status code (302) indicating that the resource has temporarily
         * moved to another location, but that future references should
         * still use the original URI to access the resource.
         * <p/>
         * This definition is being retained for backwards compatibility.
         * FOUND is now the preferred definition.
         */
        public static final int MOVED_TEMPORARILY = 302;
        /**
         * Status code (302) indicating that the resource reside
         * temporarily under a different URI. Since the redirection might
         * be altered on occasion, the client should continue to use the
         * Request-URI for future requests.(HTTP/1.1) To represent the
         * status code (302), it is recommended to use this variable.
         */
        public static final int FOUND = 302;
        /**
         * Status code (303) indicating that the response to the request
         * can be found under a different URI.
         */
        public static final int SEE_OTHER = 303;
        /**
         * Status code (304) indicating that a conditional GET operation
         * found that the resource was available and not modified.
         */
        public static final int NOT_MODIFIED = 304;
        /**
         * Status code (305) indicating that the requested resource
         * <em>MUST</em> be accessed through the proxy given by the
         * {@code <em>Location</em>} field.
         */
        public static final int USE_PROXY = 305;
        /**
         * Status code (307) indicating that the requested resource
         * resides temporarily under a different URI. The temporary URI
         * <em>SHOULD</em> be given by the {@code <em>Location</em>}
         * field in the response.
         */
        public static final int TEMPORARY_REDIRECT = 307;
        /**
         * Status code (400) indicating the request sent by the client was
         * syntactically incorrect.
         */
        public static final int BAD_REQUEST = 400;
        /**
         * Status code (401) indicating that the request requires HTTP
         * authentication.
         */
        public static final int UNAUTHORIZED = 401;
        /**
         * Status code (402) reserved for future use.
         */
        public static final int PAYMENT_REQUIRED = 402;
        /**
         * Status code (403) indicating the server understood the request
         * but refused to fulfill it.
         */
        public static final int FORBIDDEN = 403;
        /**
         * Status code (404) indicating that the requested resource is not
         * available.
         */
        public static final int NOT_FOUND = 404;
        /**
         * Status code (405) indicating that the method specified in the
         * {@code <em>Request-Line</em>} is not allowed for the resource
         * identified by the {@code <em>Request-URI</em>}.
         */
        public static final int METHOD_NOT_ALLOWED = 405;
        /**
         * Status code (406) indicating that the resource identified by the
         * request is only capable of generating response entities which have
         * content characteristics not acceptable according to the accept
         * headers sent in the request.
         */
        public static final int NOT_ACCEPTABLE = 406;
        /**
         * Status code (407) indicating that the client <em>MUST</em> first
         * authenticate itself with the proxy.
         */
        public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
        /**
         * Status code (408) indicating that the client did not produce a
         * request within the time that the server was prepared to wait.
         */
        public static final int REQUEST_TIMEOUT = 408;
        /**
         * Status code (409) indicating that the request could not be
         * completed due to a conflict with the current state of the
         * resource.
         */
        public static final int CONFLICT = 409;
        /**
         * Status code (410) indicating that the resource is no longer
         * available at the server and no forwarding address is known.
         * This condition <em>SHOULD</em> be considered permanent.
         */
        public static final int GONE = 410;
        /**
         * Status code (411) indicating that the request cannot be handled
         * without a defined {@code <em>Content-Length</em>}.
         */
        public static final int LENGTH_REQUIRED = 411;
        /**
         * Status code (412) indicating that the precondition given in one
         * or more of the request-header fields evaluated to false when it
         * was tested on the server.
         */
        public static final int PRECONDITION_FAILED = 412;
        /**
         * Status code (413) indicating that the server is refusing to process
         * the request because the request entity is larger than the server is
         * willing or able to process.
         */
        public static final int REQUEST_ENTITY_TOO_LARGE = 413;
        /**
         * Status code (414) indicating that the server is refusing to service
         * the request because the {@code <em>Request-URI</em>} is longer
         * than the server is willing to interpret.
         */
        public static final int REQUEST_URI_TOO_LONG = 414;
        /**
         * Status code (415) indicating that the server is refusing to service
         * the request because the entity of the request is in a format not
         * supported by the requested resource for the requested method.
         */
        public static final int UNSUPPORTED_MEDIA_TYPE = 415;
        /**
         * Status code (416) indicating that the server cannot serve the
         * requested byte range.
         */
        public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
        /**
         * Status code (417) indicating that the server could not meet the
         * expectation given in the Expect request header.
         */
        public static final int EXPECTATION_FAILED = 417;
        /**
         * Status code (429) indicating that the user has sent too many
         * requests in a given amount of time ("rate limiting").
         */
        public static final int TOO_MANY_REQUESTS = 429;
        /**
         * Status code (500) indicating an error inside the HTTP server
         * which prevented it from fulfilling the request.
         */
        public static final int INTERNAL_SERVER_ERROR = 500;
        /**
         * Status code (501) indicating the HTTP server does not support
         * the functionality needed to fulfill the request.
         */
        public static final int NOT_IMPLEMENTED = 501;
        /**
         * Status code (502) indicating that the HTTP server received an
         * invalid response from a server it consulted when acting as a
         * proxy or gateway.
         */
        public static final int BAD_GATEWAY = 502;
        /**
         * Status code (503) indicating that the HTTP server is
         * temporarily overloaded, and unable to handle the request.
         */
        public static final int SERVICE_UNAVAILABLE = 503;
        /**
         * Status code (504) indicating that the server did not receive
         * a timely response from the upstream server while acting as
         * a gateway or proxy.
         */
        public static final int GATEWAY_TIMEOUT = 504;
        /**
         * Status code (505) indicating that the server does not support
         * or refuses to support the HTTP protocol version that was used
         * in the request message.
         */
        public static final int HTTP_VERSION_NOT_SUPPORTED = 505;

        private Code() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class HttpClientImmutableFieldHolder {
        private static final ConnectionConfig CONNECTION_CONFIG = ConnectionConfig.copy(ConnectionConfig.DEFAULT)
                .setBufferSize(IoUtil.BUFFER_SIZE)
                .build();

        private static final HttpRequestExecutor HTTP_REQUEST_EXECUTOR = getHttpRequestExecutor();

        @Nullable
        private static final HttpHost HTTP_PROXY = getHttpProxy();

        private static HttpRequestExecutor getHttpRequestExecutor() {
            return new HttpRequestExecutor() {
                @Override
                public HttpResponse execute(HttpRequest request, HttpClientConnection conn, HttpContext context)
                        throws IOException, HttpException {
                    try {
                        return super.execute(request, conn, context);
                    } catch (IOException e) {
                        throw new IOException("Can't execute " + request + '.', e);
                    }
                }
            };
        }

        @SuppressWarnings("AccessOfSystemProperties")
        @Nullable
        private static HttpHost getHttpProxy() {
            if (!Boolean.parseBoolean(System.getProperty("proxySet"))) {
                return null;
            }

            String proxyHost = System.getProperty("http.proxyHost");
            if (StringUtil.isBlank(proxyHost)) {
                return null;
            }

            int proxyPort;
            try {
                proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
                if (proxyPort <= 0 || proxyPort > 65535) {
                    return null;
                }
            } catch (NumberFormatException ignored) {
                return null;
            }

            return new HttpHost(proxyHost, proxyPort);
        }
    }

    private interface HttpClientConnectionManagerBuilder {
        HttpClientConnectionManager build(SocketConfig socketConfig);
    }
}
