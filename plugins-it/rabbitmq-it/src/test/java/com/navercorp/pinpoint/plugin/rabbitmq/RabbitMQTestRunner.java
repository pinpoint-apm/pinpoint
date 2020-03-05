/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.rabbitmq;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.rabbitmq.util.RabbitMQTestConstants;
import com.navercorp.test.pinpoint.plugin.rabbitmq.MessageConverter;
import com.navercorp.test.pinpoint.plugin.rabbitmq.PropagationMarker;
import com.navercorp.test.pinpoint.plugin.rabbitmq.TestConsumer;
import com.navercorp.test.pinpoint.plugin.rabbitmq.TestMessagePuller;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.impl.AMQCommand;
import org.junit.Assert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
class RabbitMQTestRunner {

    private static final Random RANDOM = new Random();

    RabbitMQTestRunner(ConnectionFactory connectionFactory) {
        if (connectionFactory == null) {
            throw new NullPointerException("connectionFactory");
        }
        this.connectionFactory = connectionFactory;
    }

    private final ConnectionFactory connectionFactory;

    void runPushTest() throws Exception {
        int numMessages = RANDOM.nextInt(10) + 1;
        runPushTest(numMessages);
    }

    void runPushTest(int numMessages) throws Exception {

        final String message = "hello rabbit mq";

        // producer side
        final Connection producerConnection = connectionFactory.newConnection();
        final Channel producerChannel = producerConnection.createChannel();

        producerChannel.exchangeDeclare(RabbitMQTestConstants.EXCHANGE, "direct", false);
        producerChannel.queueDeclare(RabbitMQTestConstants.QUEUE_PUSH, false, false, false, null);
        producerChannel.queueBind(RabbitMQTestConstants.QUEUE_PUSH, RabbitMQTestConstants.EXCHANGE, RabbitMQTestConstants.ROUTING_KEY_PUSH);

        for (int i = 0; i < numMessages; i++) {
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            producerChannel.basicPublish(RabbitMQTestConstants.EXCHANGE, RabbitMQTestConstants.ROUTING_KEY_PUSH, false, false, builder.appId("test").build(), message.getBytes());
        }

        producerChannel.close();
        producerConnection.close();

        // consumer side
        final Connection consumerConnection = connectionFactory.newConnection();
        final Channel consumerChannel = consumerConnection.createChannel();
        final String remoteAddress = consumerConnection.getAddress().getHostAddress() + ":" + consumerConnection.getPort();

        consumerChannel.queueDeclare(RabbitMQTestConstants.QUEUE_PUSH, false, false, false, null);

        TestConsumer<String> consumer = new TestConsumer<String>(consumerChannel, MessageConverter.FOR_TEST);
        consumerChannel.basicConsume(RabbitMQTestConstants.QUEUE_PUSH, true, consumer);

        List<String> actualMessages = new ArrayList<String>(numMessages);
        for (int i = 0; i < numMessages; i++) {
            actualMessages.add(consumer.getMessage(10, TimeUnit.SECONDS));
        }

        Assert.assertEquals(numMessages, actualMessages.size());
        for (String actualMessage : actualMessages) {
            Assert.assertEquals(message, actualMessage);
        }

        consumerChannel.close();
        consumerConnection.close();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        // Wait till all traces are recorded (consumer traces are recorded from another thread)
        int expectedTraceCountPerMessage = 6;
        awaitAndVerifyTraceCount(verifier, expectedTraceCountPerMessage * numMessages, 5000L);

        verifier.printCache();

        Class<?> producerChannelClass = producerChannel.getClass();
        Method channelBasicPublish = producerChannelClass.getDeclaredMethod("basicPublish", String.class, String.class, boolean.class, boolean.class, AMQP.BasicProperties.class, byte[].class);
        ExpectedTrace channelBasicPublishTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT, // serviceType
                channelBasicPublish, // method
                null, // rpc
                remoteAddress, // endPoint
                "exchange-" + RabbitMQTestConstants.EXCHANGE, // destinationId
                Expectations.annotation("rabbitmq.exchange", RabbitMQTestConstants.EXCHANGE),
                Expectations.annotation("rabbitmq.routingkey", RabbitMQTestConstants.ROUTING_KEY_PUSH));
        ExpectedTrace rabbitMqConsumerInvocationTrace = Expectations.root(
                RabbitMQTestConstants.RABBITMQ_CLIENT, // serviceType
                "RabbitMQ Consumer Invocation", // method
                "rabbitmq://exchange=" + RabbitMQTestConstants.EXCHANGE, // rpc
                null, // endPoint (collected but API to retrieve local address is not available in all versions, so skip)
                remoteAddress, // remoteAddress
                Expectations.annotation("rabbitmq.routingkey", RabbitMQTestConstants.ROUTING_KEY_PUSH));
        Class<?> consumerDispatchClass = Class.forName("com.rabbitmq.client.impl.ConsumerDispatcher");
        Method consumerDispatchHandleDelivery = consumerDispatchClass.getDeclaredMethod("handleDelivery", Consumer.class, String.class, Envelope.class, AMQP.BasicProperties.class, byte[].class);
        ExpectedTrace consumerDispatcherHandleDeliveryTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL,
                consumerDispatchHandleDelivery); // method
        ExpectedTrace asynchronousInvocationTrace = Expectations.event(
                ServiceType.ASYNC.getName(),
                "Asynchronous Invocation");
        Class<?> consumerClass = consumer.getClass();
        Method consumerHandleDelivery = consumerClass.getDeclaredMethod("handleDelivery", String.class, Envelope.class, AMQP.BasicProperties.class, byte[].class);
        ExpectedTrace consumerHandleDeliveryTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL,
                consumerHandleDelivery);
        Class<?> propagationMarkerClass = PropagationMarker.class;
        Method propagationMarkerMark = propagationMarkerClass.getDeclaredMethod("mark");
        ExpectedTrace markTrace = Expectations.event(
                ServiceType.INTERNAL_METHOD.getName(),
                propagationMarkerMark);

        for (int i = 0; i < numMessages; i++) {
            verifier.verifyDiscreteTrace(channelBasicPublishTrace);
            verifier.verifyDiscreteTrace(
                    rabbitMqConsumerInvocationTrace,
                    Expectations.async(
                            consumerDispatcherHandleDeliveryTrace,
                            asynchronousInvocationTrace,
                            consumerHandleDeliveryTrace,
                            markTrace));
        }
        verifier.verifyTraceCount(0);
    }

    void runPullTest() throws Exception {

        final String message = "hello rabbit mq";

        // producer side
        final Connection producerConnection = connectionFactory.newConnection();
        final Channel producerChannel = producerConnection.createChannel();

        producerChannel.exchangeDeclare(RabbitMQTestConstants.EXCHANGE, "direct", false);
        producerChannel.queueDeclare(RabbitMQTestConstants.QUEUE_PULL, false, false, false, null);
        producerChannel.queueBind(RabbitMQTestConstants.QUEUE_PULL, RabbitMQTestConstants.EXCHANGE, RabbitMQTestConstants.ROUTING_KEY_PULL);

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        producerChannel.basicPublish(RabbitMQTestConstants.EXCHANGE, RabbitMQTestConstants.ROUTING_KEY_PULL, false, false, builder.appId("test").build(), message.getBytes());

        producerChannel.close();
        producerConnection.close();

        //comsumer side
        final Connection consumerConnection = connectionFactory.newConnection();
        final Channel consumerChannel = consumerConnection.createChannel();
        final String remoteAddress = consumerConnection.getAddress().getHostAddress() + ":" + consumerConnection.getPort();

        TestMessagePuller messagePuller = new TestMessagePuller(consumerChannel);
        Assert.assertEquals(message, messagePuller.pullMessage(MessageConverter.FOR_TEST, RabbitMQTestConstants.QUEUE_PULL, true));

        consumerChannel.close();
        consumerConnection.close();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        // Wait till all traces are recorded (consumer traces are recorded from another thread)
        awaitAndVerifyTraceCount(verifier, 5, 5000L);

        verifier.printCache();
        // verify producer traces
        Class<?> producerChannelClass = producerChannel.getClass();
        Method channelBasicPublish = producerChannelClass.getDeclaredMethod("basicPublish", String.class, String.class, boolean.class, boolean.class, AMQP.BasicProperties.class, byte[].class);
        ExpectedTrace channelBasicPublishTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT, // serviceType
                channelBasicPublish, // method
                null, // rpc
                remoteAddress, // endPoint
                "exchange-" + RabbitMQTestConstants.EXCHANGE, // destinationId
                Expectations.annotation("rabbitmq.exchange", RabbitMQTestConstants.EXCHANGE),
                Expectations.annotation("rabbitmq.routingkey", RabbitMQTestConstants.ROUTING_KEY_PULL));
        ExpectedTrace rabbitMqConsumerInvocationTrace = Expectations.root(
                RabbitMQTestConstants.RABBITMQ_CLIENT, // serviceType
                "RabbitMQ Consumer Invocation", // method
                "rabbitmq://exchange=" + RabbitMQTestConstants.EXCHANGE, // rpc
                null, // endPoint (collected but API to retrieve local address is not available in all versions, so skip)
                remoteAddress, // remoteAddress
                Expectations.annotation("rabbitmq.routingkey", RabbitMQTestConstants.ROUTING_KEY_PULL));
        Class<?> amqChannelClass = Class.forName("com.rabbitmq.client.impl.AMQChannel");
        Method handleCompleteInboundCommand = amqChannelClass.getDeclaredMethod("handleCompleteInboundCommand", AMQCommand.class);
        ExpectedTrace handleCompleteInboundCommandTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                handleCompleteInboundCommand); // method
        ExpectedTrace[] producerTraces = {channelBasicPublishTrace};
        ExpectedTrace[] consumerTraces = {rabbitMqConsumerInvocationTrace, handleCompleteInboundCommandTrace};
        verifier.verifyDiscreteTrace(producerTraces);
        verifier.verifyDiscreteTrace(consumerTraces);

        // verify consumer traces
        Class<?> consumerChannelClass = consumerChannel.getClass();
        Method channelBasicGet = consumerChannelClass.getDeclaredMethod("basicGet", String.class, boolean.class);
        ExpectedTrace channelBasicGetTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL,
                channelBasicGet);
        Class<?> propagationMarkerClass = PropagationMarker.class;
        Method propagationMarkerMark = propagationMarkerClass.getDeclaredMethod("mark");
        ExpectedTrace markTrace = Expectations.event(
                ServiceType.INTERNAL_METHOD.getName(),
                propagationMarkerMark);
        verifier.verifyDiscreteTrace(
                channelBasicGetTrace,
                markTrace);
        verifier.verifyTraceCount(0);
    }

    private void awaitAndVerifyTraceCount(PluginTestVerifier verifier, int expectedTraceCount, long maxWaitMs) throws InterruptedException {
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
        verifier.verifyTraceCount(expectedTraceCount);
    }
}
