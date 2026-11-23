/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.pulsar;

import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.async;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.root;

/**
 * The producer and the consumer both run here, on the class loader the agent instruments,
 * so the producer event, the propagated headers and the consumer span are all verified.
 * <p>
 * Implementation classes are resolved by name only: the shaded pulsar-client relocates them.
 */
public abstract class PulsarClientITBase {

    private static final String PULSAR_CLIENT = "PULSAR_CLIENT";
    private static final String PULSAR_CLIENT_INTERNAL = "PULSAR_CLIENT_INTERNAL";
    private static final String SUBSCRIPTION = "pulsar-it-subscription";
    private static final long MAX_WAIT_TIME = 10000;

    static String serviceUrl;

    @BeforeAll
    public static void beforeAll() {
        serviceUrl = System.getProperty("SERVICE_URL");
    }

    /**
     * @return package of ProducerImpl, ConsumerImpl and SendCallback
     */
    abstract String implPackage();

    @Test
    public void sendAndReceive() throws Exception {
        final String topic = newTopic();
        final byte[] payload = "sendAndReceive".getBytes(StandardCharsets.UTF_8);

        try (PulsarClient client = PulsarClient.builder().serviceUrl(serviceUrl).build();
             Consumer<byte[]> consumer = client.newConsumer()
                     .topic(topic)
                     .subscriptionName(SUBSCRIPTION)
                     .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
                     .subscribe();
             Producer<byte[]> producer = client.newProducer().topic(topic).create()) {

            producer.newMessage().value(payload).send();

            // receive() on a thread without a trace, like a real consumer
            final CompletableFuture<Message<byte[]>> future = new CompletableFuture<>();
            final Thread receiver = new Thread(() -> {
                try {
                    future.complete(consumer.receive((int) MAX_WAIT_TIME, TimeUnit.MILLISECONDS));
                } catch (Throwable th) {
                    future.completeExceptionally(th);
                }
            }, "pulsar-it-receiver");
            receiver.setDaemon(true);
            receiver.start();

            final Message<byte[]> message = future.get(MAX_WAIT_TIME * 2, TimeUnit.MILLISECONDS);
            Assertions.assertNotNull(message, "message not received");
            consumer.acknowledge(message);

            verify(producer, consumer, topic, message, payload);
        }
    }

    @Test
    public void sendAsyncAndListen() throws Exception {
        final String topic = newTopic();
        final byte[] payload = "sendAsyncAndListen".getBytes(StandardCharsets.UTF_8);
        final CompletableFuture<Message<byte[]>> future = new CompletableFuture<>();

        try (PulsarClient client = PulsarClient.builder().serviceUrl(serviceUrl).build();
             Consumer<byte[]> consumer = client.newConsumer()
                     .topic(topic)
                     .subscriptionName(SUBSCRIPTION)
                     .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
                     .messageListener((c, msg) -> {
                         future.complete(msg);
                         c.acknowledgeAsync(msg);
                     })
                     .subscribe();
             Producer<byte[]> producer = client.newProducer().topic(topic).create()) {

            final MessageId messageId = producer.newMessage().value(payload).sendAsync()
                    .get(MAX_WAIT_TIME, TimeUnit.MILLISECONDS);
            Assertions.assertNotNull(messageId);

            final Message<byte[]> message = future.get(MAX_WAIT_TIME * 2, TimeUnit.MILLISECONDS);
            Assertions.assertNotNull(message, "message not received");

            verify(producer, consumer, topic, message, payload);
        }
    }

    /**
     * A message from an unsampled trace carries Pinpoint-Sampled=s0 only: the consumer must not start a trace for it.
     */
    @Test
    public void skipUnsampledMessage() throws Exception {
        final String topic = newTopic();
        final byte[] payload = "skipUnsampledMessage".getBytes(StandardCharsets.UTF_8);

        try (PulsarClient client = PulsarClient.builder().serviceUrl(serviceUrl).build();
             Consumer<byte[]> consumer = client.newConsumer()
                     .topic(topic)
                     .subscriptionName(SUBSCRIPTION)
                     .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
                     .subscribe();
             Producer<byte[]> producer = client.newProducer().topic(topic).create()) {

            // sent on a thread without a trace, so the producer interceptors add nothing:
            // the first message looks like one from an unsampled trace, the second like one from an uninstrumented app
            runWithoutTrace(() -> {
                producer.newMessage().value(payload).property("Pinpoint-Sampled", "s0").send();
                return producer.newMessage().value(payload).send();
            });

            // received in order on a thread without a trace: both are processed when the second one is returned
            final Message<byte[]> second = runWithoutTrace(() -> {
                final Message<byte[]> first = consumer.receive((int) MAX_WAIT_TIME, TimeUnit.MILLISECONDS);
                Assertions.assertNotNull(first, "unsampled message not received");
                Assertions.assertEquals("s0", first.getProperty("Pinpoint-Sampled"));
                consumer.acknowledge(first);
                final Message<byte[]> message = consumer.receive((int) MAX_WAIT_TIME, TimeUnit.MILLISECONDS);
                Assertions.assertNotNull(message, "message not received");
                consumer.acknowledge(message);
                return message;
            });

            final PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
            verifier.printCache();
            // only the second message starts a consumer trace: root span + messageProcessed event
            verifier.verifyTraceCount(2);
            final Method messageProcessed = consumer.getClass().getDeclaredMethod("messageProcessed", Message.class);
            verifier.verifyDiscreteTrace(
                    root(PULSAR_CLIENT, "Pulsar Consumer Invocation",
                            "pulsar://topic=" + topic + "&partition=-1", serviceUrl, serviceUrl,
                            annotation("pulsar.partition.index", -1),
                            annotation("pulsar.topic", topic),
                            annotation("pulsar.message.id", second.getMessageId().toString()),
                            annotation("pulsar.message.size", payload.length)),
                    event(PULSAR_CLIENT_INTERNAL, messageProcessed));
        }
    }

    private static <T> T runWithoutTrace(Callable<T> task) throws Exception {
        final CompletableFuture<T> future = new CompletableFuture<>();
        final Thread thread = new Thread(() -> {
            try {
                future.complete(task.call());
            } catch (Throwable th) {
                future.completeExceptionally(th);
            }
        }, "pulsar-it-untraced");
        thread.setDaemon(true);
        thread.start();
        try {
            return future.get(MAX_WAIT_TIME * 3, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    private String newTopic() {
        return "persistent://public/default/pulsar-it-" + UUID.randomUUID();
    }

    private void verify(Producer<byte[]> producer, Consumer<byte[]> consumer, String topic,
                        Message<byte[]> message, byte[] payload) throws Exception {
        Assertions.assertArrayEquals(payload, message.getValue());

        // the flavour under test: relocated (shaded) or not
        final Class<?> producerClass = producer.getClass();
        final Class<?> consumerClass = consumer.getClass();
        Assertions.assertEquals(implPackage() + ".ProducerImpl", producerClass.getName());
        Assertions.assertEquals(implPackage() + ".ConsumerImpl", consumerClass.getName());

        // trace headers injected by the producer interceptor and delivered through the broker
        assertHeader(message, "Pinpoint-TraceID");
        final long spanId = Long.parseLong(assertHeader(message, "Pinpoint-SpanID"));
        final long parentSpanId = Long.parseLong(assertHeader(message, "Pinpoint-pSpanID"));
        Assertions.assertNotEquals(spanId, parentSpanId);
        assertHeader(message, "Pinpoint-Flags");
        assertHeader(message, "Pinpoint-pAppName");
        assertHeader(message, "Pinpoint-pAppType");
        Assertions.assertEquals(serviceUrl, message.getProperty("PULSAR_ACCEPTOR_HOST"));

        final PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        // producer: sendAsync event + async invocation and sendComplete events,
        // consumer: root span + messageProcessed event
        verifier.awaitTraceCount(5, 100, MAX_WAIT_TIME);
        verifier.printCache();

        // producer event and its send callback
        final ClassLoader classLoader = producerClass.getClassLoader();
        final Class<?> sendCallbackClass = Class.forName(implPackage() + ".SendCallback", false, classLoader);
        final Method sendAsync = producerClass.getDeclaredMethod("sendAsync", Message.class, sendCallbackClass);
        verifier.verifyDiscreteTrace(async(
                event(PULSAR_CLIENT, sendAsync, null, serviceUrl, serviceUrl,
                        annotation("pulsar.topic", topic),
                        annotation("pulsar.broker.url", serviceUrl)),
                event("ASYNC", "Asynchronous Invocation"),
                sendCompleteEvent(classLoader, message)));

        // consumer root span, continued from the propagated headers
        final Method messageProcessed = consumerClass.getDeclaredMethod("messageProcessed", Message.class);
        verifier.verifyDiscreteTrace(
                root(PULSAR_CLIENT, "Pulsar Consumer Invocation",
                        "pulsar://topic=" + topic + "&partition=-1", serviceUrl, serviceUrl,
                        annotation("pulsar.partition.index", -1),
                        annotation("pulsar.topic", topic),
                        annotation("pulsar.message.id", message.getMessageId().toString()),
                        annotation("pulsar.message.size", payload.length)),
                event(PULSAR_CLIENT_INTERNAL, messageProcessed));
    }

    /**
     * 4.0.14, 4.2.5 and 5.x complete the default callback through the private sendComplete(Throwable), which has no
     * OpSendMsgStats and so no sequence id and retry count. Older clients go through
     * sendComplete(Throwable, OpSendMsgStats).
     */
    private ExpectedTrace sendCompleteEvent(ClassLoader classLoader, Message<byte[]> message) throws Exception {
        final Class<?> defaultSendCallbackClass = Class.forName(
                implPackage() + ".ProducerImpl$DefaultSendMessageCallback", false, classLoader);
        final Method internalSendComplete = findDeclaredMethod(defaultSendCallbackClass, "sendComplete", Throwable.class);
        if (internalSendComplete != null) {
            return event(PULSAR_CLIENT_INTERNAL, internalSendComplete);
        }
        final Class<?> sendMsgStatsClass = Class.forName(implPackage() + ".OpSendMsgStats", false, classLoader);
        final Method sendComplete = defaultSendCallbackClass.getDeclaredMethod("sendComplete",
                Throwable.class, sendMsgStatsClass);
        return event(PULSAR_CLIENT_INTERNAL, sendComplete,
                annotation("pulsar.sequence.id", message.getSequenceId()),
                // incremented on every send attempt: 1 without a retry
                annotation("pulsar.retry.count", 1));
    }

    private static Method findDeclaredMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private String assertHeader(Message<byte[]> message, String name) {
        final String value = message.getProperty(name);
        Assertions.assertNotNull(value, name + " not propagated, properties=" + message.getProperties());
        Assertions.assertFalse(value.isEmpty(), name);
        return value;
    }
}
