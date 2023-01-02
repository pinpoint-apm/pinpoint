package com.navercorp.pinpoint.plugin.kafka.interceptor.util;

import java.util.Map;

public interface KafkaResponseDataProvider {
    Map<?, ?> getResponseData(Object fetchResponse);
}
