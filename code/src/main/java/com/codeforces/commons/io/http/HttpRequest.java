package com.codeforces.commons.io.http;

import com.codeforces.commons.io.CountingInputStream;
import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.io.IoUtil;
import com.codeforces.commons.math.NumberUtil;
import com.codeforces.commons.properties.internal.CommonsPropertiesUtil;
import com.codeforces.commons.text.StringUtil;
import com.codeforces.commons.text.UrlUtil;
import com.codeforces.commons.time.TimeUtil;
import com.google.common.base.Preconditions;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 27.11.14
 */
@NotThreadSafe
public final class HttpRequest {
    private static final Logger logger = Logger.getLogger(HttpRequest.class);

    private final String url;
    private final Map<String, List<String>> parametersByName = new LinkedHashMap<>();
    private final Map<String, List<String>> headersByName = new LinkedHashMap<>();

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
        return Collections.unmodifiableMap(parametersByName);
    }

    public HttpRequest appendParameters(Object... parameters) {
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

    public HttpRequest prependParameters(Object... parameters) {
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

    public Map<String, List<String>> getHeadersByNameMap() {
        return Collections.unmodifiableMap(headersByName);
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

    public int execute() {
        return internalExecute(false).getCode();
    }

    public HttpResponse executeAndReturnResponse() {
        return internalExecute(true);
    }

    @SuppressWarnings("OverlyLongMethod")
    private HttpResponse internalExecute(boolean readBytes) {
        String internalUrl = this.url;

        if (method == HttpMethod.GET) {
            for (Map.Entry<String, List<String>> parameterEntry : parametersByName.entrySet()) {
                String parameterName = parameterEntry.getKey();
                for (String parameterValue : parameterEntry.getValue()) {
                    internalUrl = UrlUtil.appendParameterToUrl(internalUrl, parameterName, parameterValue);
                }
            }
        }

        HttpURLConnection connection;
        try {
            connection = newConnection(readBytes, method == HttpMethod.POST && !parametersByName.isEmpty());
        } catch (IOException e) {
            String message = "Can't create HTTP connection to '" + internalUrl + "'.";
            logger.warn(message, e);
            return new HttpResponse(-1, null, null, new IOException(message, e));
        }

        if (method == HttpMethod.POST && !parametersByName.isEmpty()) {
            try {
                writePostParameters(connection, parametersByName);
            } catch (IOException e) {
                String message = "Can't write HTTP POST parameters to '" + internalUrl + "'.";
                logger.warn(message, e);
                return new HttpResponse(-1, null, null, new IOException(message, e));
            }
        }

        final long startTimeMillis = System.currentTimeMillis();

        try {
            connection.connect();

            int code = connection.getResponseCode();
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

            return new HttpResponse(code, bytes, connection.getHeaderFields(), null);
        } catch (IOException e) {
            String message = "Can't read response from '" + internalUrl + "'.";
            logger.warn(message, e);
            return new HttpResponse(-1, null, connection.getHeaderFields(), new IOException(message, e));
        } finally {
            connection.disconnect();
        }
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

    private HttpURLConnection newConnection(boolean doInput, boolean doOutput) throws IOException {
        URL urlObject = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();

        connection.setReadTimeout(timeoutMillis);
        connection.setConnectTimeout(timeoutMillis);
        connection.setRequestMethod(method.name());
        connection.setDoInput(doInput);
        connection.setDoOutput(doOutput);

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

    private static void writePostParameters(HttpURLConnection connection, Map<String, List<String>> parametersByName)
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

        OutputStream outputStream = connection.getOutputStream();
        try {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(outputStream, Charsets.UTF_8), IoUtil.BUFFER_SIZE
            );

            try {
                writer.write(result.toString());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                IoUtil.closeQuietly(writer);
                throw e;
            }

            outputStream.close();
        } catch (IOException e) {
            IoUtil.closeQuietly(outputStream);
            throw e;
        }
    }
}
