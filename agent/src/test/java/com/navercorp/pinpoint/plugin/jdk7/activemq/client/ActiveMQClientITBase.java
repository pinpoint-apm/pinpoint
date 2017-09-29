/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.plugin.jdk7.activemq.client;

import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.plugin.jdk7.activemq.client.util.ActiveMQClientITHelper;
import com.navercorp.pinpoint.plugin.jdk7.activemq.client.util.AssertTextMessageListener;
import com.navercorp.pinpoint.plugin.jdk7.activemq.client.util.MessageConsumerBuilder;
import com.navercorp.pinpoint.plugin.jdk7.activemq.client.util.MessageProducerBuilder;
import org.apache.activemq.ActiveMQMessageConsumer;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.command.MessageDispatch;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.root;

/**
 * @author HyunGil Jeong
 */
public abstract class ActiveMQClientITBase {

    public static final String ACTIVEMQ_CLIENT = "ACTIVEMQ_CLIENT";
    public static final String ACTIVEMQ_CLIENT_INTERNAL = "ACTIVEMQ_CLIENT_INTERNAL";

    protected abstract String getProducerBrokerName();

    protected abstract String getProducerBrokerUrl();

    protected abstract String getConsumerBrokerName();

    protected abstract String getConsumerBrokerUrl();

    @Test
    public void testQueuePull() throws Exception {
        // Given
        final String testQueueName = "TestPullQueue";
        final ActiveMQQueue testQueue = new ActiveMQQueue(testQueueName);
        final String testMessage = "Hello World for Queue!";
        // create producer
        ActiveMQSession producerSession = ActiveMQClientITHelper.createSession(getProducerBrokerName(), getProducerBrokerUrl());
        MessageProducer producer = producerSession.createProducer(testQueue);
        final TextMessage expectedTextMessage = producerSession.createTextMessage(testMessage);
        // When
        ActiveMQSession consumerSession = ActiveMQClientITHelper.createSession(getConsumerBrokerName(), getConsumerBrokerUrl());
        MessageConsumer consumer = consumerSession.createConsumer(testQueue);

        // Then
        producer.send(expectedTextMessage);
        Message message = consumer.receive(1000L);
        Assert.assertEquals(testMessage, ((TextMessage) message).getText());

        // Wait till all traces are recorded (consumer traces are recorded from another thread)
        awaitAndVerifyTraceCount(5, 5000L);
        verifyProducerSendEvent(testQueue, producerSession); // trace count : 1
        verifyConsumerPullEvent(testQueue, consumerSession, consumer, expectedTextMessage); // trace count : 4
    }

    @Test
    public void testTopicPull() throws Exception {
        // Given
        final String testTopicName = "TestPullTopic";
        final ActiveMQTopic testTopic = new ActiveMQTopic(testTopicName);
        final String testMessage = "Hello World for Topic!";
        // create producer
        ActiveMQSession producerSession = ActiveMQClientITHelper.createSession(getProducerBrokerName(), getProducerBrokerUrl());
        MessageProducer producer = new MessageProducerBuilder(producerSession, testTopic).waitTillStarted().build();
        final TextMessage expectedTextMessage = producerSession.createTextMessage(testMessage);
        // create 2 consumers
        ActiveMQSession consumer1Session = ActiveMQClientITHelper.createSession(getConsumerBrokerName(), getConsumerBrokerUrl());
        MessageConsumer consumer1 = new MessageConsumerBuilder(consumer1Session, testTopic).waitTillStarted().build();
        ActiveMQSession consumer2Session = ActiveMQClientITHelper.createSession(getConsumerBrokerName(), getConsumerBrokerUrl());
        MessageConsumer consumer2 = new MessageConsumerBuilder(consumer2Session, testTopic).waitTillStarted().build();

        // When
        producer.send(expectedTextMessage);
        Message message1 = consumer1.receive(1000L);
        Message message2 = consumer2.receive(1000L);
        Assert.assertEquals(testMessage, ((TextMessage) message1).getText());
        Assert.assertEquals(testMessage, ((TextMessage) message2).getText());

        // Wait till all traces are recorded (consumer traces are recorded from another thread)
        awaitAndVerifyTraceCount(9, 5000L);
        verifyProducerSendEvent(testTopic, producerSession); // trace count : 1
        verifyConsumerPullEvent(testTopic, consumer1Session, consumer1, expectedTextMessage); // trace count : 4
        verifyConsumerPullEvent(testTopic, consumer2Session, consumer2, expectedTextMessage); // trace count : 4
    }

    @Test
    public void testQueuePush() throws Exception {
        // Given
        final String testQueueName = "TestPushQueue";
        final ActiveMQQueue testQueue = new ActiveMQQueue(testQueueName);
        final String testMessage = "Hello World for Queue!";
        final CountDownLatch consumerLatch = new CountDownLatch(1);
        final Collection<Throwable> consumerThrowables = new CopyOnWriteArrayList<Throwable>();
        // create producer
        ActiveMQSession producerSession = ActiveMQClientITHelper.createSession(getProducerBrokerName(), getProducerBrokerUrl());
        MessageProducer producer = producerSession.createProducer(testQueue);
        final TextMessage expectedTextMessage = producerSession.createTextMessage(testMessage);
        // create consumer
        ActiveMQSession consumerSession = ActiveMQClientITHelper.createSession(getConsumerBrokerName(), getConsumerBrokerUrl());
        MessageConsumer consumer = consumerSession.createConsumer(testQueue);
        consumer.setMessageListener(new AssertTextMessageListener(consumerLatch, consumerThrowables, expectedTextMessage));

        // When
        producer.send(expectedTextMessage);
        consumerLatch.await(1L, TimeUnit.SECONDS);

        // Then
        assertNoConsumerError(consumerThrowables);
        // Wait till all traces are recorded (consumer traces are recorded from another thread)
        awaitAndVerifyTraceCount(2, 5000L);
        verifyProducerSendEvent(testQueue, producerSession); // trace count : 1
        verifyConsumerPushEvent(testQueue, consumerSession); // trace count : 1
    }

    @Test
    public void testTopicPush() throws Exception {
        // Given
        final String testTopicName = "TestPushTopic";
        final ActiveMQTopic testTopic = new ActiveMQTopic(testTopicName);
        final String testMessage = "Hello World for Topic!";
        final int numMessageConsumers = 2;

        final CountDownLatch consumerConsumeLatch = new CountDownLatch(numMessageConsumers);
        final Collection<Throwable> consumerThrowables = new CopyOnWriteArrayList<Throwable>();
        // create producer
        ActiveMQSession producerSession = ActiveMQClientITHelper.createSession(getProducerBrokerName(), getProducerBrokerUrl());
        MessageProducer producer = new MessageProducerBuilder(producerSession, testTopic).waitTillStarted().build();
        final TextMessage expectedTextMessage = producerSession.createTextMessage(testMessage);
        // create 2 consumers
        ActiveMQSession consumer1Session = ActiveMQClientITHelper.createSession(getConsumerBrokerName(), getConsumerBrokerUrl());
        new MessageConsumerBuilder(consumer1Session, testTopic)
                .withMessageListener(new AssertTextMessageListener(consumerConsumeLatch, consumerThrowables, expectedTextMessage))
                .waitTillStarted()
                .build();
        ActiveMQSession consumer2Session = ActiveMQClientITHelper.createSession(getConsumerBrokerName(), getConsumerBrokerUrl());
        new MessageConsumerBuilder(consumer2Session, testTopic)
                .withMessageListener(new AssertTextMessageListener(consumerConsumeLatch, consumerThrowables, expectedTextMessage))
                .waitTillStarted()
                .build();

        // When
        producer.send(expectedTextMessage);
        consumerConsumeLatch.await(1L, TimeUnit.SECONDS);

        // Then
        // Wait till all traces are recorded (consumer traces are recorded from another thread)
        awaitAndVerifyTraceCount(3, 1000L);
        verifyProducerSendEvent(testTopic, producerSession); // trace count : 1
        verifyConsumerPushEvent(testTopic, consumer1Session); // trace count : 1
        verifyConsumerPushEvent(testTopic, consumer2Session); // trace count : 1
    }

    /**
     * Verifies traced span event for when {@link org.apache.activemq.ActiveMQMessageProducer ActiveMQMessageProducer}
     * sends the message. (trace count : 1)
     *
     * @param destination the destination to which the producer is sending the message
     * @throws Exception
     */
    private void verifyProducerSendEvent(ActiveMQDestination destination, ActiveMQSession session) throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        Class<?> messageProducerClass = Class.forName("org.apache.activemq.ActiveMQMessageProducer");
        Method send = messageProducerClass.getDeclaredMethod("send", Destination.class, Message.class, int.class, int.class, long.class);
//        URI producerBrokerUri = new URI(getProducerBrokerUrl());
//        String expectedEndPoint = getProducerBrokerUri.getHost() + ":" + producerBrokerUri.getPort();
//        String expectedEndPoint = producerBrokerUri.toString();
        String expectedEndPoint = session.getConnection().getTransport().getRemoteAddress();
        verifier.verifyDiscreteTrace(event(
                ACTIVEMQ_CLIENT, // serviceType
                send, // method
                null, // rpc
                expectedEndPoint, // endPoint
                destination.getPhysicalName(), // destinationId
                annotation("message.queue.url", destination.getQualifiedName()),
                annotation("activemq.broker.address", expectedEndPoint)
        ));
    }

    /**
     * Verifies spans and span events for when {@link ActiveMQMessageConsumer} receives the message and enqueues it to
     * the {@link org.apache.activemq.MessageDispatchChannel MessageDispatchChannel}. The client then invokes any of
     * {@link ActiveMQMessageConsumer#receive() receive()}, {@link ActiveMQMessageConsumer#receive(long) receive(long)},
     * or {@link ActiveMQMessageConsumer#receiveNoWait() receiveNotWait()} to retrieve the message. (trace count : 4)
     *
     * @param destination the destination from which the consumer is receiving the message
     * @param expectedMessage the message the consumer is expected to receive
     * @throws Exception
     */
    private void verifyConsumerPullEvent(ActiveMQDestination destination, ActiveMQSession session, MessageConsumer consumer, Message expectedMessage) throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        Class<?> messageConsumerClass = Class.forName("org.apache.activemq.ActiveMQMessageConsumer");
        Method receiveWithTimeout = messageConsumerClass.getDeclaredMethod("receive", long.class);
//        URI consumerBrokerUri = new URI(getConsumerBrokerUrl());
//        String expectedEndPoint = consumerBrokerUri.toString();
        String expectedEndPoint = session.getConnection().getTransport().getRemoteAddress();

        ExpectedTrace consumerDispatchTrace = root(ACTIVEMQ_CLIENT, // serviceType
                "ActiveMQ Consumer Invocation", // method
                destination.getQualifiedName(), // rpc
                null, // endPoint (collected but there's no easy way to retrieve local address)
                expectedEndPoint);
        ExpectedTrace consumerReceiveTrace = event(ACTIVEMQ_CLIENT_INTERNAL, // serviceType
                receiveWithTimeout, // method
                annotation("activemq.message", getMessageAsString(expectedMessage)));

        Class<?> messageDispatchChannel = getMessageDispatchChannelClass(consumer);
        if (messageDispatchChannel != null) {
            Method enqueue = messageDispatchChannel.getDeclaredMethod("enqueue", MessageDispatch.class);
            Method dequeueWithTimeout = messageDispatchChannel.getDeclaredMethod("dequeue", long.class);
            // Consumer dispatches and enqueues the message to dispatch channel automatically
            verifier.verifyDiscreteTrace(consumerDispatchTrace, event(ACTIVEMQ_CLIENT_INTERNAL, enqueue));
            // Client receives the message by dequeueing it from the dispatch channel
            verifier.verifyDiscreteTrace(consumerReceiveTrace, event(ACTIVEMQ_CLIENT_INTERNAL, dequeueWithTimeout));
        } else {
            // Consumer dispatches and enqueues the message to dispatch channel automatically
            verifier.verifyDiscreteTrace(consumerDispatchTrace);
            // Client receives the message by dequeueing it from the dispatch channel
            verifier.verifyDiscreteTrace(consumerReceiveTrace);
        }
    }

    /**
     * Verifies spans and span events for when {@link ActiveMQMessageConsumer} receives the message and invokes it's
     * {@link javax.jms.MessageListener MessageListener}. (trace count : 1)
     *
     * @param destination the destination from which the consumer is receiving the message
     * @throws Exception
     */
    private void verifyConsumerPushEvent(ActiveMQDestination destination, ActiveMQSession session) throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
//        URI consumerBrokerUri = new URI(getConsumerBrokerUrl());
//        String expectedRemoteAddress = consumerBrokerUri.toString();
        String expectedRemoteAddress = session.getConnection().getTransport().getRemoteAddress();
        verifier.verifyDiscreteTrace(root(
                ACTIVEMQ_CLIENT, // serviceType
                "ActiveMQ Consumer Invocation", // method
                destination.getQualifiedName(), // rpc
                null, // endPoint (collected but there's no easy way to retrieve local address so skip check)
                expectedRemoteAddress // remoteAddress
        ));
    }

    private Class<?> getMessageDispatchChannelClass(MessageConsumer consumer) throws NoSuchFieldException, IllegalAccessException {
        final String messageDispatchChannelFieldName = "unconsumedMessages";
        Class<?> consumerClass = consumer.getClass();
        // Need a better way as field names could change in future versions. Comparing classes or class names doesn't
        // work due to class loading issue, and some versions may not have certain implementations of
        // MessageDispatchChannel.
        // Test should be fixed if anything changes in future ActiveMQClient library
        Field messageDispatchChannelField = consumerClass.getDeclaredField(messageDispatchChannelFieldName);
        messageDispatchChannelField.setAccessible(true);
        return messageDispatchChannelField.get(consumer).getClass();
    }

    private String getMessageAsString(Message message) throws JMSException {
        StringBuilder messageStringBuilder = new StringBuilder(message.getClass().getSimpleName());
        if (message instanceof TextMessage) {
            messageStringBuilder.append('{').append(((TextMessage) message).getText()).append('}');
        }
        return messageStringBuilder.toString();
    }

    protected final void assertNoConsumerError(Collection<Throwable> consumerThrowables) {
        Assert.assertTrue("Consumer Error : " + consumerThrowables.toString(), consumerThrowables.isEmpty());
    }

    protected final void awaitAndVerifyTraceCount(int expectedTraceCount, long maxWaitMs) throws InterruptedException {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        final long waitIntervalMs = 100L;
        long maxWaitTime = maxWaitMs;
        if (maxWaitMs < waitIntervalMs) {
            maxWaitTime = waitIntervalMs;
        }
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < maxWaitTime) {
            try {
                verifier.verifyTraceCount(expectedTraceCount);
                return;
            } catch (AssertionError e) {
                // ignore and retry
                Thread.sleep(waitIntervalMs);
            }
        }
        verifier.printCache();
        verifier.verifyTraceCount(expectedTraceCount);
    }
}
