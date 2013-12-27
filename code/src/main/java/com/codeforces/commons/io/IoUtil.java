package com.codeforces.commons.io;

import com.codeforces.commons.math.NumberUtil;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.lang.StrictMath.min;

/**
 * @author Mike Mirzayanov
 */
public class IoUtil {
    private static final int BUFFER_SIZE = NumberUtil.toInt(4L * FileUtil.BYTES_PER_MB);

    private IoUtil() {
        throw new UnsupportedOperationException();
    }

    public static String sha1Hex(InputStream inputStream) throws IOException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            copy(new DigestInputStream(inputStream, messageDigest), NullOutputStream.NULL_OUTPUT_STREAM);
            byte[] digest = messageDigest.digest();
            return Hex.encodeHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } finally {
            inputStream.close();
        }
    }

    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        return toByteArray(inputStream, Integer.MAX_VALUE);
    }

    public static byte[] toByteArray(InputStream inputStream, int maxSize) throws IOException {
        return toByteArray(inputStream, maxSize, true);
    }

    public static byte[] toByteArray(InputStream inputStream, int maxSize, boolean throwIfExceeded) throws IOException {
        ByteArrayOutputStream outputStream = new LimitedByteArrayOutputStream(maxSize, throwIfExceeded);
        try {
            IOUtils.copyLarge(inputStream, outputStream, new byte[min(maxSize, BUFFER_SIZE)]);
        } finally {
            inputStream.close();
            outputStream.close();
        }
        return outputStream.toByteArray();
    }

    public static String toString(InputStream inputStream) throws IOException {
        try {
            return IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new IOException("Can't read from input stream.", e);
        } finally {
            inputStream.close();
        }
    }

    public static String toString(InputStream inputStream, String charsetName) throws IOException {
        try {
            return IOUtils.toString(inputStream, charsetName);
        } catch (IOException e) {
            throw new IOException("Can't read from input stream.", e);
        } finally {
            inputStream.close();
        }
    }

    public static String toString(InputStream inputStream, Charset charset) throws IOException {
        try {
            return IOUtils.toString(inputStream, charset);
        } catch (IOException e) {
            throw new IOException("Can't read from input stream.", e);
        } finally {
            inputStream.close();
        }
    }

    public static String toString(Reader reader) throws IOException {
        try {
            return IOUtils.toString(reader);
        } catch (IOException e) {
            throw new IOException("Can't read from reader.", e);
        } finally {
            reader.close();
        }
    }

    /**
     * @deprecated Use {@link #copy(InputStream, OutputStream)}.
     */
    @Deprecated
    public static void transfer(InputStream inputStream, OutputStream outputStream) throws IOException {
        copy(inputStream, outputStream);
    }

    public static long copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        try {
            return IOUtils.copyLarge(inputStream, outputStream, new byte[BUFFER_SIZE]);
        } finally {
            inputStream.close();
        }
    }

    public static void closeQuietly(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
                // No operations.
            }
        }
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public static void closeQuietly(Closeable... closeables) {
        for (int i = 0, count = closeables.length; i < count; ++i) {
            closeQuietly(closeables[i]);
        }
    }
}
