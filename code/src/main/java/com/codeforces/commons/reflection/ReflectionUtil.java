package com.codeforces.commons.reflection;

import com.codeforces.commons.text.StringUtil;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 16.01.14
 */
public class ReflectionUtil {
    private static final ConcurrentMap<Class<?>, Map<String, List<Field>>> fieldsByNameByClass
            = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, Map<MethodSignature, Method>> publicMethodBySignatureByClass
            = new ConcurrentHashMap<>();

    @Nullable
    public static <T> Object getDeepValue(@Nonnull T object, String propertyName) {
        return getDeepValue(object, propertyName, false, false, false);
    }

    /**
     * Returns deep value of object's property specified by name.
     * You can use long dot-separated queries to get properties of inner objects.
     * For example: innerObject.innerInnerObject.someProperty, innerList.3, innerCollection.-25, innerMap.someKey.
     *
     * @param object                To get property of.
     * @param propertyName          Object's property name.
     * @param ignoreGetters         {@code false} iff method should find and invoke {@link Method getters} to get value
     *                              (in case if {@link Field field} is not found).
     * @param ignoreMapEntries      {@code false} iff method should try to get value from {@link Map map} (in case
     *                              if {@link Method getter} is not found or {@code ignoreGetters} is {@code true}).
     * @param ignoreCollectionItems {@code false} iff method should try to get value from {@link Collection collection}
     *                              (in case if deep object is not {@link Map map} or {@code ignoreMapEntries}
     *                              is {@code true}).
     * @param <T>                   Type of specified object.
     * @return Value of object's property.
     */
    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "ChainOfInstanceofChecks"})
    @Nullable
    public static <T> Object getDeepValue(
            @Nonnull T object, String propertyName,
            boolean ignoreGetters, boolean ignoreMapEntries, boolean ignoreCollectionItems) {
        Object deepValue = null;
        Object deepObject = object;

        String[] pathParts = splitPath(propertyName);

        for (int partIndex = 0, partCount = pathParts.length; partIndex < partCount; ++partIndex) {
            String pathPart = pathParts[partIndex];
            if (StringUtil.isBlank(pathPart)) {
                throw new IllegalArgumentException("Field name can not be neither 'null' nor blank.");
            }

            boolean gotValue = false;

            List<Field> fields = getFieldsByNameMap(deepObject.getClass()).get(pathPart);
            if (fields != null && !fields.isEmpty()) {
                deepValue = getFieldValue(fields.get(0), deepObject);
                gotValue = true;
            }

            if (!gotValue && !ignoreGetters) {
                Method getter = findPublicGetter(pathPart, deepObject.getClass());
                try {
                    if (getter != null) {
                        deepValue = getter.invoke(deepObject);
                        gotValue = true;
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("This exception is unexpected because method should be public.", e);
                } catch (InvocationTargetException e) {
                    if (e.getTargetException() instanceof RuntimeException) {
                        throw (RuntimeException) e.getTargetException();
                    } else {
                        throw new IllegalStateException("This type of exception is unexpected.", e);
                    }
                }
            }

            if (!gotValue && !ignoreMapEntries) {
                if (deepObject instanceof Map) {
                    deepValue = ((Map) deepObject).get(pathPart);
                    gotValue = true;
                }
            }

            if (!gotValue && !ignoreCollectionItems) {
                try {
                    int itemIndex = Integer.parseInt(pathPart);

                    if (deepObject instanceof List) {
                        List list = (List) deepObject;
                        deepValue = list.get(itemIndex < 0 ? list.size() + itemIndex : itemIndex);
                        gotValue = true;
                    } else if (deepObject instanceof Collection) {
                        Collection collection = (Collection) deepObject;
                        Iterator iterator = collection.iterator();

                        if (itemIndex < 0) {
                            itemIndex += collection.size();
                        }

                        for (int i = 0; i <= itemIndex; ++i) {
                            deepValue = iterator.next();
                        }

                        gotValue = true;
                    }
                } catch (NumberFormatException ignored) {
                    // No operations.
                }
            }

            if (!gotValue) {
                throw new IllegalArgumentException(String.format(
                        "Can't find '%s' in %s.", pathPart, deepObject.getClass()
                ));
            }

            if (deepValue == null) {
                break;
            }

            deepObject = deepValue;
        }

        return deepValue;
    }

    @Nullable
    public static Method findPublicGetter(@Nonnull String propertyName, @Nonnull Class<?> clazz) {
        Map<MethodSignature, Method> publicMethodBySignature = getPublicMethodBySignatureMap(clazz);
        String capitalizedPropertyName = StringUtils.capitalize(propertyName);

        Method getter = publicMethodBySignature.get(new MethodSignature("is" + capitalizedPropertyName));
        if (getter != null && getter.getReturnType() == boolean.class && throwsOnlyRuntimeExceptions(getter)) {
            return getter;
        }

        getter = publicMethodBySignature.get(new MethodSignature("get" + capitalizedPropertyName));
        if (getter != null && getter.getReturnType() != void.class && getter.getReturnType() != Void.class
                && throwsOnlyRuntimeExceptions(getter)) {
            return getter;
        }

        getter = publicMethodBySignature.get(new MethodSignature(propertyName));
        if (getter != null && getter.getReturnType() != void.class && getter.getReturnType() != Void.class
                && throwsOnlyRuntimeExceptions(getter)) {
            return getter;
        }

        return null;
    }

    @Nonnull
    public static Map<String, List<Field>> getFieldsByNameMap(@Nonnull Class clazz) {
        Map<String, List<Field>> fieldsByName = fieldsByNameByClass.get(clazz);

        if (fieldsByName == null) {
            fieldsByName = new LinkedHashMap<>();
            Class tempClass = clazz;

            while (tempClass != null) {
                for (Field field : tempClass.getDeclaredFields()) {
                    if (field.isEnumConstant() || Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                        continue;
                    }

                    Name nameAnnotation = field.getAnnotation(Name.class);
                    String fieldName = nameAnnotation == null ? field.getName() : nameAnnotation.value();
                    List<Field> fields = fieldsByName.get(fieldName);

                    if (fields == null) {
                        fields = new ArrayList<>(1);
                    } else {
                        List<Field> tempFields = fields;
                        fields = new ArrayList<>(tempFields.size() + 1);
                        fields.addAll(tempFields);
                    }

                    field.setAccessible(true);
                    fields.add(field);
                    fieldsByName.put(fieldName, Collections.unmodifiableList(fields));
                }

                tempClass = tempClass.getSuperclass();
            }

            fieldsByNameByClass.putIfAbsent(clazz, Collections.unmodifiableMap(fieldsByName));
            return fieldsByNameByClass.get(clazz);
        } else {
            return fieldsByName;
        }
    }

    private static boolean throwsOnlyRuntimeExceptions(@Nonnull Method method) {
        for (Class<?> exceptionClass : method.getExceptionTypes()) {
            if (!RuntimeException.class.isAssignableFrom(exceptionClass)) {
                return false;
            }
        }

        return true;
    }

    @Nonnull
    public static Collection<Method> getPublicMethods(@Nonnull Class clazz) {
        return getPublicMethodBySignatureMap(clazz).values();
    }

    @Nonnull
    public static Map<MethodSignature, Method> getPublicMethodBySignatureMap(@Nonnull Class clazz) {
        Map<MethodSignature, Method> publicMethodBySignature = publicMethodBySignatureByClass.get(clazz);

        if (publicMethodBySignature == null) {
            Method[] methods = clazz.getMethods();
            int methodCount = methods.length;

            publicMethodBySignature = new LinkedHashMap<>(methodCount);

            for (int methodIndex = 0; methodIndex < methodCount; ++methodIndex) {
                Method method = methods[methodIndex];
                Name nameAnnotation = method.getAnnotation(Name.class);
                String methodName = nameAnnotation == null ? method.getName() : nameAnnotation.value();
                method.setAccessible(true);
                publicMethodBySignature.put(new MethodSignature(methodName, method.getParameterTypes()), method);
            }

            publicMethodBySignatureByClass.putIfAbsent(clazz, Collections.unmodifiableMap(publicMethodBySignature));
            return publicMethodBySignatureByClass.get(clazz);
        } else {
            return publicMethodBySignature;
        }
    }

    @Nullable
    private static Object getFieldValue(@Nonnull Field field, @Nonnull Object object) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            Name nameAnnotation = field.getAnnotation(Name.class);
            String fieldName = nameAnnotation == null ? field.getName() : nameAnnotation.value();
            throw new IllegalArgumentException("Can't get value of inaccessible field '" + fieldName + "'.", e);
        }
    }

    private static String[] splitPath(@Nonnull String propertyName) {
        char[] chars = propertyName.toCharArray();
        int length = chars.length;
        int dotCount = 0;

        for (int i = 0; i < length; ++i) {
            if (chars[i] == '.') {
                ++dotCount;
            }
        }

        if (dotCount == 0) {
            return new String[]{propertyName};
        }

        String[] pathParts = new String[dotCount + 1];

        int previousDotPosition = -1;
        int partIndex = 0;

        for (int i = 0; i < length; ++i) {
            if (chars[i] == '.') {
                pathParts[partIndex++] = new String(chars, previousDotPosition + 1, i - previousDotPosition - 1);
                previousDotPosition = i;
            }
        }

        pathParts[partIndex] = new String(chars, previousDotPosition + 1, length - previousDotPosition - 1);

        return pathParts;
    }

    private ReflectionUtil() {
        throw new UnsupportedOperationException();
    }

    public static final class MethodSignature {
        private final String name;
        private final List<Class<?>> parameterTypes;
        private final int hashCode;

        public MethodSignature(String name, Class<?>... parameterTypes) {
            this.name = name;
            this.parameterTypes = Arrays.asList(parameterTypes);

            int hash = this.name.hashCode();
            hash = 31 * hash + this.parameterTypes.hashCode();
            this.hashCode = hash;
        }

        public String getName() {
            return name;
        }

        public List<Class<?>> getParameterTypes() {
            return Collections.unmodifiableList(parameterTypes);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            MethodSignature methodSignature = (MethodSignature) o;

            return name.equals(methodSignature.name)
                    && parameterTypes.equals(methodSignature.parameterTypes);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
