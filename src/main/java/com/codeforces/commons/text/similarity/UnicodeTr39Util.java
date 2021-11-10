package com.codeforces.commons.text.similarity;

import com.codeforces.commons.resource.ResourceUtil;
import com.codeforces.commons.text.Patterns;
import com.codeforces.commons.text.StringUtil;

import javax.annotation.Nonnull;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

class UnicodeTr39Util {
    private static final Map<Integer, CharSequence> codePointToProto = new HashMap<>();

    @SuppressWarnings("unused")
    public static boolean isConfusable(@Nonnull String a, @Nonnull String b) {
        return skeleton(a).equals(skeleton(b));
    }

    public static String skeleton(@Nonnull String s) {
        String nfd = Normalizer.normalize(s, Normalizer.Form.NFD);
        StringBuilder proto = new StringBuilder();

        nfd.chars().forEach(codePoint -> {
            CharSequence protoSeq = codePointToProto.get(codePoint);
            if (protoSeq != null) {
                proto.append(protoSeq);
            } else {
                proto.appendCodePoint(codePoint);
            }
        });

        return Normalizer.normalize(proto, Normalizer.Form.NFD);
    }

    private static void loadUnicodeConfusables() {
        Patterns.LINE_BREAK_PATTERN.splitAsStream(
                ResourceUtil.getResourceAsString(UnicodeTr39Util.class, "unicodeTr39Confusables.txt")
        ).filter(StringUtil::isNotBlank).forEach(line -> {
            if (line.startsWith("#") || line.startsWith("\uFEFF")) {
                return;
            }

            String[] fields = line.split(";");
            String fromCodePointHex = fields[0].replace(" ", "").replace("\t", "");
            String[] toCodePointsHex = fields[1].replace("\t", "").split(" ");

            int fromCodePoint = codePointIntFromHex(fromCodePointHex);
            int[] toCodePoints = new int[toCodePointsHex.length];
            for (int i = 0; i < toCodePointsHex.length; i++) {
                toCodePoints[i] = codePointIntFromHex(toCodePointsHex[i]);
            }

            String proto = findProto(toCodePoints).toString();
            codePointToProto.put(fromCodePoint, proto);
        });
    }

    private static StringBuilder findProto(int[] codePoints) {
        StringBuilder result = new StringBuilder();
        for (int codePoint : codePoints) {
            CharSequence proto = codePointToProto.get(codePoint);
            if (proto == null) {
                result.append(new String(Character.toChars(codePoint)));
            } else {
                proto = findProto(proto.codePoints().toArray());
                result.append(proto);
                codePointToProto.put(codePoint, proto);
            }
        }
        return result;
    }

    private static int codePointIntFromHex(String hex) {
        return Integer.parseInt(hex, 16);
    }

    private UnicodeTr39Util() {}

    static {
        loadUnicodeConfusables();
    }
}
