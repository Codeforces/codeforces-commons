package com.codeforces.commons.io.http;

/**
 * @author Mike Mirzayanov (mirzayanovmr@gmail.com)
 */
@SuppressWarnings("UnusedDeclaration")
public class HttpUtil {
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

    public static HttpResponse executePostRequestAndReturnResponse(int timeoutMillis, String url, Object... parameters) {
        return newRequest(url, parameters).setTimeoutMillis(timeoutMillis).setMethod(HttpMethod.POST).executeAndReturnResponse();
    }
}
