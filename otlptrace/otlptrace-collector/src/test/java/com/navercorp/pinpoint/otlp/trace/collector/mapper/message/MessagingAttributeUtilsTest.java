/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.otlp.trace.collector.mapper.message;

import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceConstants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MessagingAttributeUtilsTest {

    @Test
    void resolveConsumerAcceptorHost_prefersBroker() {
        Map<String, AttributeValue> attributes = attrs(
                OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_ADDRESS, "broker1",
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_CLIENT_ID, "client-1",
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_DESTINATION_NAME, "orders");
        attributes.put(OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_PORT, AttributeValue.of(9092L));

        assertThat(MessagingAttributeUtils.resolveConsumerAcceptorHost(attributes)).isEqualTo("broker1:9092");
    }

    @Test
    void resolveConsumerAcceptorHost_thenClientId() {
        Map<String, AttributeValue> attributes = attrs(
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_CLIENT_ID, "client-1",
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_DESTINATION_NAME, "orders");

        assertThat(MessagingAttributeUtils.resolveConsumerAcceptorHost(attributes)).isEqualTo("client-1");
    }

    @Test
    void resolveConsumerAcceptorHost_thenDestinationName() {
        Map<String, AttributeValue> attributes = attrs(
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_DESTINATION_NAME, "orders");

        assertThat(MessagingAttributeUtils.resolveConsumerAcceptorHost(attributes)).isEqualTo("orders");
    }

    @Test
    void resolveConsumerAcceptorHost_finallyUnknown() {
        Map<String, AttributeValue> attributes = attrs(
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_SYSTEM, "kafka");

        assertThat(MessagingAttributeUtils.resolveConsumerAcceptorHost(attributes))
                .isEqualTo(OtlpTraceConstants.UNKNOWN_ADDRESS)
                .isEqualTo("Unknown");
    }

    @Test
    void resolveEndPoint_staysNullWithoutBrokerOrClientId() {
        // producer SpanEvents share resolveEndPoint and tolerate null; the consumer-side
        // non-null guarantee lives in resolveConsumerAcceptorHost / MessageConsumerRecorder.
        Map<String, AttributeValue> attributes = attrs(
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_DESTINATION_NAME, "orders");

        assertThat(MessagingAttributeUtils.resolveEndPoint(attributes)).isNull();
    }

    private static Map<String, AttributeValue> attrs(String... kv) {
        Map<String, AttributeValue> map = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put(kv[i], AttributeValue.of(kv[i + 1]));
        }
        return map;
    }
}
