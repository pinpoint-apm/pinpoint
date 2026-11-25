/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.rocketmq;

import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTraceField;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.impl.MQClientAPIImpl;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.pinpoint.external.rocketmq.OutOfBasePackageListener;
import test.pinpoint.plugin.rocketmq.MessageHolder;
import test.pinpoint.plugin.rocketmq.RocketMQITConstants;
import test.pinpoint.plugin.rocketmq.TestConcurrentlyListener;
import test.pinpoint.plugin.rocketmq.TestOrderlyListener;
import test.pinpoint.plugin.rocketmq.TestSendCallback;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.async;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static test.pinpoint.plugin.rocketmq.RocketMQITConstants.MAX_MESSAGE_WAIT_TIME;
import static test.pinpoint.plugin.rocketmq.RocketMQITConstants.MAX_TRACE_WAIT_TIME;
import static test.pinpoint.plugin.rocketmq.RocketMQITConstants.ROCKETMQ_CLIENT_INTERNAL_SERVICE_TYPE;
import static test.pinpoint.plugin.rocketmq.RocketMQITConstants.ROCKETMQ_CLIENT_SERVICE_TYPE;

/**
 * Every test uses its own topic (one queue, so the queue id is always 0) and its own producer/consumer groups:
 * all client versions of a range run against the same broker and must not see each other's messages.
 * <p>
 * The plugin test framework already runs each test method inside a trace, which is what the producer interceptors
 * attach to.
 */
public abstract class RocketMQClientITBase {

    private static final String ASYNC = "ASYNC";

    private static final int QUEUE_ID = 0;
    private static final String MESSAGE_BODY = "hello rocketmq";

    // what the producer interceptor appends to the message properties
    private static final String TRACE_ID_PROPERTY = "Pinpoint-TraceID";
    private static final String SPAN_ID_PROPERTY = "Pinpoint-SpanID";
    private static final String PARENT_SPAN_ID_PROPERTY = "Pinpoint-pSpanID";
    private static final String ACCEPTOR_HOST_PROPERTY = "ROCKETMQ_ACCEPTOR_HOST";
    private static final String IS_ASYNC_SEND_PROPERTY = "IS_ASYNC_SEND";
    private static final String SAMPLED_PROPERTY = "Pinpoint-Sampled";

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    static String namesrvAddr;
    static String brokerAddr;

    // one producer for all tests of a client version: starting a client is the slow part of each test
    private static DefaultMQProducer producer;

    private String topic;
    private DefaultMQPushConsumer consumer;

    @BeforeAll
    public static void beforeAll() throws Exception {
        namesrvAddr = System.getProperty(RocketMQITConstants.NAMESRV_ADDR_PROPERTY);
        brokerAddr = System.getProperty(RocketMQITConstants.BROKER_ADDR_PROPERTY);

        final String id = newId();
        producer = new DefaultMQProducer("pinpoint-it-producer-" + id);
        producer.setNamesrvAddr(namesrvAddr);
        // producer and consumers live in one JVM: without distinct instance names they would share one client
        producer.setInstanceName("producer-" + id);
        producer.start();
    }

    @AfterAll
    public static void afterAll() {
        if (producer != null) {
            producer.shutdown();
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        final String id = newId();
        topic = "pinpoint-it-topic-" + id;
        createTopic(producer, topic);

        consumer = new DefaultMQPushConsumer("pinpoint-it-consumer-" + id);
        consumer.setNamesrvAddr(namesrvAddr);
        consumer.setInstanceName("consumer-" + id);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.subscribe(topic, "*");
    }

    @AfterEach
    public void tearDown() {
        if (consumer != null) {
            consumer.shutdown();
        }
    }

    private static String newId() {
        return Long.toString(System.currentTimeMillis(), 36) + "-" + SEQUENCE.incrementAndGet();
    }

    @Test
    public void syncSend_concurrentlyListener() throws Exception {
        final MessageHolder messageHolder = new MessageHolder();
        consumer.registerMessageListener(new TestConcurrentlyListener(messageHolder));
        final MessageExt received = sendAndReceive(messageHolder);

        final PluginTestVerifier verifier = awaitTraces(3);
        verifier.verifyDiscreteTrace(producerSend());
        verifier.verifyDiscreteTrace(
                consumerRoot(received),
                event(ROCKETMQ_CLIENT_INTERNAL_SERVICE_TYPE, concurrentlyConsumeMessage(TestConcurrentlyListener.class),
                        annotation("rocketmq.send.status", "CONSUME_SUCCESS")));
        verifier.verifyTraceCount(0);
    }

    @Test
    public void syncSend_orderlyListener() throws Exception {
        final MessageHolder messageHolder = new MessageHolder();
        consumer.registerMessageListener(new TestOrderlyListener(messageHolder));
        final MessageExt received = sendAndReceive(messageHolder);

        final PluginTestVerifier verifier = awaitTraces(3);
        verifier.verifyDiscreteTrace(producerSend());
        verifier.verifyDiscreteTrace(
                consumerRoot(received),
                event(ROCKETMQ_CLIENT_INTERNAL_SERVICE_TYPE, orderlyConsumeMessage(TestOrderlyListener.class),
                        annotation("rocketmq.send.status", "SUCCESS")));
        verifier.verifyTraceCount(0);
    }

    @Test
    public void asyncSend_sendCallback() throws Exception {
        final MessageHolder messageHolder = new MessageHolder();
        consumer.registerMessageListener(new TestConcurrentlyListener(messageHolder));
        consumer.start();

        // the constructor is where the plugin opens the async context, so it has to run inside the trace
        final TestSendCallback sendCallback = new TestSendCallback();
        producer.send(newMessage(), sendCallback);
        final SendResult sendResult = sendCallback.awaitSendResult(MAX_MESSAGE_WAIT_TIME);
        Assertions.assertEquals(SendStatus.SEND_OK, sendResult.getSendStatus());

        final MessageExt received = awaitMessage(messageHolder);
        assertPropagated(received, true);

        final PluginTestVerifier verifier = awaitTraces(7);
        final ExpectedTrace asyncInvocation = event(ASYNC, "Asynchronous Invocation");
        final Method onSuccess = TestSendCallback.class.getDeclaredMethod("onSuccess", SendResult.class);
        // the send and the callback both continue the async context opened by the constructor
        verifier.verifyDiscreteTrace(async(
                event(ROCKETMQ_CLIENT_INTERNAL_SERVICE_TYPE, TestSendCallback.class.getDeclaredConstructor()),
                asyncInvocation,
                producerSend(),
                asyncInvocation,
                event(ROCKETMQ_CLIENT_INTERNAL_SERVICE_TYPE, onSuccess,
                        annotation("rocketmq.send.status", "SEND_OK"))));
        verifier.verifyDiscreteTrace(
                consumerRoot(received),
                event(ROCKETMQ_CLIENT_INTERNAL_SERVICE_TYPE, concurrentlyConsumeMessage(TestConcurrentlyListener.class),
                        annotation("rocketmq.send.status", "CONSUME_SUCCESS")));
        verifier.verifyTraceCount(0);
    }

    /**
     * A listener the plugin does not instrument must not break the consumer: it is only left untraced.
     */
    @Test
    public void syncSend_listenerOutOfBasePackage() throws Exception {
        final MessageHolder messageHolder = new MessageHolder();
        consumer.registerMessageListener(new OutOfBasePackageListener(messageHolder));
        sendAndReceive(messageHolder);

        final PluginTestVerifier verifier = awaitTraces(1);
        verifier.verifyDiscreteTrace(producerSend());
        verifier.verifyTraceCount(0);
    }

    /**
     * Same for a lambda, which never matches the base package filter.
     */
    @Test
    public void syncSend_lambdaListener() throws Exception {
        final MessageHolder messageHolder = new MessageHolder();
        final MessageListenerConcurrently listener = (msgs, context) -> {
            messageHolder.addAll(msgs);
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        };
        consumer.registerMessageListener(listener);
        sendAndReceive(messageHolder);

        final PluginTestVerifier verifier = awaitTraces(1);
        verifier.verifyDiscreteTrace(producerSend());
        verifier.verifyTraceCount(0);
    }

    /**
     * A message from an unsampled trace carries Pinpoint-Sampled=s0 only: the consumer must not start a trace for it.
     */
    @Test
    public void syncSend_skipUnsampledMessage() throws Exception {
        final MessageHolder messageHolder = new MessageHolder();
        // one queue consumed in order: the first message is done with before the second one is consumed
        consumer.registerMessageListener(new TestOrderlyListener(messageHolder));
        consumer.start();

        // sent on a thread without a trace, so the producer interceptor adds nothing
        final Message unsampled = newMessage();
        unsampled.putUserProperty(SAMPLED_PROPERTY, "s0");
        sendWithoutTrace(unsampled);
        final MessageExt skipped = awaitMessage(messageHolder);
        Assertions.assertEquals("s0", skipped.getUserProperty(SAMPLED_PROPERTY), SAMPLED_PROPERTY);

        final SendResult sendResult = producer.send(newMessage());
        Assertions.assertEquals(SendStatus.SEND_OK, sendResult.getSendStatus());
        final MessageExt received = awaitMessage(messageHolder);
        assertPropagated(received, false);

        // only the traced message has a consumer trace
        final PluginTestVerifier verifier = awaitTraces(3);
        verifier.verifyDiscreteTrace(producerSend());
        verifier.verifyDiscreteTrace(
                consumerRoot(received),
                event(ROCKETMQ_CLIENT_INTERNAL_SERVICE_TYPE, orderlyConsumeMessage(TestOrderlyListener.class),
                        annotation("rocketmq.send.status", "SUCCESS")));
        verifier.verifyTraceCount(0);
    }

    private static void sendWithoutTrace(Message message) throws Exception {
        final AtomicReference<Object> result = new AtomicReference<>();
        final Thread sender = new Thread(() -> {
            try {
                result.set(producer.send(message));
            } catch (Throwable th) {
                result.set(th);
            }
        }, "rocketmq-it-untraced-sender");
        sender.start();
        sender.join(MAX_MESSAGE_WAIT_TIME);
        if (result.get() instanceof Exception) {
            throw (Exception) result.get();
        }
        Assertions.assertTrue(result.get() instanceof SendResult, "send result: " + result.get());
        Assertions.assertEquals(SendStatus.SEND_OK, ((SendResult) result.get()).getSendStatus());
    }

    private MessageExt sendAndReceive(MessageHolder messageHolder) throws Exception {
        consumer.start();

        final SendResult sendResult = producer.send(newMessage());
        Assertions.assertEquals(SendStatus.SEND_OK, sendResult.getSendStatus());

        final MessageExt received = awaitMessage(messageHolder);
        assertPropagated(received, false);
        return received;
    }

    private PluginTestVerifier awaitTraces(int expectedTraceCount) {
        final PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTraceCount(expectedTraceCount, 100, MAX_TRACE_WAIT_TIME);
        verifier.printCache();
        return verifier;
    }

    /**
     * createTopic(key, newTopic, queueNum) is the 4.x signature; 5.x replaced it with
     * createTopic(key, newTopic, queueNum, attributes), so neither can be linked statically for the whole range.
     */
    static void createTopic(DefaultMQProducer producer, String topic) throws Exception {
        final String key = RocketMQITConstants.AUTO_CREATE_TOPIC_KEY;
        try {
            final Method createTopic = DefaultMQProducer.class.getMethod("createTopic", String.class, String.class, int.class);
            createTopic.invoke(producer, key, topic, 1);
        } catch (NoSuchMethodException e) {
            final Method createTopic = DefaultMQProducer.class.getMethod("createTopic", String.class, String.class, int.class, Map.class);
            createTopic.invoke(producer, key, topic, 1, new HashMap<String, String>());
        }
    }

    private Message newMessage() {
        return new Message(topic, MESSAGE_BODY.getBytes(StandardCharsets.UTF_8));
    }

    private MessageExt awaitMessage(MessageHolder messageHolder) throws InterruptedException {
        final MessageExt received = messageHolder.poll(MAX_MESSAGE_WAIT_TIME);
        Assertions.assertNotNull(received, "message not consumed within " + MAX_MESSAGE_WAIT_TIME + "ms");
        Assertions.assertEquals(topic, received.getTopic());
        Assertions.assertEquals(MESSAGE_BODY, new String(received.getBody(), StandardCharsets.UTF_8));
        return received;
    }

    /**
     * The trace context travels in the message properties; this is what links the consumer span to the producer.
     */
    private void assertPropagated(MessageExt received, boolean asyncSend) {
        Assertions.assertNotNull(received.getUserProperty(TRACE_ID_PROPERTY), TRACE_ID_PROPERTY);
        Assertions.assertNotNull(received.getUserProperty(SPAN_ID_PROPERTY), SPAN_ID_PROPERTY);
        Assertions.assertNotNull(received.getUserProperty(PARENT_SPAN_ID_PROPERTY), PARENT_SPAN_ID_PROPERTY);
        Assertions.assertEquals(namesrvAddr, received.getUserProperty(ACCEPTOR_HOST_PROPERTY), ACCEPTOR_HOST_PROPERTY);
        Assertions.assertEquals(String.valueOf(asyncSend), received.getUserProperty(IS_ASYNC_SEND_PROPERTY), IS_ASYNC_SEND_PROPERTY);
    }

    private ExpectedTrace producerSend() {
        final ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(ROCKETMQ_CLIENT_SERVICE_TYPE);
        eventBuilder.setMethod(sendMessageMethod());
        eventBuilder.setEndPoint(namesrvAddr);
        eventBuilder.setDestinationId(namesrvAddr);
        eventBuilder.setAnnotations(
                annotation("rocketmq.topic", topic),
                annotation("rocketmq.message.queue", QUEUE_ID));
        return eventBuilder.build();
    }

    private ExpectedTrace consumerRoot(MessageExt received) {
        final long offset = received.getCommitLogOffset();
        final ExpectedTrace.Builder rootBuilder = ExpectedTrace.createRootBuilder(ROCKETMQ_CLIENT_SERVICE_TYPE);
        rootBuilder.setMethodSignature("RocketMQ Consumer Invocation");
        rootBuilder.setRpc("rocketmq://topic=" + topic + "?partition=" + QUEUE_ID + "&offset=" + offset);
        // the consumer's own end of its connection to the broker: an ephemeral port
        rootBuilder.setEndPoint(ExpectedTraceField.createStartWith(hostOf(brokerAddr) + ":"));
        rootBuilder.setRemoteAddr(namesrvAddr);
        rootBuilder.setAnnotations(
                annotation("rocketmq.broker.server", brokerAddr),
                annotation("rocketmq.message.queue", QUEUE_ID),
                annotation("rocketmq.offset", offset),
                annotation("rocketmq.topic", topic));
        return rootBuilder.build();
    }

    private static String hostOf(String address) {
        return address.substring(0, address.lastIndexOf(':'));
    }

    /**
     * The hooked overload is the 12 argument one; its parameter types differ between client versions.
     */
    private static Method sendMessageMethod() {
        for (Method method : MQClientAPIImpl.class.getDeclaredMethods()) {
            if (method.getName().equals("sendMessage") && method.getParameterTypes().length == 12) {
                return method;
            }
        }
        throw new IllegalStateException("MQClientAPIImpl.sendMessage with 12 parameters not found");
    }

    private static Method concurrentlyConsumeMessage(Class<?> listenerClass) throws NoSuchMethodException {
        return listenerClass.getDeclaredMethod("consumeMessage", List.class, ConsumeConcurrentlyContext.class);
    }

    private static Method orderlyConsumeMessage(Class<?> listenerClass) throws NoSuchMethodException {
        return listenerClass.getDeclaredMethod("consumeMessage", List.class, ConsumeOrderlyContext.class);
    }
}
