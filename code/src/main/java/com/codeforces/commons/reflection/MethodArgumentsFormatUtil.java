package com.codeforces.commons.reflection;

import com.codeforces.commons.text.Patterns;
import com.codeforces.commons.text.StringUtil;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Utility class with the only public method {@code public static String format(String pattern, Method method, Object[] args)}.
 * <p>&nbsp;</p>
 * Use it to format string containing expressions like "${user}", "${city.name.length}" and so on,
 * there "user" and "city" and named parameters of the method (see @Name).
 */
public class MethodArgumentsFormatUtil {
    private static final Map<ClassAndProperty, FastMethod> methodByProperty = new ConcurrentHashMap<>();

    private static final Pattern JAVA_IDENTIFIER_PATTERN = Pattern.compile("[a-zA-Z_$][a-zA-Z\\d_$]*");

    /**
     * Formats string containing expressions like "${user}", "${city.name.length}" and so on,
     * there "user" and "city" and named parameters of the method (see @Name).
     *
     * @param pattern Pattern, example "User-${user.id}-${city.code.name}".
     * @param method  java.lang.reflect.Method instance.
     * @param args    Method invocation arguments.
     * @return Formatted pattern.
     */
    public static String format(String pattern, Method method, Object[] args) {
        if (!pattern.contains("$") || !pattern.contains("{") || !pattern.contains("}")) {
            return pattern;
        }

        String[] parameterNames = new String[args.length];
        Object[] namedParameterValues = new Object[args.length];
        int namedParameterCount = 0;

        int index = 0;
        for (Annotation[] annotations : method.getParameterAnnotations()) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof Name) {
                    String name = ((Name) annotation).value();
                    if (StringUtil.isEmpty(name)) {
                        throw new IllegalArgumentException("Method parameter names expected to be non-empty, but " + method + " has.");
                    }
                    parameterNames[namedParameterCount] = name;
                    namedParameterValues[namedParameterCount] = args[index];
                    namedParameterCount++;
                }
            }
            index++;
        }

        StringBuilder result = new StringBuilder(pattern.length());
        int length = pattern.length();

        for (int i = 0; i < length; i++) {
            if (i + 1 < length && pattern.charAt(i) == '$' && pattern.charAt(i + 1) == '{') {
                int end = i + 2;
                while (end < length && pattern.charAt(end) != '}') {
                    end++;
                }

                if (end < length) {
                    result.append(getNamedParameterValue(pattern.substring(i + 2, end), namedParameterCount, parameterNames, namedParameterValues));
                    i = end;
                    continue;
                }
            }

            result.append(pattern.charAt(i));
        }

        return result.toString();
    }

    private static Object getNamedParameterValue(String expression, int namedParameterCount,
                                                 String[] parameterNames, Object[] namedParameterValues) {
        String[] tokens = Patterns.DOT_PATTERN.split(expression);

        if (tokens.length == 0) {
            throw new IllegalArgumentException("Expression `" + expression + "` is not formatted properly.");
        }
        for (String token : tokens) {
            if (token.isEmpty()) {
                throw new IllegalArgumentException("Expression `" + expression + "` is not formatted properly.");
            }
            if (!JAVA_IDENTIFIER_PATTERN.matcher(token).matches()) {
                throw new IllegalArgumentException("Expression `" + expression + "` is not formatted properly.");
            }
        }

        Object object = null;
        boolean found = false;
        for (int i = 0; i < namedParameterCount; i++) {
            if (parameterNames[i].equals(tokens[0])) {
                if (found) {
                    throw new IllegalArgumentException("Parameter names should be unique, but `"
                            + parameterNames[i] + "` seems not to be unique.");
                }
                found = true;
                object = namedParameterValues[i];
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Unable to find parameter named `" + tokens[0] + "` (use @Name).");
        }

        if (object == null) {
            if (tokens.length == 1) {
                return "null";
            } else {
                throw new NullPointerException("Parameter `" + tokens[0] + "` is null and can't be used to get it's property.");
            }
        }

        for (int i = 1; i < tokens.length; i++) {
            if (object == null) {
                throw new NullPointerException("Parameter `" + tokens[0] + "` has null before property `" + tokens[i] + "`.");
            }
            object = getProperty(object, tokens[i]);
        }

        return object;
    }

    private static FastMethod getPropertyMethod(Class<?> clazz, String property) {
        ClassAndProperty classAndProperty = new ClassAndProperty(clazz, property);
        FastMethod method = methodByProperty.get(classAndProperty);

        if (method == null) {
            FastClass cglibClazz = FastClass.create(clazz);

            try {
                method = cglibClazz.getMethod("get" + StringUtils.capitalize(property), ArrayUtils.EMPTY_CLASS_ARRAY);
            } catch (NoSuchMethodError ignored) {
                // No operations.
            }

            if (method == null) {
                try {
                    method = cglibClazz.getMethod("is" + StringUtils.capitalize(property), ArrayUtils.EMPTY_CLASS_ARRAY);
                } catch (NoSuchMethodError ignored) {
                    // No operations.
                }
            }

            if (method == null) {
                try {
                    method = cglibClazz.getMethod(property, ArrayUtils.EMPTY_CLASS_ARRAY);
                } catch (NoSuchMethodError ignored) {
                    // No operations.
                }
            }

            if (method != null) {
                methodByProperty.put(classAndProperty, method);
            } else {
                throw new IllegalArgumentException("Unable to find getter for property `" + property + "` in " + clazz + '.');
            }
        }

        return method;
    }

    private static Object getProperty(Object object, String property) {
        FastMethod method = getPropertyMethod(object.getClass(), property);

        try {
            return method.invoke(object, ArrayUtils.EMPTY_OBJECT_ARRAY);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Unable to call method " + method + '.', e);
        }
    }

    private static final class ClassAndProperty {
        private final Class clazz;
        private final String property;

        private ClassAndProperty(Class clazz, String property) {
            this.clazz = clazz;
            this.property = property;
        }

        @SuppressWarnings("RedundantIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ClassAndProperty classAndProperty = (ClassAndProperty) o;

            return clazz.equals(classAndProperty.clazz)
                    && property.equals(classAndProperty.property);
        }

        @Override
        public int hashCode() {
            int result = clazz.hashCode();
            result = 31 * result + property.hashCode();
            return result;
        }
    }
}

