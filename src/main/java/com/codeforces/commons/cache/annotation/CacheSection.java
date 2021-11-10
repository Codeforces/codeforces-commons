package com.codeforces.commons.cache.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 24.01.12
 */
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface CacheSection {
}
