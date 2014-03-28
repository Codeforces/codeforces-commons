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

    public void testReplaceParameterInUrl() throws Exception {
        assertEquals(
                "http://localhost/?pageIndex=999",
                UrlUtil.replaceParameterInUrl("http://localhost/?", "pageIndex", "999")
        );
        assertEquals(
                "http://localhost/?pageIndex=pageIndex",
                UrlUtil.replaceParameterInUrl("http://localhost/?pageIndex=0", "pageIndex", "pageIndex")
        );
        assertEquals(
                "http://localhost/?pageIndex=350#pageIndex",
                UrlUtil.replaceParameterInUrl("http://localhost/?pageIndex=1#pageIndex", "pageIndex", "350")
        );
        assertEquals("http://127.0.0.1?id#id", UrlUtil.replaceParameterInUrl("http://127.0.0.1#id", "id", null));
        assertEquals(
                "http://google.ru/?pageIndex&id=17&pageIndex1&counter=0&flag=1",
                UrlUtil.replaceParameterInUrl(
                        "http://google.ru/?id=17&pageIndex=0&pageIndex1&counter=0&pageIndex&flag=1", "pageIndex", null
                )
        );
        assertEquals(
                "http://google.ru/?pageIndex1=0#?id=17&pageIndex=0&pageIndex1&counter=0&pageIndex&flag=1",
                UrlUtil.replaceParameterInUrl(
                        "http://google.ru/#?id=17&pageIndex=0&pageIndex1&counter=0&pageIndex&flag=1", "pageIndex1", "0"
                )
        );
    }
}
