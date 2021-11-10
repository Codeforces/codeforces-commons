package com.codeforces.commons.io.http;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 01.06.15
 */
public interface HttpResponseChecker {
    boolean check(HttpResponse response);
}
