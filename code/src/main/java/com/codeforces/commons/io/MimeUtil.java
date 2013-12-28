package com.codeforces.commons.io;

import com.codeforces.commons.collection.MapBuilder;
import com.codeforces.commons.collection.SetBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Mirzayanov
 */
public class MimeUtil {
    public static final String DEFAULT_MIME_TYPE = Type.TEXT_PLAIN;

    private static final Map<String, String> MIME_TYPE_BY_EXTENSION = new MapBuilder<String, String>()
            .put(".tif", "image/tiff")
            .put(".tiff", "image/tiff")
            .put(".gif", "image/gif")
            .put(".png", "image/png")
            .put(".jpg", "image/jpeg")
            .put(".jpe", "image/jpeg")
            .put(".jpeg", "image/jpeg")
            .put(".bmp", "image/bmp")

            .put(".html", Type.TEXT_HTML)
            .put(".js", "application/x-javascript")
            .put(".css", Type.TEXT_CSS)
            .put(".ps", "application/postscript")
            .put(".xml", Type.APPLICATION_XML)

            .put(".dot", "application/msword")
            .put(".doc", "application/msword")
            .put(".docx", "application/msword")
            .put(".rtf", "application/rtf")
            .put(".odt", "application/vnd.oasis.opendocument.text")
            .put(".pdf", Type.APPLICATION_PDF)
            .put(".tex", Type.APPLICATION_X_TEX)

            .put(".exe", "application/octet-stream")
            .put(".dll", "application/x-msdownload")
            .put(".zip", Type.APPLICATION_ZIP)
            .put(".tar", "application/x-tar-compressed")
            .put(".rar", "application/x-rar-compressed")
            .put(".7z", "application/x-7z-compressed")

            .put(".jnlp", "application/x-java-jnlp-file")
            .buildUnmodifiable();

    private MimeUtil() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    public static String getContentTypeByFile(@Nonnull String fileName) {
        return getContentTypeByFile(fileName, DEFAULT_MIME_TYPE);
    }

    @Nullable
    public static String getContentTypeByFile(@Nonnull String fileName, @Nullable String defaultMimeType) {
        String mimeType = MIME_TYPE_BY_EXTENSION.get(FileUtil.getExt(fileName));
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
        private static final Set<String> KNOWN_MIME_TYPES = new SetBuilder<String>()
                .addAll(MIME_TYPE_BY_EXTENSION.values())
                .add(DEFAULT_MIME_TYPE)
                .buildUnmodifiable();

        private KnownMimeTypesHolder() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class Type {
        public static final String TEXT_PLAIN = "text/plain";

        public static final String TEXT_HTML = "text/html";
        public static final String TEXT_CSS = "text/css";
        public static final String APPLICATION_XML = "application/xml";

        public static final String APPLICATION_PDF = "application/pdf";
        public static final String APPLICATION_X_TEX = "application/x-tex";

        public static final String APPLICATION_ZIP = "application/zip";

        private Type() {
            throw new UnsupportedOperationException();
        }
    }
}
