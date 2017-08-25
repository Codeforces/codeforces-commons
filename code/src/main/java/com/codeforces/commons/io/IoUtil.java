package com.codeforces.commons.io;

import com.codeforces.commons.math.NumberUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.tools.zip.ZipFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.security.*;

/**
 * @author Mike Mirzayanov
 * @author Maxim Shipko (sladethe@gmail.com)
 */
@SuppressWarnings("WeakerAccess")
public class IoUtil {
    public static final int BUFFER_SIZE = NumberUtil.toInt(FileUtil.BYTES_PER_MB);

    private IoUtil() {
        throw new UnsupportedOperationException();
    }

    public static byte[] sha1(InputStream inputStream) throws IOException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            copy(new DigestInputStream(inputStream, messageDigest), NullOutputStream.NULL_OUTPUT_STREAM, true, true);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
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
        copy(inputStream, outputStream, true, true);
        return outputStream.toByteArray();
    }

    @Nonnull
    public static String toString(InputStream inputStream) throws IOException {
        try {
            String s = IOUtils.toString(inputStream, Charset.defaultCharset());
            inputStream.close();
            return s;
        } catch (IOException e) {
            closeQuietly(inputStream);
            throw new IOException("Can't read from input stream.", e);
        }
    }

    @Nonnull
    public static String toString(InputStream inputStream, @Nullable String charsetName) throws IOException {
        try {
            String s = IOUtils.toString(inputStream, charsetName);
            inputStream.close();
            return s;
        } catch (IOException e) {
            closeQuietly(inputStream);
            throw new IOException("Can't read from input stream.", e);
        }
    }

    @Nonnull
    public static String toString(InputStream inputStream, @Nullable Charset charset) throws IOException {
        try {
            String s = IOUtils.toString(inputStream, charset);
            inputStream.close();
            return s;
        } catch (IOException e) {
            closeQuietly(inputStream);
            throw new IOException("Can't read from input stream.", e);
        }
    }

    @Nonnull
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

    public static long copy(InputStream inputStream, OutputStream outputStream,
                            boolean closeInputStream, boolean closeOutputStream, int maxSize) throws IOException {
        try {
            long byteCount = IOUtils.copyLarge(inputStream, outputStream, 0, maxSize, new byte[BUFFER_SIZE]);
            if (closeInputStream) {
                inputStream.close();
            }
            if (closeOutputStream) {
                outputStream.close();
            }
            return byteCount;
        } catch (IOException e) {
            if (closeInputStream) {
                closeQuietly(inputStream);
            }
            if (closeOutputStream) {
                closeQuietly(outputStream);
            }
            throw e;
        }
    }

    public static long copy(InputStream inputStream, OutputStream outputStream,
                            boolean closeInputStream, boolean closeOutputStream) throws IOException {
        return copy(inputStream, outputStream, closeInputStream, closeOutputStream, Integer.MAX_VALUE);
    }

    public static long copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        return copy(inputStream, outputStream, true, false);
    }

    public static boolean contentEquals(@Nonnull InputStream inputA, @Nonnull InputStream inputB) throws IOException {
        if (inputA.equals(inputB)) {
            return true;
        }

        if (!(inputA instanceof BufferedInputStream)) {
            inputA = new BufferedInputStream(inputA, BUFFER_SIZE);
        }

        if (!(inputB instanceof BufferedInputStream)) {
            inputB = new BufferedInputStream(inputB, BUFFER_SIZE);
        }

        int value;

        while ((value = inputA.read()) != IOUtils.EOF) {
            if (value != inputB.read()) {
                return false;
            }
        }

        return inputB.read() == IOUtils.EOF;
    }

    public static void closeQuietly(@Nullable AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // No operations.
            }
        }
    }

    @SuppressWarnings({"OverloadedVarargsMethod", "ForLoopWithMissingComponent"})
    public static void closeQuietly(AutoCloseable... closeables) {
        for (int i = closeables.length; --i >= 0; ) {
            closeQuietly(closeables[i]);
        }
    }

    public static void closeQuietly(Iterable<? extends AutoCloseable> closeables) {
        for (AutoCloseable closeable : closeables) {
            closeQuietly(closeable);
        }
    }

    public static void closeQuietly(@Nullable FTPClient ftpClient) {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (IOException ignored) {
                // No operations.
            }
        }
    }

    public static void closeQuietly(@Nullable ZipFile zipFile) {
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException ignored) {
                // No operations.
            }
        }
    }
}
