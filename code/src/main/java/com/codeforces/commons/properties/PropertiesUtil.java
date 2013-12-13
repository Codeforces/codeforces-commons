package com.codeforces.commons.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Mike Mirzayanov
 */
@SuppressWarnings("OverloadedVarargsMethod")
public class PropertiesUtil {
    private static final ConcurrentMap<String, Properties> propertiesByResourceName = new ConcurrentHashMap<String, Properties>();

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
                    throw new RuntimeException("Can't read properties from resource \"" + resourceName + "\".", e);
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

    private static void ensurePropertiesByResourceName(String resourceName) throws IOException {
        if (!propertiesByResourceName.containsKey(resourceName)) {
            Properties properties = new Properties();
            InputStream inputStream = PropertiesUtil.class.getResourceAsStream(resourceName);
            properties.load(inputStream);
            inputStream.close();

            propertiesByResourceName.putIfAbsent(resourceName, properties);
        }
    }
}
