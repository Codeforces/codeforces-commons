package com.codeforces.commons.math;

import junit.framework.TestCase;

import java.math.BigDecimal;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 17.06.14
 */
public class NumberUtilTest extends TestCase {
    public void testToLong() throws Exception {
        assertEquals("Can't convert double to long.", 100L, NumberUtil.toLong(100.01D));
        assertEquals("Can't convert double to long.", -1000000000L, NumberUtil.toLong(-1000000000.0D));
        assertEquals("Can't convert double to long.", -1000000000L, NumberUtil.toLong(-1000000000.9D));
        assertEquals("Can't convert double to long.", -1000000001L, NumberUtil.toLong(-1000000000.999999999999999999D));
        assertEquals("Can't convert string to long.", -100L, (long) NumberUtil.toLong("-100.0"));
        assertEquals("Can't convert string to long.", -101L, (long) NumberUtil.toLong(new BigDecimal("-101.0")));
    }
}
