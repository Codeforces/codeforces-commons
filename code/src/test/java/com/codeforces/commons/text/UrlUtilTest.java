package com.codeforces.commons.text;

import junit.framework.TestCase;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 19.03.14
 */
@SuppressWarnings("MessageMissingOnJUnitAssertion")
public class UrlUtilTest extends TestCase {
    public void testRemoveParameterFromUrl() throws Exception {
        assertEquals("http://localhost/", UrlUtil.removeParameterFromUrl("http://localhost/?", "pageIndex"));
        assertEquals("http://localhost/", UrlUtil.removeParameterFromUrl("http://localhost/?pageIndex=0", "pageIndex"));
        assertEquals(
                "http://localhost/#pageIndex",
                UrlUtil.removeParameterFromUrl("http://localhost/?pageIndex=1#pageIndex", "pageIndex")
        );
        assertEquals("http://127.0.0.1#id", UrlUtil.removeParameterFromUrl("http://127.0.0.1#id", "id"));
        assertEquals(
                "http://google.ru/?id=17&pageIndex1&counter=0&flag=1",
                UrlUtil.removeParameterFromUrl(
                        "http://google.ru/?id=17&pageIndex=0&pageIndex1&counter=0&pageIndex&flag=1", "pageIndex"
                )
        );
        assertEquals(
                "http://google.ru/#?id=17&pageIndex=0&pageIndex1&counter=0&pageIndex&flag=1",
                UrlUtil.removeParameterFromUrl(
                        "http://google.ru/#?id=17&pageIndex=0&pageIndex1&counter=0&pageIndex&flag=1", "pageIndex1"
                )
        );
    }
}
