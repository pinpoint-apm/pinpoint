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

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTraceField;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.processor.StreamPartitioner;
import org.apache.kafka.streams.processor.internals.RecordCollectorImpl;
import org.apache.kafka.streams.processor.internals.StreamTask;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static test.pinpoint.plugin.kafka.KafkaITConstants.KAFKA_CLIENT_SERVICE_TYPE;
import static test.pinpoint.plugin.kafka.KafkaITConstants.KAFKA_STREAMS_SERVICE_TYPE;
import static test.pinpoint.plugin.kafka.KafkaITConstants.MAX_TRACE_WAIT_TIME;

public class KafkaStreamsITBase {

    // KafkaProducer.send() event of the test + per forwarded record: async invocation, RecordCollectorImpl.send(), KafkaProducer.send()
    private static final int TRACES_PER_RECORD = 4;
    // streams root span + StreamTask.addRecords() event
    private static final int TRACES_PER_BATCH = 2;

    /**
     * The test produces {@code messageCount} records to the input topic as a single batch (see TestProducer).
     * The streams application receives them in one StreamTask.addRecords() call, which is one transaction,
     * and forwards each of them to the output topic.
     */
    public static void verifyStreams(String brokerUrl, String inputTopic, String outputTopic, int messageCount) throws NoSuchMethodException {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTraceCount(messageCount * TRACES_PER_RECORD + TRACES_PER_BATCH, 100, MAX_TRACE_WAIT_TIME);
        verifier.printCache();

        // producer side: the test itself
        ExpectedTrace inputSendExpected = producerSend(brokerUrl, inputTopic);
        for (int i = 0; i < messageCount; i++) {
            verifier.verifyDiscreteTrace(inputSendExpected);
        }

        // consumer side: the streams application
        ExpectedTrace.Builder rootBuilder = ExpectedTrace.createRootBuilder(KAFKA_STREAMS_SERVICE_TYPE);
        rootBuilder.setMethodSignature("Kafka Streams Invocation");
        rootBuilder.setRpc("kafka-streams://topic=" + inputTopic + "?batch=" + messageCount);
        // The endPoint is the list of the consumer's local socket addresses, e.g. "[127.0.0.1:8310, 127.0.0.1:8311]".
        // When the plugin fails to copy it to the records, it falls back to the broker address, which does not match.
        rootBuilder.setEndPoint(ExpectedTraceField.createStartWith("["));
        rootBuilder.setRemoteAddr(brokerUrl);
        rootBuilder.setAnnotations(annotation("kafka.topic", inputTopic), annotation("kafka.batch", messageCount));
        ExpectedTrace streamsInvocationExpected = rootBuilder.build();

        ExpectedTrace[] forwardExpected = new ExpectedTrace[messageCount * 3];
        for (int i = 0; i < messageCount; i++) {
            forwardExpected[i * 3] = Expectations.event("ASYNC", "Asynchronous Invocation");
            forwardExpected[i * 3 + 1] = Expectations.event(KAFKA_STREAMS_SERVICE_TYPE, recordCollectorSend());
            forwardExpected[i * 3 + 2] = producerSend(brokerUrl, outputTopic);
        }
        Method addRecordsMethod = StreamTask.class.getDeclaredMethod("addRecords", TopicPartition.class, Iterable.class);
        ExpectedTrace addRecordsExpected = Expectations.async(Expectations.event(KAFKA_STREAMS_SERVICE_TYPE, addRecordsMethod), forwardExpected);

        verifier.verifyDiscreteTrace(streamsInvocationExpected, addRecordsExpected);

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

    /**
     * RecordCollectorImpl.send(..., StreamPartitioner), the variant a sink node calls. Its parameter list is internal
     * API, so it is looked up by its last parameter instead of being spelled out.
     */
    private static Method recordCollectorSend() {
        for (Method method : RecordCollectorImpl.class.getDeclaredMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if ("send".equals(method.getName()) && parameterTypes.length > 0
                    && parameterTypes[parameterTypes.length - 1] == StreamPartitioner.class) {
                return method;
            }
        }
        throw new IllegalStateException("RecordCollectorImpl.send(..., StreamPartitioner) not found");
    }
}
