package com.navercorp.pinpoint.plugin.kafka.interceptor.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

public final class Kafka2ResponseDataProvider implements KafkaResponseDataProvider {

    private final Method responseDataMethod;
    public Kafka2ResponseDataProvider(Method responseDataMethod) {
        this.responseDataMethod = Objects.requireNonNull(responseDataMethod, "responseDataMethod");
    }
    @Override
    public Map<?, ?> getResponseData(Object fetchResponse) {
        try {
            return (Map<?, ?>)responseDataMethod.invoke(fetchResponse);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

}
