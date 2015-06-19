package com.codeforces.commons.codec;

import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.io.IoUtil;
import com.codeforces.commons.math.NumberUtil;
import com.codeforces.commons.math.RandomUtil;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 14.09.11
 */
@SuppressWarnings({"MessageMissingOnJUnitAssertion", "UnnecessaryCodeBlock"})
public class Base65536Test extends TestCase {
    public void testBase256Encoding() throws Exception {
        {
            // Encode to chars, decode from chars
            byte[] plainBytes = getBytes("description");
            char[] encodedChars = Base65536.encodeBase65536(plainBytes);
            assertTrue(Arrays.equals(plainBytes, Base65536.decodeBase65536(encodedChars)));
        }
        {
            // Encode to chars, decode from string
            byte[] plainBytes = getBytes("realtek.log");
            char[] encodedChars = Base65536.encodeBase65536(plainBytes);
            assertTrue(Arrays.equals(plainBytes, Base65536.decodeBase65536(new String(encodedChars))));
        }
        {
            // Encode to string, decode from string
            byte[] plainBytes = RandomUtil.getRandomBytes(NumberUtil.toInt(
                    10L * FileUtil.BYTES_PER_MB + RandomUtil.getRandomInt(1000)
            ));
            String encodedString = Base65536.encodeBase65536String(plainBytes);
            assertTrue(Arrays.equals(plainBytes, Base65536.decodeBase65536(encodedString)));
        }
        {
            // Encode to string, decode from chars
            byte[] plainBytes = getBytes("memoryx");
            String encodedString = Base65536.encodeBase65536String(plainBytes);
            assertTrue(Arrays.equals(plainBytes, Base65536.decodeBase65536(encodedString.toCharArray())));
        }
    }

    private static byte[] getBytes(String resourceName) throws IOException {
        InputStream resourceStream = Base65536Test.class.getResourceAsStream(
                "/com/codeforces/commons/codec/" + resourceName
        );

        try {
            return IoUtil.toByteArray(resourceStream);
        } finally {
            IoUtil.closeQuietly(resourceStream);
        }
    }
}
