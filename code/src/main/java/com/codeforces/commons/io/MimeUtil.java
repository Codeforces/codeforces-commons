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
            .put(".tif", Type.IMAGE_TIFF)
            .put(".tiff", Type.IMAGE_TIFF)
            .put(".gif", Type.IMAGE_GIF)
            .put(".png", Type.IMAGE_PNG)
            .put(".jpg", Type.IMAGE_JPEG)
            .put(".jpe", Type.IMAGE_JPEG)
            .put(".jpeg", Type.IMAGE_JPEG)
            .put(".bmp", Type.IMAGE_BMP)

            .put(".html", Type.TEXT_HTML)
            .put(".js", Type.APPLICATION_JAVASCRIPT)
            .put(".css", Type.TEXT_CSS)
            .put(".ps", Type.APPLICATION_POSTSCRIPT)
            .put(".xml", Type.APPLICATION_XML)
            .put(".json", Type.APPLICATION_JSON)

            .put(".dot", Type.APPLICATION_MSWORD)
            .put(".doc", Type.APPLICATION_MSWORD)
            .put(".docx", Type.APPLICATION_MSWORD)
            .put(".rtf", Type.APPLICATION_RTF)
            .put(".odt", "application/vnd.oasis.opendocument.text")
            .put(".pdf", Type.APPLICATION_PDF)
            .put(".tex", Type.APPLICATION_X_TEX)
            .put(".csv", "text/csv")
            .put(".mp", Type.TEXT_X_METAPOST)

            .put(".exe", Type.APPLICATION_OCTET_STREAM)
            .put(".dll", "application/x-msdownload")
            .put(".zip", Type.APPLICATION_ZIP)
            .put(".tar", "application/x-tar-compressed")
            .put(".rar", "application/x-rar-compressed")
            .put(".7z", Type.APPLICATION_X_7Z_COMPRESSED)

            .put(".jnlp", "application/x-java-jnlp-file")
            .put(".bat", "application/bat")
            .put(".sh", "application/x-sh")

            .put(".avi", Type.VIDEO_X_MSVIDEO)
            .put(".wmv", Type.VIDEO_X_MS_WMV)
            .put(".mov", Type.VIDEO_QUICKTIME)
            .put(".3gp", Type.VIDEO_3GPP)
            .put(".mp4", Type.VIDEO_MP4)
            .put(".flv", Type.VIDEO_FLV)
            .put(".mpeg", Type.VIDEO_MPEG)
            .put(".ogg", Type.VIDEO_OGG)
            .put(".webm", Type.VIDEO_WEBM)

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

        public static final String APPLICATION_ZIP = "application/zip";
        public static final String APPLICATION_X_7Z_COMPRESSED = "application/x-7z-compressed";

        public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

        public static final String IMAGE_JPEG = "image/jpeg";
        public static final String IMAGE_PNG = "image/png";
        public static final String IMAGE_GIF = "image/gif";
        public static final String IMAGE_TIFF = "image/tiff";
        public static final String IMAGE_BMP = "image/bmp";

        public static final String VIDEO_X_MSVIDEO = "video/x-msvideo";
        public static final String VIDEO_AVI = "video/x-msvideo";
        public static final String VIDEO_WMV = "video/x-ms-wmv";
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

        private Type() {
            throw new UnsupportedOperationException();
        }
    }
}
