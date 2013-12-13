package com.codeforces.commons.resource;

import com.codeforces.commons.exception.CantReadResourceException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * @author Edvard Davtyan
 */
public class ResourceUtil {
    public static String getResourceAsString(Class clazz, String resourceName) {
        InputStream resourceInputStream = clazz.getResourceAsStream(resourceName);
        if (resourceInputStream == null) {
            throw new CantReadResourceException("Can't find resource " + resourceName + " for " + clazz + '.');
        }

        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(resourceInputStream, writer, "UTF-8");
            return writer.toString();
        } catch (IOException e) {
            throw new CantReadResourceException("Can't read resource " + resourceName + " for " + clazz + '.', e);
        } finally {
            IOUtils.closeQuietly(resourceInputStream);
        }
    }
}
