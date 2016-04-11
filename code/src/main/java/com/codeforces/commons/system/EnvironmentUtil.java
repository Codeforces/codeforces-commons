package com.codeforces.commons.system;

import com.codeforces.commons.text.StringUtil;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 11.04.2016
 */
public final class EnvironmentUtil {
    private static final Pattern WINDOWS_SYSTEM_VARIABLE_PATTERN = Pattern.compile("%[A-Za-z][A-Za-z0-9_]*%");

    @SuppressWarnings("CallToSystemGetenv")
    @Contract("null -> null; !null -> !null")
    @Nullable
    public static String expandSystemVariables(@Nullable String value) {
        if (StringUtil.isBlank(value)) {
            return value;
        }

        StringBuilder expandedValue = new StringBuilder();

        if (SystemUtils.IS_OS_WINDOWS) {
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
        } else {
            throw new NotImplementedException('\'' + SystemUtils.OS_NAME + "' OS is not supported.");
        }
    }

    private EnvironmentUtil() {
        throw new UnsupportedOperationException();
    }
}
