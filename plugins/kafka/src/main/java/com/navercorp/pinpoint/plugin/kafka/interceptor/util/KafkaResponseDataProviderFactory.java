package com.navercorp.pinpoint.plugin.kafka.interceptor.util;

import com.navercorp.pinpoint.plugin.kafka.KafkaVersion;

import java.lang.reflect.Method;

public final class KafkaResponseDataProviderFactory {

    public static KafkaResponseDataProvider getResponseDataProvider(int version, Method responseDataMethod) {
        switch (version) {
            case KafkaVersion.KAFKA_VERSION_LOW:
                return new Kafka2ResponseDataProvider(responseDataMethod);
            case KafkaVersion.KAFKA_VERSION_3_1:
                return new Kafka3ResponseDataProvider();
            default:
                return new UnsupportedResponseDataProvider();
        }
    }

}
