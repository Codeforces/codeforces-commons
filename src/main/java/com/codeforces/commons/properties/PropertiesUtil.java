package com.codeforces.commons.properties;

import com.codeforces.commons.resource.CantReadResourceException;
import com.codeforces.commons.text.Patterns;
import com.codeforces.commons.text.StringUtil;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Mike Mirzayanov
 * @author Maxim Shipko (sladethe@gmail.com)
 */
@SuppressWarnings({"OverloadedVarargsMethod", "unused", "WeakerAccess"})
public class PropertiesUtil {
    private static final Logger logger = Logger.getLogger(PropertiesUtil.class);

    private static final ConcurrentMap<String, Properties> propertiesByResourceName = new ConcurrentHashMap<>();

    private PropertiesUtil() {
        throw new UnsupportedOperationException();
    }

    public static String getProperty(boolean throwOnFileReadError, String propertyName,
                                     String defaultValue, String... resourceNames) throws CantReadResourceException {
        for (String resourceName : resourceNames) {
            Properties properties;

            try {
                properties = ensurePropertiesByResourceName(resourceName);
            } catch (IOException e) {
                String message = String.format("Can't read properties from resource '%s'.", resourceName);
                if (throwOnFileReadError) {
                    logger.error(message, e);
                    throw new CantReadResourceException(message, e);
                } else {
                    logger.warn(message, e);
                    continue;
                }
            }

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

        return Collections.unmodifiableList(Arrays.asList(Patterns.SEMICOLON_PATTERN.split(propertyValue)));
    }

    public static List<String> getListProperty(String propertyName, String defaultValue, String... resourceNames) {
        return getListProperty(true, propertyName, defaultValue, resourceNames);
    }

    public static List<String> getListPropertyQuietly(String propertyName, String defaultValue, String... resourceNames) {
        return getListProperty(false, propertyName, defaultValue, resourceNames);
    }

    @Nonnull
    private static Properties ensurePropertiesByResourceName(String resourceName) throws IOException {
        Properties properties = propertiesByResourceName.get(resourceName);

        if (properties == null) {
            properties = new Properties();
            try (InputStream inputStream = PropertiesUtil.class.getResourceAsStream(resourceName)) {
                if (inputStream != null) {
                    try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                        properties.load(inputStreamReader);
                    }
                } else {
                    logger.warn(String.format("Can't find resource file '%s'.", resourceName));
                }
            }
            propertiesByResourceName.putIfAbsent(resourceName, properties);
            properties = propertiesByResourceName.get(resourceName);
        }

        return properties;
    }
}
