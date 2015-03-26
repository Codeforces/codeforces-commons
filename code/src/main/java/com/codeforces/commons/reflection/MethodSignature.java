package com.codeforces.commons.reflection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 26.03.15
 */
public final class MethodSignature {
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
