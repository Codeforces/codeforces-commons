package com.codeforces.commons.reflection;

import com.codeforces.commons.lang.ObjectUtil;
import com.codeforces.commons.text.StringUtil;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 16.01.14
 */
@SuppressWarnings("WeakerAccess")
public class ReflectionUtil {
    private static final ConcurrentMap<Class<?>, Map<String, List<Field>>> fieldsByNameByClass
            = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, Map<MethodSignature, Method>> publicMethodBySignatureByClass
            = new ConcurrentHashMap<>();

    private static final Map<Class<?>, FastClass> fastClassCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, FastMethod>> gettersCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, FastMethod>> settersCache = new ConcurrentHashMap<>();

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

        String[] pathParts = StringUtil.split(propertyName, '.');

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

            Class superclass = clazz.getSuperclass();
            if (superclass != null) {
                fieldsByName.putAll(getFieldsByNameMap(superclass));
            }

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isEnumConstant() || Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }

                field.setAccessible(true);

                Name nameAnnotation = field.getAnnotation(Name.class);
                String fieldName = nameAnnotation == null ? field.getName() : nameAnnotation.value();
                List<Field> fields = fieldsByName.get(fieldName);

                if (fields == null) {
                    fields = new ArrayList<>(1);
                    fields.add(field);
                } else {
                    List<Field> tempFields = fields;
                    fields = new ArrayList<>(tempFields.size() + 1);
                    fields.add(field);
                    fields.addAll(tempFields);
                }

                fieldsByName.put(fieldName, Collections.unmodifiableList(fields));
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

    private static FastClass getFastClass(Class<?> clazz) {
        FastClass result = fastClassCache.get(clazz);

        while (result == null) {
            FastClass fastClass = FastClass.create(clazz);
            fastClassCache.putIfAbsent(clazz, fastClass);
            result = fastClassCache.get(clazz);
        }

        return result;
    }

    private static Map<String, FastMethod> getGettersMap(Class<?> clazz) {
        Map<String, FastMethod> result = gettersCache.get(clazz);

        while (result == null) {
            result = buildGettersMap(clazz);
            gettersCache.putIfAbsent(clazz, result);
            result = gettersCache.get(clazz);
        }

        return result;
    }

    private static Map<String, FastMethod> buildGettersMap(Class<?> clazz) {
        Map<String, FastMethod> result = new HashMap<>();

        FastClass fastClass = getFastClass(clazz);
        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            if (!Modifier.isStatic(method.getModifiers())) {
                String property = getGetterProperty(method);
                if (property != null) {
                    result.put(property, fastClass.getMethod(method));
                }
            }
        }

        return result;
    }

    private static Map<String, FastMethod> getSettersMap(Class<?> clazz) {
        Map<String, FastMethod> result = settersCache.get(clazz);

        while (result == null) {
            result = buildSettersMap(clazz);
            settersCache.putIfAbsent(clazz, result);
            result = settersCache.get(clazz);
        }

        return result;
    }

    private static Map<String, FastMethod> buildSettersMap(Class<?> clazz) {
        Map<String, FastMethod> result = new HashMap<>();

        FastClass fastClass = getFastClass(clazz);
        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            if (!Modifier.isStatic(method.getModifiers())) {
                String property = getSetterProperty(method);
                if (property != null) {
                    result.put(property, fastClass.getMethod(method));
                }
            }
        }

        return result;
    }

    @Nullable
    private static String getGetterProperty(Method method) {
        if (method.getParameterTypes().length > 0 || method.getDeclaringClass() == Object.class) {
            return null;
        }

        String name = method.getName();
        if (name.length() > 3 && name.startsWith("get") && Character.isUpperCase(name.charAt(3))) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        if (name.length() > 2 && name.startsWith("is") && Character.isUpperCase(name.charAt(2))) {
            return Character.toLowerCase(name.charAt(2)) + name.substring(3);
        }

        return null;
    }

    @Nullable
    private static String getSetterProperty(Method method) {
        if (method.getParameterTypes().length != 1 || method.getDeclaringClass() == Object.class) {
            return null;
        }

        String name = method.getName();
        if (name.length() > 3 && name.startsWith("set") && Character.isUpperCase(name.charAt(3))) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }

        return null;
    }

    @SuppressWarnings({"OverlyLongMethod", "OverlyNestedMethod"})
    public static void copyProperties(Object source, Object target) {
        if (ObjectUtil.referenceEquals(source, target)) {
            return;
        }

        if (source == null) {
            throw new NullPointerException("Argument source can't be null (if target is not null).");
        }

        if (target == null) {
            throw new NullPointerException("Argument target can't be null (if source is not null).");
        }

        Map<String, FastMethod> sourceGetters = getGettersMap(source.getClass());
        Map<String, FastMethod> targetSetters = getSettersMap(target.getClass());

        for (Map.Entry<String, FastMethod> getterEntry : sourceGetters.entrySet()) {
            FastMethod getter = getterEntry.getValue();
            FastMethod setter = targetSetters.get(getterEntry.getKey());

            if (setter == null) {
                continue;
            }

            Class<?> getterReturnsClass = getter.getReturnType();
            Class<?> setterExpectsClass = setter.getParameterTypes()[0];

            Object value;
            try {
                value = getter.invoke(source, ArrayUtils.EMPTY_OBJECT_ARRAY);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(String.format(
                        "Can't get property '%s' from %s.", getterEntry.getKey(), source.getClass()
                ), e);
            }

            if (setterExpectsClass.isAssignableFrom(getterReturnsClass)) {
                try {
                    setter.invoke(target, new Object[] {value});
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(String.format(
                            "Can't copy assignable property '%s' from %s to %s.",
                            getterEntry.getKey(), source.getClass(), target.getClass()
                    ), e);
                }
                continue;
            }

            if (setterExpectsClass.isAssignableFrom(String.class)) {
                try {
                    setter.invoke(target, new Object[] {ObjectUtil.mapNotNull(value, Object::toString)});
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(String.format(
                            "Can't copy assignable property '%s' from %s to string assignable property of %s.",
                            getterEntry.getKey(), source.getClass(), target.getClass()
                    ), e);
                }
                continue;
            }

            if (setterExpectsClass.isEnum()) {
                try {
                    if (value == null) {
                        setter.invoke(target, new Object[] {null});
                    } else {
                        String valueString = value.toString();
                        Object[] constants = setterExpectsClass.getEnumConstants();
                        for (Object constant : constants) {
                            if (constant.toString().equals(valueString)) {
                                setter.invoke(target, new Object[] {constant});
                                break;
                            }
                        }
                    }
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(String.format(
                            "Can't copy enum property '%s' from %s to %s.",
                            getterEntry.getKey(), source.getClass(), target.getClass()
                    ), e);
                }
                continue;
            }

            if (value != null) {
                try {
                    Object valueCopy = setterExpectsClass.getConstructor().newInstance();
                    copyProperties(value, valueCopy);
                    setter.invoke(target, new Object[] {valueCopy});
                } catch (Exception e) {
                    throw new RuntimeException(String.format(
                            "Can't copy object property '%s' from %s to %s.",
                            getterEntry.getKey(), source.getClass(), target.getClass()
                    ), e);
                }
            }
        }
    }

    public static void iterateProperties(Object source, BiConsumer<String, Object> nameAndValueConsumer, Set<String> ignoreFieldNames) {
        if (source == null) {
            throw new NullPointerException("Argument source can't be null (if target is not null).");
        }

        Map<String, FastMethod> sourceGetters = getGettersMap(source.getClass());

        for (Map.Entry<String, FastMethod> getterEntry : sourceGetters.entrySet()) {
            if (ignoreFieldNames.contains(getterEntry.getKey())) {
                continue;
            }

            FastMethod getter = getterEntry.getValue();

            Object value;
            try {
                value = getter.invoke(source, ArrayUtils.EMPTY_OBJECT_ARRAY);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(String.format(
                        "Can't get property '%s' from %s.", getterEntry.getKey(), source.getClass()
                ), e);
            }

            if (value != null) {
                nameAndValueConsumer.accept(getterEntry.getKey(), value);
            }
        }
    }

    private ReflectionUtil() {
        throw new UnsupportedOperationException();
    }
}
