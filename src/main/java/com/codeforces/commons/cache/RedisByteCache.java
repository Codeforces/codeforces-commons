package com.codeforces.commons.cache;

import com.codeforces.commons.math.RandomUtil;
import com.codeforces.commons.text.Patterns;
import com.codeforces.commons.time.TimeUtil;
import org.apache.log4j.Logger;
import org.xerial.snappy.Snappy;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class RedisByteCache extends ByteCache {
    private static final Logger logger = Logger.getLogger(RedisByteCache.class);
    private static final long DEFAULT_LIFETIME = TimeUtil.MILLIS_PER_DAY;

    private final String host;
    @SuppressWarnings("FieldCanBeLocal")
    private final int port;

    private final JedisPool jedisPool;

    public RedisByteCache(String hostAndPort) {
        String[] items = Patterns.COLON_PATTERN.split(hostAndPort);

        if (items.length == 1) {
            this.host = hostAndPort;
            this.port = 11211;
        } else {
            this.host = items[0];
            this.port = Integer.parseInt(items[1]);
        }

        logger.info("Redis trying to connect initially.");
        jedisPool = new JedisPool(host, port);
    }

    private Jedis getJedis() {
        Jedis jedis = jedisPool.getResource();

        if (!jedis.isConnected()) {
            jedis.connect();
        }

        return jedis;
    }

    @SuppressWarnings("deprecation")
    private void releaseJedis(Jedis jedis) {
        jedisPool.returnResource(jedis);
    }

    @Override
    public boolean validate() {
        Jedis jedis = getJedis();

        try {
            return jedis.isConnected();
        } finally {
            releaseJedis(jedis);
        }
    }

    private static byte[] getFilename(@Nonnull String section, @Nonnull String key) {
        return (section + '/' + key).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean contains(@Nonnull String section, @Nonnull String key) {
        Jedis jedis = getJedis();

        try {
            return jedis.exists(getFilename(section, key));
        } finally {
            releaseJedis(jedis);
        }
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        put(section, key, value, DEFAULT_LIFETIME);
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        Jedis jedis = getJedis();

        try {
            byte[] keyBytes = getFilename(section, key);
            byte[] valueBytes = new Item(value, System.currentTimeMillis() + lifetimeMillis).toByteArray();
            jedis.set(keyBytes, valueBytes);
        } catch (IOException e) {
            logger.error("Can't put " + section + '/' + key + " to the Redis.", e);
        } finally {
            releaseJedis(jedis);
        }
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        if (!contains(section, key)) {
            put(section, key, value, DEFAULT_LIFETIME);
        }
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        if (!contains(section, key)) {
            put(section, key, value, lifetimeMillis);
        }
    }

    @Nullable
    @Override
    public byte[] get(@Nonnull String section, @Nonnull String key) {
        Jedis jedis = getJedis();

        try {
            byte[] keyBytes = getFilename(section, key);
            byte[] valueBytes = jedis.get(keyBytes);

            if (valueBytes != null) {
                try {
                    Item item = Item.fromByteArray(valueBytes);
                    long deadlineTime = item.deadlineTime;

                    if (System.currentTimeMillis() <= deadlineTime) {
                        return item.bytes;
                    }
                } catch (IOException e) {
                    logger.error("Can't get " + section + '/' + key + " to the Redis.", e);
                }
            }

            return null;
        } finally {
            releaseJedis(jedis);
        }
    }

    @Override
    public boolean remove(@Nonnull String section, @Nonnull String key) {
        Jedis jedis = getJedis();

        try {
            return jedis.del(getFilename(section, key)) > 0;
        } finally {
            releaseJedis(jedis);
        }
    }

    @Override
    public void clearSection(@Nonnull String section) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        Jedis jedis = getJedis();

        try {
            jedis.flushAll();
        } finally {
            releaseJedis(jedis);
        }
    }

    @Override
    public void close() {
        jedisPool.destroy();
    }

    @Override
    public String toString() {
        return "RedisByteCache {host='" + host + "'}";
    }

    private static class Item {
        public static final int LONG_SIZE_BYTES = Long.SIZE / Byte.SIZE;

        private final byte[] bytes;
        private final long deadlineTime;

        private Item(byte[] bytes, long deadlineTime) {
            this.bytes = bytes;
            this.deadlineTime = deadlineTime;
        }

        public byte[] toByteArray() throws IOException {
            byte[] compressedBytes = Snappy.compress(bytes);
            ByteBuffer byteBuffer = ByteBuffer.allocate(LONG_SIZE_BYTES + compressedBytes.length);
            byteBuffer.putLong(deadlineTime);
            byteBuffer.put(compressedBytes);
            return byteBuffer.array();
        }

        public static Item fromByteArray(byte[] byteArray) throws IOException {
            ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
            long deadlineTime = byteBuffer.getLong();
            byte[] compressedBytes = new byte[byteArray.length - LONG_SIZE_BYTES];
            byteBuffer.get(compressedBytes);
            return new Item(Snappy.uncompress(compressedBytes), deadlineTime);
        }
    }

    public static void main(String[] args) {
        ByteCache byteCache = new RedisByteCache("127.0.0.1:88");

        long sumd = 0;
        for (int i = 0; i < 100; i++) {
            String key = RandomUtil.getRandomAlphanumeric(10);
            byte[] value = RandomUtil.getRandomBytes(1024 * 1024 * 8);
            long from = System.currentTimeMillis();
            byteCache.put("a", key, value);
            long d = System.currentTimeMillis() - from;
            System.out.println(d);
            sumd += d;
        }

        System.out.println(sumd);
//
//        System.out.println(byteCache.validate());
//        System.out.println(byteCache.contains("a", "b"));
//        byteCache.put("a", "b", new byte[]{1, 2, 3});
//        System.out.println(byteCache.contains("a", "b"));
//        byte[] res = byteCache.get("a", "b");
//        System.out.println(res.length);
    }
}
