package com.codeforces.commons.text;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 19.03.14
 */
@SuppressWarnings("MessageMissingOnJUnitAssertion")
public class UrlUtilTest {
    @Test
    public void testAppendParameterToUrl() {
        assertEquals(
                "http://localhost/?pageIndex=19",
                UrlUtil.appendParameterToUrl("http://localhost/", "pageIndex", "19")
        );
        assertEquals(
                "http://localhost/",
                UrlUtil.appendParameterToUrl("http://localhost/", "", "19")
        );
        assertEquals(
                "123?pageIndex=19",
                UrlUtil.appendParameterToUrl("123", "pageIndex", "19")
        );
        assertEquals(
                "http://localhost?pageIndex=1#pageIndex",
                UrlUtil.appendParameterToUrl("http://localhost#pageIndex", "pageIndex", "1")
        );
        assertEquals(
                "http://localhost?pageIndex=1#?pageIndex=7",
                UrlUtil.appendParameterToUrl("http://localhost#?pageIndex=7", "pageIndex", "1")
        );
        assertEquals(
                "http://localhost/?compact",
                UrlUtil.appendParameterToUrl("http://localhost/", "compact", null)
        );
        assertEquals(
                "http://google.ru/?compact&id=17&pageIndex1&counter=0&flag=1",
                UrlUtil.appendParameterToUrl("http://google.ru/?id=17&pageIndex1&counter=0&flag=1", "compact", null)
        );
        assertEquals(
                "http://google.ru/?compact#?id=17&pageIndex=0&pageIndex1&counter=0&pageIndex&flag=1",
                UrlUtil.appendParameterToUrl(
                        "http://google.ru/#?id=17&pageIndex=0&pageIndex1&counter=0&pageIndex&flag=1", "compact", null
                )
        );
    }

    @Test
    public void testAppendParametersToUrl() {
        // Single parameter
        assertEquals(
                "http://localhost/?pageIndex=19",
                UrlUtil.appendParametersToUrl("http://localhost/", "pageIndex", "19")
        );
        assertEquals(
                "http://localhost/",
                UrlUtil.appendParametersToUrl("http://localhost/", "", "19")
        );
        assertEquals(
                "123?pageIndex=19",
                UrlUtil.appendParametersToUrl("123", "pageIndex", "19")
        );
        assertEquals(
                "http://localhost?pageIndex=1#pageIndex",
                UrlUtil.appendParametersToUrl("http://localhost#pageIndex", "pageIndex", "1")
        );
        assertEquals(
                "http://localhost?pageIndex=1#?pageIndex=7",
                UrlUtil.appendParametersToUrl("http://localhost#?pageIndex=7", "pageIndex", "1")
        );
        assertEquals(
                "http://localhost/?compact",
                UrlUtil.appendParametersToUrl("http://localhost/", "compact", null)
        );
        assertEquals(
                "http://google.ru/?compact&id=17&pageIndex1&counter=0&flag=1",
                UrlUtil.appendParametersToUrl("http://google.ru/?id=17&pageIndex1&counter=0&flag=1", "compact", null)
        );
        assertEquals(
                "http://google.ru/?compact#?id=17&pageIndex=0&pageIndex1&counter=0&pageIndex&flag=1",
                UrlUtil.appendParametersToUrl(
                        "http://google.ru/#?id=17&pageIndex=0&pageIndex1&counter=0&pageIndex&flag=1", "compact", null
                )
        );

        // Multiple parameters
        assertEquals(
                "http://localhost/?pageIndex=19&friends=true",
                UrlUtil.appendParametersToUrl("http://localhost/", "pageIndex", "19", "friends", "true")
        );
        assertEquals(
                "http://localhost/?friends=true&cool=false&width=100",
                UrlUtil.appendParametersToUrl(
                        "http://localhost/", "", "19", "friends", "true", "cool", "false", "width", "100"
                )
        );
        assertEquals(
                "123?pageIndex=19&friends=true",
                UrlUtil.appendParametersToUrl("123", "pageIndex", "19", "friends", "true")
        );
        assertEquals(
                "http://localhost?pageIndex=1&friends=true#pageIndex",
                UrlUtil.appendParametersToUrl("http://localhost#pageIndex", "pageIndex", "1", "friends", "true")
        );
        assertEquals(
                "http://localhost?pageIndex=1&friends=true#?pageIndex=7",
                UrlUtil.appendParametersToUrl("http://localhost#?pageIndex=7", "pageIndex", "1", "friends", "true")
        );
        assertEquals(
                "http://localhost/?compact&friends=true",
                UrlUtil.appendParametersToUrl("http://localhost/", "compact", null, "friends", "true")
        );
        assertEquals(
                "http://google.ru/?compact&friends=true&id=17&pageIndex1&counter=0&flag=1",
                UrlUtil.appendParametersToUrl(
                        "http://google.ru/?id=17&pageIndex1&counter=0&flag=1", "compact", null, "friends", "true"
                )
        );
        assertEquals(
                "http://google.ru/?compact&friends=true#?id=17&pageIndex=0&pageIndex1&counter=0&pageIndex&flag=1",
                UrlUtil.appendParametersToUrl(
                        "http://google.ru/#?id=17&pageIndex=0&pageIndex1&counter=0&pageIndex&flag=1",
                        "compact", null, "friends", "true"
                )
        );
    }

    @Test
    public void testRemoveParameterFromUrl() {
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

    @Test
    public void testReplaceParameterInUrl() {
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

    @Test
    public void testIsValidUrl() {
        assertTrue(UrlUtil.isValidUrl("https://ru.wikipedia.org/wiki/"
                + "%D0%97%D0%B0%D0%B3%D0%BB%D0%B0%D0%B2%D0%BD%D0%B0%D1%8F_%D1%81%D1%82%D1%80%D0%B0%D0%BD%D0%B8%D1%86%D0%B0"));
        assertTrue(UrlUtil.isValidUrl("http://codeforces.com/profile/MikeMirzayanov/%D1%83%D0%BF%D1%81#%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82"));
        assertFalse(UrlUtil.isValidUrl(null));
        assertFalse(UrlUtil.isValidUrl(""));
        assertFalse(UrlUtil.isValidUrl("\t"));
        assertFalse(UrlUtil.isValidUrl(" "));
        assertFalse(UrlUtil.isValidUrl("    "));
        assertFalse(UrlUtil.isValidUrl("http://codeforces.com/#\"><script>alert(\"hello world!\");</script><br"));
    }
}
