package com.navercorp.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.bootstrap.plugin.test.*;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.processor.internals.StreamTask;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static test.pinpoint.plugin.kafka.KafkaITConstants.*;

public class KafkaStreamsITBase {

    public static void verifyProducerSend(String brokerUrl, int messageCount) throws NoSuchMethodException {

        int consumerInvocationCount = messageCount * 3 + 2;
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTraceCount(messageCount + consumerInvocationCount, 100, MAX_TRACE_WAIT_TIME);
        verifier.printCache();

        Method sendMethod = KafkaProducer.class.getDeclaredMethod("send", ProducerRecord.class, Callback.class);
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(KAFKA_CLIENT_SERVICE_TYPE);
        eventBuilder.setMethod(sendMethod);
        eventBuilder.setEndPoint(brokerUrl);
        eventBuilder.setDestinationId(brokerUrl);
        eventBuilder.setAnnotations(annotation("kafka.topic", OUTPUT_TOPIC));
        ExpectedTrace producerSendExpected = eventBuilder.build();

        for (int i = 0; i < messageCount; i++) {
            verifier.verifyDiscreteTrace(producerSendExpected);
        }
    }

    public static void verifyMultiConsumerEntryPoint(String brokerUrl) throws NoSuchMethodException {

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTraceCount(3, 100, MAX_TRACE_WAIT_TIME);

        String expectedRpc = "kafka-streams://topic=" + INPUT_TOPIC + "?batch=1";
        verifyConsumerEntryPoint(verifier, brokerUrl, INPUT_TOPIC, expectedRpc, annotation("kafka.topic", INPUT_TOPIC), annotation("kafka.batch", 1)
        );
    }

    private static void verifyConsumerEntryPoint(PluginTestVerifier verifier, String brokerUrl, String topic, String expectedRpc, ExpectedAnnotation... expectedAnnotations)
            throws NoSuchMethodException {

        Method sendMethod = KafkaProducer.class.getDeclaredMethod("send", ProducerRecord.class, Callback.class);
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(KAFKA_CLIENT_SERVICE_TYPE);
        eventBuilder.setMethod(sendMethod);
        eventBuilder.setEndPoint(brokerUrl);
        eventBuilder.setDestinationId(brokerUrl);
        eventBuilder.setAnnotations(annotation("kafka.topic", topic));
        ExpectedTrace producerSendExpected = eventBuilder.build();

        ExpectedTrace.Builder rootBuilder = ExpectedTrace.createRootBuilder(KAFKA_STREAMS_SERVICE_TYPE);
        rootBuilder.setMethodSignature("Kafka Streams Invocation");
        rootBuilder.setRpc(expectedRpc);
        rootBuilder.setRemoteAddr(brokerUrl);
        rootBuilder.setAnnotations(expectedAnnotations);
        ExpectedTrace consumerEntryPointInvocationExpected = rootBuilder.build();

        Method consumeRecordsMethod = StreamTask.class.getDeclaredMethod("addRecords", TopicPartition.class, Iterable.class);
        ExpectedTrace messageArrivedExpected = Expectations.event(KAFKA_STREAMS_SERVICE_TYPE, consumeRecordsMethod);

        verifier.printCache();
        verifier.verifyDiscreteTrace(producerSendExpected, consumerEntryPointInvocationExpected, messageArrivedExpected);
    }
}
