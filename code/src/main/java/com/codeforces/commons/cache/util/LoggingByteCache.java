package com.codeforces.commons.cache.util;

import com.codeforces.commons.cache.ByteCache;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 02.11.13
 */
class LoggingByteCache extends ByteCache {
    private static final Logger logger = Logger.getLogger(LoggingByteCache.class);

    private ByteCache cache;

    @LogPerformance
    @Override
    public boolean validate() {
        return cache.validate();
    }

    @LogPerformance
    @Override
    public boolean contains(@Nonnull String section, @Nonnull String key) {
        return cache.contains(section, key);
    }

    @LogPerformance
    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        cache.put(section, key, value);
    }

    @LogPerformance
    @Override
    public void put(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        cache.put(section, key, value, lifetimeMillis);
    }

    @LogPerformance
    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value) {
        cache.putIfAbsent(section, key, value);
    }

    @LogPerformance
    @Override
    public void putIfAbsent(@Nonnull String section, @Nonnull String key, @Nonnull byte[] value, long lifetimeMillis) {
        cache.putIfAbsent(section, key, value, lifetimeMillis);
    }

    @LogPerformance
    @Nullable
    @Override
    public byte[] get(@Nonnull String section, @Nonnull String key) {
        return cache.get(section, key);
    }

    @LogPerformance
    @Override
    public boolean remove(@Nonnull String section, @Nonnull String key) {
        return cache.remove(section, key);
    }

    @LogPerformance
    @Override
    public void clearSection(@Nonnull String section) {
        cache.clearSection(section);
    }

    @LogPerformance
    @Override
    public void clear() {
        cache.clear();
    }

    @LogPerformance
    @Override
    public void close() {
        cache.close();
    }

    static LoggingByteCache newInstance(ByteCache cache) {
        LoggingByteCache loggingByteCache
                = Guice.createInjector(new LoggingByteCacheModule()).getInstance(LoggingByteCache.class);
        loggingByteCache.cache = cache;
        return loggingByteCache;
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "ObjectToString", "OverlyLongMethod"})
    private static final class LoggingByteCacheModule extends AbstractModule {
        @Override
        protected void configure() {
            bindInterceptor(
                    Matchers.only(LoggingByteCache.class), Matchers.annotatedWith(LogPerformance.class),
                    new MethodInterceptor() {
                        @Override
                        public Object invoke(MethodInvocation invocation) throws Throwable {
                            LoggingByteCache loggingByteCache = (LoggingByteCache) invocation.getThis();
                            String internalCacheAsString = String.valueOf(loggingByteCache.cache);

                            Method method = invocation.getMethod();
                            Class<?>[] parameterClasses = method.getParameterTypes();
                            Object[] parameters = invocation.getArguments();
                            int parameterCount = parameterClasses.length;

                            StringBuilder methodStringBuilder = new StringBuilder()
                                    .append(method.getReturnType().getSimpleName())
                                    .append(' ').append(method.getName()).append('(');
                            StringBuilder parametersStringBuilder = new StringBuilder("(");

                            for (int parameterIndex = 0; parameterIndex < parameterCount; ++parameterIndex) {
                                if (parameterIndex > 0) {
                                    methodStringBuilder.append(", ");
                                    parametersStringBuilder.append(", ");
                                }

                                methodStringBuilder.append(parameterClasses[parameterIndex].getSimpleName());
                                parametersStringBuilder.append(toSimpleString(parameters[parameterIndex]));
                            }
                            methodStringBuilder.append(')');
                            parametersStringBuilder.append(')');

                            String methodAsString = methodStringBuilder.toString();
                            String parametersAsString = parametersStringBuilder.toString();

                            logger.info(String.format(
                                    "%s: started to invoke '%s' with parameters %s.",
                                    internalCacheAsString, methodAsString, parametersAsString
                            ));

                            Object result = null;
                            Throwable exception = null;
                            long startTimeMillis = System.currentTimeMillis();

                            try {
                                result = invocation.proceed();
                            } catch (Throwable e) {
                                exception = e;
                            }

                            long finishTimeMillis = System.currentTimeMillis();

                            if (exception == null) {
                                if (method.getReturnType() == void.class) {
                                    logger.info(String.format(
                                            "%s: finished to invoke '%s' with parameters %s in %d ms.",
                                            internalCacheAsString, methodAsString, parametersAsString,
                                            finishTimeMillis - startTimeMillis
                                    ));
                                } else {
                                    logger.info(String.format(
                                            "%s: finished to invoke '%s' with parameters %s in %d ms. " +
                                                    "Result is: %s.",
                                            internalCacheAsString, methodAsString, parametersAsString,
                                            finishTimeMillis - startTimeMillis, toSimpleString(result)
                                    ));
                                }
                                return result;
                            } else {
                                logger.info(String.format(
                                        "%s: finished to invoke '%s' with parameters %s in %d ms. " +
                                                "Method threw an exception: %s",
                                        internalCacheAsString, methodAsString, parametersAsString,
                                        finishTimeMillis - startTimeMillis, ExceptionUtils.getStackTrace(exception)
                                ));
                                throw exception;
                            }
                        }

                        private String toSimpleString(Object o) {
                            if (o == null) {
                                return "null";
                            }

                            Class<?> objectClass = o.getClass();

                            if (objectClass == Void.class || objectClass == void.class) {
                                return "void";
                            } else if (objectClass.isArray() && objectClass.getComponentType() == byte.class) {
                                return "byte[" + Array.getLength(o) + ']';
                            } else if (objectClass == String.class) {
                                return '\'' + (String) o + '\'';
                            } else if (objectClass == boolean.class || objectClass == long.class
                                    || objectClass == Boolean.class || objectClass == Long.class) {
                                return String.valueOf(o);
                            } else {
                                throw new IllegalArgumentException("Unexpected object: '" + o + "'.");
                            }
                        }
                    }
            );
        }
    }

    @Target({METHOD})
    @Retention(RUNTIME)
    public @interface LogPerformance {
    }
}
