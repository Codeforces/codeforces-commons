package com.codeforces.commons.io.http;

import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.text.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

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
                : HttpRequest.getDeepUnmodifiableMap(headersByName);
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

    @Nonnull
    public Map<String, List<String>> getHeadersByNameMap() {
        return headersByName == null ? Collections.emptyMap() : headersByName;
    }

    @Nonnull
    public List<String> getHeaders(String headerName) {
        List<String> headers = getHeadersByNameMap().get(headerName);
        return headers == null ? Collections.emptyList() : Collections.unmodifiableList(headers);
    }

    @Nullable
    public String getHeader(String headerName) {
        return getHeader(headerName, false);
    }

    @Nullable
    public String getHeader(String headerName, boolean throwIfMany) {
        return HttpUtil.getHeader(getHeaders(headerName), headerName, throwIfMany);
    }

    @Nullable
    public IOException getIoException() {
        return ioException;
    }

    public boolean hasIoException() {
        return ioException != null;
    }

    public void throwIoException() throws IOException {
        if (ioException != null) {
            throw ioException;
        }
    }

    public void throwIoException(
            @Nonnull Function<IOException, IOException> existingExceptionMapper) throws IOException {
        if (ioException != null) {
            IOException mappedIoException = existingExceptionMapper.apply(ioException);
            if (mappedIoException != null) {
                throw mappedIoException;
            }
        }
    }

    public void throwIoException(
            @Nonnull Supplier<IOException> nullExceptionReplacement) throws IOException {
        if (ioException != null) {
            throw ioException;
        } else {
            IOException replacedIoException = nullExceptionReplacement.get();
            if (replacedIoException != null) {
                throw replacedIoException;
            }
        }
    }

    public void throwIoException(
            @Nonnull Function<IOException, IOException> existingExceptionMapper,
            @Nonnull Supplier<IOException> nullExceptionReplacement) throws IOException {
        if (ioException != null) {
            IOException mappedIoException = existingExceptionMapper.apply(ioException);
            if (mappedIoException != null) {
                throw mappedIoException;
            }
        } else {
            IOException replacedIoException = nullExceptionReplacement.get();
            if (replacedIoException != null) {
                throw replacedIoException;
            }
        }
    }

    @Nullable
    public String getUtf8String() {
        return bytes == null ? null : new String(bytes, UTF_8);
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
                "Response {code=%d, size=%s, s='%s'}",
                code,
                bytes == null ? "null" : FileUtil.formatSize(bytes.length),
                StringUtil.shrinkTo(getUtf8String(), 50)
        );
    }
}
