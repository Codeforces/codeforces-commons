package com.codeforces.commons.text;

import org.apache.commons.validator.routines.UrlValidator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 23.11.12
 */
public class UrlUtil {
    private UrlUtil() {
        throw new UnsupportedOperationException();
    }

    public static String appendParameterToUrl(
            @Nonnull String url, @Nullable String parameterName, @Nullable String parameterValue) {
        if (StringUtil.isBlank(parameterName)) {
            return url;
        }

        String parameter = StringUtil.isBlank(parameterValue) ? parameterName : parameterName + '=' + parameterValue;

        int questionSignPos = url.indexOf('?');
        int sharpPos = url.indexOf('#');

        if (questionSignPos == -1 && sharpPos == -1) {
            return url + '?' + parameter;
        } else if (questionSignPos == -1 || questionSignPos > sharpPos && sharpPos != -1) {
            return url.substring(0, sharpPos) + '?' + parameter + url.substring(sharpPos);
        } else {
            String resultUrl = url.substring(0, questionSignPos + 1) + parameter;
            return url.length() > questionSignPos + 1
                    ? resultUrl + '&' + url.substring(questionSignPos + 1)
                    : resultUrl;
        }
    }

    public static String appendRelativePathToUrl(@Nonnull String url, @Nullable String relativePath) {
        if (StringUtil.isBlank(relativePath) || relativePath.length() == 1 && relativePath.charAt(0) == '/') {
            return url;
        }

        int questionSignPos = url.indexOf('?');
        int sharpPos = url.indexOf('#');

        String urlPrefix;
        String urlPostfix;

        if (questionSignPos == -1 && sharpPos == -1) {
            urlPrefix = url;
            urlPostfix = null;
        } else if (questionSignPos == -1 || questionSignPos > sharpPos && sharpPos != -1) {
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
        String[] allowedSchemes = {"http", "https"};
        return isValidUrl(url, allowedSchemes);
    }

    public static boolean isValidUrl(@Nullable String url, String[] allowedSchemes) {
        if (StringUtil.isBlank(url)) {
            return false;
        }

        UrlValidator urlValidator = new UrlValidator(allowedSchemes, UrlValidator.ALLOW_LOCAL_URLS);
        return urlValidator.isValid(url);
    }

    @Nullable
    public static String extractFileName(@Nullable String url) {
        if (StringUtil.isBlank(url)) {
            return null;
        }

        try {
            String path = new URL(url).getPath();
            return StringUtil.isBlank(path) ? null : new File(path).getName();
        } catch (MalformedURLException ignored) {
            return null;
        }
    }
}
