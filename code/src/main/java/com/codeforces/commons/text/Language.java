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
    POLISH("pl", "pl"),
    CHINESE("zh", "zh"),
    UKRAINIAN("uk", "uk"),
    JAPANESE("ja", "ja"),
    SLOVAK("sk", "sk"),
    BELARUSIAN("be", "be"),
    KOREAN("ko", "ko"),
    DUTCH("nl", "nl"),
    GERMAN("de", "de"),
    HINDI("hi", "hi"),
    CROATIAN("hr", "hr"),
    BULGARIAN("bg", "bg"),
    SPANISH("es", "es"),
    ROMANIAN("ro", "ro"),
    PORTUGUESE("pt", "pt"),
    NORWEGIAN("no", "no"),
    PERSIAN("fa", "fa"),
    VIETNAMESE("vi", "vi"),
    INDONESIAN("id", "id"),
    SWEDISH("sv", "sv"),
    ARABIC("ar", "ar"),
    BENGALI("bn", "bn"),
    FRENCH("fr", "fr"),
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

        Map<String, Language> languageByIetfTagModifiableMap = new HashMap<String, Language>(languageCount);
        Map<String, Language> languageByIso6391CodeModifiableMap = new HashMap<String, Language>(languageCount);

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
