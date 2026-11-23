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

package com.navercorp.pinpoint.it.plugin.rabbitmq.spring;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.it.plugin.rabbitmq.TestBrokerServer;
import com.navercorp.pinpoint.it.plugin.rabbitmq.util.RabbitMQTestConstants;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.api.Dependency;
import com.navercorp.pinpoint.test.plugin.api.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.api.JvmArgument;
import com.navercorp.pinpoint.test.plugin.api.JvmVersion;
import com.navercorp.pinpoint.test.plugin.api.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.api.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.api.PluginTest;
import com.navercorp.pinpoint.test.plugin.api.SharedDependency;
import com.navercorp.pinpoint.test.plugin.api.SharedTestLifeCycleClass;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.impl.AMQCommand;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.pinpoint.plugin.rabbitmq.PropagationMarker;
import test.pinpoint.plugin.rabbitmq.spring.config.CommonConfig;
import test.pinpoint.plugin.rabbitmq.spring.config.MessageListenerConfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * spring-rabbit 3.x (Spring 6) and 4.x (Spring 7) require Java 17.
 * Earlier versions are covered by the <tt>rabbitmq-it</tt> module.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("rabbitmq/client/pinpoint-rabbitmq.config")
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-rabbitmq-plugin", "com.navercorp.pinpoint:pinpoint-user-plugin"})
@Dependency({"org.springframework.amqp:spring-rabbit:[3.0.0,3.max],[4.0.0,4.max]"})
@JvmVersion(17)
@JvmArgument("-DtestLoggerEnable=false")
@SharedDependency({TestcontainersOption.TEST_CONTAINER, TestcontainersOption.RABBITMQ})
@SharedTestLifeCycleClass(TestBrokerServer.class)
public class SpringAmqpRabbit_3_x_to_4_x_IT extends SpringAmqpRabbitITBase {

    @AutoClose
    private static final TestApplicationContext CONTEXT = new TestApplicationContext();

    private final SpringAmqpRabbitTestRunner testRunner = new SpringAmqpRabbitTestRunner(CONTEXT);

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        CONTEXT.init(
                CommonConfig.class,
                MessageListenerConfig.class);
    }

    // SimpleMessageListenerContainer
    @Test
    public void testPush() throws Exception {
        Class<?> blockingQueueConsumerInternalConsumerClass = Class.forName("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$InternalConsumer");
        Method blockingQueueConsumerInternalConsumerHandleDelivery = blockingQueueConsumerInternalConsumerClass.getDeclaredMethod("handleDelivery", String.class, Envelope.class, AMQP.BasicProperties.class, byte[].class);
        ExpectedTrace blockingQueueConsumerInternalConsumerHandleDeliveryTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                blockingQueueConsumerInternalConsumerHandleDelivery);

        ExpectedTrace[] producerTraces = {
                rabbitTemplateConvertAndSendTrace(),
                channelNBasicPublishTrace(RabbitMQTestConstants.ROUTING_KEY_PUSH)
        };
        ExpectedTrace[] consumerTraces = {
                rabbitMqConsumerInvocationTrace(RabbitMQTestConstants.ROUTING_KEY_PUSH),
                consumerDispatcherHandleDeliveryTrace(),
                asynchronousInvocationTrace(),
                blockingQueueConsumerInternalConsumerHandleDeliveryTrace,
                deliveryConstructorTrace(),
                asynchronousInvocationTrace(),
                executeListenerTrace(),
                markTrace()
        };

        final int expectedTraceCount = producerTraces.length + consumerTraces.length;
        final PluginTestVerifier verifier = testRunner.runPush(expectedTraceCount);

        verifier.verifyDiscreteTrace(producerTraces);
        verifier.verifyDiscreteTrace(consumerTraces);
        verifier.verifyTraceCount(0);
    }

    // DirectMessageListenerContainer
    // The plugin has no hook for DirectMessageListenerContainer$SimpleConsumer, so the consumer trace ends at
    // ConsumerDispatcher.handleDelivery and the listener is NOT linked to it: executeListener is not recorded
    // and PropagationMarker.mark() starts a trace of its own.
    // Update the expectations below once the plugin propagates the trace to the listener of this container.
    @Test
    public void testDirectPush() throws Exception {
        ExpectedTrace[] producerTraces = {
                rabbitTemplateConvertAndSendTrace(),
                channelNBasicPublishTrace(RabbitMQTestConstants.ROUTING_KEY_DIRECT_PUSH)
        };
        ExpectedTrace[] consumerTraces = {
                rabbitMqConsumerInvocationTrace(RabbitMQTestConstants.ROUTING_KEY_DIRECT_PUSH),
                consumerDispatcherHandleDeliveryTrace()
        };
        // not propagated: a separate trace started by the profiler.entrypoint setting
        ExpectedTrace[] detachedListenerTraces = {
                Expectations.root(ServiceType.STAND_ALONE.getName(), "Entry Point Process", null, null, null),
                markTrace()
        };

        final int expectedTraceCount = producerTraces.length + consumerTraces.length + detachedListenerTraces.length;
        final PluginTestVerifier verifier = testRunner.runDirectPush(expectedTraceCount);

        verifier.verifyDiscreteTrace(producerTraces);
        verifier.verifyDiscreteTrace(consumerTraces);
        verifier.verifyDiscreteTrace(detachedListenerTraces);
        verifier.verifyTraceCount(0);
    }

    @Test
    public void testPull() throws Exception {
        Class<?> rabbitTemplateClass = Class.forName("org.springframework.amqp.rabbit.core.RabbitTemplate");
        Class<?> channelNClass = Class.forName("com.rabbitmq.client.impl.ChannelN");

        // verify queue-initiated traces
        Class<?> amqChannelClass = Class.forName("com.rabbitmq.client.impl.AMQChannel");
        Method amqChannelHandleCompleteInboundCommand = amqChannelClass.getDeclaredMethod("handleCompleteInboundCommand", AMQCommand.class);
        ExpectedTrace amqChannelHandleCompleteInboundCommandTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // method
                amqChannelHandleCompleteInboundCommand);

        ExpectedTrace[] producerTraces = {
                rabbitTemplateConvertAndSendTrace(),
                channelNBasicPublishTrace(RabbitMQTestConstants.ROUTING_KEY_PULL)
        };
        ExpectedTrace[] consumerTraces = {
                rabbitMqConsumerInvocationTrace(RabbitMQTestConstants.ROUTING_KEY_PULL),
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

        ExpectedTrace[] clientInitiatedTraces = {
                rabbitTemplateReceiveTrace,
                channelNBasicGetTrace,
                markTrace()
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
        Class<?> rabbitTemplateClass = Class.forName("org.springframework.amqp.rabbit.core.RabbitTemplate");

        // verify queue-initiated traces
        // RabbitTemplate internal consumer implementation - may change in future versions which will cause tests to
        // fail, in which case the integration test needs to be updated to match code changes
        Class<?> rabbitTemplateInternalConsumerClass = getRabbitTemplateInternalConsumerClass();
        Method rabbitTemplateInternalConsumerHandleDelivery = rabbitTemplateInternalConsumerClass.getDeclaredMethod("handleDelivery", String.class, Envelope.class, AMQP.BasicProperties.class, byte[].class);
        ExpectedTrace rabbitTemplateInternalConsumerHandleDeliveryTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                rabbitTemplateInternalConsumerHandleDelivery); // method

        ExpectedTrace[] producerTraces = {
                rabbitTemplateConvertAndSendTrace(),
                channelNBasicPublishTrace(RabbitMQTestConstants.ROUTING_KEY_PULL)
        };
        ExpectedTrace[] consumerTraces = {
                rabbitMqConsumerInvocationTrace(RabbitMQTestConstants.ROUTING_KEY_PULL),
                consumerDispatcherHandleDeliveryTrace(),
                asynchronousInvocationTrace(),
                rabbitTemplateInternalConsumerHandleDeliveryTrace,
                deliveryConstructorTrace()
        };

        // verify client-initiated traces
        Method rabbitTemplateReceive = rabbitTemplateClass.getDeclaredMethod("receive", String.class, long.class);
        ExpectedTrace rabbitTemplateReceiveTrace = Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                rabbitTemplateReceive); // method

        ExpectedTrace[] clientInitiatedTraces = {
                rabbitTemplateReceiveTrace,
                markTrace()
        };

        final int expectedTraceCount = producerTraces.length + consumerTraces.length + clientInitiatedTraces.length;
        final PluginTestVerifier verifier = testRunner.runPull(expectedTraceCount, 5000L);

        verifier.verifyDiscreteTrace(producerTraces);
        verifier.verifyDiscreteTrace(consumerTraces);
        verifier.verifyDiscreteTrace(clientInitiatedTraces);

        verifier.verifyTraceCount(0);
    }

    private ExpectedTrace rabbitTemplateConvertAndSendTrace() throws Exception {
        Class<?> rabbitTemplateClass = Class.forName("org.springframework.amqp.rabbit.core.RabbitTemplate");
        Method rabbitTemplateConvertAndSend = rabbitTemplateClass.getDeclaredMethod("convertAndSend", String.class, String.class, Object.class);
        return Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                rabbitTemplateConvertAndSend); // method
    }

    private ExpectedTrace channelNBasicPublishTrace(String routingKey) throws Exception {
        // automatic recovery deliberately disabled as Spring has it's own recovery mechanism
        Class<?> channelNClass = Class.forName("com.rabbitmq.client.impl.ChannelN");
        Method channelNBasicPublish = channelNClass.getDeclaredMethod("basicPublish", String.class, String.class, boolean.class, boolean.class, AMQP.BasicProperties.class, byte[].class);
        return Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT, // serviceType
                channelNBasicPublish, // method
                null, // rpc
                testRunner.getRemoteAddress(), // endPoint
                "exchange-" + RabbitMQTestConstants.EXCHANGE, // destinationId
                Expectations.annotation("rabbitmq.exchange", RabbitMQTestConstants.EXCHANGE),
                Expectations.annotation("rabbitmq.routingkey", routingKey));
    }

    private ExpectedTrace rabbitMqConsumerInvocationTrace(String routingKey) {
        return Expectations.root(
                RabbitMQTestConstants.RABBITMQ_CLIENT, // serviceType
                "RabbitMQ Consumer Invocation", // method
                "rabbitmq://exchange=" + RabbitMQTestConstants.EXCHANGE, // rpc
                null, // endPoint (collected but API to retrieve local address is not available in all versions, so skip)
                testRunner.getRemoteAddress(), // remoteAddress
                Expectations.annotation("rabbitmq.routingkey", routingKey));
    }

    private ExpectedTrace consumerDispatcherHandleDeliveryTrace() throws Exception {
        Class<?> consumerDispatcherClass = Class.forName("com.rabbitmq.client.impl.ConsumerDispatcher");
        Method consumerDispatcherHandleDelivery = consumerDispatcherClass.getDeclaredMethod("handleDelivery", Consumer.class, String.class, Envelope.class, AMQP.BasicProperties.class, byte[].class);
        return Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                consumerDispatcherHandleDelivery); // method
    }

    private ExpectedTrace asynchronousInvocationTrace() {
        return Expectations.event(
                ServiceType.ASYNC.getName(),
                "Asynchronous Invocation");
    }

    private ExpectedTrace deliveryConstructorTrace() throws Exception {
        Class<?> deliveryClass = Class.forName("org.springframework.amqp.rabbit.support.Delivery");
        Constructor<?> deliveryConstructor = deliveryClass.getDeclaredConstructor(String.class, Envelope.class, AMQP.BasicProperties.class, byte[].class, String.class);
        return Expectations.event(
                RabbitMQTestConstants.RABBITMQ_CLIENT_INTERNAL, // serviceType
                deliveryConstructor);
    }

    private ExpectedTrace executeListenerTrace() throws Exception {
        Class<?> abstractMessageListenerContainerClass = Class.forName("org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer");
        Method executeListener = abstractMessageListenerContainerClass.getDeclaredMethod("executeListener", Channel.class, Object.class);
        return Expectations.event(
                ServiceType.INTERNAL_METHOD.getName(),
                executeListener);
    }

    private ExpectedTrace markTrace() throws Exception {
        Method propagationMarkerMark = PropagationMarker.class.getDeclaredMethod("mark");
        return Expectations.event(
                ServiceType.INTERNAL_METHOD.getName(),
                propagationMarkerMark);
    }

    // anonymous DefaultConsumer of RabbitTemplate.createConsumer(), used by RabbitTemplate.receive(queueName, timeoutMillis).
    // RabbitTemplate.doSendAndReceiveWithTemporary() has another anonymous consumer, so the index alone is not enough.
    private Class<?> getRabbitTemplateInternalConsumerClass() {
        for (int index = 1; index <= 10; index++) {
            final Class<?> nestedClass;
            try {
                nestedClass = Class.forName("org.springframework.amqp.rabbit.core.RabbitTemplate$" + index);
            } catch (ClassNotFoundException ignored) {
                continue;
            }
            Method enclosingMethod = nestedClass.getEnclosingMethod();
            if (Consumer.class.isAssignableFrom(nestedClass) && enclosingMethod != null && enclosingMethod.getName().equals("createConsumer")) {
                return nestedClass;
            }
        }
        throw new IllegalArgumentException("Failed to find RabbitTemplate internal consumer class");
    }
}
