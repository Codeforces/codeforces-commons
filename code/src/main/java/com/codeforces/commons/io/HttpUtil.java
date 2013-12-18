package com.codeforces.commons.io;

import com.codeforces.commons.properties.internal.CodeforcesCommonsPropertiesUtil;
import com.codeforces.commons.text.StringUtil;
import com.codeforces.commons.text.UrlUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 07.11.11
 */
@SuppressWarnings("OverloadedVarargsMethod")
public class HttpUtil {
    private HttpUtil() {
        throw new UnsupportedOperationException();
    }

    public static void executeGetRequest(boolean encodeParameters, String url, Object... parameters)
            throws IOException {
        internalExecuteGetRequest(encodeParameters, url, parameters);
    }

    public static void executeGetRequest(String url, Object... parameters) throws IOException {
        executeGetRequest(false, url, parameters);
    }

    public static byte[] executeGetRequestAndReturnResponseBytes(
            boolean encodeParameters, String url, Object... parameters) throws IOException {
        InputStream inputStream = internalExecuteGetRequest(encodeParameters, url, parameters).getEntity().getContent();
        try {
            return IOUtils.toByteArray(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public static byte[] executeGetRequestAndReturnResponseBytes(String url, Object... parameters) throws IOException {
        return executeGetRequestAndReturnResponseBytes(false, url, parameters);
    }

    public static String executeGetRequestAndReturnResponseAsString(
            boolean encodeParameters, String url, Object... parameters) throws IOException {
        HttpEntity responseEntity = internalExecuteGetRequest(encodeParameters, url, parameters).getEntity();
        InputStream inputStream = responseEntity.getContent();
        try {
            return responseEntity.getContentEncoding() == null
                    ? IOUtils.toString(inputStream)
                    : IOUtils.toString(inputStream, responseEntity.getContentEncoding().getValue());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public static String executeGetRequestAndReturnResponseAsString(String url, Object... parameters)
            throws IOException {
        return executeGetRequestAndReturnResponseAsString(false, url, parameters);
    }

    public static Response executeGetRequestAndReturnResponse(
            boolean encodeParameters, String url, Object... parameters) throws IOException {
        HttpResponse response = internalExecuteGetRequest(encodeParameters, url, parameters);
        HttpEntity responseEntity = response.getEntity();
        return new Response(
                response.getStatusLine().getStatusCode(),
                responseEntity.getContent(),
                responseEntity.getContentEncoding() == null ? null : responseEntity.getContentEncoding().getValue(),
                responseEntity.getContentLength()
        );
    }

    public static Response executeGetRequestAndReturnResponse(String url, Object... parameters) throws IOException {
        return executeGetRequestAndReturnResponse(false, url, parameters);
    }

    private static HttpResponse internalExecuteGetRequest(boolean encodeParameters, String url, Object... parameters)
            throws IOException {
        parameters = validateAndPreprocessParameters(encodeParameters, url, parameters);

        for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex += 2) {
            url = UrlUtil.appendParameterToUrl(
                    url, (String) parameters[parameterIndex], parameters[parameterIndex + 1].toString()
            );
        }

        HttpClient httpClient = newDefaultHttpClient();
        HttpGet request = new HttpGet(url);

        return httpClient.execute(request);
    }

    public static void executePostRequest(String url, Object... parameters) throws IOException {
        internalExecutePostRequest(url, parameters);
    }

    @Nullable
    public static byte[] executePostRequestAndReturnResponseBytes(String url, Object... parameters) throws IOException {
        HttpEntity responseEntity = internalExecutePostRequest(url, parameters).getEntity();
        if (responseEntity == null) {
            return null;
        }

        InputStream inputStream = responseEntity.getContent();
        try {
            return IOUtils.toByteArray(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Nullable
    public static String executePostRequestAndReturnResponseAsString(String url, Object... parameters)
            throws IOException {
        HttpEntity responseEntity = internalExecutePostRequest(url, parameters).getEntity();
        if (responseEntity == null) {
            return null;
        }

        InputStream inputStream = responseEntity.getContent();
        try {
            return responseEntity.getContentEncoding() == null
                    ? IOUtils.toString(inputStream)
                    : IOUtils.toString(inputStream, responseEntity.getContentEncoding().getValue());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public static Response executePostRequestAndReturnResponse(String url, Object... parameters) throws IOException {
        HttpResponse response = internalExecutePostRequest(url, parameters);
        HttpEntity responseEntity = response.getEntity();

        if (responseEntity == null) {
            return new Response(response.getStatusLine().getStatusCode(), null, null, 0);
        } else {
            return new Response(
                    response.getStatusLine().getStatusCode(),
                    responseEntity.getContent(),
                    responseEntity.getContentEncoding() == null ? null : responseEntity.getContentEncoding().getValue(),
                    responseEntity.getContentLength()
            );
        }
    }

    private static HttpResponse internalExecutePostRequest(String url, Object... parameters)
            throws IOException {
        parameters = validateAndPreprocessParameters(false, url, parameters);

        HttpClient httpClient = newDefaultHttpClient();
        HttpPost request = new HttpPost(url);

        List<NameValuePair> postParameters = new ArrayList<NameValuePair>();

        for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex += 2) {
            postParameters.add(new BasicNameValuePair(
                    (String) parameters[parameterIndex],
                    parameters[parameterIndex + 1].toString()
            ));
        }

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters, "UTF-8");
        request.setEntity(entity);

        return httpClient.execute(request);
    }

    @SuppressWarnings("OverlyComplexMethod")
    private static Object[] validateAndPreprocessParameters(
            boolean encodeParameters, String url, Object... parameters) {
        if (StringUtil.isBlank(url)) {
            throw new IllegalArgumentException("Argument 'url' is blank.");
        }

        boolean secureHost;
        try {
            secureHost = CodeforcesCommonsPropertiesUtil.getSecureHosts().contains(new URL(url).getHost());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException('\'' + url + "' is not valid URL.", e);
        }

        int parameterCount = parameters.length;

        if (parameterCount % 2 != 0) {
            throw new IllegalArgumentException("Argument 'parameters' should contain even number of elements, " +
                    "i.e. should consist of key-value pairs."
            );
        }

        List<String> securePasswords = CodeforcesCommonsPropertiesUtil.getSecurePasswords();
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

    public static HttpClient newDefaultHttpClient() {
        return HttpClientBuilder.create().build();
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
}
