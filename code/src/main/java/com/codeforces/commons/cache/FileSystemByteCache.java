package com.codeforces.commons.cache;

import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.text.StringUtil;
import com.codeforces.commons.compress.ZipUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.DataFormatException;

import static com.codeforces.commons.compress.ZipUtil.MAXIMAL_COMPRESSION_LEVEL;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 14.02.11
 */
public class FileSystemByteCache extends ByteCache {
    private static final Logger logger = Logger.getLogger(FileSystemByteCache.class);

    private static final int GROUP_DIR_NAME_LENGTH = 3;
    private static final String TEMP_DIR_NAME = ".tmp";

    private static final ByteOrder CACHE_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
    private static final int BYTES_PER_INTEGER = Integer.SIZE / Byte.SIZE;
    private static final int BYTES_PER_LONG = Long.SIZE / Byte.SIZE;

    private final File directory;
    private final File tempDirectory;
    private final boolean useCompression;

    public FileSystemByteCache(File directory, boolean useCompression) {
        this(directory, useCompression, true);
    }

    public FileSystemByteCache(File directory, boolean useCompression, boolean validateOnCreate) {
        this.directory = directory;
        this.tempDirectory = new File(directory, TEMP_DIR_NAME);
        this.useCompression = useCompression;
        if (validateOnCreate && !validate()) {
            throw new IllegalArgumentException("Can't validate cache.");
        }
    }

    @Override
    public final boolean validate() {
        try {
            FileUtil.ensureDirectoryExists(directory);
            FileUtil.ensureDirectoryExists(tempDirectory);
            return true;
        } catch (IOException e) {
            logger.error("Unexpected exception while validating.", e);
            return false;
        }
    }

    @Override
    public boolean contains(@Nonnull String section, @Nonnull String key) {
        return getValueLocation(section, key).isFile();
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        internalPut(section, key, value, Long.MAX_VALUE, true);
    }

    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        internalPut(section, key, value, lifetimeMillis, true);
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        internalPut(section, key, value, Long.MAX_VALUE, false);
    }

    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        internalPut(section, key, value, lifetimeMillis, false);
    }

    private void internalPut(String section, String key, byte[] value, long lifetimeMillis, boolean overwrite) {
        if (value == null) {
            throw new IllegalArgumentException(String.format(
                    "Argument 'value' can't be 'null' (section='%s', key='%s').", section, key
            ));
        }

        if (lifetimeMillis < 1) {
            throw new IllegalArgumentException(String.format(
                    "Argument 'lifetimeMillis' must be a positive long integer (section='%s', key='%s').", section, key
            ));
        }

        if (!overwrite && contains(section, key)) {
            return;
        }

        File tempFile = null;
        try {
            tempFile = File.createTempFile("cache-", null, tempDirectory);
            writeValueToFile(tempFile, value, lifetimeMillis);
            File storageFile = getValueLocation(section, key);
            FileUtil.renameFile(tempFile, storageFile, overwrite);
        } catch (IOException e) {
            logger.error(String.format(
                    "Got I/O-exception while storing value (section='%s', key='%s') in directory " + tempDirectory + '.',
                    section, key
            ), e);
        } finally {
            if (tempFile != null && tempFile.isFile()) {
                FileUtil.deleteTotallyAsync(tempFile);
            }
        }
    }

    private void writeValueToFile(File file, byte[] value, long lifetimeMillis) throws IOException {
        FileUtil.writeFile(file, packValue(value, lifetimeMillis));
    }

    private byte[] packValue(byte[] valueBytes, long lifetimeMillis) {
        long expirationTimeMillis;
        if (lifetimeMillis == Long.MAX_VALUE) {
            expirationTimeMillis = Long.MAX_VALUE;
        } else {
            expirationTimeMillis = System.currentTimeMillis();
            if (expirationTimeMillis + lifetimeMillis <= expirationTimeMillis) {
                expirationTimeMillis = Long.MAX_VALUE;
            } else {
                expirationTimeMillis += lifetimeMillis;
            }
        }

        byte[] hashBytes = calculateHash(valueBytes);

        ByteBuffer byteBuffer = ByteBuffer
                .allocate(BYTES_PER_INTEGER + hashBytes.length + BYTES_PER_LONG + valueBytes.length)
                .order(CACHE_BYTE_ORDER);

        byte[] bytes = byteBuffer
                .putInt(hashBytes.length)
                .put(hashBytes)
                .putLong(expirationTimeMillis)
                .put(valueBytes)
                .array();

        return useCompression ? ZipUtil.compress(bytes, MAXIMAL_COMPRESSION_LEVEL) : bytes;
    }

    @Nullable
    @Override
    public byte[] get(@Nonnull String section, @Nonnull String key) {
        return internalGet(section, key);
    }

    @Nullable
    private byte[] internalGet(String section, String key) {
        File storageFile = getValueLocation(section, key);
        if (storageFile.isFile()) {
            try {
                return readValueFromFile(storageFile);
            } catch (LifetimeExpiredException ignored) {
                remove(section, key);
                return null;
            } catch (IOException e) {
                logger.error(String.format(
                        "Got I/O-exception while reading value (section='%s', key='%s').", section, key
                ), e);
                remove(section, key);
                return null;
            }
        } else {
            return null;
        }
    }

    private byte[] readValueFromFile(File file) throws IOException, LifetimeExpiredException {
        return extractValue(FileUtil.getBytes(file));
    }

    private byte[] extractValue(byte[] storageBytes) throws IOException, LifetimeExpiredException {
        try {
            byte[] bytes = useCompression ? ZipUtil.decompress(storageBytes) : storageBytes;
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(CACHE_BYTE_ORDER);

            byte[] hashBytes = new byte[byteBuffer.getInt()];
            byteBuffer.get(hashBytes);

            long expirationTimeMillis = byteBuffer.getLong();
            if (System.currentTimeMillis() > expirationTimeMillis) {
                throw new LifetimeExpiredException("Value lifetime has expired.");
            }

            byte[] valueBytes = new byte[bytes.length - BYTES_PER_INTEGER - hashBytes.length - BYTES_PER_LONG];
            byteBuffer.get(valueBytes);

            if (!Arrays.equals(hashBytes, calculateHash(valueBytes))) {
                throw new DataFormatException("Hash validation failed.");
            }

            return valueBytes;
        } catch (LifetimeExpiredException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Can't extract value.", e);
        }
    }

    @Override
    public boolean remove(@Nonnull String section, @Nonnull String key) {
        if (contains(section, key)) {
            File storageFile = getValueLocation(section, key);
            if (storageFile.delete()) {
                return true;
            } else {
                logger.error(String.format(
                        "Can't remove storage file (path='%s', section='%s', key='%s').",
                        storageFile.getPath(), section, key
                ));
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void clearSection(@Nonnull String section) {
        try {
            FileUtil.deleteTotally(getSectionLocation(section));
        } catch (IOException e) {
            logger.error(String.format("Got I/O-exception while clearing section '%s'.", section), e);
        }
    }

    @Override
    public void clear() {
        try {
            for (File file : directory.listFiles()) {
                if (TEMP_DIR_NAME.equalsIgnoreCase(file.getName())) {
                    FileUtil.cleanDirectory(file);
                } else {
                    FileUtil.deleteTotally(file);
                }
            }
        } catch (IOException e) {
            logger.error("Got I/O-exception while clearing cache.", e);
        }
    }

    private static byte[] calculateHash(byte[] value) {
        return DigestUtils.sha1(value);
    }

    @Override
    public void close() {
        // No operations.
    }

    @Override
    public String toString() {
        return StringUtil.toString(this, false, "directory", "useCompression");
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private File getValueLocation(@Nonnull String section, @Nonnull String key) {
        ensureCacheKeyName(key);

        return new File(getSectionLocation(section), new StringBuilder()
                .append(key.length() >= GROUP_DIR_NAME_LENGTH ? key.substring(0, GROUP_DIR_NAME_LENGTH) : '_')
                .append(File.separatorChar).append(key).toString()
        );
    }

    private File getSectionLocation(@Nonnull String section) {
        ensureCacheSectionName(section);
        return new File(directory, section);
    }

    private static final class LifetimeExpiredException extends Exception {
        private LifetimeExpiredException(String message) {
            super(message);
        }
    }
}
