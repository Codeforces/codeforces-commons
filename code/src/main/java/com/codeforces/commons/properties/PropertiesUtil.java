package com.codeforces.commons.properties;

import com.codeforces.commons.text.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Mike Mirzayanov
 */
@SuppressWarnings("OverloadedVarargsMethod")
public class PropertiesUtil {
    private static final ConcurrentMap<String, Properties> propertiesByResourceName = new ConcurrentHashMap<>();

    private PropertiesUtil() {
        throw new UnsupportedOperationException();
    }

    public static String getProperty(
            boolean throwOnFileReadError, String propertyName, String defaultValue, String... resourceNames) {
        for (String resourceName : resourceNames) {
            try {
                ensurePropertiesByResourceName(resourceName);
            } catch (Exception e) {
                if (throwOnFileReadError) {
                    throw new RuntimeException("Can't read properties from resource '" + resourceName + "'.", e);
                } else {
                    continue;
                }
            }

            Properties properties = propertiesByResourceName.get(resourceName);
            String value = properties.getProperty(propertyName);
            if (value != null) {
                return value;
            }
        }

        return defaultValue;
    }

    public static String getProperty(String propertyName, String defaultValue, String... resourceNames) {
        return getProperty(true, propertyName, defaultValue, resourceNames);
    }

    public static String getPropertyQuietly(String propertyName, String defaultValue, String... resourceNames) {
        return getProperty(false, propertyName, defaultValue, resourceNames);
    }

    public static List<String> getListProperty(
            boolean throwOnFileReadError, String propertyName, String defaultValue, String... resourceNames) {
        String propertyValue = getProperty(throwOnFileReadError, propertyName, defaultValue, resourceNames);
        if (StringUtil.isBlank(propertyValue)) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(Arrays.asList(StringUtil.Patterns.SEMICOLON_PATTERN.split(propertyValue)));
    }

    public static List<String> getListProperty(String propertyName, String defaultValue, String... resourceNames) {
        return getListProperty(true, propertyName, defaultValue, resourceNames);
    }

    public static List<String> getListPropertyQuietly(String propertyName, String defaultValue, String... resourceNames) {
        return getListProperty(false, propertyName, defaultValue, resourceNames);
    }

    private static void ensurePropertiesByResourceName(String resourceName) throws IOException {
        if (!propertiesByResourceName.containsKey(resourceName)) {
            Properties properties = new Properties();
            try (InputStream inputStream = PropertiesUtil.class.getResourceAsStream(resourceName)) {
                properties.load(inputStream);
            }

            propertiesByResourceName.putIfAbsent(resourceName, properties);
        }
    }
}
