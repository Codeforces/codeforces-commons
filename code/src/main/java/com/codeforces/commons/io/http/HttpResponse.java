package com.codeforces.commons.io.http;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang.ArrayUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 27.11.14
 */
public final class HttpResponse {
    private final int code;

    @Nullable
    private final byte[] bytes;

    private final Map<String, List<String>> headersByName;

    @Nullable
    private final IOException ioException;

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    HttpResponse(int code, @Nullable byte[] bytes, @Nullable Map<String, List<String>> headersByName,
                 @Nullable IOException ioException) {
        if ((code == -1) == (ioException == null)) {
            throw new IllegalArgumentException(
                    "Argument 'ioException' should be set if and only if argument 'code' is -1."
            );
        }

        this.code = code;
        this.bytes = bytes;
        this.headersByName = headersByName == null || headersByName.isEmpty()
                ? null
                : new LinkedHashMap<>(headersByName);
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
    public Map<String, List<String>> getHeadersByNameMap() {
        return headersByName == null ? null : Collections.unmodifiableMap(headersByName);
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
                bytes == null ? "null" : new String(ArrayUtils.subarray(bytes, 0, 20), Charsets.UTF_8),
                bytes != null && bytes.length > 20 ? "..." : ""
        );
    }
}
