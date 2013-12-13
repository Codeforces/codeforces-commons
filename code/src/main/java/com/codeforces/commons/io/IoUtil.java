package com.codeforces.commons.io;

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

/**
 * @author Mike Mirzayanov
 */
public class IoUtil {
    private IoUtil() {
        throw new UnsupportedOperationException();
    }

    public static String sha1(InputStream inputStream) throws IOException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest);

            IOUtils.copy(digestInputStream, NullOutputStream.NULL_OUTPUT_STREAM);
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
            IOUtils.copy(inputStream, outputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
        return outputStream.toByteArray();
    }

    public static void transfer(InputStream inputStream, OutputStream outputStream) throws IOException {
        try {
            IOUtils.copy(inputStream, outputStream);
        } finally {
            inputStream.close();
        }
    }
}
