package com.codeforces.commons.io;

import com.codeforces.commons.properties.internal.CommonsPropertiesUtil;
import com.codeforces.commons.text.StringUtil;
import com.codeforces.commons.text.UrlUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Mike Mirzayanov (mirzayanovmr@gmail.com)
 */
@SuppressWarnings("UnusedDeclaration")
public class HttpUtil {
    private static final Logger logger = Logger.getLogger(HttpUtil.class);

    private static final int DEFAULT_TIMEOUT_MILLIS = (int) TimeUnit.MINUTES.toMillis(1);

    /* Get. */

    public static int executeGetRequest(String url, Object... parameters) throws IOException {
        return internalExecuteRequest(DEFAULT_TIMEOUT_MILLIS, Method.GET, url, parameters, false).getCode();
    }

    public static Response executeGetRequestAndReturnResponse(String url, Object... parameters) throws IOException {
        return internalExecuteRequest(DEFAULT_TIMEOUT_MILLIS, Method.GET, url, parameters, true);
    }

    public static int executeGetRequest(int timeoutMillis, String url, Object... parameters) throws IOException {
        return internalExecuteRequest(timeoutMillis, Method.GET, url, parameters, false).getCode();
    }

    public static Response executeGetRequestAndReturnResponse(int timeoutMillis, String url, Object... parameters) throws IOException {
        return internalExecuteRequest(timeoutMillis, Method.GET, url, parameters, true);
    }

    /* Post. */

    public static int executePostRequest(String url, Object... parameters) throws IOException {
        return internalExecuteRequest(DEFAULT_TIMEOUT_MILLIS, Method.POST, url, parameters, false).getCode();
    }

    public static Response executePostRequestAndReturnResponse(String url, Object... parameters) throws IOException {
        return internalExecuteRequest(DEFAULT_TIMEOUT_MILLIS, Method.POST, url, parameters, true);
    }

    public static int executePostRequest(int timeoutMillis, String url, Object... parameters) throws IOException {
        return internalExecuteRequest(timeoutMillis, Method.POST, url, parameters, false).getCode();
    }

    public static Response executePostRequestAndReturnResponse(int timeoutMillis, String url, Object... parameters) throws IOException {
        return internalExecuteRequest(timeoutMillis, Method.POST, url, parameters, true);
    }

    /* Common. */

    private static Response internalExecuteRequest(int timeoutMillis, Method method, String url, Object[] parameters, boolean readBytes) throws IOException {
        String[] encodedParameters = validateAndPreprocessParameters(url, parameters);

        if (method == Method.GET) {
            for (int parameterIndex = 0; parameterIndex < encodedParameters.length; parameterIndex += 2) {
                url = UrlUtil.appendParameterToUrl(
                        url, encodedParameters[parameterIndex], encodedParameters[parameterIndex + 1]
                );
            }
        }

        HttpURLConnection connection = newConnection(timeoutMillis, method, url, readBytes,
                method == Method.POST && parameters.length > 0);

        if (method == Method.POST && parameters.length > 0) {
            putPostParameters(connection, encodedParameters);
        }

        try {
            connection.connect();

            int code = connection.getResponseCode();
            InputStream connectionInputStream;

            try {
                connectionInputStream = connection.getInputStream();
            } catch (IOException e) {
                // Some status codes like 409 doesn't give you to read data.
                connectionInputStream = null;
            }

            byte[] bytes = readBytes && connectionInputStream != null
                    ? IoUtil.toByteArray(connectionInputStream, (int) FileUtil.BYTES_PER_GB, true)
                    : null;
            return new Response(code, bytes);
        } catch (IOException e) {
            logger.warn("Can't read data from `" + url + "`.", e);
            return new Response(Code.BAD_GATEWAY, null);
        } finally {
            connection.disconnect();
        }
    }

    private static void putPostParameters(HttpURLConnection connection, String[] parameters) throws IOException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (int i = 0; i < parameters.length; i += 2) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            result.append(parameters[i]);
            result.append("=");
            result.append(parameters[i + 1]);
        }

        OutputStream outputStream = connection.getOutputStream();
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            try {
                writer.write(result.toString());
                writer.flush();
            } finally {
                IoUtil.closeQuietly(writer);
            }
        } finally {
            IoUtil.closeQuietly(outputStream);
        }
    }

    private static HttpURLConnection newConnection(int timeoutMillis, Method method, String url,
                                                   boolean doInput, boolean doOutput) throws IOException {
        if (timeoutMillis < 0) {
            throw new IllegalArgumentException("Illegal parameter: timeoutMillis < 0.");
        }

        if (method == null) {
            throw new NullPointerException("Illegal parameter: method should be non-null.");
        }

        URL urlObject = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
        connection.setReadTimeout(timeoutMillis);
        connection.setConnectTimeout(timeoutMillis);
        connection.setRequestMethod(method.toString());
        connection.setDoInput(doInput);
        connection.setDoOutput(doOutput);
        return connection;
    }

    @SuppressWarnings("OverlyComplexMethod")
    private static String[] validateAndPreprocessParameters(String url, Object... parameters) {
        if (!UrlUtil.isValidUrl(url)) {
            throw new IllegalArgumentException('\'' + url + "' is not valid URL.");
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

    public static final class Response {
        private final int code;

        @Nullable
        private final byte[] bytes;

        private Response(int code, @Nullable byte[] bytes) {
            this.code = code;
            this.bytes = bytes;
        }

        public int getCode() {
            return code;
        }

        @Nullable
        public byte[] getBytes() {
            return bytes;
        }

        @Nullable
        public String asString() {
            if (bytes == null) {
                return null;
            } else {
                try {
                    return new String(bytes, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Can't find UTF-8.");
                }
            }
        }

        @Override
        public String toString() {
            return "Response{" +
                    "code=" + code +
                    ", size=" + (bytes == null ? "null" : Integer.toString(bytes.length)) +
                    ", s='" + (bytes == null ? "null" : new String(ArrayUtils.subarray(bytes, 0, 20))) + (bytes != null && bytes.length > 20 ? "..." : "") + "'" +
                    '}';
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

    private enum Method {
        GET,
        POST
    }
}
