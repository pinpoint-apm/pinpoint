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

package com.navercorp.pinpoint.plugin.rabbitmq.spring;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.rabbitmq.util.RabbitMQTestConstants;
import com.navercorp.pinpoint.plugin.rabbitmq.util.TestBroker;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.test.pinpoint.plugin.rabbitmq.PropagationMarker;
import com.navercorp.test.pinpoint.plugin.rabbitmq.spring.config.CommonConfig;
import com.navercorp.test.pinpoint.plugin.rabbitmq.spring.config.MessageListenerConfig_Post_1_4_0;
import com.navercorp.test.pinpoint.plugin.rabbitmq.spring.config.ReceiverConfig_Post_1_6_0;
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
 * <p>Spring-amqp rabbit 2.0.3 introduced <tt>BlockingQueueConsumer$ConsumerDecorator</tt>, which wraps
 * <tt>BlockingQueueConsumer$InternalConsumer</tt>.
 *
 * <p>This affects asynchronous trace propagation as async trace object now gets attached to
 * <tt>BlockingQueueConsumer$ConsumerDecorator</tt> instead of the previous
 * <tt>BlockingQueueConsumer$InternalConsumer</tt>.
 *
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("rabbitmq/client/pinpoint-rabbitmq.config")
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-rabbitmq-plugin", "com.navercorp.pinpoint:pinpoint-jetty-plugin", "com.navercorp.pinpoint:pinpoint-user-plugin"})
@Dependency({"org.springframework.amqp:spring-rabbit:[2.0.3.RELEASE,2.1.0.RELEASE)", "com.fasterxml.jackson.core:jackson-core:2.8.11", "org.apache.qpid:qpid-broker:6.1.1"})
@JvmVersion(8)
public class SpringAmqpRabbit_2_0_3_to_2_1_0_IT {

    private static final TestBroker BROKER = new TestBroker();
    private static final TestApplicationContext CONTEXT = new TestApplicationContext();

    private final SpringAmqpRabbitTestRunner testRunner = new SpringAmqpRabbitTestRunner(CONTEXT);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        BROKER.start();
        CONTEXT.init(
                CommonConfig.class,
                MessageListenerConfig_Post_1_4_0.class,
                ReceiverConfig_Post_1_6_0.class);
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
        Class<?> blockingQueueConsumerConsumerDecoratorClass = Class.forName("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$ConsumerDecorator");
        Method blockingQueueConsumerConsumerDecoratorHandleDelivery = blockingQueueConsumerConsumerDecoratorClass.getDeclaredMethod("handleDelivery", String.class, Envelope.class, AMQP.BasicProperties.class, byte[].class);
        ExpectedTrace blockingQueueConsumerConsumerDecoratorHandleDeliveryTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                blockingQueueConsumerConsumerDecoratorHandleDelivery);
        Class<?> deliveryClass = Class.forName("org.springframework.amqp.rabbit.support.Delivery");
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

        ExpectedTrace[] producerTraces = {
                rabbitTemplateConvertAndSendTrace,
                channelNBasicPublishTrace
        };
        ExpectedTrace[] consumerTraces = {
                rabbitMqConsumerInvocationTrace,
                consumerDispatcherHandleDeliveryTrace,
                asynchronousInvocationTrace,
                blockingQueueConsumerConsumerDecoratorHandleDeliveryTrace,
                deliveryConstructorTrace,
                asynchronousInvocationTrace,
                abstractMessageListenerContainerExecuteListenerTrace,
                markTrace
        };

        final int expectedTraceCount = producerTraces.length + consumerTraces.length;
        final PluginTestVerifier verifier = testRunner.runPush(expectedTraceCount);

        verifier.verifyDiscreteTrace(producerTraces);
        verifier.verifyDiscreteTrace(consumerTraces);
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

        ExpectedTrace[] producerTraces = {
                rabbitTemplateConvertAndSendTrace,
                channelNBasicPublishTrace
        };
        ExpectedTrace[] consumerTraces = {
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

        final int expectedTraceCount = producerTraces.length + consumerTraces.length + clientInitiatedTraces.length;
        final PluginTestVerifier verifier = testRunner.runPull(expectedTraceCount);

        verifier.verifyDiscreteTrace(producerTraces);
        verifier.verifyDiscreteTrace(consumerTraces);
        verifier.verifyDiscreteTrace(clientInitiatedTraces);

        verifier.verifyTraceCount(0);
    }

    @Test
    public void testPullWithTimeout() throws Exception {
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
        Class<?> consumerDispatcherClass = Class.forName("com.rabbitmq.client.impl.ConsumerDispatcher");
        Method consumerDispatcherHandleDelivery = consumerDispatcherClass.getDeclaredMethod("handleDelivery", Consumer.class, String.class, Envelope.class, AMQP.BasicProperties.class, byte[].class);
        ExpectedTrace consumerDispatcherHandleDeliveryTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                consumerDispatcherHandleDelivery); // method
        ExpectedTrace asynchronousInvocationTrace = Expectations.event(
                ServiceType.ASYNC.getName(),
                "Asynchronous Invocation");
        // RabbitTemplate internal consumer implementation - may change in future versions which will cause tests to
        // fail, in which case the integration test needs to be updated to match code changes
        Class<?> rabbitTemplateInternalConsumerClass = Class.forName("org.springframework.amqp.rabbit.core.RabbitTemplate$2");
        Method rabbitTemplateInternalConsumerHandleDelivery = rabbitTemplateInternalConsumerClass.getDeclaredMethod("handleDelivery", String.class, Envelope.class, AMQP.BasicProperties.class, byte[].class);
        ExpectedTrace rabbitTemplateInternalConsumerHandleDeliveryTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                rabbitTemplateInternalConsumerHandleDelivery); // method
        Class<?> deliveryClass = Class.forName("org.springframework.amqp.rabbit.support.Delivery");
        Constructor<?> deliveryConstructor = deliveryClass.getDeclaredConstructor(String.class, Envelope.class, AMQP.BasicProperties.class, byte[].class);
        ExpectedTrace deliveryConstructorTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                deliveryConstructor);

        ExpectedTrace[] producerTraces = {
                rabbitTemplateConvertAndSendTrace,
                channelNBasicPublishTrace
        };
        ExpectedTrace[] consumerTraces = {
                rabbitMqConsumerInvocationTrace,
                consumerDispatcherHandleDeliveryTrace,
                asynchronousInvocationTrace,
                rabbitTemplateInternalConsumerHandleDeliveryTrace,
                deliveryConstructorTrace
        };

        // verify client-initiated traces
        Method rabbitTemplateReceive = rabbitTemplateClass.getDeclaredMethod("receive", String.class, long.class);
        ExpectedTrace rabbitTemplateReceiveTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                rabbitTemplateReceive); // method
        Class<?> propagationMarkerClass = PropagationMarker.class;
        Method propagationMarkerMark = propagationMarkerClass.getDeclaredMethod("mark");
        ExpectedTrace markTrace = Expectations.event(
                ServiceType.INTERNAL_METHOD.getName(),
                propagationMarkerMark);

        ExpectedTrace[] clientInitiatedTraces = {
                rabbitTemplateReceiveTrace,
                markTrace
        };

        final int expectedTraceCount = producerTraces.length + consumerTraces.length + clientInitiatedTraces.length;
        final PluginTestVerifier verifier = testRunner.runPull(expectedTraceCount, 5000L);

        verifier.verifyDiscreteTrace(producerTraces);
        verifier.verifyDiscreteTrace(consumerTraces);
        verifier.verifyDiscreteTrace(clientInitiatedTraces);

        verifier.verifyTraceCount(0);
    }
}
