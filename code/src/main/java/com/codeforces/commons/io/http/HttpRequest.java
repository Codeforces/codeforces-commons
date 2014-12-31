package com.codeforces.commons.io.http;

import com.codeforces.commons.io.CountingInputStream;
import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.io.IoUtil;
import com.codeforces.commons.io.MimeUtil;
import com.codeforces.commons.math.NumberUtil;
import com.codeforces.commons.properties.internal.CommonsPropertiesUtil;
import com.codeforces.commons.text.StringUtil;
import com.codeforces.commons.text.UrlUtil;
import com.codeforces.commons.time.TimeUtil;
import com.google.common.base.Preconditions;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 27.11.14
 */
@NotThreadSafe
public final class HttpRequest {
    private static final Logger logger = Logger.getLogger(HttpRequest.class);

    private final String url;

    private final Map<String, List<String>> parametersByName = new LinkedHashMap<>(8);
    @Nullable
    private byte[] binaryEntity;
    private boolean gzip;

    private final Map<String, List<String>> headersByName = new LinkedHashMap<>(8);
    private HttpMethod method = HttpMethod.GET;
    private int timeoutMillis = NumberUtil.toInt(10L * TimeUtil.MILLIS_PER_MINUTE);

    public static HttpRequest create(String url, Object... parameters) {
        return new HttpRequest(url, parameters);
    }

    private HttpRequest(String url, Object... parameters) {
        this.url = url;
        appendParameters(parameters);
    }

    public String getUrl() {
        return url;
    }

    public Map<String, List<String>> getParametersByNameMap() {
        return getDeepUnmodifiableMap(parametersByName);
    }

    public List<String> getParameters(String parameterName) {
        List<String> parameters = parametersByName.get(parameterName);
        return parameters == null ? Collections.<String>emptyList() : Collections.unmodifiableList(parameters);
    }

    @Nullable
    public String getParameter(String parameterName) {
        return getParameter(parameterName, false);
    }

    @Nullable
    public String getParameter(String parameterName, boolean throwIfMany) {
        List<String> parameters = getParameters(parameterName);
        int parameterCount = parameters.size();

        if (parameterCount == 0) {
            return null;
        }

        if (parameterCount > 1 && throwIfMany) {
            throw new IllegalStateException(String.format(
                    "Expected only one parameter with name '%s' but %d has been found.", parameterName, parameterCount
            ));
        }

        return parameters.get(0);
    }

    public HttpRequest appendParameters(Object... parameters) {
        if (hasBinaryEntity()) {
            throw new IllegalStateException("Can't send parameters and binary entity with a single request.");
        }

        String[] encodedParameters = validateAndEncodeParameters(url, parameters);
        int parameterCount = encodedParameters.length;

        for (int parameterIndex = 0; parameterIndex < parameterCount; parameterIndex += 2) {
            String parameterName = encodedParameters[parameterIndex];
            String parameterValue = encodedParameters[parameterIndex + 1];

            List<String> parametersForName = parametersByName.get(parameterName);
            if (parametersForName == null) {
                parametersForName = new ArrayList<>(1);
                parametersByName.put(parameterName, parametersForName);
            }
            parametersForName.add(parameterValue);
        }

        return this;
    }

    public HttpRequest appendParameter(@Nonnull String parameterName, @Nonnull Object parameterValue) {
        return appendParameters(parameterName, parameterValue);
    }

    public HttpRequest prependParameters(Object... parameters) {
        if (hasBinaryEntity()) {
            throw new IllegalStateException("Can't send parameters and binary entity with a single request.");
        }

        String[] encodedParameters = validateAndEncodeParameters(url, parameters);
        int parameterCount = encodedParameters.length;

        for (int parameterIndex = parameterCount - 2; parameterIndex >= 0; parameterIndex -= 2) {
            String parameterName = encodedParameters[parameterIndex];
            String parameterValue = encodedParameters[parameterIndex + 1];

            List<String> parametersForName = parametersByName.get(parameterName);
            if (parametersForName == null) {
                parametersForName = new ArrayList<>(1);
                parametersByName.put(parameterName, parametersForName);
            }
            parametersForName.add(0, parameterValue);
        }

        return this;
    }

    public HttpRequest prependParameter(@Nonnull String parameterName, @Nonnull Object parameterValue) {
        return prependParameters(parameterName, parameterValue);
    }

    public HttpRequest removeParameters(String parameterName) {
        parametersByName.remove(parameterName);
        return this;
    }

    public HttpRequest removeParameter(String parameterName, int index) {
        List<String> parameters = parametersByName.get(parameterName);
        parameters.remove(index);
        if (parameters.isEmpty()) {
            parametersByName.remove(parameterName);
        }
        return this;
    }

    public HttpRequest removeFirstParameter(String parameterName) {
        List<String> parameters = parametersByName.get(parameterName);
        parameters.remove(0);
        if (parameters.isEmpty()) {
            parametersByName.remove(parameterName);
        }
        return this;
    }

    public HttpRequest removeLastParameter(String parameterName) {
        List<String> parameters = parametersByName.get(parameterName);
        parameters.remove(parameters.size() - 1);
        if (parameters.isEmpty()) {
            parametersByName.remove(parameterName);
        }
        return this;
    }

    public HttpRequest removeAllParameters() {
        parametersByName.clear();
        return this;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    @Nullable
    public byte[] getBinaryEntity() {
        return binaryEntity;
    }

    /**
     * Sets binary entity. Ignored if {@code {@link #method method}} is not {@code {@link HttpMethod#POST POST}}.
     *
     * @param binaryEntity binary entity to send as POST data
     * @return this HTTP request
     * @throws IllegalStateException if {@code {@link #parametersByName parametersByName}} is not empty
     */
    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public HttpRequest setBinaryEntity(@Nullable byte[] binaryEntity) {
        if (!parametersByName.isEmpty()) {
            throw new IllegalStateException("Can't send parameters and binary entity with a single request.");
        }
        this.binaryEntity = binaryEntity;
        return this;
    }

    @SuppressWarnings("InstanceVariableUsedBeforeInitialized")
    public boolean hasBinaryEntity() {
        return binaryEntity != null;
    }

    public HttpRequest removeBinaryEntity() {
        this.binaryEntity = null;
        return this;
    }

    public boolean isGzip() {
        return gzip;
    }

    /**
     * Sets whether POST data of request should be GZIP-compressed or not.
     * It is absolutely normal to compress binary entity,
     * but many HTTP servers in default configuration do not support compression of parameters.
     * So use it with caution. Google for 'compressableMimeType' for more information.
     *
     * @param gzip compression flag value
     * @return this HTTP request
     */
    public HttpRequest setGzip(boolean gzip) {
        this.gzip = gzip;
        return this;
    }

    public Map<String, List<String>> getHeadersByNameMap() {
        return getDeepUnmodifiableMap(headersByName);
    }

    public List<String> getHeaders(String headerName) {
        List<String> headers = headersByName.get(headerName);
        return headers == null ? Collections.<String>emptyList() : Collections.unmodifiableList(headers);
    }

    @Nullable
    public String getHeader(String headerName) {
        return getHeader(headerName, false);
    }

    @Nullable
    public String getHeader(String headerName, boolean throwIfMany) {
        List<String> headers = getHeaders(headerName);
        int headerCount = headers.size();

        if (headerCount == 0) {
            return null;
        }

        if (headerCount > 1 && throwIfMany) {
            throw new IllegalStateException(String.format(
                    "Expected only one header with name '%s' but %d has been found.", headerName, headerCount
            ));
        }

        return headers.get(0);
    }

    public HttpRequest appendHeaders(String... headers) {
        validateHeaders(headers);
        int headerCount = headers.length;

        for (int headerIndex = 0; headerIndex < headerCount; headerIndex += 2) {
            String headerName = headers[headerIndex];
            String headerValue = headers[headerIndex + 1];

            List<String> headersForName = headersByName.get(headerName);
            if (headersForName == null) {
                headersForName = new ArrayList<>(1);
                headersByName.put(headerName, headersForName);
            }
            headersForName.add(headerValue);
        }

        return this;
    }

    public HttpRequest appendHeader(@Nonnull String headerName, @Nonnull String headerValue) {
        return appendHeaders(headerName, headerValue);
    }

    public HttpRequest prependHeaders(String... headers) {
        validateHeaders(headers);
        int headerCount = headers.length;

        for (int headerIndex = headerCount - 2; headerIndex >= 0; headerIndex -= 2) {
            String headerName = headers[headerIndex];
            String headerValue = headers[headerIndex + 1];

            List<String> headersForName = headersByName.get(headerName);
            if (headersForName == null) {
                headersForName = new ArrayList<>(1);
                headersByName.put(headerName, headersForName);
            }
            headersForName.add(0, headerValue);
        }

        return this;
    }

    public HttpRequest prependHeader(@Nonnull String headerName, @Nonnull String headerValue) {
        return prependHeaders(headerName, headerValue);
    }

    public HttpRequest removeHeaders(String headerName) {
        headersByName.remove(headerName);
        return this;
    }

    public HttpRequest removeHeader(String headerName, int index) {
        List<String> headers = headersByName.get(headerName);
        headers.remove(index);
        if (headers.isEmpty()) {
            headersByName.remove(headerName);
        }
        return this;
    }

    public HttpRequest removeFirstHeader(String headerName) {
        List<String> headers = headersByName.get(headerName);
        headers.remove(0);
        if (headers.isEmpty()) {
            headersByName.remove(headerName);
        }
        return this;
    }

    public HttpRequest removeLastHeader(String headerName) {
        List<String> headers = headersByName.get(headerName);
        headers.remove(headers.size() - 1);
        if (headers.isEmpty()) {
            headersByName.remove(headerName);
        }
        return this;
    }

    public HttpRequest removeAllHeaders() {
        headersByName.clear();
        return this;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public HttpRequest setMethod(HttpMethod method) {
        Preconditions.checkNotNull(method, "Argument 'method' is null.");
        this.method = method;
        return this;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    public HttpRequest setTimeoutMillis(int timeoutMillis) {
        Preconditions.checkArgument(timeoutMillis > 0, "Argument 'timeoutMillis' is zero or negative.");
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public HttpRequest setTimeoutMillis(long timeoutMillis) {
        return setTimeoutMillis(NumberUtil.toInt(timeoutMillis));
    }

    public HttpRequest setTimeout(long value, TimeUnit unit) {
        return setTimeoutMillis(NumberUtil.toInt(unit.toMillis(value)));
    }

    public int execute() {
        return internalExecute(false).getCode();
    }

    public HttpResponse executeAndReturnResponse() {
        return internalExecute(true);
    }

    @SuppressWarnings("OverlyLongMethod")
    private HttpResponse internalExecute(boolean readBytes) {
        String internalUrl = appendGetParametersToUrl(this.url);

        if (method == HttpMethod.GET && hasBinaryEntity()) {
            String message = "Can't write binary entity to '" + internalUrl + "' with GET method.";
            logger.warn(message);
            return new HttpResponse(-1, null, null, new IOException(message));
        }

        HttpURLConnection connection;
        try {
            connection = newConnection(
                    internalUrl, method == HttpMethod.POST && (!parametersByName.isEmpty() || hasBinaryEntity())
            );
        } catch (IOException e) {
            String message = "Can't create connection to '" + internalUrl + "'.";
            logger.warn(message, e);
            return new HttpResponse(-1, null, null, new IOException(message, e));
        }

        if (method == HttpMethod.POST) {
            if (!parametersByName.isEmpty()) {
                try {
                    writePostParameters(connection, parametersByName);
                } catch (IOException e) {
                    String message = "Can't write POST parameters to '" + internalUrl + "'.";
                    logger.warn(message, e);
                    return new HttpResponse(-1, null, null, new IOException(message, e));
                }
            }

            if (hasBinaryEntity()) {
                try {
                    writeEntity(connection, binaryEntity);
                } catch (IOException e) {
                    String message = "Can't write binary entity to '" + internalUrl + "'.";
                    logger.warn(message, e);
                    return new HttpResponse(-1, null, null, new IOException(message, e));
                }
            }
        }

        long startTimeMillis = System.currentTimeMillis();

        try {
            connection.connect();

            int code = connection.getResponseCode();
            byte[] bytes = getBytes(connection, readBytes, startTimeMillis);

            return new HttpResponse(code, bytes, connection.getHeaderFields(), null);
        } catch (IOException e) {
            String message = "Can't read response from '" + internalUrl + "'.";
            logger.warn(message, e);
            return new HttpResponse(-1, null, connection.getHeaderFields(), new IOException(message, e));
        } finally {
            connection.disconnect();
        }
    }

    private byte[] getBytes(HttpURLConnection connection, boolean readBytes, final long startTimeMillis)
            throws IOException {
        byte[] bytes;

        if (readBytes) {
            InputStream connectionInputStream;

            try {
                connectionInputStream = connection.getInputStream();
            } catch (IOException e) {
                connectionInputStream = connection.getErrorStream();
                if (connectionInputStream == null) {
                    throw e;
                }
            }

            if (connectionInputStream == null) {
                bytes = null;
            } else {
                if ("gzip".equalsIgnoreCase(connection.getContentEncoding())) {
                    connectionInputStream = new GZIPInputStream(connectionInputStream);
                } else if ("deflate".equalsIgnoreCase(connection.getContentEncoding())) {
                    connectionInputStream = new InflaterInputStream(connectionInputStream);
                } else if ("zip".equalsIgnoreCase(connection.getContentEncoding())) {
                    connectionInputStream = new ZipInputStream(connectionInputStream);
                }

                connectionInputStream = new CountingInputStream(connectionInputStream, new CountingInputStream.ReadEvent() {
                    @Override
                    public void onRead(long readByteCount, long totalReadByteCount) throws IOException {
                        if (System.currentTimeMillis() - startTimeMillis > timeoutMillis) {
                            throw new IOException("Can't read response within " + timeoutMillis + " ms.");
                        }
                    }
                });

                bytes = IoUtil.toByteArray(connectionInputStream, NumberUtil.toInt(FileUtil.BYTES_PER_GB), true);
            }
        } else {
            bytes = null;
        }

        return bytes;
    }

    private String appendGetParametersToUrl(String url) {
        if (method == HttpMethod.GET) {
            for (Map.Entry<String, List<String>> parameterEntry : parametersByName.entrySet()) {
                String parameterName = parameterEntry.getKey();
                for (String parameterValue : parameterEntry.getValue()) {
                    url = UrlUtil.appendParameterToUrl(url, parameterName, parameterValue);
                }
            }
        }

        return url;
    }

    private static String[] validateAndEncodeParameters(String url, Object... parameters) {
        if (!UrlUtil.isValidUrl(url)) {
            throw new IllegalArgumentException('\'' + url + "' is not a valid URL.");
        }

        boolean secureHost;
        try {
            secureHost = CommonsPropertiesUtil.getSecureHosts().contains(new URL(url).getHost());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException('\'' + url + "' is not a valid URL.", e);
        }

        int parameterCount = parameters.length;

        if (parameterCount == 0) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }

        if (parameterCount % 2 != 0) {
            throw new IllegalArgumentException("Argument 'parameters' should contain even number of elements, " +
                    "i.e. should consist of key-value pairs."
            );
        }

        List<String> securePasswords = CommonsPropertiesUtil.getSecurePasswords();
        List<String> privateParameters = CommonsPropertiesUtil.getPrivateParameters();

        String[] parameterCopies = new String[parameterCount];

        for (int parameterIndex = 0; parameterIndex < parameterCount; parameterIndex += 2) {
            Object parameterName = parameters[parameterIndex];
            Object parameterValue = parameters[parameterIndex + 1];

            if (!(parameterName instanceof String) || StringUtil.isBlank((String) parameterName)) {
                throw new IllegalArgumentException(String.format(
                        "Each parameter name should be non-blank string, but found: '%s'.", parameterName
                ));
            }

            if (parameterValue == null) {
                throw new IllegalArgumentException(String.format("Value of parameter '%s' is null.", parameterName));
            }

            try {
                parameterCopies[parameterIndex] = URLEncoder.encode((String) parameterName, "UTF-8");

                if (secureHost
                        || !privateParameters.contains(parameterName)
                        && !securePasswords.contains(parameterValue.toString())) {
                    parameterCopies[parameterIndex + 1] = URLEncoder.encode(parameterValue.toString(), "UTF-8");
                } else {
                    parameterCopies[parameterIndex + 1] = "";
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 is unsupported.", e);
            }
        }

        return parameterCopies;
    }

    private static void validateHeaders(String... headers) {
        int headerCount = headers.length;
        if (headerCount % 2 != 0) {
            throw new IllegalArgumentException("Argument 'headers' should contain even number of elements, " +
                    "i.e. should consist of key-value pairs."
            );
        }

        for (int headerIndex = 0; headerIndex < headerCount; headerIndex += 2) {
            String headerName = headers[headerIndex];
            String headerValue = headers[headerIndex + 1];

            if (StringUtil.isBlank(headerName)) {
                throw new IllegalArgumentException(String.format(
                        "Each header name should be non-blank string, but found: '%s'.", headerName
                ));
            }

            if (headerValue == null) {
                throw new IllegalArgumentException(String.format("Value of header '%s' is null.", headerName));
            }
        }
    }

    private HttpURLConnection newConnection(String url, boolean doOutput) throws IOException {
        URL urlObject = new URL(url);
        @Nullable Proxy proxy = getProxy(urlObject.getProtocol());

        HttpURLConnection connection = (HttpURLConnection) (
                proxy == null ? urlObject.openConnection() : urlObject.openConnection(proxy)
        );

        if (connection instanceof HttpsURLConnection) {
            bypassSecureHostSslCertificateCheck((HttpsURLConnection) connection, urlObject);
        }

        connection.setReadTimeout(timeoutMillis);
        connection.setConnectTimeout(timeoutMillis);
        connection.setRequestMethod(method.name());
        connection.setDoInput(true);
        connection.setDoOutput(doOutput);

        connection.setRequestProperty("Connection", "close");

        if (method == HttpMethod.POST) {
            if (hasBinaryEntity()) {
                connection.setRequestProperty("Content-Type", MimeUtil.Type.APPLICATION_OCTET_STREAM);
            } else if (!parametersByName.isEmpty()) {
                connection.setRequestProperty("Content-Type", MimeUtil.Type.APPLICATION_X_WWW_FORM_URLENCODED);
            }

            if (gzip && (hasBinaryEntity() || !parametersByName.isEmpty())) {
                connection.setRequestProperty("Content-Encoding", "gzip");
            }
        }

        for (Map.Entry<String, List<String>> headerEntry : headersByName.entrySet()) {
            String headerName = headerEntry.getKey();
            boolean first = true;

            for (String headerValue : headerEntry.getValue()) {
                if (first) {
                    connection.setRequestProperty(headerName, headerValue);
                    first = false;
                } else {
                    connection.addRequestProperty(headerName, headerValue);
                }
            }
        }

        return connection;
    }

    private static void bypassSecureHostSslCertificateCheck(HttpsURLConnection connection, URL url) {
        if (!CommonsPropertiesUtil.isBypassCertificateCheck()
                || !CommonsPropertiesUtil.getSecureHosts().contains(url.getHost())) {
            return;
        }

        X509TrustManager insecureTrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                // No operations.
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                // No operations.
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Can't get instance of SSL context.", e);
            return;
        }

        try {
            sslContext.init(null, new TrustManager[]{insecureTrustManager}, new SecureRandom());
        } catch (KeyManagementException e) {
            logger.warn("Can't initialize SSL context.", e);
            return;
        }

        connection.setSSLSocketFactory(sslContext.getSocketFactory());
    }

    @SuppressWarnings("AccessOfSystemProperties")
    @Nullable
    private static Proxy getProxy(String protocol) {
        if (!Boolean.parseBoolean(System.getProperty("proxySet"))) {
            return null;
        }

        if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
            return null;
        }

        String proxyHost = System.getProperty(protocol + ".proxyHost");
        if (StringUtil.isBlank(proxyHost)) {
            return null;
        }

        int proxyPort;
        try {
            proxyPort = Integer.parseInt(System.getProperty(protocol + ".proxyPort"));
            if (proxyPort <= 0 || proxyPort > 65535) {
                return null;
            }
        } catch (NumberFormatException ignored) {
            return null;
        }

        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
    }

    private void writePostParameters(HttpURLConnection connection, Map<String, List<String>> parametersByName)
            throws IOException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, List<String>> parameterEntry : parametersByName.entrySet()) {
            String parameterName = parameterEntry.getKey();
            for (String parameterValue : parameterEntry.getValue()) {
                if (result.length() > 0) {
                    result.append('&');
                }

                result.append(parameterName).append('=').append(parameterValue);
            }
        }

        writeEntity(connection, result.toString().getBytes(Charsets.UTF_8));
    }

    private void writeEntity(HttpURLConnection connection, byte[] entity) throws IOException {
        OutputStream outputStream = new BufferedOutputStream(
                gzip ? new GZIPOutputStream(connection.getOutputStream()) : connection.getOutputStream(),
                IoUtil.BUFFER_SIZE
        );

        long startTimeMillis = System.currentTimeMillis();

        try {
            outputStream.write(entity);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            IoUtil.closeQuietly(outputStream);
            throw e;
        } finally {
            long writeTimeMillis = System.currentTimeMillis() - startTimeMillis;
            if (writeTimeMillis > 100) {
                logger.info(String.format(
                        "Writing of HTTP entity takes %d ms (size=%d, gzip=%s).",
                        writeTimeMillis, entity.length, gzip
                ));
            }
        }
    }

    static <K, V> Map<K, List<V>> getDeepUnmodifiableMap(Map<K, List<V>> map) {
        Map<K, List<V>> copy = new LinkedHashMap<>(map);
        for (Map.Entry<K, List<V>> entry : copy.entrySet()) {
            entry.setValue(Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(copy);
    }
}
