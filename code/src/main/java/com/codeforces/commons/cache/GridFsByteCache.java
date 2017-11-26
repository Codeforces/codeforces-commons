package com.codeforces.commons.cache;

import com.codeforces.commons.io.ByteArrayOutputStream;
import com.codeforces.commons.math.NumberUtil;
import com.codeforces.commons.time.TimeUtil;
import com.mongodb.*;
import com.mongodb.gridfs.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.UnknownHostException;

public class GridFsByteCache extends ByteCache {
    private static final Logger logger = Logger.getLogger(GridFsByteCache.class);
    private static final String DBNAME = "gridfs_0";

    private final String host;
    private final MongoClient mongoClient;
    private GridFS fs;

    private long defaultLifetime = TimeUtil.MILLIS_PER_DAY;

    public GridFsByteCache(String host) {
        this.host = host;

        try {
            mongoClient = new MongoClient(host);
        } catch (UnknownHostException e) {
            logger.error("Can't connect to Mongo [host=" + host + "].", e);
            throw new RuntimeException("Can't connect to Mongo [host=" + host + "].", e);
        }

        DB db = mongoClient.getDB(DBNAME);
        fs = new GridFS(db);

        logger.info("Created mongo gridfs connection..");
    }

    @Override
    public boolean validate() {
        try {
            return !mongoClient.getDatabaseNames().isEmpty();
        } catch (MongoException e) {
            logger.error("Can't validate().", e);
            return false;
        }
    }

    private static String getFilename(@Nonnull String section, @Nonnull String key) {
        return section + '/' + key;
    }

    @Override
    public boolean contains(@Nonnull String section, @Nonnull String key) {
        GridFSDBFile file = fs.findOne(getFilename(section, key));
        if (file != null) {
            try {
                long deadlineTime = (Long) file.getMetaData().get("deadlineTime");
                if (System.currentTimeMillis() <= deadlineTime) {
                    return true;
                } else {
                    fs.remove(file);
                }
            } catch (RuntimeException e) {
                logger.error("Can't execute contains().", e);
            }
        }

        return false;
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        put(section, key, value, defaultLifetime);
    }

    private static DBObject newMetaData(@Nonnull byte[] value, long lifetimeMillis) {
        DBObject result = new BasicDBObject();
        result.put("sha1", DigestUtils.sha1Hex(value));
        result.put("deadlineTime", lifetimeMillis == Long.MAX_VALUE ? Long.MAX_VALUE : System.currentTimeMillis() + lifetimeMillis);
        result.put("size", value.length);
        return result;
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        fs.remove(getFilename(section, key));
        GridFSInputFile file = fs.createFile(value);
        file.setFilename(getFilename(section, key));
        file.setMetaData(newMetaData(value, lifetimeMillis));
        file.save();
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        if (!contains(section, key)) {
            put(section, key, value, defaultLifetime);
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
        GridFSDBFile file = fs.findOne(getFilename(section, key));

        if (file == null) {
            return null;
        }

        try {
            long deadlineTime = (Long) file.getMetaData().get("deadlineTime");

            if (System.currentTimeMillis() <= deadlineTime) {
                ByteArrayOutputStream dataOutputStream = new ByteArrayOutputStream(NumberUtil.toInt(file.getLength()));
                file.writeTo(dataOutputStream);
                return dataOutputStream.toByteArray();
            }
        } catch (Exception e) {
            logger.error("Can't get get().", e);
        }

        return null;
    }

    @Override
    public boolean remove(@Nonnull String section, @Nonnull String key) {
        if (contains(section, key)) {
            fs.remove(getFilename(section, key));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clearSection(@Nonnull String section) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        mongoClient.dropDatabase(DBNAME);
        DB db = mongoClient.getDB(DBNAME);
        fs = new GridFS(db);
    }

    @Override
    public void close() {
        mongoClient.close();
    }

    @Override
    public String toString() {
        return "GridFsByteCache {host='" + host + "'}";
    }
}
