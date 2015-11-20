package com.codeforces.commons.reflection;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 26.03.15
 */
public final class MethodSignature {
    @Nonnull
    private final String name;

    @Nonnull
    private final List<Class<?>> parameterTypes;

    private final int hashCode;

    public MethodSignature(@Nonnull String name, Class<?>... parameterTypes) {
        this.name = name;
        this.parameterTypes = Collections.unmodifiableList(Arrays.asList(parameterTypes));

        int hash = this.name.hashCode();
        hash = 31 * hash + this.parameterTypes.hashCode();
        this.hashCode = hash;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    @Nonnull
    public List<Class<?>> getParameterTypes() {
        return parameterTypes;
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
