package com.codeforces.commons.io;

import com.codeforces.commons.math.NumberUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Mike Mirzayanov
 * @author Maxim Shipko (sladethe@gmail.com)
 */
public class IoUtil {
    private static final int BUFFER_SIZE = NumberUtil.toInt(4L * FileUtil.BYTES_PER_MB);

    private IoUtil() {
        throw new UnsupportedOperationException();
    }

    public static byte[] sha1(InputStream inputStream) throws IOException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            copy(new DigestInputStream(inputStream, messageDigest), NullOutputStream.NULL_OUTPUT_STREAM);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } finally {
            inputStream.close();
        }
    }

    public static String sha1Hex(InputStream inputStream) throws IOException {
        return Hex.encodeHexString(sha1(inputStream));
    }

    public static String sha1Base64(InputStream inputStream) throws IOException {
        return Base64.encodeBase64String(sha1(inputStream));
    }

    public static String sha1Base64UrlSafe(InputStream inputStream) throws IOException {
        return Base64.encodeBase64URLSafeString(sha1(inputStream));
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
            copy(inputStream, outputStream);
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
