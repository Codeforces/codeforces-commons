package com.codeforces.commons.text;

import java.util.regex.Pattern;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 08.04.14
 */
public final class Patterns {
    public static final Pattern LINE_BREAK_PATTERN = Pattern.compile("\\r\\n|\\r|\\n");
    public static final Pattern PLUS_PATTERN = Pattern.compile("\\+");
    public static final Pattern MINUS_PATTERN = Pattern.compile("\\-");
    public static final Pattern EQ_PATTERN = Pattern.compile("=");
    public static final Pattern LT_PATTERN = Pattern.compile("<");
    public static final Pattern GT_PATTERN = Pattern.compile(">");
    public static final Pattern SPACE_PATTERN = Pattern.compile(" ");
    public static final Pattern NBSP_PATTERN = Pattern.compile("" + (char) 160);
    public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    public static final Pattern THIN_SPACE_PATTERN = Pattern.compile("" + '\u2009');
    public static final Pattern ZERO_WIDTH_SPACE_PATTERN = Pattern.compile("" + '\u200B');
    public static final Pattern TAB_PATTERN = Pattern.compile("\\t");
    public static final Pattern CR_LF_PATTERN = Pattern.compile("\\r\\n");
    public static final Pattern CR_PATTERN = Pattern.compile("\\r");
    public static final Pattern LF_PATTERN = Pattern.compile("\\n");
    public static final Pattern SLASH_PATTERN = Pattern.compile("/");
    public static final Pattern DOT_PATTERN = Pattern.compile("\\.");
    public static final Pattern COMMA_PATTERN = Pattern.compile(",");
    public static final Pattern SEMICOLON_PATTERN = Pattern.compile(";");
    public static final Pattern COLON_PATTERN = Pattern.compile(":");
    public static final Pattern AMP_PATTERN = Pattern.compile("&");

    private Patterns() {
        throw new UnsupportedOperationException();
    }
}
