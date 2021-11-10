package com.codeforces.commons.io.http;

import com.codeforces.commons.io.IoUtil;
import com.codeforces.commons.process.ThreadUtil;
import com.codeforces.commons.properties.internal.CommonsPropertiesUtil;
import com.codeforces.commons.text.StringUtil;
import com.codeforces.commons.text.UrlUtil;
import org.apache.http.*;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
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

import javax.annotation.Nonnull;
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
@SuppressWarnings({"OverloadedVarargsMethod", "deprecation"})
@Deprecated
public class HttpClientUtil {
    private static final int CONNECTION_POOL_DEFAULT_MAX_SIZE = 50;
    private static final int CONNECTION_POOL_DEFAULT_MAX_SIZE_PER_HOST = 25;

    private static final ExecutorService timedRequestExecutor = new ThreadPoolExecutor(
            0, Short.MAX_VALUE, 5L, TimeUnit.MINUTES, new LinkedBlockingQueue<>(),
            ThreadUtil.getCustomPoolThreadFactory(new ThreadUtil.ThreadCustomizer() {
                private final AtomicLong threadIndex = new AtomicLong();

                @Override
                public void customize(Thread thread) {
                    thread.setDaemon(true);
                    thread.setName(String.format(
                            "%s#RequestExecutionThread-%d",
                            HttpClientUtil.class.getSimpleName(), threadIndex.incrementAndGet()
                    ));
                }
            })
    );

    private HttpClientUtil() {
        throw new UnsupportedOperationException();
    }

    public static void executeGetRequest(
            @Nullable HttpClient httpClient, boolean encodeParameters, String url, Object... parameters
    ) throws IOException {
        internalExecuteGetRequest(httpClient, encodeParameters, url, parameters);
    }

    @SuppressWarnings("ConstantConditions")
    public static void executeGetRequest(
            long executionTimeoutMillis, HttpClient httpClient, boolean encodeParameters,
            String url, Object... parameters) throws IOException {
        internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, () -> {
            executeGetRequest(httpClient, encodeParameters, url, parameters);
            return null;
        });
    }

    public static void executeGetRequest(
            boolean encodeParameters, String url, Object... parameters) throws IOException {
        executeGetRequest(null, encodeParameters, url, parameters);
    }

    @SuppressWarnings("ConstantConditions")
    public static void executeGetRequest(
            long executionTimeoutMillis, boolean encodeParameters,
            String url, Object... parameters) throws IOException {
        internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, () -> {
            executeGetRequest(encodeParameters, url, parameters);
            return null;
        });
    }

    public static void executeGetRequest(String url, Object... parameters) throws IOException {
        executeGetRequest(false, url, parameters);
    }

    @SuppressWarnings("ConstantConditions")
    public static void executeGetRequest(
            long executionTimeoutMillis, String url, Object... parameters) throws IOException {
        internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, () -> {
            executeGetRequest(url, parameters);
            return null;
        });
    }

    public static byte[] executeGetRequestAndReturnResponseBytes(
            @Nullable HttpClient httpClient, boolean encodeParameters, String url, Object... parameters
    ) throws IOException {
        HttpResponse httpResponse = internalExecuteGetRequest(httpClient, encodeParameters, url, parameters);
        InputStream inputStream = httpResponse.getEntity().getContent();

        return IoUtil.toByteArray(inputStream);
    }

    public static byte[] executeGetRequestAndReturnResponseBytes(
            long executionTimeoutMillis, HttpClient httpClient, boolean encodeParameters,
            String url, Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(
                executionTimeoutMillis, url,
                () -> executeGetRequestAndReturnResponseBytes(httpClient, encodeParameters, url, parameters)
        );
    }

    public static byte[] executeGetRequestAndReturnResponseBytes(
            boolean encodeParameters, String url, Object... parameters) throws IOException {
        return executeGetRequestAndReturnResponseBytes(null, encodeParameters, url, parameters);
    }

    public static byte[] executeGetRequestAndReturnResponseBytes(
            long executionTimeoutMillis, boolean encodeParameters,
            String url, Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(
                executionTimeoutMillis, url,
                () -> executeGetRequestAndReturnResponseBytes(encodeParameters, url, parameters)
        );
    }

    public static byte[] executeGetRequestAndReturnResponseBytes(String url, Object... parameters) throws IOException {
        return executeGetRequestAndReturnResponseBytes(false, url, parameters);
    }

    public static byte[] executeGetRequestAndReturnResponseBytes(
            long executionTimeoutMillis, String url, Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(
                executionTimeoutMillis, url,
                () -> executeGetRequestAndReturnResponseBytes(url, parameters)
        );
    }

    public static String executeGetRequestAndReturnResponseAsString(
            @Nullable HttpClient httpClient, boolean encodeParameters, String url, Object... parameters
    ) throws IOException {
        HttpResponse httpResponse = internalExecuteGetRequest(httpClient, encodeParameters, url, parameters);
        HttpEntity httpEntity = httpResponse.getEntity();
        InputStream inputStream = httpEntity.getContent();

        return httpEntity.getContentEncoding() == null
                ? IoUtil.toString(inputStream)
                : IoUtil.toString(inputStream, httpEntity.getContentEncoding().getValue());
    }

    public static String executeGetRequestAndReturnResponseAsString(
            long executionTimeoutMillis, HttpClient httpClient, boolean encodeParameters,
            String url, Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(
                executionTimeoutMillis, url,
                () -> executeGetRequestAndReturnResponseAsString(httpClient, encodeParameters, url, parameters)
        );
    }

    public static String executeGetRequestAndReturnResponseAsString(
            boolean encodeParameters, String url, Object... parameters) throws IOException {
        return executeGetRequestAndReturnResponseAsString(null, encodeParameters, url, parameters);
    }

    public static String executeGetRequestAndReturnResponseAsString(
            long executionTimeoutMillis, boolean encodeParameters,
            String url, Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(
                executionTimeoutMillis, url,
                () -> executeGetRequestAndReturnResponseAsString(encodeParameters, url, parameters)
        );
    }

    public static String executeGetRequestAndReturnResponseAsString(
            String url, Object... parameters) throws IOException {
        return executeGetRequestAndReturnResponseAsString(false, url, parameters);
    }

    public static String executeGetRequestAndReturnResponseAsString(
            long executionTimeoutMillis, String url, Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(
                executionTimeoutMillis, url, () -> executeGetRequestAndReturnResponseAsString(url, parameters)
        );
    }

    @Nonnull
    public static Response executeGetRequestAndReturnResponse(
            @Nullable HttpClient httpClient, boolean encodeParameters, String url, Object... parameters
    ) throws IOException {
        HttpResponse httpResponse = internalExecuteGetRequest(httpClient, encodeParameters, url, parameters);
        HttpEntity httpEntity = httpResponse.getEntity();

        return new Response(
                httpResponse.getStatusLine().getStatusCode(),
                httpEntity.getContent(),
                httpEntity.getContentEncoding() == null ? null : httpEntity.getContentEncoding().getValue(),
                httpEntity.getContentLength(),
                httpResponse
        );
    }

    public static Response executeGetRequestAndReturnResponse(
            long executionTimeoutMillis, HttpClient httpClient, boolean encodeParameters,
            String url, Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(
                executionTimeoutMillis, url,
                () -> executeGetRequestAndReturnResponse(httpClient, encodeParameters, url, parameters)
        );
    }

    @Nonnull
    public static Response executeGetRequestAndReturnResponse(
            boolean encodeParameters, String url, Object... parameters) throws IOException {
        return executeGetRequestAndReturnResponse(null, encodeParameters, url, parameters);
    }

    public static Response executeGetRequestAndReturnResponse(
            long executionTimeoutMillis, boolean encodeParameters,
            String url, Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(
                executionTimeoutMillis, url, () -> executeGetRequestAndReturnResponse(encodeParameters, url, parameters)
        );
    }

    @Nonnull
    public static Response executeGetRequestAndReturnResponse(String url, Object... parameters) throws IOException {
        return executeGetRequestAndReturnResponse(false, url, parameters);
    }

    @Nonnull
    public static Response executeGetRequestAndReturnResponse(
            long executionTimeoutMillis, String url, Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(
                executionTimeoutMillis, url, () -> executeGetRequestAndReturnResponse(url, parameters)
        );
    }

    private static HttpResponse internalExecuteGetRequest(
            @Nullable HttpClient httpClient, boolean encodeParameters, String url, Object... parameters
    ) throws IOException {
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

    public static void executePostRequest(
            @Nullable HttpClient httpClient, String url, Object... parameters) throws IOException {
        internalExecutePostRequest(httpClient, url, parameters);
    }

    @SuppressWarnings("ConstantConditions")
    public static void executePostRequest(
            long executionTimeoutMillis, HttpClient httpClient,
            String url, Object... parameters) throws IOException {
        internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, () -> {
            executePostRequest(httpClient, url, parameters);
            return null;
        });
    }

    public static void executePostRequest(String url, Object... parameters) throws IOException {
        executePostRequest(null, url, parameters);
    }

    @SuppressWarnings("ConstantConditions")
    public static void executePostRequest(
            long executionTimeoutMillis, String url, Object... parameters) throws IOException {
        internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, () -> {
            executePostRequest(url, parameters);
            return null;
        });
    }

    @Nullable
    public static byte[] executePostRequestAndReturnResponseBytes(
            @Nullable HttpClient httpClient, String url, Object... parameters) throws IOException {
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
            long executionTimeoutMillis, HttpClient httpClient,
            String url, Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<byte[]>() {
            @Nullable
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
            long executionTimeoutMillis, String url, Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<byte[]>() {
            @Nullable
            @Override
            public byte[] call() throws Exception {
                return executePostRequestAndReturnResponseBytes(url, parameters);
            }
        });
    }

    @Nullable
    public static String executePostRequestAndReturnResponseAsString(
            @Nullable HttpClient httpClient, String url, Object... parameters) throws IOException {
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
            long executionTimeoutMillis, HttpClient httpClient,
            String url, Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<String>() {
            @Nullable
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
            long executionTimeoutMillis, String url, Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(executionTimeoutMillis, url, new Callable<String>() {
            @Nullable
            @Override
            public String call() throws Exception {
                return executePostRequestAndReturnResponseAsString(url, parameters);
            }
        });
    }

    public static Response executePostRequestAndReturnResponse(
            @Nullable HttpClient httpClient, String url, Object... parameters) throws IOException {
        HttpResponse httpResponse = internalExecutePostRequest(httpClient, url, parameters);
        HttpEntity httpEntity = httpResponse.getEntity();

        if (httpEntity == null) {
            return new Response(httpResponse.getStatusLine().getStatusCode(), null, null, 0, httpResponse);
        } else {
            return new Response(
                    httpResponse.getStatusLine().getStatusCode(),
                    httpEntity.getContent(),
                    httpEntity.getContentEncoding() == null ? null : httpEntity.getContentEncoding().getValue(),
                    httpEntity.getContentLength(),
                    httpResponse
            );
        }
    }

    public static Response executePostRequestAndReturnResponse(
            long executionTimeoutMillis, HttpClient httpClient,
            String url, Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(
                executionTimeoutMillis, url, () -> executePostRequestAndReturnResponse(httpClient, url, parameters)
        );
    }

    public static Response executePostRequestAndReturnResponse(String url, Object... parameters) throws IOException {
        return executePostRequestAndReturnResponse(null, url, parameters);
    }

    public static Response executePostRequestAndReturnResponse(
            long executionTimeoutMillis, String url, Object... parameters) throws IOException {
        return internalExecuteLimitedTimeRequest(
                executionTimeoutMillis, url, () -> executePostRequestAndReturnResponse(url, parameters)
        );
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
            long executionTimeoutMillis, String url, Callable<R> httpTask) throws IOException {
        Future<R> requestFuture = timedRequestExecutor.submit(httpTask);

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
            new Thread(() -> {
                ThreadUtil.sleep(executionTimeoutMillis);
                requestFuture.cancel(true);
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

    @Nonnull
    private static HttpClientConnectionManagerBuilder getBasicConnectionManagerBuilder() {
        return socketConfig -> {
            BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager();
            connectionManager.setConnectionConfig(HttpClientImmutableFieldHolder.CONNECTION_CONFIG);
            connectionManager.setSocketConfig(socketConfig);
            return connectionManager;
        };
    }

    @Nonnull
    private static HttpClientConnectionManagerBuilder getPoolingConnectionManagerBuilder(
            int maxPoolSizePerHost, int maxPoolSize) {
        return socketConfig -> {
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                    1L, TimeUnit.HOURS
            );
            connectionManager.setDefaultMaxPerRoute(maxPoolSizePerHost);
            connectionManager.setMaxTotal(maxPoolSize);
            connectionManager.setDefaultConnectionConfig(HttpClientImmutableFieldHolder.CONNECTION_CONFIG);
            connectionManager.setDefaultSocketConfig(socketConfig);
            return connectionManager;
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
     * @param httpClient http client to close
     * @param request    http request to close
     * @param response   http response to close
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
     * @param httpClient http client to close
     * @deprecated Use {@link #closeQuietly(CloseableHttpClient)}.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public static void closeQuietly(@Nullable HttpClient httpClient) {
        if (httpClient instanceof Closeable) {
            IoUtil.closeQuietly((AutoCloseable) httpClient);
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

            if (response instanceof Closeable) {
                IoUtil.closeQuietly((AutoCloseable) response);
            }
        }
    }

    public static final class Response implements Closeable {
        private final int responseCode;
        @Nullable
        private final InputStream inputStream;
        @Nullable
        private final String charsetName;
        private final long contentLength;

        private final HttpResponse internalHttpResponse;

        private Response(
                int responseCode, @Nullable InputStream inputStream, @Nullable String charsetName, long contentLength,
                @Nonnull HttpResponse internalHttpResponse) {
            this.responseCode = responseCode;
            this.inputStream = inputStream;
            this.charsetName = charsetName;
            this.contentLength = contentLength;
            this.internalHttpResponse = internalHttpResponse;
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

        @Override
        public void close() {
            closeQuietly(internalHttpResponse);
        }
    }

    private static final class HttpClientImmutableFieldHolder {
        private static final ConnectionConfig CONNECTION_CONFIG = ConnectionConfig.copy(ConnectionConfig.DEFAULT)
                .setBufferSize(IoUtil.BUFFER_SIZE)
                .build();

        private static final HttpRequestExecutor HTTP_REQUEST_EXECUTOR = getHttpRequestExecutor();

        @Nullable
        private static final HttpHost HTTP_PROXY = getHttpProxy();

        @Nonnull
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
