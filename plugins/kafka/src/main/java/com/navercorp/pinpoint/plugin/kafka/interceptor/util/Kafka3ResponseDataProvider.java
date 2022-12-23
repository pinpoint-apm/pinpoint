package com.navercorp.pinpoint.plugin.kafka.interceptor.util;

import org.apache.kafka.common.requests.FetchResponse;

import java.util.Map;

public final class Kafka3ResponseDataProvider implements KafkaResponseDataProvider {
    @Override
    public Map<?, ?> getResponseData(Object fetchResponse) {
        return ((FetchResponse)fetchResponse).responseData(null, (short)11);
    }
}
