package com.codeforces.commons.text;

import org.apache.commons.validator.routines.UrlValidator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 23.11.12
 */
public class UrlUtil {
    private static final String[] ALLOWED_SCHEMES = {"http", "https"};

    private UrlUtil() {
        throw new UnsupportedOperationException();
    }

    public static Set<String> getParameterNames(@Nonnull String url) {
        Set<String> parameterNames = new HashSet<>();

        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException ignored) {
            return parameterNames;
        }

        String rawQuery = uri.getRawQuery();
        if (rawQuery != null) {
            String[] parameters = StringUtil.split(rawQuery, '&');
            int parameterCount = parameters.length;

            for (int parameterIndex = 0; parameterIndex < parameterCount; ++parameterIndex) {
                String parameter = parameters[parameterIndex];
                int equalitySignPos = parameter.indexOf('=');
                String parameterName = equalitySignPos == -1
                        ? parameter.trim()
                        : parameter.substring(0, equalitySignPos).trim();
                if (!parameterName.isEmpty()) {
                    parameterNames.add(parameterName.toLowerCase());
                }
            }
        }

        return parameterNames;
    }

    public static String removeParameterFromUrl(@Nonnull String url, @Nullable String parameterName) {
        if (!isValidUri(url) || StringUtil.isBlank(parameterName)) {
            return url;
        }

        int questionSignPos = url.indexOf('?');
        int sharpPos = url.indexOf('#');

        if (questionSignPos == -1 && sharpPos == -1) {
            return url;
        } else if (questionSignPos == -1 || sharpPos != -1 && questionSignPos > sharpPos) {
            return url;
        } else {
            StringBuilder resultUrl = new StringBuilder(url.substring(0, questionSignPos));

            if (url.length() > questionSignPos + 1) {
                String query = sharpPos == -1
                        ? url.substring(questionSignPos + 1)
                        : url.substring(questionSignPos + 1, sharpPos);

                query = removeParameterFromQuery(query, parameterName);

                if (!StringUtil.isBlank(query)) {
                    resultUrl.append('?').append(query);
                }

                if (sharpPos != -1) {
                    resultUrl.append(url.substring(sharpPos));
                }
            }

            return resultUrl.toString();
        }
    }

    public static String replaceParameterInUrl(
            @Nonnull String url, @Nullable String parameterName, @Nullable String parameterValue) {
        if (!isValidUri(url) || StringUtil.isBlank(parameterName)) {
            return url;
        }

        String parameter = StringUtil.isBlank(parameterValue) ? parameterName : parameterName + '=' + parameterValue;

        int questionSignPos = url.indexOf('?');
        int sharpPos = url.indexOf('#');

        if (questionSignPos == -1 && sharpPos == -1) {
            return url + '?' + parameter;
        } else if (questionSignPos == -1 || sharpPos != -1 && questionSignPos > sharpPos) {
            return url.substring(0, sharpPos) + '?' + parameter + url.substring(sharpPos);
        } else {
            StringBuilder resultUrl = new StringBuilder(url.substring(0, questionSignPos + 1)).append(parameter);

            if (url.length() > questionSignPos + 1) {
                String query = sharpPos == -1
                        ? url.substring(questionSignPos + 1)
                        : url.substring(questionSignPos + 1, sharpPos);

                query = removeParameterFromQuery(query, parameterName);

                if (!StringUtil.isBlank(query)) {
                    resultUrl.append('&').append(query);
                }

                if (sharpPos != -1) {
                    resultUrl.append(url.substring(sharpPos));
                }
            }

            return resultUrl.toString();
        }
    }

    public static String appendParameterToUrl(
            @Nonnull String url, @Nullable String parameterName, @Nullable String parameterValue) {
        if (!isValidUri(url) || StringUtil.isBlank(parameterName)) {
            return url;
        }

        String parameter = StringUtil.isBlank(parameterValue) ? parameterName : parameterName + '=' + parameterValue;

        int questionSignPos = url.indexOf('?');
        int sharpPos = url.indexOf('#');

        if (questionSignPos == -1 && sharpPos == -1) {
            return url + '?' + parameter;
        } else if (questionSignPos == -1 || sharpPos != -1 && questionSignPos > sharpPos) {
            return url.substring(0, sharpPos) + '?' + parameter + url.substring(sharpPos);
        } else {
            StringBuilder resultUrl = new StringBuilder(url.substring(0, questionSignPos + 1)).append(parameter);

            if (url.length() > questionSignPos + 1) {
                resultUrl.append('&').append(url.substring(questionSignPos + 1));
            }

            return resultUrl.toString();
        }
    }

    @SuppressWarnings("OverlyComplexMethod")
    public static String appendParametersToUrl(@Nonnull String url, @Nonnull String... parameterParts) {
        int partCount = parameterParts.length;
        if (!isValidUri(url) || partCount == 0) {
            return url;
        }

        if (partCount % 2 != 0) {
            throw new IllegalArgumentException("Expected even number of parameter parts.");
        }

        int questionSignPos = url.indexOf('?');
        int sharpPos = url.indexOf('#');

        StringBuilder resultUrl;
        String urlAppendix;

        if (questionSignPos == -1 && sharpPos == -1) {
            resultUrl = new StringBuilder(url);
            urlAppendix = "";
        } else if (questionSignPos == -1 || sharpPos != -1 && questionSignPos > sharpPos) {
            resultUrl = new StringBuilder(url.substring(0, sharpPos));
            urlAppendix = url.substring(sharpPos);
        } else {
            resultUrl = new StringBuilder(url.substring(0, questionSignPos));
            urlAppendix = url.length() > questionSignPos + 1 ? '&' + url.substring(questionSignPos + 1) : "";
        }

        boolean firstParameter = true;

        for (int partIndex = 0; partIndex < partCount; partIndex += 2) {
            String parameterName = parameterParts[partIndex];
            if (StringUtil.isBlank(parameterName)) {
                continue;
            }

            String parameterValue = parameterParts[partIndex + 1];

            String parameter = StringUtil.isBlank(parameterValue) ? parameterName : parameterName + '=' + parameterValue;

            resultUrl.append(firstParameter ? '?' : '&').append(parameter);
            firstParameter = false;
        }

        return firstParameter ? url : resultUrl.append(urlAppendix).toString();
    }

    public static String appendRelativePathToUrl(@Nonnull String url, @Nullable String relativePath) {
        if (!isValidUri(url) || StringUtil.isBlank(relativePath)
                || relativePath.length() == 1 && relativePath.charAt(0) == '/') {
            return url;
        }

        int questionSignPos = url.indexOf('?');
        int sharpPos = url.indexOf('#');

        String urlPrefix;
        String urlPostfix;

        if (questionSignPos == -1 && sharpPos == -1) {
            urlPrefix = url;
            urlPostfix = null;
        } else if (questionSignPos == -1 || sharpPos != -1 && questionSignPos > sharpPos) {
            urlPrefix = url.substring(0, sharpPos);
            urlPostfix = url.substring(sharpPos);
        } else {
            urlPrefix = url.substring(0, questionSignPos);
            urlPostfix = url.substring(questionSignPos);
        }

        if (urlPrefix.charAt(urlPrefix.length() - 1) == '/') {
            urlPrefix += relativePath.charAt(0) == '/' ? relativePath.substring(1) : relativePath;
        } else {
            urlPrefix += relativePath.charAt(0) == '/' ? relativePath : '/' + relativePath;
        }

        return urlPostfix == null ? urlPrefix : urlPrefix + urlPostfix;
    }

    public static boolean isValidUrl(@Nullable String url) {
        return isValidUrl(url, ALLOWED_SCHEMES);
    }

    public static boolean isValidUrl(@Nullable String url, String[] allowedSchemes) {
        if (StringUtil.isBlank(url)) {
            return false;
        }

        UrlValidator urlValidator = new UrlValidator(allowedSchemes, UrlValidator.ALLOW_LOCAL_URLS);
        return urlValidator.isValid(url);
    }

    public static boolean isValidUri(@Nullable String uri) {
        if (uri == null) {
            return false;
        }

        try {
            URI.create(uri);
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    @Nullable
    public static String extractFileName(@Nullable String url) {
        if (!isValidUrl(url)) {
            return null;
        }

        try {
            String path = new URL(url).getPath();
            return StringUtil.isBlank(path) ? null : new File(path).getName();
        } catch (MalformedURLException ignored) {
            return null;
        }
    }

    private static String removeParameterFromQuery(@Nullable String query, @Nullable String parameterName) {
        if (StringUtil.isBlank(query) || StringUtil.isBlank(parameterName)) {
            return query;
        }

        String[] parameters = StringUtil.split(query, '&');
        int parameterCount = parameters.length;

        StringBuilder queryBuilder = new StringBuilder(query.length());

        for (int parameterIndex = 0; parameterIndex < parameterCount; ++parameterIndex) {
            String parameter = parameters[parameterIndex];
            if (parameter.startsWith(parameterName + '=') || parameterName.equals(parameter)) {
                continue;
            }

            if (queryBuilder.length() > 0) {
                queryBuilder.append('&');
            }

            queryBuilder.append(parameter);
        }

        return queryBuilder.toString();
    }
}
