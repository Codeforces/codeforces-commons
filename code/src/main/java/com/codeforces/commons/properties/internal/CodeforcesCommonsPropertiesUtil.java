package com.codeforces.commons.properties.internal;

import com.codeforces.commons.properties.PropertiesUtil;

import java.util.List;

/**
 * @author Mike Mirzayanov
 * @author Maxim Shipko (sladethe@gmail.com)
 */
public class CodeforcesCommonsPropertiesUtil {
    private CodeforcesCommonsPropertiesUtil() {
        throw new UnsupportedOperationException();
    }

    public static String getProperty(String propertyName, String defaultValue) {
        return PropertiesUtil.getPropertyQuietly(
                propertyName, defaultValue,
                "/com/codeforces/commons/properties/codeforces_commons.properties",
                "/com/codeforces/commons/properties/codeforces_commons_default.properties"
        );
    }

    public static List<String> getListProperty(String propertyName, String defaultValue) {
        return PropertiesUtil.getListPropertyQuietly(
                propertyName, defaultValue,
                "/com/codeforces/commons/properties/codeforces_commons.properties",
                "/com/codeforces/commons/properties/codeforces_commons_default.properties"
        );
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

    public static String getSubscriptionToken() {
        return PropertyValuesHolder.SUBSCRIPTION_TOKEN;
    }

    private static final class PropertyValuesHolder {
        private static final String TEMP_DIR_NAME = getProperty("temp-dir.name", "temp");
        private static final List<String> SECURE_PASSWORDS = getListProperty("security.secure-passwords", "");
        private static final List<String> SECURE_HOSTS = getListProperty("security.secure-hosts", "");
        private static final String SUBSCRIPTION_TOKEN = getProperty("security.subscription-token", "secret");

        private PropertyValuesHolder() {
            throw new UnsupportedOperationException();
        }
    }
}
