package com.codeforces.commons.io.http;

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
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 27.11.14
 */
@NotThreadSafe
public class HttpRequest {
    private static final Logger logger = Logger.getLogger(HttpRequest.class);

    private final String url;
    private final Map<String, List<String>> parametersByName = new LinkedHashMap<>();

    private HttpMethod method = HttpMethod.GET;
    private int timeoutMillis = NumberUtil.toInt(5L * TimeUtil.MILLIS_PER_MINUTE);

    public static HttpRequest create(String url, Object... parameters) {
        return new HttpRequest(url, parameters);
    }

    private HttpRequest(String url, Object... parameters) {
        this.url = url;

        String[] encodedParameters = validateAndPreprocessParameters(url, parameters);
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
    }

    public String getUrl() {
        return url;
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
            connection = newConnection(
                    timeoutMillis, method, internalUrl, readBytes,
                    method == HttpMethod.POST && !parametersByName.isEmpty()
            );
        } catch (IOException e) {
            logger.warn("Can't create HTTP connection to '" + internalUrl + "'.", e);
            return new HttpResponse(-1, null, new IOException("Can't create HTTP connection.", e));
        }

        if (method == HttpMethod.POST && !parametersByName.isEmpty()) {
            try {
                putPostParameters(connection, parametersByName);
            } catch (IOException e) {
                logger.warn("Can't send HTTP parameters to '" + internalUrl + "'.", e);
                return new HttpResponse(-1, null, new IOException("Can't send HTTP parameters.", e));
            }
        }

        try {
            connection.connect();

            int code = connection.getResponseCode();
            byte[] bytes;

            if (readBytes) {
                InputStream connectionInputStream;

                try {
                    connectionInputStream = connection.getInputStream();
                } catch (UnknownServiceException ignored) {
                    connectionInputStream = connection.getErrorStream();
                }

                bytes = connectionInputStream == null
                        ? null
                        : IoUtil.toByteArray(connectionInputStream, NumberUtil.toInt(FileUtil.BYTES_PER_GB), true);
            } else {
                bytes = null;
            }

            return new HttpResponse(code, bytes, null);
        } catch (IOException e) {
            logger.warn("Can't read response from '" + internalUrl + "'.", e);
            return new HttpResponse(-1, null, e);
        } finally {
            connection.disconnect();
        }
    }

    private static String[] validateAndPreprocessParameters(String url, Object... parameters) {
        if (!UrlUtil.isValidUrl(url)) {
            throw new IllegalArgumentException('\'' + url + "' is not a valid URL.");
        }

        if (ArrayUtils.isEmpty(parameters)) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
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
                throw new IllegalArgumentException(String.format("Parameter '%s' is 'null'.", parameterName));
            }

            try {
                parameterCopies[parameterIndex] = URLEncoder.encode((String) parameterName, "UTF-8");
                parameterCopies[parameterIndex + 1] = !secureHost && securePasswords.contains(parameterValue.toString())
                        ? ""
                        : URLEncoder.encode(String.valueOf(parameterValue), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 is unsupported.", e);
            }
        }

        return parameterCopies;
    }

    private static HttpURLConnection newConnection(
            int timeoutMillis, HttpMethod method, String url, boolean doInput, boolean doOutput) throws IOException {
        URL urlObject = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
        connection.setReadTimeout(timeoutMillis);
        connection.setConnectTimeout(timeoutMillis);
        connection.setRequestMethod(method.toString());
        connection.setDoInput(doInput);
        connection.setDoOutput(doOutput);
        return connection;
    }

    private static void putPostParameters(HttpURLConnection connection, Map<String, List<String>> parametersByName)
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
