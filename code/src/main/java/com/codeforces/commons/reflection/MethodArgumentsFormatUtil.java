package com.codeforces.commons.reflection;

import com.codeforces.commons.text.StringUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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

    private static final LoadingCache<Method, Annotation[][]> parameterAnnotationsCache = CacheBuilder.newBuilder()
            .maximumSize(100000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(
                    new CacheLoader<Method, Annotation[][]>() {
                        @SuppressWarnings("NullableProblems")
                        public Annotation[][] load(@NotNull Method method) {
                            return method.getParameterAnnotations();
                        }
                    });

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
        for (Annotation[] annotations : getParameterAnnotations(method)) {
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
                    result.append(getNamedParameterValue(pattern.substring(i + 2, end),
                            namedParameterCount, parameterNames, namedParameterValues));
                    i = end;
                    continue;
                }
            }

            result.append(pattern.charAt(i));
        }

        return result.toString();
    }

    private static Annotation[][] getParameterAnnotations(Method method) {
        try {
            return parameterAnnotationsCache.get(method);
        } catch (ExecutionException e) {
            return method.getParameterAnnotations();
        }
    }

    private static Object getNamedParameterValue(String expression, int namedParameterCount,
                                                 String[] parameterNames, Object[] namedParameterValues) {
        ExpressionPart[] parts = parseExpression(expression);

        Object object = null;
        boolean found = false;
        for (int i = 0; i < namedParameterCount; i++) {
            if (parameterNames[i].equals(parts[0].getName())) {
                if (found) {
                    throw new IllegalArgumentException("Parameter names should be unique, but `"
                            + parameterNames[i] + "` seems not to be unique.");
                }
                found = true;
                object = namedParameterValues[i];
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Unable to find parameter named `"
                    + parts[0].getName() + "` (use @Name).");
        }

        if (object == null && parts.length == 1) {
            return "null";
        }

        for (int i = 1; i < parts.length; i++) {
            if (object == null) {
                if (parts[i].isNullGuard()) {
                    return null;
                }
                throw new NullPointerException("Parameter `" + parts[0].getName()
                        + "` has null before property `" + parts[i].getName() + "`.");
            }
            object = getProperty(object, parts[i].getName());
        }

        return object;
    }

    private static @NotNull ExpressionPart[] parseExpression(String expression) {
        List<ExpressionPart> parts = new ArrayList<>();

        int prev = 0;
        boolean hadNullGuard = false;
        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '.') {
                parts.add(new ExpressionPart(expression.substring(prev, i), hadNullGuard));
                prev = i + 1;
                hadNullGuard = false;
            } else if (expression.charAt(i) == '?') {
                if (i + 1 >= expression.length() || expression.charAt(i + 1) != '.') {
                    throw new IllegalArgumentException("Expression `" + expression + "` is not formatted properly.");
                }
                parts.add(new ExpressionPart(expression.substring(prev, i), hadNullGuard));
                i++;
                prev = i + 1;
                hadNullGuard = true;
            }
        }
        parts.add(new ExpressionPart(expression.substring(prev), hadNullGuard));

        for (ExpressionPart part : parts) {
            if (part.getName().isEmpty()) {
                throw new IllegalArgumentException("Expression `" + expression + "` is not formatted properly.");
            }
            if (!JAVA_IDENTIFIER_PATTERN.matcher(part.getName()).matches()) {
                throw new IllegalArgumentException("Expression `" + expression + "` is not formatted properly.");
            }
        }

        return parts.toArray(new ExpressionPart[0]);
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
        private final Class<?> clazz;
        private final String property;

        private ClassAndProperty(Class<?> clazz, String property) {
            this.clazz = clazz;
            this.property = property;
        }

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
            result = 32323 * result + property.hashCode();
            return result;
        }
    }

    private static final class ExpressionPart {
        private final String name;
        private final boolean nullGuard;

        public ExpressionPart(String name, boolean nullGuard) {
            this.name = name;
            this.nullGuard = nullGuard;
        }

        public String getName() {
            return name;
        }

        public boolean isNullGuard() {
            return nullGuard;
        }
    }
}

