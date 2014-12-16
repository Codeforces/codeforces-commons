package com.codeforces.commons.text;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 16.12.14
 */
public class LanguageTest {
    @Test
    public void testInitialization() throws Exception {
        Language[] languages = Language.values();
        Assert.assertNotNull("Languages are null.", languages);
    }
}
