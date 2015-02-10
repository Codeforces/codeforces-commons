package com.codeforces.commons.template;

import com.codeforces.commons.resource.ResourceUtil;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Edvard Davtyan
 */
public class TemplateUtil {
    private static final Version FREEMARKER_VERSION = Configuration.VERSION_2_3_21;

    @SuppressWarnings("unchecked")
    public static String parseResourceTemplate(String resourceName, Object... params) {
        if (params.length == 1 && params[0] instanceof Map) {
            return parseResourceTemplateByMap(resourceName, (Map<String, Object>) params[0]);
        }

        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("Expected params.length % 2 == 0.");
        }

        Map<String, Object> paramsMap = new HashMap<>(params.length / 2);
        for (int index = 0; index < params.length; index += 2) {
            if (!(params[index] instanceof String)) {
                throw new IllegalArgumentException("Parameters with even indexes (keys) must be instances of String," +
                        " but parameter number " + index + " is not.");
            }
            paramsMap.put((String) params[index], params[index + 1]);
        }

        return parseResourceTemplate(resourceName, paramsMap);
    }

    public static String parseResourceTemplateByMap(String resourceName, Map<String, Object> params) {
        String template = ResourceUtil.getResourceAsString(TemplateUtil.class, resourceName);
        return parseTemplate(template, params);
    }

    public static String parseTemplate(String template, Map<String, Object> params) {
        Configuration configuration = new Configuration(FREEMARKER_VERSION);
        configuration.setObjectWrapper(new DefaultObjectWrapper(FREEMARKER_VERSION));

        StringTemplateLoader templateLoader = new StringTemplateLoader();
        templateLoader.putTemplate("template", template);
        configuration.setTemplateLoader(templateLoader);

        try {
            Template templateObj = configuration.getTemplate("template");
            StringWriter out = new StringWriter();
            templateObj.process(params, out);
            out.flush();
            return out.toString();
        } catch (IOException | TemplateException e) {
            throw new IllegalArgumentException("Can't parse template.", e);
        }
    }

    private TemplateUtil() {
        throw new UnsupportedOperationException();
    }
}
