package com.codeforces.commons.io;

import com.codeforces.commons.math.NumberUtil;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public static String sha1(InputStream inputStream) throws IOException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest);

            IOUtils.copyLarge(digestInputStream, NullOutputStream.NULL_OUTPUT_STREAM, new byte[BUFFER_SIZE]);
            digestInputStream.close();

            byte[] digest = messageDigest.digest();
            return Hex.encodeHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
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
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
        return outputStream.toByteArray();
    }

    public static void transfer(InputStream inputStream, OutputStream outputStream) throws IOException {
        try {
            IOUtils.copyLarge(inputStream, outputStream, new byte[BUFFER_SIZE]);
        } finally {
            inputStream.close();
        }
    }
}
