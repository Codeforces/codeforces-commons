package com.codeforces.commons.properties.internal;

import com.codeforces.commons.properties.PropertiesUtil;
import com.codeforces.commons.text.StringUtil;

import java.io.File;
import java.util.List;

import javax.annotation.Nullable;

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

    @Nullable
    public static String getApplicationTempDirParent() {
        return PropertyValuesHolder.TEMP_DIR_PARENT;
    }

    public static List<String> getSecurePasswords() {
        return PropertyValuesHolder.SECURE_PASSWORDS;
    }

    public static List<String> getSecureHosts() {
        return PropertyValuesHolder.SECURE_HOSTS;
    }

    public static boolean isBypassCertificateCheck() {
        return PropertyValuesHolder.BYPASS_CERTIFICATE_CHECK;
    }

    public static List<String> getPrivateParameters() {
        return PropertyValuesHolder.PRIVATE_PARAMETERS;
    }

    public static String getSubscriptionToken() {
        return PropertyValuesHolder.SUBSCRIPTION_TOKEN;
    }

    private static final class PropertyValuesHolder {
        private static final String TEMP_DIR_NAME = getProperty("temp-dir.name", "temp");
        @Nullable
        private static final String TEMP_DIR_PARENT = StringUtil.trimToNull(getProperty("temp-dir.parent", null));
        private static final List<String> SECURE_PASSWORDS = getListProperty("security.secure-passwords", "");
        private static final List<String> SECURE_HOSTS = getListProperty("security.secure-hosts", "");
        private static final boolean BYPASS_CERTIFICATE_CHECK
                = Boolean.parseBoolean(getProperty("security.secure-hosts.bypass-certificate-check", "false"));
        private static final List<String> PRIVATE_PARAMETERS = getListProperty("security.private-parameters", "");
        private static final String SUBSCRIPTION_TOKEN = getProperty("security.subscription-token", "secret");

        private PropertyValuesHolder() {
            throw new UnsupportedOperationException();
        }
    }
}
