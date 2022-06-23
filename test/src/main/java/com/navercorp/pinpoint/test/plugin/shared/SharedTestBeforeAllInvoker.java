package com.navercorp.pinpoint.test.plugin.shared;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author emeroad
 */
public class SharedTestBeforeAllInvoker {
    private final Class<?> testClazz;

    public SharedTestBeforeAllInvoker(Class<?> testClazz) {
        this.testClazz = Objects.requireNonNull(testClazz, "testClazz");
    }


    List<Method> getMethods(Class<?> testClazz, Predicate<Method> predicate) {
        Method[] methods = testClazz.getMethods();
        Stream<Method> stream = Arrays.stream(methods);
        return stream.filter(predicate)
                .collect(Collectors.toList());
    }

    boolean beforeAllFilter(Method method) {
        if (method.getAnnotation(SharedTestBeforeAllResult.class) == null) {
            return false;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            return false;
        }
        return Properties.class.isAssignableFrom(parameterTypes[0]);
    }

    public void invoke(Properties properties) throws Throwable {
        List<Method> methods = getMethods(testClazz, this::beforeAllFilter);
        for (Method method : methods) {
            method.invoke(testClazz, properties);
        }
    }
}
