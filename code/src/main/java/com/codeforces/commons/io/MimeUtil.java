package com.codeforces.commons.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Mike Mirzayanov
 */
public class MimeUtil {
    public static final String DEFAULT_MIME_TYPE = Type.TEXT_PLAIN;

    private static final ConcurrentMap<String, String> mimeTypeByExtension = new ConcurrentHashMap<>();

    private MimeUtil() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    public static String getContentTypeByFile(@Nonnull String fileName) {
        return getContentTypeByFile(fileName, DEFAULT_MIME_TYPE);
    }

    @Nullable
    public static String getContentTypeByFile(@Nonnull String fileName, @Nullable String defaultMimeType) {
        String mimeType = mimeTypeByExtension.get(FileUtil.getExt(fileName));
        return mimeType == null ? defaultMimeType : mimeType;
    }

    public static boolean isKnownMimeType(@Nonnull String mimeType) {
        return KnownMimeTypesHolder.KNOWN_MIME_TYPES.contains(mimeType);
    }

    @Nonnull
    public static Set<String> getKnownMimeTypes() {
        return KnownMimeTypesHolder.KNOWN_MIME_TYPES;
    }

    private static final class KnownMimeTypesHolder {
        private static final Set<String> KNOWN_MIME_TYPES;

        static {
            Set<String> knownMimeTypes = new HashSet<>(mimeTypeByExtension.values());
            knownMimeTypes.add(DEFAULT_MIME_TYPE);
            KNOWN_MIME_TYPES = Collections.unmodifiableSet(knownMimeTypes);
        }
    }

    static {
        mimeTypeByExtension.put(".tif", "image/tiff");
        mimeTypeByExtension.put(".tiff", "image/tiff");
        mimeTypeByExtension.put(".gif", "image/gif");
        mimeTypeByExtension.put(".png", "image/png");
        mimeTypeByExtension.put(".jpg", "image/jpeg");
        mimeTypeByExtension.put(".jpe", "image/jpeg");
        mimeTypeByExtension.put(".jpeg", "image/jpeg");
        mimeTypeByExtension.put(".bmp", "image/bmp");

        mimeTypeByExtension.put(".html", Type.TEXT_HTML);
        mimeTypeByExtension.put(".js", "application/x-javascript");
        mimeTypeByExtension.put(".css", Type.TEXT_CSS);
        mimeTypeByExtension.put(".ps", "application/postscript");
        mimeTypeByExtension.put(".xml", Type.APPLICATION_XML);

        mimeTypeByExtension.put(".dot", "application/msword");
        mimeTypeByExtension.put(".doc", "application/msword");
        mimeTypeByExtension.put(".docx", "application/msword");
        mimeTypeByExtension.put(".rtf", "application/rtf");
        mimeTypeByExtension.put(".odt", "application/vnd.oasis.opendocument.text");
        mimeTypeByExtension.put(".pdf", Type.APPLICATION_PDF);
        mimeTypeByExtension.put(".tex", Type.APPLICATION_X_TEX);

        mimeTypeByExtension.put(".exe", "application/octet-stream");
        mimeTypeByExtension.put(".dll", "application/x-msdownload");
        mimeTypeByExtension.put(".zip", Type.APPLICATION_ZIP);
        mimeTypeByExtension.put(".tar", "application/x-tar-compressed");
        mimeTypeByExtension.put(".rar", "application/x-rar-compressed");
        mimeTypeByExtension.put(".7z", "application/x-7z-compressed");

        mimeTypeByExtension.put(".jnlp", "application/x-java-jnlp-file");
    }

    public static final class Type {
        private Type() {
            throw new UnsupportedOperationException();
        }

        public static final String TEXT_PLAIN = "text/plain";

        public static final String TEXT_HTML = "text/html";
        public static final String TEXT_CSS = "text/css";
        public static final String APPLICATION_XML = "application/xml";

        public static final String APPLICATION_PDF = "application/pdf";
        public static final String APPLICATION_X_TEX = "application/x-tex";

        public static final String APPLICATION_ZIP = "application/zip";
    }
}
