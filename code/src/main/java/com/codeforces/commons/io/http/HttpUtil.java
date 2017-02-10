package com.codeforces.commons.io.http;

import com.codeforces.commons.process.ThreadUtil;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Mike Mirzayanov (mirzayanovmr@gmail.com)
 */
@SuppressWarnings({"UnusedDeclaration", "OverloadedVarargsMethod", "WeakerAccess"})
public final class HttpUtil {
    public static HttpRequest newRequest(String url, Object... parameters) {
        return HttpRequest.create(url, parameters);
    }

    public static int executeGetRequest(String url, Object... parameters) {
        return newRequest(url, parameters).execute();
    }

    public static HttpResponse executeGetRequestAndReturnResponse(String url, Object... parameters) {
        return newRequest(url, parameters).executeAndReturnResponse();
    }

    public static int executeGetRequest(int timeoutMillis, String url, Object... parameters) {
        return newRequest(url, parameters).setTimeoutMillis(timeoutMillis).execute();
    }

    public static HttpResponse executeGetRequestAndReturnResponse(int timeoutMillis, String url, Object... parameters) {
        return newRequest(url, parameters).setTimeoutMillis(timeoutMillis).executeAndReturnResponse();
    }

    public static int executePostRequest(String url, Object... parameters) {
        return newRequest(url, parameters).setMethod(HttpMethod.POST).execute();
    }

    public static HttpResponse executePostRequestAndReturnResponse(String url, Object... parameters) {
        return newRequest(url, parameters).setMethod(HttpMethod.POST).executeAndReturnResponse();
    }

    public static int executePostRequest(int timeoutMillis, String url, Object... parameters) {
        return newRequest(url, parameters).setTimeoutMillis(timeoutMillis).setMethod(HttpMethod.POST).execute();
    }

    public static void executePostRequestAsync(String url, Object... parameters) {
        ThreadUtil.newThread(new Runnable() {
            @Override
            public void run() {
                executePostRequest(url, parameters);
            }
        }).start();
    }

    public static HttpResponse executePostRequestAndReturnResponse(int timeoutMillis, String url, Object... parameters) {
        return newRequest(url, parameters).setTimeoutMillis(timeoutMillis).setMethod(HttpMethod.POST)
                .executeAndReturnResponse();
    }

    @Nullable
    static String getHeader(List<String> headers, String headerName, boolean throwIfMany) {
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

    private HttpUtil() {
        throw new UnsupportedOperationException();
    }
}
