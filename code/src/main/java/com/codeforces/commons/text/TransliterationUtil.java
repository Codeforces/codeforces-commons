package com.codeforces.commons.text;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 18.01.12
 */
public final class TransliterationUtil {
    private static final ConcurrentMap<Character, String> transliterationMap = new ConcurrentHashMap<Character, String>();

    private TransliterationUtil() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static String transliterate(@Nullable String s) {
        if (s == null) {
            return null;
        }

        StringBuilder transliteratedString = new StringBuilder();

        for (int charIndex = 0, charCount = s.length(); charIndex < charCount; ++charIndex) {
            char currentChar = s.charAt(charIndex);
            String transliteratedChar = transliterationMap.get(currentChar);
            transliteratedString.append(transliteratedChar == null ? currentChar : transliteratedChar);
        }

        return transliteratedString.toString();
    }

    static {
        transliterationMap.put('А', "A");
        transliterationMap.put('Б', "B");
        transliterationMap.put('В', "V");
        transliterationMap.put('Г', "G");
        transliterationMap.put('Д', "D");
        transliterationMap.put('Е', "Ye");
        transliterationMap.put('Ё', "Yo");
        transliterationMap.put('Ж', "Zh");
        transliterationMap.put('З', "Z");
        transliterationMap.put('И', "I");
        transliterationMap.put('Й', "Y");
        transliterationMap.put('К', "K");
        transliterationMap.put('Л', "L");
        transliterationMap.put('М', "M");
        transliterationMap.put('Н', "N");
        transliterationMap.put('О', "O");
        transliterationMap.put('П', "P");
        transliterationMap.put('Р', "R");
        transliterationMap.put('С', "S");
        transliterationMap.put('Т', "T");
        transliterationMap.put('У', "U");
        transliterationMap.put('Ф', "F");
        transliterationMap.put('Х', "H");
        transliterationMap.put('Ц', "C");
        transliterationMap.put('Ч', "Ch");
        transliterationMap.put('Ш', "Sh");
        transliterationMap.put('Щ', "Sch");
        transliterationMap.put('Ъ', "'");
        transliterationMap.put('Ы', "Y");
        transliterationMap.put('Ь', "'");
        transliterationMap.put('Э', "E");
        transliterationMap.put('Ю', "U");
        transliterationMap.put('Я', "Ya");
        transliterationMap.put('а', "a");
        transliterationMap.put('б', "b");
        transliterationMap.put('в', "v");
        transliterationMap.put('г', "g");
        transliterationMap.put('д', "d");
        transliterationMap.put('е', "ye");
        transliterationMap.put('ё', "yo");
        transliterationMap.put('ж', "zh");
        transliterationMap.put('з', "z");
        transliterationMap.put('и', "i");
        transliterationMap.put('й', "y");
        transliterationMap.put('к', "k");
        transliterationMap.put('л', "l");
        transliterationMap.put('м', "m");
        transliterationMap.put('н', "n");
        transliterationMap.put('о', "o");
        transliterationMap.put('п', "p");
        transliterationMap.put('р', "r");
        transliterationMap.put('с', "s");
        transliterationMap.put('т', "t");
        transliterationMap.put('у', "u");
        transliterationMap.put('ф', "f");
        transliterationMap.put('х', "h");
        transliterationMap.put('ц', "c");
        transliterationMap.put('ч', "ch");
        transliterationMap.put('ш', "sh");
        transliterationMap.put('щ', "sch");
        transliterationMap.put('ъ', "'");
        transliterationMap.put('ы', "y");
        transliterationMap.put('ь', "'");
        transliterationMap.put('э', "e");
        transliterationMap.put('ю', "u");
        transliterationMap.put('я', "ya");
    }
}
