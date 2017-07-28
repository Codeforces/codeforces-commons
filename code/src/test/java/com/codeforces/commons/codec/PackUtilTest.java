package com.codeforces.commons.codec;

import gnu.trove.list.*;
import gnu.trove.list.array.*;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 28.07.2017
 */
public class PackUtilTest {
    @Test
    public void testPackIntsToLong() throws Exception {
        @SuppressWarnings("UnsecureRandomNumberGeneration") Random random = new Random();
        TIntList ints = new TIntArrayList();

        ints.add(0);
        ints.add(1);
        ints.add(-1);
        ints.add(1_000);
        ints.add(-1_000);
        ints.add(1_000_000);
        ints.add(-1_000_000);
        ints.add(1_000_000_000);
        ints.add(-1_000_000_000);
        ints.add(Integer.MIN_VALUE);
        ints.add(Integer.MAX_VALUE);

        for (int i = 0; i < 10000; ++i) {
            ints.add(random.nextInt());
        }

        for (int i = 0; i < ints.size(); ++i) {
            int left0 = ints.get(i);

            for (int j = 0; j < ints.size(); ++j) {
                int right0 = ints.get(j);

                long packedValue = PackUtil.packInts(left0, right0);

                Assert.assertEquals("Can't unpack left integer.", left0, PackUtil.unpackLeftInt(packedValue));
                Assert.assertEquals("Can't unpack right integer.", right0, PackUtil.unpackRightInt(packedValue));
            }
        }
    }

    @Test
    public void testPackShortsToInt() throws Exception {
        @SuppressWarnings("UnsecureRandomNumberGeneration") Random random = new Random();
        TShortList shorts = new TShortArrayList();

        shorts.add((short) 0);
        shorts.add((short) 1);
        shorts.add((short) -1);
        shorts.add((short) 1_000);
        shorts.add((short) -1_000);
        shorts.add(Short.MIN_VALUE);
        shorts.add(Short.MAX_VALUE);

        byte[] bytes = new byte[Short.BYTES];

        for (int i = 0; i < 10000; ++i) {
            random.nextBytes(bytes);
            shorts.add(ByteBuffer.allocate(Short.BYTES).put(bytes).getShort(0));
        }

        for (int i = 0; i < shorts.size(); ++i) {
            short left0 = shorts.get(i);

            for (int j = 0; j < shorts.size(); ++j) {
                short right0 = shorts.get(j);

                int packedValue = PackUtil.packShorts(left0, right0);

                Assert.assertEquals("Can't unpack left short.", left0, PackUtil.unpackLeftShort(packedValue));
                Assert.assertEquals("Can't unpack right short.", right0, PackUtil.unpackRightShort(packedValue));
            }
        }
    }

    @Test
    public void testPackFloatsToLong() throws Exception {
        @SuppressWarnings("UnsecureRandomNumberGeneration") Random random = new Random();
        TFloatList floats = new TFloatArrayList();

        floats.add(0.0f);
        floats.add(0.001f);
        floats.add(-0.001f);
        floats.add(1.0f);
        floats.add(-1.0f);
        floats.add(1_000.0f);
        floats.add(-1_000.0f);
        floats.add(Float.MIN_VALUE);
        floats.add(Float.MAX_VALUE);

        for (int i = 0; i < 10000; ++i) {
            floats.add(Float.intBitsToFloat(random.nextInt()));
        }

        for (int i = 0; i < floats.size(); ++i) {
            float left0 = floats.get(i);

            for (int j = 0; j < floats.size(); ++j) {
                float right0 = floats.get(j);

                long packedValue = PackUtil.packFloats(left0, right0);

                Assert.assertEquals("Can't unpack left float.", left0, PackUtil.unpackLeftFloat(packedValue), 0.0f);
                Assert.assertEquals("Can't unpack right float.", right0, PackUtil.unpackRightFloat(packedValue), 0.0f);
            }
        }
    }
}
