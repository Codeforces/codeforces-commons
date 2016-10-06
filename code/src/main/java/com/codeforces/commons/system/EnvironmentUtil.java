package com.codeforces.commons.system;

import com.codeforces.commons.text.StringUtil;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 11.04.2016
 */
@SuppressWarnings({"WeakerAccess", "CallToSystemGetenv"})
public final class EnvironmentUtil {
    private static final Pattern WINDOWS_SYSTEM_VARIABLE_PATTERN = Pattern.compile("%[A-Za-z][A-Za-z0-9_]*%");
    private static final Pattern NX_SYSTEM_VARIABLE_PATTERN = Pattern.compile("\\$[A-Za-z_][A-Za-z0-9_]*");

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static String expandSystemVariablesQuietly(@Nullable String value) {
        try {
            return expandSystemVariables(value);
        } catch (RuntimeException ignored) {
            return value;
        }
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static String expandSystemVariables(@Nullable String value) {
        if (StringUtil.isBlank(value)) {
            return value;
        }

        if (SystemUtils.IS_OS_WINDOWS) {
            return expandWindowsStyleSystemVariables(value);
        } else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_UNIX || SystemUtils.IS_OS_FREE_BSD
                || SystemUtils.IS_OS_MAC_OSX) {
            return expandNxStyleSystemVariables(value);
        } else {
            throw new NotImplementedException('\'' + SystemUtils.OS_NAME + "' OS is not supported.");
        }
    }

    @Nonnull
    static String expandWindowsStyleSystemVariables(@Nonnull String value) {
        StringBuilder expandedValue = new StringBuilder();
        Matcher systemVariableMatcher = WINDOWS_SYSTEM_VARIABLE_PATTERN.matcher(value);
        int previousEnd = 0;

        while (systemVariableMatcher.find()) {
            int start = systemVariableMatcher.start();
            int end = systemVariableMatcher.end();

            if (start > previousEnd) {
                expandedValue.append(value.substring(previousEnd, start));
            }

            String variableName = value.substring(start + 1, end - 1);
            String variableValue = StringUtil.trimToNull(System.getenv(variableName));

            if (variableValue == null) {
                expandedValue.append('%').append(variableName).append('%');
            } else {
                expandedValue.append(variableValue);
            }

            previousEnd = end;
        }

        return expandedValue.append(value.substring(previousEnd)).toString();
    }

    @Nonnull
    static String expandNxStyleSystemVariables(@Nonnull String value) {
        StringBuilder expandedValue = new StringBuilder();
        Matcher systemVariableMatcher = NX_SYSTEM_VARIABLE_PATTERN.matcher(value);
        int previousEnd = 0;

        while (systemVariableMatcher.find()) {
            int start = systemVariableMatcher.start();
            int end = systemVariableMatcher.end();

            if (start > previousEnd) {
                expandedValue.append(value.substring(previousEnd, start));
            }

            String variableName = value.substring(start + 1, end);
            String variableValue = StringUtil.trimToNull(System.getenv(variableName));

            if (start > 0 && (
                    end < value.length() && value.charAt(start - 1) == '\'' && value.charAt(end) == '\''
                            || value.charAt(start - 1) == '\\'
            )) {
                expandedValue.append('$').append(variableName);
            } else if (variableValue == null) {
                // No operations.
            } else {
                expandedValue.append(variableValue);
            }

            previousEnd = end;
        }

        return expandedValue.append(value.substring(previousEnd)).toString();
    }

    private EnvironmentUtil() {
        throw new UnsupportedOperationException();
    }
}
