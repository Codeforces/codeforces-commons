package com.codeforces.commons.text;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 21.07.11
 */
public enum Language {
    ENGLISH("en", "en"),
    RUSSIAN("ru", "ru"),

    AFRIKAANS("af", "af"),
    ARABIC("ar", "ar"),
    ARMENIAN("hy", "hy"),
    AZERBAIJANI("az", "az"),
    BELARUSIAN("be", "be"),
    BENGALI("bn", "bn"),
    BOSNIAN("bs", "bs"),
    BULGARIAN("bg", "bg"),
    CATALAN("ca", "ca"),
    CHINESE("zh", "zh"),
    CROATIAN("hr", "hr"),
    CZECH("cs", "cs"),
    DANISH("da", "da"),
    DUTCH("nl", "nl"),
    ESTONIAN("et", "et"),
    FILIPINO("tl", "tl"),
    FINNISH("fi", "fi"),
    FRENCH("fr", "fr"),
    GEORGIAN("ka", "ka"),
    GERMAN("de", "de"),
    GREEK("el", "el"),
    HEBREW("he", "he"),
    HINDI("hi", "hi"),
    HUNGARIAN("hu", "hu"),
    ICELANDIC("is", "is"),
    INDONESIAN("id", "id"),
    IRISH("ga", "ga"),
    ITALIAN("it", "it"),
    JAPANESE("ja", "ja"),
    KAZAKH("kk", "kk"),
    KOREAN("ko", "ko"),
    KYRGYZ("ky", "ky"),
    LATVIAN("lv", "lv"),
    LITHUANIAN("lt", "lt"),
    MACEDONIAN("mk", "mk"),
    MALAY("ms", "ms"),
    MONGOLIAN("mn", "mn"),
    NORWEGIAN("no", "no"),
    PERSIAN("fa", "fa"),
    POLISH("pl", "pl"),
    PORTUGUESE("pt", "pt"),
    ROMANIAN("ro", "ro"),
    SERBIAN("sr", "sr"),
    SINHALA("si", "si"),
    SLOVAK("sk", "sk"),
    SLOVENE("sl", "sl"),
    SPANISH("es", "es"),
    SWEDISH("sv", "sv"),
    TAJIK("tg", "tg"),
    TAMIL("ta", "ta"),
    THAI("th", "th"),
    TURKISH("tr", "tr"),
    TURKMEN("tk", "tk"),
    UKRAINIAN("uk", "uk"),
    URDU("ur", "ur"),
    UZBEK("uz", "uz"),
    VIETNAMESE("vi", "vi"),

    OTHER(null, null);

    private static final Map<String, Language> languageByIetfTag;
    private static final Map<String, Language> languageByIso6391Code;

    private final String ietfTag;
    private final String iso6391Code;

    Language(String ietfTag, String iso6391Code) {
        this.ietfTag = ietfTag;
        this.iso6391Code = iso6391Code;
    }

    /**
     * @return IETF tag or {@code null} if no tag exists for this language
     */
    @Nullable
    public String getIetfTag() {
        return ietfTag;
    }

    /**
     * @return 2-letter ISO code or {@code null} if no code exists for this language
     */
    @Nullable
    public String getIso6391Code() {
        return iso6391Code;
    }

    @Nullable
    public static Language getLanguageByIetfTag(@Nullable String ietfTag) {
        if (ietfTag == null) {
            return OTHER;
        }

        return languageByIetfTag.get(ietfTag);
    }

    @Nullable
    public static Language getLanguageByIso6391Code(@Nullable String iso6391Code) {
        if (iso6391Code == null) {
            return OTHER;
        }

        return languageByIso6391Code.get(iso6391Code);
    }

    static {
        Language[] languages = Language.values();
        int languageCount = languages.length;

        Map<String, Language> languageByIetfTagModifiableMap = new HashMap<>(languageCount);
        Map<String, Language> languageByIso6391CodeModifiableMap = new HashMap<>(languageCount);

        for (int languageIndex = 0; languageIndex < languageCount; ++languageIndex) {
            Language language = languages[languageIndex];
            if (language != OTHER) {
                languageByIetfTagModifiableMap.put(language.ietfTag, language);
                languageByIso6391CodeModifiableMap.put(language.iso6391Code, language);
            }
        }

        languageByIetfTag = Collections.unmodifiableMap(languageByIetfTagModifiableMap);
        languageByIso6391Code = Collections.unmodifiableMap(languageByIso6391CodeModifiableMap);
    }
}
