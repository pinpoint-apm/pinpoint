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

package com.navercorp.pinpoint.it.plugin.kafka;

import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTraceField;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import test.pinpoint.plugin.kafka.TestConsumerRecordEntryPoint;

import java.lang.reflect.Method;
import java.util.List;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static test.pinpoint.plugin.kafka.KafkaITConstants.KAFKA_CLIENT_INTERNAL_SERVICE_TYPE;
import static test.pinpoint.plugin.kafka.KafkaITConstants.KAFKA_CLIENT_SERVICE_TYPE;
import static test.pinpoint.plugin.kafka.KafkaITConstants.MAX_TRACE_WAIT_TIME;
import static test.pinpoint.plugin.kafka.KafkaITConstants.PARTITION;

/**
 * @author Younsung Hwang
 */
public class KafkaClientITBase {

    // per record: KafkaProducer.send() event + consumer root span + entry point event
    private static final int TRACES_PER_RECORD = 3;

    /**
     * Every record is produced once and consumed once, each consumption being its own transaction.
     */
    public static void verifyProducerSend(String brokerUrl, String topic, List<Long> offsets) throws NoSuchMethodException {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTraceCount(offsets.size() * TRACES_PER_RECORD, 100, MAX_TRACE_WAIT_TIME);
        verifier.printCache();

        ExpectedTrace producerSendExpected = producerSend(brokerUrl, topic);
        for (int i = 0; i < offsets.size(); i++) {
            verifier.verifyDiscreteTrace(producerSendExpected);
        }

        ExpectedTrace entryPointExpected = entryPoint(ConsumerRecord.class);
        for (Long offset : offsets) {
            verifier.verifyDiscreteTrace(consumerInvocation(brokerUrl, topic, offset), entryPointExpected);
        }

        verifier.verifyTraceCount(0);
    }

    public static void verifySingleConsumerEntryPoint(String brokerUrl, String topic, long offset) throws NoSuchMethodException {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTraceCount(TRACES_PER_RECORD, 100, MAX_TRACE_WAIT_TIME);
        verifier.printCache();

        verifier.verifyDiscreteTrace(producerSend(brokerUrl, topic), consumerInvocation(brokerUrl, topic, offset), entryPoint(ConsumerRecord.class));
        verifier.verifyTraceCount(0);
    }

    public static void verifyMultiConsumerEntryPoint(String brokerUrl, String topic) throws NoSuchMethodException {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTraceCount(TRACES_PER_RECORD, 100, MAX_TRACE_WAIT_TIME);
        verifier.printCache();

        String expectedRpc = "kafka://topic=" + topic + "?batch=1";
        ExpectedTrace consumerInvocationExpected = consumerInvocation(brokerUrl, expectedRpc,
                annotation("kafka.topic", topic),
                annotation("kafka.batch", 1)
        );

        verifier.verifyDiscreteTrace(producerSend(brokerUrl, topic), consumerInvocationExpected, entryPoint(ConsumerRecords.class));
        verifier.verifyTraceCount(0);
    }

    private static ExpectedTrace producerSend(String brokerUrl, String topic) throws NoSuchMethodException {
        Method sendMethod = KafkaProducer.class.getDeclaredMethod("send", ProducerRecord.class, Callback.class);
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(KAFKA_CLIENT_SERVICE_TYPE);
        eventBuilder.setMethod(sendMethod);
        eventBuilder.setEndPoint(brokerUrl);
        eventBuilder.setDestinationId(brokerUrl);
        eventBuilder.setAnnotations(annotation("kafka.topic", topic));
        return eventBuilder.build();
    }

    private static ExpectedTrace consumerInvocation(String brokerUrl, String topic, long offset) {
        String expectedRpc = "kafka://topic=" + topic + "?partition=" + PARTITION + "&offset=" + offset;
        return consumerInvocation(brokerUrl, expectedRpc,
                annotation("kafka.topic", topic),
                annotation("kafka.partition", PARTITION),
                annotation("kafka.offset", offset)
        );
    }

    private static ExpectedTrace consumerInvocation(String brokerUrl, String expectedRpc, ExpectedAnnotation... expectedAnnotations) {
        ExpectedTrace.Builder rootBuilder = ExpectedTrace.createRootBuilder(KAFKA_CLIENT_SERVICE_TYPE);
        rootBuilder.setMethodSignature("Kafka Consumer Invocation");
        rootBuilder.setRpc(expectedRpc);
        // The endPoint is the list of the consumer's local socket addresses, e.g. "[127.0.0.1:7655, 127.0.0.1:7658]".
        // When the plugin fails to copy it to the records, it falls back to the broker address, which does not match.
        rootBuilder.setEndPoint(ExpectedTraceField.createStartWith("["));
        rootBuilder.setRemoteAddr(brokerUrl);
        rootBuilder.setAnnotations(expectedAnnotations);
        return rootBuilder.build();
    }

    private static ExpectedTrace entryPoint(Class<?> paramType) throws NoSuchMethodException {
        Method consumeRecordMethod = TestConsumerRecordEntryPoint.class.getDeclaredMethod("consumeRecord", paramType);
        return event(KAFKA_CLIENT_INTERNAL_SERVICE_TYPE, consumeRecordMethod);
    }
}
