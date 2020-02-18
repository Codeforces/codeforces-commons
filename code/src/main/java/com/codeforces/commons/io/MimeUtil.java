package com.codeforces.commons.io;

import com.codeforces.commons.collection.MapBuilder;
import com.codeforces.commons.collection.SetBuilder;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Mirzayanov
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class MimeUtil {
    public static final String DEFAULT_MIME_TYPE = Type.TEXT_PLAIN;

    private static final Map<String, String> MIME_TYPE_BY_EXTENSION = new MapBuilder<String, String>()
            .put(".7z", Type.APPLICATION_X_7Z_COMPRESSED)
            .put(".abw", Type.APPLICATION_X_ABIWORD)
            .put(".arc", Type.APPLICATION_OCTET_STREAM)
            .put(".azw", "application/vnd.amazon.ebook")
            .put(".bat", Type.APPLICATION_BAT)
            .put(".bin", Type.APPLICATION_OCTET_STREAM)
            .put(".bz", Type.APPLICATION_X_BZIP)
            .put(".bz2", Type.APPLICATION_X_BZIP2)
            .put(".csh", Type.APPLICATION_X_CSH)
            .put(".dll", Type.APPLICATION_X_MSDOWNLOAD)
            .put(".doc", Type.APPLICATION_MSWORD)
            .put(".docm", "application/vnd.ms-word.document.macroEnabled.12")
            .put(".docx", Type.APPLICATION_MSWORD)
            .put(".dot", Type.APPLICATION_MSWORD)
            .put(".dotm", "application/vnd.ms-word.template.macroEnabled.12")
            .put(".dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template")
            .put(".eot", "application/vnd.ms-fontobject")
            .put(".epub", Type.APPLICATION_EPUB_ZIP)
            .put(".exe", Type.APPLICATION_OCTET_STREAM)
            .put(".jar", Type.APPLICATION_JAVA_ARCHIVE)
            .put(".jnlp", Type.APPLICATION_X_JAVA_JNLP_FILE)
            .put(".js", Type.APPLICATION_X_JAVASCRIPT)
            .put(".json", Type.APPLICATION_JSON)
            .put(".mdb", "application/vnd.ms-access")
            .put(".mpkg", "application/vnd.apple.installer+xml")
            .put(".odp", "application/vnd.oasis.opendocument.presentation")
            .put(".ods", "application/vnd.oasis.opendocument.spreadsheet")
            .put(".odt", "application/vnd.oasis.opendocument.text")
            .put(".ogx", Type.APPLICATION_OGG)
            .put(".pdf", Type.APPLICATION_PDF)
            .put(".pot", "application/vnd.ms-powerpoint")
            .put(".potm", "application/vnd.ms-powerpoint.template.macroEnabled.12")
            .put(".potx", "application/vnd.openxmlformats-officedocument.presentationml.template")
            .put(".ppa", "application/vnd.ms-powerpoint")
            .put(".ppam", "application/vnd.ms-powerpoint.addin.macroEnabled.12")
            .put(".pps", "application/vnd.ms-powerpoint")
            .put(".ppsm", "application/vnd.ms-powerpoint.slideshow.macroEnabled.12")
            .put(".ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow")
            .put(".ppt", "application/vnd.ms-powerpoint")
            .put(".pptm", "application/vnd.ms-powerpoint.presentation.macroEnabled.12")
            .put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation")
            .put(".ps", Type.APPLICATION_POSTSCRIPT)
            .put(".rar", Type.APPLICATION_X_RAR_COMPRESSED)
            .put(".rtf", Type.APPLICATION_RTF)
            .put(".sh", Type.APPLICATION_X_SH)
            .put(".swf", Type.APPLICATION_X_SHOCKWAVE_FLASH)
            .put(".tar", Type.APPLICATION_X_TAR_COMPRESSED)
            .put(".tex", Type.APPLICATION_X_TEX)
            .put(".vsd", "application/vnd.visio")
            .put(".xhtml", Type.APPLICATION_XHTML_XML)
            .put(".xla", "application/vnd.ms-excel")
            .put(".xlam", "application/vnd.ms-excel.addin.macroEnabled.12")
            .put(".xls", "application/vnd.ms-excel")
            .put(".xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12")
            .put(".xlsm", "application/vnd.ms-excel.sheet.macroEnabled.12")
            .put(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .put(".xlt", "application/vnd.ms-excel")
            .put(".xltm", "application/vnd.ms-excel.template.macroEnabled.12")
            .put(".xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template")
            .put(".xml", Type.APPLICATION_XML)
            .put(".xul", "application/vnd.mozilla.xul+xml")
            .put(".zip", Type.APPLICATION_ZIP)

            .put(".bmp", Type.IMAGE_BMP)
            .put(".gif", Type.IMAGE_GIF)
            .put(".ico", Type.IMAGE_X_ICON)
            .put(".jpe", Type.IMAGE_JPEG)
            .put(".jpeg", Type.IMAGE_JPEG)
            .put(".jpg", Type.IMAGE_JPEG)
            .put(".png", Type.IMAGE_PNG)
            .put(".svg", Type.IMAGE_SVG_XML)
            .put(".tif", Type.IMAGE_TIFF)
            .put(".tiff", Type.IMAGE_TIFF)
            .put(".webp", Type.IMAGE_WEBP)

            .put(".aac", Type.AUDIO_AAC)
            .put(".mid", Type.AUDIO_MIDI)
            .put(".midi", Type.AUDIO_MIDI)
            .put(".oga", Type.AUDIO_OGG)
            .put(".wav", Type.AUDIO_X_WAV)
            .put(".weba", Type.AUDIO_WEBM)

            .put(".3gp", Type.VIDEO_3GPP)
            .put(".avi", Type.VIDEO_X_MSVIDEO)
            .put(".flv", Type.VIDEO_X_FLV)
            .put(".mov", Type.VIDEO_QUICKTIME)
            .put(".mp4", Type.VIDEO_MP4)
            .put(".mpeg", Type.VIDEO_MPEG)
            .put(".ogv", Type.VIDEO_OGG)
            .put(".ts", "video/vnd.dlna.mpeg-tts")
            .put(".webm", Type.VIDEO_WEBM)
            .put(".wmv", Type.VIDEO_X_MS_WMV)

            .put(".css", Type.TEXT_CSS)
            .put(".csv", Type.TEXT_CSV)
            .put(".htm", Type.TEXT_HTML)
            .put(".html", Type.TEXT_HTML)
            .put(".ics", Type.TEXT_CALENDAR)
            .put(".md", Type.TEXT_MARKDOWN)
            .put(".mp", Type.TEXT_X_METAPOST)
            .put(".txt", Type.TEXT_PLAIN)

            .put(".otf", Type.FONT_OTF)
            .put(".ttf", Type.FONT_TTF)
            .put(".woff", Type.FONT_WOFF)
            .put(".woff2", Type.FONT_WOFF2)

            .buildUnmodifiable();

    private MimeUtil() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    public static String getContentTypeByFile(@Nonnull String fileName) {
        return getContentTypeByFile(fileName, DEFAULT_MIME_TYPE);
    }

    @Nullable
    @Contract("_, !null -> !null")
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
        public static final String APPLICATION_XML = "application/xml";
        public static final String APPLICATION_JSON = "application/json";
        public static final String APPLICATION_JAVASCRIPT = "application/javascript";
        public static final String APPLICATION_POSTSCRIPT = "application/postscript";
        public static final String APPLICATION_PDF = "application/pdf";
        public static final String APPLICATION_X_TEX = "application/x-tex";
        public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
        public static final String APPLICATION_ZIP = "application/zip";
        public static final String APPLICATION_MSWORD = "application/msword";
        public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
        public static final String APPLICATION_X_7Z_COMPRESSED = "application/x-7z-compressed";
        public static final String APPLICATION_X_ABIWORD = "application/x-abiword";
        public static final String APPLICATION_BAT = "application/bat";
        public static final String APPLICATION_X_BZIP = "application/x-bzip";
        public static final String APPLICATION_X_BZIP2 = "application/x-bzip2";
        public static final String APPLICATION_X_CSH = "application/x-csh";
        public static final String APPLICATION_X_MSDOWNLOAD = "application/x-msdownload";
        public static final String APPLICATION_EPUB_ZIP = "application/epub+zip";
        public static final String APPLICATION_JAVA_ARCHIVE = "application/java-archive";
        public static final String APPLICATION_X_JAVA_JNLP_FILE = "application/x-java-jnlp-file";
        public static final String APPLICATION_X_JAVASCRIPT = "application/x-javascript";
        public static final String APPLICATION_OGG = "application/ogg";
        public static final String APPLICATION_X_RAR_COMPRESSED = "application/x-rar-compressed";
        public static final String APPLICATION_RTF = "application/rtf";
        public static final String APPLICATION_X_SH = "application/x-sh";
        public static final String APPLICATION_X_SHOCKWAVE_FLASH = "application/x-shockwave-flash";
        public static final String APPLICATION_X_TAR_COMPRESSED = "application/x-tar-compressed";
        public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";

        public static final String IMAGE_BMP = "image/bmp";
        public static final String IMAGE_GIF = "image/gif";
        public static final String IMAGE_X_ICON = "image/x-icon";
        public static final String IMAGE_JPEG = "image/jpeg";
        public static final String IMAGE_PNG = "image/png";
        public static final String IMAGE_SVG_XML = "image/svg+xml";
        public static final String IMAGE_TIFF = "image/tiff";
        public static final String IMAGE_WEBP = "image/webp";

        public static final String AUDIO_AAC = "audio/aac";
        public static final String AUDIO_MIDI = "audio/midi";
        public static final String AUDIO_OGG = "audio/ogg";
        public static final String AUDIO_X_WAV = "audio/x-wav";
        public static final String AUDIO_WEBM = "audio/webm";

        public static final String VIDEO_3GPP = "video/3gpp";
        public static final String VIDEO_X_MSVIDEO = "video/x-msvideo";
        public static final String VIDEO_X_FLV = "video/x-flv";
        public static final String VIDEO_QUICKTIME = "video/quicktime";
        public static final String VIDEO_MP4 = "video/mp4";
        public static final String VIDEO_MPEG = "video/mpeg";
        public static final String VIDEO_OGG = "video/ogg";
        public static final String VIDEO_WEBM = "video/webm";
        public static final String VIDEO_X_MS_WMV = "video/x-ms-wmv";

        public static final String TEXT_PLAIN = "text/plain";
        public static final String TEXT_HTML = "text/html";
        public static final String TEXT_CSS = "text/css";
        public static final String TEXT_CSV = "text/csv";
        public static final String TEXT_CALENDAR = "text/calendar";
        public static final String TEXT_X_METAPOST = "text/x-metapost";
        public static final String TEXT_MARKDOWN = "text/markdown";

        public static final String FONT_OTF = "font/otf";
        public static final String FONT_TTF = "font/ttf";
        public static final String FONT_WOFF = "font/woff";
        public static final String FONT_WOFF2 = "font/woff2";

        private Type() {
            throw new UnsupportedOperationException();
        }
    }
}
