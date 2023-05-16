package com.navercorp.pinpoint.plugin.kafka;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

public final class KafkaVersion {
    public static final int KAFKA_VERSION_UNKNOWN = -1;
    public static final int KAFKA_VERSION_LOW = 3_0_0;
    public static final int KAFKA_VERSION_3_1 = 3_1_0;

    static final String KAFKA_FETCH_RESPONSE = "org.apache.kafka.common.requests.FetchResponse";
    static final String KAFKA_RESPONSE_DATA = "responseData";
    static final Class<?> KAFKA_LOW_RESPONSE_DATA_ARGS[] = new Class[]{};
    static final Class<?> KAFKA_3_RESPONSE_DATA_ARGS[] = new Class[]{Map.class, short.class};

    static Method responseDataMethod = null;

    public static int getVersion(ClassLoader cl) {
        final Class<?> fetchResponse = getClass(cl, KAFKA_FETCH_RESPONSE);
        if (fetchResponse == null) {
            throw new RuntimeException("Kafka FetchResponse not available");
        }

        final Method responseData3 = getMethod(fetchResponse, KAFKA_RESPONSE_DATA, KAFKA_3_RESPONSE_DATA_ARGS);
        if (responseData3 != null) {
            responseDataMethod = responseData3;
            return KAFKA_VERSION_3_1;
        }

        // kafka 0 ~ 2
        final Method responseData2 = getMethod(fetchResponse, KAFKA_RESPONSE_DATA, KAFKA_LOW_RESPONSE_DATA_ARGS);
        if (responseData2 != null) {
            responseDataMethod = responseData2;
            return KAFKA_VERSION_LOW;
        }

        responseDataMethod = null;
        return KAFKA_VERSION_UNKNOWN;
    }

    public static Method getResponseDataMethod() {
        return responseDataMethod;
    }

    private static Class<?> getClass(ClassLoader classLoader, String className) {
        Objects.requireNonNull(className, "className");
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private static Method getMethod(Class<?> clazz, String methodName, Class<?> args[]) {
        Objects.requireNonNull(methodName, "methodName");
        Objects.requireNonNull(args, "args");
        final Method method;
        try {
            method = clazz.getDeclaredMethod(methodName, args);
        } catch (NoSuchMethodException e) {
            return null;
        }
        return method;
    }
}
