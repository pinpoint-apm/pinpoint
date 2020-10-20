/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import test.pinpoint.plugin.kafka.*;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static test.pinpoint.plugin.kafka.KafkaITConstants.*;

/**
 * @author Younsung Hwang
 */
public abstract class KafkaClientITBase {

    private static final KafkaUnitServer KAFKA_UNIT_SERVER = new KafkaUnitServer(2189, 9092);
    private static final OffsetStore OFFSET_STORE = new OffsetStore();
    private static final TestConsumer TEST_CONSUMER = new TestConsumer(OFFSET_STORE);
    protected final TestProducer producer = new TestProducer();

    @BeforeClass
    public static void beforeClass() {
        KAFKA_UNIT_SERVER.startup();
        TEST_CONSUMER.start();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException {
        TEST_CONSUMER.shutdown();
        KAFKA_UNIT_SERVER.shutdown();
    }

    protected void verifyProducerSend(int messageCount) throws NoSuchMethodException {

        int consumerInvocationCount = messageCount * 2;
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTraceCount(messageCount + consumerInvocationCount, 100, MAX_TRACE_WAIT_TIME);
        verifier.printCache();

        Method sendMethod = KafkaProducer.class.getDeclaredMethod("send", ProducerRecord.class, Callback.class);
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(KAFKA_CLIENT_SERVICE_TYPE);
        eventBuilder.setMethod(sendMethod);
        eventBuilder.setEndPoint(BROKER_URL);
        eventBuilder.setDestinationId(BROKER_URL);
        eventBuilder.setAnnotations(annotation("kafka.topic", TOPIC));
        ExpectedTrace producerSendExpected = eventBuilder.build();

        for (int i = 0; i < messageCount; i++) {
            verifier.verifyDiscreteTrace(producerSendExpected);
        }

        verifier.verifyTraceCount(consumerInvocationCount);
    }

    protected void verifySingleConsumerEntryPoint() throws NoSuchMethodException {

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTraceCount(3, 100, MAX_TRACE_WAIT_TIME);

        String expectedRpc = "kafka://topic=" + TOPIC + "?partition=" + PARTITION + "&offset=" + OFFSET_STORE.getOffset();
        verifyConsumerEntryPoint(verifier, TOPIC, expectedRpc, ConsumerRecord.class,
                annotation("kafka.topic", TOPIC),
                annotation("kafka.partition", PARTITION),
                annotation("kafka.offset", OFFSET_STORE.getOffset())
        );
    }

    protected void verifyMultiConsumerEntryPoint() throws NoSuchMethodException {

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTraceCount(3, 100, MAX_TRACE_WAIT_TIME);

        String expectedRpc = "kafka://topic=" + TOPIC + "?batch=1";
        verifyConsumerEntryPoint(verifier, TOPIC, expectedRpc, ConsumerRecords.class,
                annotation("kafka.topic", TOPIC),
                annotation("kafka.batch", 1)
        );
    }

    private void verifyConsumerEntryPoint(PluginTestVerifier verifier, String topic, String expectedRpc, Class paramType, ExpectedAnnotation... expectedAnnotations)
            throws NoSuchMethodException {

        Method sendMethod = KafkaProducer.class.getDeclaredMethod("send", ProducerRecord.class, Callback.class);
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(KAFKA_CLIENT_SERVICE_TYPE);
        eventBuilder.setMethod(sendMethod);
        eventBuilder.setEndPoint(BROKER_URL);
        eventBuilder.setDestinationId(BROKER_URL);
        eventBuilder.setAnnotations(annotation("kafka.topic", topic));
        ExpectedTrace producerSendExpected = eventBuilder.build();

        ExpectedTrace.Builder rootBuilder = ExpectedTrace.createRootBuilder(KAFKA_CLIENT_SERVICE_TYPE);
        rootBuilder.setMethodSignature("Kafka Consumer Invocation");
        rootBuilder.setRpc(expectedRpc);
        rootBuilder.setRemoteAddr(BROKER_URL);
        rootBuilder.setAnnotations(expectedAnnotations);
        ExpectedTrace consumerEntryPointInvocationExpected = rootBuilder.build();

        Method consumeRecordsMethod = TestConsumerRecordEntryPoint.class.getDeclaredMethod("consumeRecord", paramType);
        ExpectedTrace messageArrivedExpected = event(KAFKA_CLIENT_INTERNAL_SERVICE_TYPE, consumeRecordsMethod);

        verifier.printCache();
        verifier.verifyDiscreteTrace(producerSendExpected, consumerEntryPointInvocationExpected, messageArrivedExpected);
    }
}
