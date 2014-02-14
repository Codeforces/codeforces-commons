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
    private static final int BUFFER_SIZE = NumberUtil.toInt(8L * FileUtil.BYTES_PER_MB);

    private IoUtil() {
        throw new UnsupportedOperationException();
    }

    public static byte[] sha1(InputStream inputStream) throws IOException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            copy(new DigestInputStream(inputStream, messageDigest), NullOutputStream.NULL_OUTPUT_STREAM);
            inputStream.close();
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            closeQuietly(inputStream);
            throw e;
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
            outputStream.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            closeQuietly(outputStream);
            throw e;
        }
    }

    public static String toString(InputStream inputStream) throws IOException {
        try {
            String s = IOUtils.toString(inputStream);
            inputStream.close();
            return s;
        } catch (IOException e) {
            closeQuietly(inputStream);
            throw new IOException("Can't read from input stream.", e);
        }
    }

    public static String toString(InputStream inputStream, String charsetName) throws IOException {
        try {
            String s = IOUtils.toString(inputStream, charsetName);
            inputStream.close();
            return s;
        } catch (IOException e) {
            closeQuietly(inputStream);
            throw new IOException("Can't read from input stream.", e);
        }
    }

    public static String toString(InputStream inputStream, Charset charset) throws IOException {
        try {
            String s = IOUtils.toString(inputStream, charset);
            inputStream.close();
            return s;
        } catch (IOException e) {
            closeQuietly(inputStream);
            throw new IOException("Can't read from input stream.", e);
        }
    }

    public static String toString(Reader reader) throws IOException {
        try {
            String s = IOUtils.toString(reader);
            reader.close();
            return s;
        } catch (IOException e) {
            closeQuietly(reader);
            throw new IOException("Can't read from reader.", e);
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
            long byteCount = IOUtils.copyLarge(inputStream, outputStream, new byte[BUFFER_SIZE]);
            inputStream.close();
            return byteCount;
        } catch (IOException e) {
            closeQuietly(inputStream);
            throw e;
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
