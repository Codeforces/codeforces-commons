package com.codeforces.commons.properties.internal;

import com.codeforces.commons.properties.PropertiesUtil;

import java.util.List;

/**
 * @author Mike Mirzayanov
 * @author Maxim Shipko (sladethe@gmail.com)
 */
public class CommonsPropertiesUtil {
    private static final String[] RESOURCE_NAMES = {
            "/com/codeforces/commons/properties/commons.properties",
            "/com/codeforces/commons/properties/commons_default.properties"
    };

    private CommonsPropertiesUtil() {
        throw new UnsupportedOperationException();
    }

    public static String getProperty(String propertyName, String defaultValue) {
        return PropertiesUtil.getPropertyQuietly(propertyName, defaultValue, RESOURCE_NAMES);
    }

    public static List<String> getListProperty(String propertyName, String defaultValue) {
        return PropertiesUtil.getListPropertyQuietly(propertyName, defaultValue, RESOURCE_NAMES);
    }

    public static String getApplicationTempDirName() {
        return PropertyValuesHolder.TEMP_DIR_NAME;
    }

    public static List<String> getSecurePasswords() {
        return PropertyValuesHolder.SECURE_PASSWORDS;
    }

    public static List<String> getSecureHosts() {
        return PropertyValuesHolder.SECURE_HOSTS;
    }

    public static List<String> getPrivateParameters() {
        return PropertyValuesHolder.PRIVATE_PARAMETERS;
    }

    public static String getSubscriptionToken() {
        return PropertyValuesHolder.SUBSCRIPTION_TOKEN;
    }

    private static final class PropertyValuesHolder {
        private static final String TEMP_DIR_NAME = getProperty("temp-dir.name", "temp");
        private static final List<String> SECURE_PASSWORDS = getListProperty("security.secure-passwords", "");
        private static final List<String> SECURE_HOSTS = getListProperty("security.secure-hosts", "");
        private static final List<String> PRIVATE_PARAMETERS = getListProperty("security.private-parameters", "");
        private static final String SUBSCRIPTION_TOKEN = getProperty("security.subscription-token", "secret");

        private PropertyValuesHolder() {
            throw new UnsupportedOperationException();
        }
    }
}
