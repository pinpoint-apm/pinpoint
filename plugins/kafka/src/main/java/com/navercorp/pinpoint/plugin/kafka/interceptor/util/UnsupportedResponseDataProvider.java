package com.navercorp.pinpoint.plugin.kafka.interceptor.util;

import java.util.Map;

public final class UnsupportedResponseDataProvider implements KafkaResponseDataProvider {
    @Override
    public Map<?, ?> getResponseData(Object fetchResponse) {
        return null;
    }
}
