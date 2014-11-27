package com.codeforces.commons.io.http;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang.ArrayUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 27.11.14
 */
public final class HttpResponse {
    private final int code;

    @Nullable
    private final byte[] bytes;

    @Nullable
    private final IOException ioException;

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    HttpResponse(int code, @Nullable byte[] bytes, @Nullable IOException ioException) {
        if ((code == -1) == (ioException == null)) {
            throw new IllegalArgumentException(
                    "Argument 'ioException' should be set if and only if argument 'code' is -1."
            );
        }

        this.code = code;
        this.bytes = bytes;
        this.ioException = ioException;
    }

    /**
     * @return HTTP response code or {@code -1} if an exception has occured
     * @see #getIoException()
     */
    public int getCode() {
        return code;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    @Nullable
    public byte[] getBytes() {
        return bytes;
    }

    @Nullable
    public IOException getIoException() {
        return ioException;
    }

    @Nullable
    public String getUtf8String() {
        return bytes == null ? null : new String(bytes, Charsets.UTF_8);
    }

    @Nullable
    public String getString(String charset) throws UnsupportedEncodingException {
        return bytes == null ? null : new String(bytes, charset);
    }

    @Nullable
    public String getString(Charset charset) {
        return bytes == null ? null : new String(bytes, charset);
    }

    @Override
    public String toString() {
        return String.format(
                "Response {code=%d, size=%s, s='%s%s'}",
                code,
                bytes == null ? "null" : Integer.toString(bytes.length),
                bytes == null ? "null" : new String(ArrayUtils.subarray(bytes, 0, 20)),
                bytes != null && bytes.length > 20 ? "..." : ""
        );
    }
}
