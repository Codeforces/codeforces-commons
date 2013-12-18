package com.codeforces.commons.cache;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 29.12.12
 */
final class CachePath {
    private final String section;
    private final String key;

    CachePath(String section, String key) {
        this.section = section;
        this.key = key;
    }

    public String getSection() {
        return section;
    }

    public String getKey() {
        return key;
    }
}
