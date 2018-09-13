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

package com.navercorp.pinpoint.plugin.jdk7.rabbitmq.spring;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.AgentPath;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.test.pinpoint.plugin.rabbitmq.spring.config.CommonConfig;
import com.navercorp.test.pinpoint.plugin.rabbitmq.spring.config.MessageListenerConfig_Post_1_4_0;
import com.navercorp.test.pinpoint.plugin.rabbitmq.spring.config.ReceiverConfig_Pre_1_6_0;
import com.navercorp.pinpoint.plugin.jdk7.rabbitmq.util.RabbitMQTestConstants;
import com.navercorp.pinpoint.plugin.jdk7.rabbitmq.util.TestBroker;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.test.pinpoint.plugin.rabbitmq.PropagationMarker;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.impl.AMQCommand;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("rabbitmq/client/pinpoint-rabbitmq.config")
// 1.4.5, 1.4.6, 1.6.4.RELEASE has dependency issues
@Dependency({"org.springframework.amqp:spring-rabbit:[1.4.2.RELEASE,1.4.5.RELEASE),[1.5.0.RELEASE,1.6.4.RELEASE),[1.6.5.RELEASE,1.7.0.RELEASE)", "com.fasterxml.jackson.core:jackson-core:2.8.11", "org.apache.qpid:qpid-broker:6.1.1"})
public class SpringAmqpRabbit_1_4_2_to_1_7_0_IT {

    private static final TestBroker BROKER = new TestBroker();
    private static final TestApplicationContext CONTEXT = new TestApplicationContext();

    private final SpringAmqpRabbitTestRunner testRunner = new SpringAmqpRabbitTestRunner(CONTEXT);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        BROKER.start();
        CONTEXT.init(
                CommonConfig.class,
                MessageListenerConfig_Post_1_4_0.class,
                ReceiverConfig_Pre_1_6_0.class);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        CONTEXT.close();
        BROKER.shutdown();
    }

    @Test
    public void testPush() throws Exception {
        final String remoteAddress = testRunner.getRemoteAddress();

        Class<?> rabbitTemplateClass = Class.forName("org.springframework.amqp.rabbit.core.RabbitTemplate");
        Method rabbitTemplateConvertAndSend = rabbitTemplateClass.getDeclaredMethod("convertAndSend", String.class, String.class, Object.class);
        ExpectedTrace rabbitTemplateConvertAndSendTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                rabbitTemplateConvertAndSend); // method
        // automatic recovery deliberately disabled as Spring has it's own recovery mechanism
        Class<?> channelNClass = Class.forName("com.rabbitmq.client.impl.ChannelN");
        Method channelNBasicPublish = channelNClass.getDeclaredMethod("basicPublish", String.class, String.class, boolean.class, boolean.class, AMQP.BasicProperties.class, byte[].class);
        ExpectedTrace channelNBasicPublishTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT, // serviceType
                channelNBasicPublish, // method
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
        Class<?> consumerDispatcherClass = Class.forName("com.rabbitmq.client.impl.ConsumerDispatcher");
        Method consumerDispatcherHandleDelivery = consumerDispatcherClass.getDeclaredMethod("handleDelivery", Consumer.class, String.class, Envelope.class, AMQP.BasicProperties.class, byte[].class);
        ExpectedTrace consumerDispatcherHandleDeliveryTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                consumerDispatcherHandleDelivery); // method
        ExpectedTrace asynchronousInvocationTrace = Expectations.event(
                ServiceType.ASYNC.getName(),
                "Asynchronous Invocation");
        Class<?> blockingQueueConsumerInternalConsumerClass = Class.forName("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$InternalConsumer");
        Method blockingQueueConsumerInternalConsumerHandleDelivery = blockingQueueConsumerInternalConsumerClass.getDeclaredMethod("handleDelivery", String.class, Envelope.class, AMQP.BasicProperties.class, byte[].class);
        ExpectedTrace blockingQueueConsumerInternalConsumerHandleDeliveryTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                blockingQueueConsumerInternalConsumerHandleDelivery);
        Class<?> deliveryClass = Class.forName("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$Delivery");
        Constructor<?> deliveryConstructor = deliveryClass.getDeclaredConstructor(String.class, Envelope.class, AMQP.BasicProperties.class, byte[].class);
        ExpectedTrace deliveryConstructorTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                deliveryConstructor);
        Class<?> abstractMessageListenerContainerClass = Class.forName("org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer");
        Method abstractMessageListenerContainerExecuteListener = abstractMessageListenerContainerClass.getDeclaredMethod("executeListener", Channel.class, Message.class);
        ExpectedTrace abstractMessageListenerContainerExecuteListenerTrace = Expectations.event(
                ServiceType.INTERNAL_METHOD.getName(),
                abstractMessageListenerContainerExecuteListener);
        Class<?> propagationMarkerClass = PropagationMarker.class;
        Method propagationMarkerMark = propagationMarkerClass.getDeclaredMethod("mark");
        ExpectedTrace markTrace = Expectations.event(
                ServiceType.INTERNAL_METHOD.getName(),
                propagationMarkerMark);

        ExpectedTrace[] expectedTraces = {
                rabbitTemplateConvertAndSendTrace,
                channelNBasicPublishTrace,
                rabbitMqConsumerInvocationTrace,
                consumerDispatcherHandleDeliveryTrace,
                asynchronousInvocationTrace,
                blockingQueueConsumerInternalConsumerHandleDeliveryTrace,
                deliveryConstructorTrace,
                asynchronousInvocationTrace,
                abstractMessageListenerContainerExecuteListenerTrace,
                markTrace
        };

        final PluginTestVerifier verifier = testRunner.runPush(expectedTraces.length);

        verifier.verifyTrace(expectedTraces);
        verifier.verifyTraceCount(0);
    }

    @Test
    public void testPull() throws Exception {
        final String remoteAddress = testRunner.getRemoteAddress();

        Class<?> rabbitTemplateClass = Class.forName("org.springframework.amqp.rabbit.core.RabbitTemplate");
        // verify queue-initiated traces
        Method rabbitTemplateConvertAndSend = rabbitTemplateClass.getDeclaredMethod("convertAndSend", String.class, String.class, Object.class);
        ExpectedTrace rabbitTemplateConvertAndSendTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                rabbitTemplateConvertAndSend); // method
        // automatic recovery deliberately disabled as Spring has it's own recovery mechanism
        Class<?> channelNClass = Class.forName("com.rabbitmq.client.impl.ChannelN");
        Method channelNBasicPublish = channelNClass.getDeclaredMethod("basicPublish", String.class, String.class, boolean.class, boolean.class, AMQP.BasicProperties.class, byte[].class);
        ExpectedTrace channelNBasicPublishTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT, // serviceType
                channelNBasicPublish, // method
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
        Method amqChannelHandleCompleteInboundCommand = amqChannelClass.getDeclaredMethod("handleCompleteInboundCommand", AMQCommand.class);
        ExpectedTrace amqChannelHandleCompleteInboundCommandTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // method
                amqChannelHandleCompleteInboundCommand);

        ExpectedTrace[] queueInitiatedTraces = {
                rabbitTemplateConvertAndSendTrace,
                channelNBasicPublishTrace,
                rabbitMqConsumerInvocationTrace,
                amqChannelHandleCompleteInboundCommandTrace
        };

        // verify client-initiated traces
        Method rabbitTemplateReceive = rabbitTemplateClass.getDeclaredMethod("receive", String.class);
        ExpectedTrace rabbitTemplateReceiveTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                rabbitTemplateReceive); // method
        Method channelNBasicGet = channelNClass.getDeclaredMethod("basicGet", String.class, boolean.class);
        ExpectedTrace channelNBasicGetTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL,
                channelNBasicGet);
        Class<?> propagationMarkerClass = PropagationMarker.class;
        Method propagationMarkerMark = propagationMarkerClass.getDeclaredMethod("mark");
        ExpectedTrace markTrace = Expectations.event(
                ServiceType.INTERNAL_METHOD.getName(),
                propagationMarkerMark);

        ExpectedTrace[] clientInitiatedTraces = {
                rabbitTemplateReceiveTrace,
                channelNBasicGetTrace,
                markTrace
        };

        int expectedTraceCount = queueInitiatedTraces.length + clientInitiatedTraces.length;
        final PluginTestVerifier verifier = testRunner.runPull(expectedTraceCount);

        verifier.verifyDiscreteTrace(queueInitiatedTraces);
        verifier.verifyDiscreteTrace(clientInitiatedTraces);

        verifier.verifyTraceCount(0);
    }
}
