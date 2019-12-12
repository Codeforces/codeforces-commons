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
            .put(".json", Type.APPLICATION_JSON)

            .put(".dot", "application/msword")
            .put(".doc", "application/msword")
            .put(".docx", "application/msword")
            .put(".rtf", "application/rtf")
            .put(".odt", "application/vnd.oasis.opendocument.text")
            .put(".pdf", Type.APPLICATION_PDF)
            .put(".tex", Type.APPLICATION_X_TEX)
            .put(".csv", "text/csv")
            .put(".mp", "text/x-metapost")
            .put(".tex", "application/x-latex")

            .put(".exe", Type.APPLICATION_OCTET_STREAM)
            .put(".dll", "application/x-msdownload")
            .put(".zip", Type.APPLICATION_ZIP)
            .put(".tar", "application/x-tar-compressed")
            .put(".rar", "application/x-rar-compressed")
            .put(".7z", "application/x-7z-compressed")

            .put(".jnlp", "application/x-java-jnlp-file")
            .put(".bat", "application/bat")
            .put(".sh", "application/x-sh")

            .put(".avi", "video/x-msvideo")
            .put(".wmv", "video/x-ms-wmv")
            .put(".mov", "video/quicktime")
            .put(".3gp", "video/3gpp")
            .put(".mp4", "video/mp4")
            .put(".flv", "video/x-flv")
            .put(".mpeg", "video/mpeg")
            .put(".ogg", "video/ogg")
            .put(".webm", "video/webm")

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
        public static final String TEXT_X_METAPOST = "text/x-metapost";

        public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

        public static final String APPLICATION_XML = "application/xml";
        public static final String APPLICATION_JSON = "application/json";
        public static final String APPLICATION_JAVASCRIPT = "application/javascript";

        public static final String APPLICATION_PDF = "application/pdf";
        public static final String APPLICATION_POSTSCRIPT = "application/postscript";
        public static final String APPLICATION_MSWORD = "application/msword";
        public static final String APPLICATION_RTF = "application/rtf";
        public static final String APPLICATION_X_TEX = "application/x-tex";
        public static final String APPLICATION_X_LATEX = "application/x-latex";

        public static final String APPLICATION_ZIP = "application/zip";
        public static final String APPLICATION_X_7Z_COMPRESSED = "application/x-7z-compressed";

        public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

        public static final String IMAGE_JPEG = "image/jpeg";
        public static final String IMAGE_PNG = "image/png";
        public static final String IMAGE_GIF = "image/gif";
        public static final String IMAGE_TIFF = "image/tiff";

        public static final String VIDEO_X_MSVIDEO = "video/x-msvideo";
        public static final String VIDEO_AVI = "video/x-msvideo";
        public static final String VIDEO_X_MS_WMV = "video/x-ms-wmv";
        public static final String VIDEO_MOV = "video/quicktime";
        public static final String VIDEO_QUICKTIME = "video/quicktime";
        public static final String VIDEO_3GP = "video/3gpp";
        public static final String VIDEO_3GPP = "video/3gpp";
        public static final String VIDEO_MP4 = "video/mp4";
        public static final String VIDEO_FLV = "video/x-flv";
        public static final String VIDEO_MPEG = "video/mpeg";
        public static final String VIDEO_OGG = "video/ogg";
        public static final String VIDEO_WEBM = "video/webm";
        public static final String VIDEO_WMV = "video/x-ms-wmv";

        private Type() {
            throw new UnsupportedOperationException();
        }
    }
}
