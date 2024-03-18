/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import test.pinpoint.plugin.kafka.TestConsumerRecordEntryPoint;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static test.pinpoint.plugin.kafka.KafkaITConstants.KAFKA_CLIENT_INTERNAL_SERVICE_TYPE;
import static test.pinpoint.plugin.kafka.KafkaITConstants.KAFKA_CLIENT_SERVICE_TYPE;
import static test.pinpoint.plugin.kafka.KafkaITConstants.MAX_TRACE_WAIT_TIME;
import static test.pinpoint.plugin.kafka.KafkaITConstants.PARTITION;
import static test.pinpoint.plugin.kafka.KafkaITConstants.TOPIC;

/**
 * @author Younsung Hwang
 */
public class KafkaClientITBase {

    public static void verifyProducerSend(String brokerUrl, int messageCount) throws NoSuchMethodException {

        int consumerInvocationCount = messageCount * 2;
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTraceCount(messageCount + consumerInvocationCount, 100, MAX_TRACE_WAIT_TIME);
        verifier.printCache();

        Method sendMethod = KafkaProducer.class.getDeclaredMethod("send", ProducerRecord.class, Callback.class);
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(KAFKA_CLIENT_SERVICE_TYPE);
        eventBuilder.setMethod(sendMethod);
        eventBuilder.setEndPoint(brokerUrl);
        eventBuilder.setDestinationId(brokerUrl);
        eventBuilder.setAnnotations(annotation("kafka.topic", TOPIC));
        ExpectedTrace producerSendExpected = eventBuilder.build();

        for (int i = 0; i < messageCount; i++) {
            verifier.verifyDiscreteTrace(producerSendExpected);
        }

//        verifier.verifyTraceCount(consumerInvocationCount);
    }

    public static void verifySingleConsumerEntryPoint(String brokerUrl, long offset) throws NoSuchMethodException {

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTraceCount(3, 100, MAX_TRACE_WAIT_TIME);

        String expectedRpc = "kafka://topic=" + TOPIC + "?partition=" + PARTITION + "&offset=" + offset;
        verifyConsumerEntryPoint(verifier, brokerUrl, TOPIC, expectedRpc, ConsumerRecord.class,
                annotation("kafka.topic", TOPIC),
                annotation("kafka.partition", PARTITION),
                annotation("kafka.offset", offset)
        );
    }

    public static void verifyMultiConsumerEntryPoint(String brokerUrl) throws NoSuchMethodException {

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTraceCount(3, 100, MAX_TRACE_WAIT_TIME);

        String expectedRpc = "kafka://topic=" + TOPIC + "?batch=1";
        verifyConsumerEntryPoint(verifier, brokerUrl, TOPIC, expectedRpc, ConsumerRecords.class,
                annotation("kafka.topic", TOPIC),
                annotation("kafka.batch", 1)
        );
    }

    private static void verifyConsumerEntryPoint(PluginTestVerifier verifier, String brokerUrl, String topic, String expectedRpc, Class<?> paramType, ExpectedAnnotation... expectedAnnotations)
            throws NoSuchMethodException {

        Method sendMethod = KafkaProducer.class.getDeclaredMethod("send", ProducerRecord.class, Callback.class);
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(KAFKA_CLIENT_SERVICE_TYPE);
        eventBuilder.setMethod(sendMethod);
        eventBuilder.setEndPoint(brokerUrl);
        eventBuilder.setDestinationId(brokerUrl);
        eventBuilder.setAnnotations(annotation("kafka.topic", topic));
        ExpectedTrace producerSendExpected = eventBuilder.build();

        ExpectedTrace.Builder rootBuilder = ExpectedTrace.createRootBuilder(KAFKA_CLIENT_SERVICE_TYPE);
        rootBuilder.setMethodSignature("Kafka Consumer Invocation");
        rootBuilder.setRpc(expectedRpc);
        rootBuilder.setRemoteAddr(brokerUrl);
        rootBuilder.setAnnotations(expectedAnnotations);
        ExpectedTrace consumerEntryPointInvocationExpected = rootBuilder.build();

        Method consumeRecordsMethod = TestConsumerRecordEntryPoint.class.getDeclaredMethod("consumeRecord", paramType);
        ExpectedTrace messageArrivedExpected = event(KAFKA_CLIENT_INTERNAL_SERVICE_TYPE, consumeRecordsMethod);

        verifier.printCache();
//        verifier.verifyDiscreteTrace(producerSendExpected, consumerEntryPointInvocationExpected, messageArrivedExpected);
    }
}
