package com.codeforces.commons.text.similarity;

import com.codeforces.commons.resource.ResourceUtil;
import com.codeforces.commons.text.Patterns;
import com.codeforces.commons.text.StringUtil;
import org.apache.commons.text.similarity.LevenshteinDistance;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

class SimilarityUtil {
    private static final int DEFAULT_LEVENSHTEIN_THRESHOLD = 1;
    private static final LevenshteinDistance defaultLevenshtein = new LevenshteinDistance(DEFAULT_LEVENSHTEIN_THRESHOLD);

    private static final Map<String, Integer> protoRuEn;
    private static final Map<String, Integer> protoEn;

    private static final int[] skipTypes = new int[]{
            Character.CONTROL,
            Character.FORMAT,
            Character.ENCLOSING_MARK,
            Character.LINE_SEPARATOR,
            Character.PARAGRAPH_SEPARATOR
    };

    static boolean levenshteinCheck(String a, String b) {
        int distance = defaultLevenshtein.apply(a, b);
        if (distance < 0) {
            return false;
        }

        return distance < DEFAULT_LEVENSHTEIN_THRESHOLD;
    }

    static String normalizeByRuEnMap(String s) {
        return normalizeByMap(s, protoRuEn);
    }

    static String normalizeByEnMap(String s) {
        return normalizeByMap(s, protoEn);
    }

    private static String normalizeByMap(@Nonnull String s, Map<String, Integer> protoMap) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            String key;
            if (i + 1 < s.length()) {
                char[] charBuf = new char[2];
                charBuf[0] = s.charAt(i);
                charBuf[1] = s.charAt(i + 1);
                key = new String(charBuf);

                if (protoMap.containsKey(key)) {
                    result.appendCodePoint(protoMap.get(key));
                    i++;
                    continue;
                }
            }

            key = String.valueOf(s.charAt(i));
            if (protoMap.containsKey(key)) {
                result.appendCodePoint(protoMap.get(key));
            } else {
                result.append(key);
            }
        }
        return result.toString();
    }

    @SuppressWarnings("unused")
    private static boolean isSkip(int codePoint) {
        int type = Character.getType(codePoint);

        for (int skipType : skipTypes) {
            if (type == skipType) {
                return true;
            }
        }

        return false;
    }

    private static Map<String, Integer> loadSimple(String resourceName) {
        Map<String, Integer> protoMap = new HashMap<>();
        Patterns.LINE_BREAK_PATTERN.splitAsStream(
                ResourceUtil.getResourceAsString(SimilarityUtil.class, resourceName)
        ).filter(StringUtil::isNotBlank).forEach(line -> {
            String[] fields = line.split(" ");
            int proto = fields[0].codePointAt(0);
            for (int i = 1; i < fields.length; i++) {
                protoMap.put(fields[i], proto);
            }
        });

        return protoMap;
    }

    private SimilarityUtil() {}

    static {
        protoRuEn = loadSimple("simpleRuEn.txt");
        protoEn = loadSimple("simpleEn.txt");
    }
}
