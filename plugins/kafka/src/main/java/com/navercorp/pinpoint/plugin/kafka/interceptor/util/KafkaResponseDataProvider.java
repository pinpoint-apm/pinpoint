package com.navercorp.pinpoint.plugin.kafka.interceptor.util;

import org.apache.kafka.common.requests.FetchResponse;

import java.util.Map;

public interface KafkaResponseDataProvider {
    Map getResponseData(Object fetchResponse);
}
