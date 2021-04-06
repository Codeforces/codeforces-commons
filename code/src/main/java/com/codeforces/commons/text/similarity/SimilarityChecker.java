package com.codeforces.commons.text.similarity;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class SimilarityChecker implements Predicate<String> {
    private final String sampleNormalized;
    private final Mode mode;

    public SimilarityChecker(@Nonnull String sample, Mode mode) {
        sampleNormalized = mode.normalize(sample);
        this.mode = mode;
    }

    @Override
    public boolean test(String s) {
        return mode.isSimilar(sampleNormalized, mode.normalize(s));
    }

    public static boolean isSimilar(@Nonnull String a, @Nonnull String b, Mode mode) {
        return new SimilarityChecker(a, mode).test(b);
    }

    public enum Mode {
        UNICODE_TR_39(UnicodeTr39Util::skeleton, Objects::equals),
        SIMPLE_EN(SimilarityUtil::normalizeByEnMap, SimilarityUtil::levenshteinCheck),
        SIMPLE_RU_EN(SimilarityUtil::normalizeByRuEnMap, SimilarityUtil::levenshteinCheck);

        private final Function<String, String> normalizer;
        private final BiPredicate<String, String> normsChecker;

        Mode(Function<String, String> normalizer, BiPredicate<String, String> normsChecker) {
            this.normalizer = normalizer;
            this.normsChecker = normsChecker;
        }

        public String normalize(String s) {
            return normalizer.apply(s);
        }

        public boolean isSimilar(String a, String b) {
            return normsChecker.test(a, b);
        }
    }
}
