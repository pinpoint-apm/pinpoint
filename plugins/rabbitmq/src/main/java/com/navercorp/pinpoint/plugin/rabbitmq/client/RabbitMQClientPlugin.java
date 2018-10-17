/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.rabbitmq.client;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.instrument.ClassFilters;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.List;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Jinkai.Ma
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
public class RabbitMQClientPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        RabbitMQClientPluginConfig config = new RabbitMQClientPluginConfig(context.getConfig());
        if (!config.isTraceRabbitMQClient()) {
            return;
        }

        addCommonEditors(config.isTraceRabbitMQClientProducer(), config.isTraceRabbitMQClientConsumer());
        if (config.isTraceRabbitMQClientProducer()) {
            addAmqpBasicPropertiesEditor();
        }
        if (config.isTraceRabbitMQClientConsumer()) {
            addAMQChannelEditor(config.getExcludeExchangeFilter());
            addConsumerDispatchEditor(config.getExcludeExchangeFilter());
            addConsumerEditors();
            addCustomConsumerEditors(config.getConsumerClasses());
        }

        addSpringAmqpSupport(config.isTraceRabbitMQClientProducer(), config.isTraceRabbitMQClientConsumer());
    }

    private void addAmqpBasicPropertiesEditor() {
        transformTemplate.transform("com.rabbitmq.client.AMQP$BasicProperties", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addSetter("com.navercorp.pinpoint.plugin.rabbitmq.client.field.setter.HeadersFieldSetter", "headers");
                return target.toBytecode();
            }
        });
    }

    private static class ChannelTransformCallback implements TransformCallback {
        private final boolean traceProducer;
        private final boolean traceConsumer;

        private ChannelTransformCallback(boolean traceProducer, boolean traceConsumer) {
            this.traceProducer = traceProducer;
            this.traceConsumer = traceConsumer;
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (traceProducer) {
                // copy AMQP.BasicProperties
                target.weave("com.navercorp.pinpoint.plugin.rabbitmq.client.aspect.ChannelAspect");

                final InstrumentMethod basicPublish = target.getDeclaredMethod("basicPublish", "java.lang.String", "java.lang.String", "boolean", "boolean", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
                if (basicPublish != null) {
                    basicPublish.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.ChannelBasicPublishInterceptor", RabbitMQClientConstants.RABBITMQ_PRODUCER_SCOPE, ExecutionPolicy.BOUNDARY);
                }
            }
            if (traceConsumer) {
                final InstrumentMethod basicGet = target.getDeclaredMethod("basicGet", "java.lang.String", "boolean");
                if (basicGet != null) {
                    basicGet.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.ChannelBasicGetInterceptor", RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE);
                }
            }
            return target.toBytecode();
        }
    }

    private void addCommonEditors(final boolean traceProducer, final boolean traceConsumer) {
        if (!traceProducer && !traceConsumer) {
            return;
        }
        // Channels
        transformTemplate.transform("com.rabbitmq.client.impl.ChannelN", new ChannelTransformCallback(traceProducer, traceConsumer));
        transformTemplate.transform("com.rabbitmq.client.impl.recovery.AutorecoveringChannel", new ChannelTransformCallback(traceProducer, traceConsumer));
        // FrameHandler implementations for end point and remote address
        transformTemplate.transform("com.rabbitmq.client.impl.SocketFrameHandler", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addField("com.navercorp.pinpoint.plugin.rabbitmq.client.field.accessor.LocalAddressAccessor");
                target.addField("com.navercorp.pinpoint.plugin.rabbitmq.client.field.accessor.RemoteAddressAccessor");
                final InstrumentMethod constructor1 = target.getConstructor("java.net.Socket");
                if (constructor1 != null) {
                    constructor1.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.SocketFrameHandlerConstructInterceptor", RabbitMQClientConstants.RABBITMQ_FRAME_HANDLER_CREATION_SCOPE);
                }
                final InstrumentMethod constructor2 = target.getConstructor("java.net.Socket", "java.util.concurrent.ExecutorService");
                if (constructor2 != null) {
                    constructor2.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.SocketFrameHandlerConstructInterceptor", RabbitMQClientConstants.RABBITMQ_FRAME_HANDLER_CREATION_SCOPE);
                }
                return target.toBytecode();
            }
        });
        transformTemplate.transform("com.rabbitmq.client.impl.nio.SocketChannelFrameHandler", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addField("com.navercorp.pinpoint.plugin.rabbitmq.client.field.accessor.LocalAddressAccessor");
                target.addField("com.navercorp.pinpoint.plugin.rabbitmq.client.field.accessor.RemoteAddressAccessor");
                final InstrumentMethod constructor = target.getConstructor("com.rabbitmq.client.impl.nio.SocketChannelFrameHandlerState");
                if (constructor != null) {
                    constructor.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.SocketChannelFrameHandlerConstructInterceptor", RabbitMQClientConstants.RABBITMQ_FRAME_HANDLER_CREATION_SCOPE);
                }
                return target.toBytecode();
            }
        });
        // Envelope - for asynchrnous trace propagation for consumers
        transformTemplate.transform("com.rabbitmq.client.Envelope", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addField(AsyncContextAccessor.class.getName());
                return target.toBytecode();
            }
        });
        // AMQCommand - for pinpoint header propagation
        transformTemplate.transform("com.rabbitmq.client.impl.AMQCommand", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                InstrumentMethod constructor = target.getConstructor("com.rabbitmq.client.Method", "com.rabbitmq.client.impl.AMQContentHeader", "byte[]");
                if (constructor != null) {
                    constructor.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.AMQCommandConstructInterceptor", RabbitMQClientConstants.RABBITMQ_PRODUCER_SCOPE, ExecutionPolicy.INTERNAL);
                }
                return target.toBytecode();
            }
        });
    }

    private void addAMQChannelEditor(final Filter<String> excludeExchangeFilter) {
        transformTemplate.transform("com.rabbitmq.client.impl.AMQChannel", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                final InstrumentMethod handleCompleteInboundCommand = target.getDeclaredMethod("handleCompleteInboundCommand", "com.rabbitmq.client.impl.AMQCommand");
                if (handleCompleteInboundCommand != null) {
                    handleCompleteInboundCommand.addInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.RabbitMQConsumerHandleCompleteInboundCommandInterceptor", va(excludeExchangeFilter));
                }
                return target.toBytecode();
            }
        });
    }

    private void addConsumerDispatchEditor(final Filter<String> excludeExchangeFilter) {
        transformTemplate.transform("com.rabbitmq.client.impl.ConsumerDispatcher", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                final InstrumentMethod handleDelivery = target.getDeclaredMethod("handleDelivery", "com.rabbitmq.client.Consumer", "java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
                if (handleDelivery == null) {
                    return null;
                }
                handleDelivery.addInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.RabbitMQConsumerDispatchInterceptor", va(excludeExchangeFilter));
                target.addGetter("com.navercorp.pinpoint.plugin.rabbitmq.client.field.getter.ChannelGetter", "channel");
                return target.toBytecode();
            }
        });
    }

    private boolean addConsumerHandleDeliveryInterceptor(InstrumentClass target) throws InstrumentException {
        if (target == null) {
            return false;
        }
        final InstrumentMethod handleDelivery = target.getDeclaredMethod("handleDelivery", "java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
        if (handleDelivery == null) {
            return false;
        }
        handleDelivery.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.ConsumerHandleDeliveryInterceptor", RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE);
        return true;
    }

    private void addConsumerEditors() {
        // DefaultConsumer
        transformTemplate.transform("com.rabbitmq.client.DefaultConsumer", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                if (addConsumerHandleDeliveryInterceptor(target)) {
                    return target.toBytecode();
                }
                return null;
            }
        });
        // QueueingConsumer
        transformTemplate.transform("com.rabbitmq.client.QueueingConsumer$Delivery", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                InstrumentMethod constructor = target.getConstructor("com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
                if (constructor == null) {
                    return null;
                }
                constructor.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.DeliveryConstructInterceptor", RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE, ExecutionPolicy.INTERNAL);
                target.addField(AsyncContextAccessor.class.getName());
                return target.toBytecode();
            }
        });
        transformTemplate.transform("com.rabbitmq.client.QueueingConsumer", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                if (addConsumerHandleDeliveryInterceptor(target)) {
                    InstrumentMethod nextDelivery = target.getDeclaredMethod("nextDelivery");
                    if (nextDelivery != null) {
                        nextDelivery.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.QueueingConsumerOnNextInterceptor", RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE);
                    }
                    InstrumentMethod nextDeliveryTimeout = target.getDeclaredMethod("nextDelivery", "long");
                    if (nextDeliveryTimeout != null) {
                        nextDeliveryTimeout.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.QueueingConsumerOnNextInterceptor", RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE);
                    }
                    InstrumentMethod handle = target.getDeclaredMethod("handle", "com.rabbitmq.client.QueueingConsumer$Delivery");
                    if (handle != null) {
                        handle.addInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.QueueingConsumerHandleInterceptor");
                    }
                    return target.toBytecode();
                }
                return null;
            }
        });
    }

    private void addCustomConsumerEditors(List<String> customConsumers) {
        for (String customConsumer : customConsumers) {
            transformTemplate.transform(customConsumer, new TransformCallback() {
                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                    if (addConsumerHandleDeliveryInterceptor(target)) {
                        return target.toBytecode();
                    }
                    // Check inner classes for consumer implementations
                    for (InstrumentClass potentialConsumer : target.getNestedClasses(ClassFilters.ACCEPT_ALL)) {
                        if (potentialConsumer.hasMethod("handleDelivery", "java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]")) {
                            instrumentor.transform(loader, potentialConsumer.getName(), new TransformCallback() {
                                @Override
                                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                    if (addConsumerHandleDeliveryInterceptor(target)) {
                                        return target.toBytecode();
                                    }
                                    return null;
                                }
                            });
                        }
                    }
                    return null;
                }
            });
        }
    }

    private void addSpringAmqpSupport(final boolean traceProducer, final boolean traceConsumer) {
        if (!traceProducer && !traceConsumer) {
            return;
        }
        // RabbitTemplate
        // public APIs
        final MethodFilter publicApiFilter = MethodFilters.chain(
                MethodFilters.name("execute", "convertAndSend", "convertSendAndReceive", "convertSendAndReceiveAsType",
                        "correlationConvertAndSend", "doSend", "send", "sendAndReceive",
                        "receive", "receiveAndConvert", "receiveAndReply"),
                MethodFilters.modifier(Modifier.PUBLIC));
        transformTemplate.transform("org.springframework.amqp.rabbit.core.RabbitTemplate", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                for (InstrumentMethod publicApi : target.getDeclaredMethods(publicApiFilter)) {
                    publicApi.addScopedInterceptor(BasicMethodInterceptor.class.getName(), va(RabbitMQClientConstants.RABBITMQ_CLIENT_INTERNAL), RabbitMQClientConstants.RABBITMQ_TEMPLATE_API_SCOPE);
                }
                InstrumentMethod invoke = target.getDeclaredMethod("invoke", "org.springframework.amqp.rabbit.core.RabbitOperations$OperationsCallback");
                if (invoke != null) {
                    invoke.addInterceptor(BasicMethodInterceptor.class.getName(), va(RabbitMQClientConstants.RABBITMQ_CLIENT_INTERNAL));
                }

                if (traceConsumer) {
                    // Internal consumer implementations
                    if (!addConsumerHandleDeliveryInterceptor(target)) {
                        // Check inner classes for consumer implementations
                        for (InstrumentClass potentialConsumer : target.getNestedClasses(ClassFilters.ACCEPT_ALL)) {
                            if (potentialConsumer.hasMethod("handleDelivery", "java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]")) {
                                instrumentor.transform(loader, potentialConsumer.getName(), new TransformCallback() {
                                    @Override
                                    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                        InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                        if (addConsumerHandleDeliveryInterceptor(target)) {
                                            return target.toBytecode();
                                        }
                                        return null;
                                    }
                                });
                            }
                        }
                    }
                }

                return target.toBytecode();
            }
        });

        // Spring-amqp rabbit client transformation
        if (traceConsumer) {
            // Message
            transformTemplate.transform("org.springframework.amqp.core.Message", new TransformCallback() {
                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                    target.addField(AsyncContextAccessor.class.getName());
                    return target.toBytecode();
                }
            });

            // Delivery
            // spring-rabbit pre-1.7.0
            transformTemplate.transform("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$Delivery", new TransformCallback() {
                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                    // spring-rabbit pre-1.4.2
                    InstrumentMethod constructor1 = target.getConstructor("com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
                    if (constructor1 != null) {
                        constructor1.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.DeliveryConstructInterceptor", RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE, ExecutionPolicy.INTERNAL);
                    }
                    // spring-rabbit 1.4.2 to 1.6.x
                    InstrumentMethod constructor2 = target.getConstructor("java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
                    if (constructor2 != null) {
                        constructor2.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.DeliveryConstructInterceptor", RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE, ExecutionPolicy.INTERNAL);
                    }
                    target.addField(AsyncContextAccessor.class.getName());
                    return target.toBytecode();
                }
            });
            // spring-rabbit 1.7.0+
            transformTemplate.transform("org.springframework.amqp.rabbit.support.Delivery", new TransformCallback() {
                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                    InstrumentMethod constructor = target.getConstructor("java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
                    if (constructor != null) {
                        constructor.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.DeliveryConstructInterceptor", RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE, ExecutionPolicy.INTERNAL);
                    }
                    // spring-rabbit 2.1.0+
                    InstrumentMethod constructor_2_1_0 = target.getConstructor("java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]", "java.lang.String");
                    if (constructor_2_1_0 != null) {
                        constructor_2_1_0.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.DeliveryConstructInterceptor", RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE, ExecutionPolicy.INTERNAL);
                    }
                    target.addField(AsyncContextAccessor.class.getName());
                    return target.toBytecode();
                }
            });

            // BlockingQueueConsumer
            transformTemplate.transform("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer", new TransformCallback() {
                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                    InstrumentMethod nextMessage = target.getDeclaredMethod("nextMessage");
                    if (nextMessage != null) {
                        nextMessage.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.QueueingConsumerOnNextInterceptor", RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE);
                    }
                    InstrumentMethod nextMessageTimeout = target.getDeclaredMethod("nextMessage", "long");
                    if (nextMessageTimeout != null) {
                        nextMessageTimeout.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.QueueingConsumerOnNextInterceptor", RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE);
                    }
                    // spring-rabbit pre-1.7.0
                    InstrumentMethod handle1 = target.getDeclaredMethod("handle", "org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$Delivery");
                    if (handle1 != null) {
                        handle1.addInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.QueueingConsumerHandleInterceptor");
                    }
                    // spring-rabbit 1.7.0+
                    InstrumentMethod handle2 = target.getDeclaredMethod("handle", "org.springframework.amqp.rabbit.support.Delivery");
                    if (handle2 != null) {
                        handle2.addInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.QueueingConsumerHandleInterceptor");
                    }
                    return target.toBytecode();
                }
            });
            transformTemplate.transform("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$InternalConsumer", new TransformCallback() {
                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                    if (addConsumerHandleDeliveryInterceptor(target)) {
                        return target.toBytecode();
                    }
                    return null;
                }
            });
            // Spring-rabbit 1.7.7+, 2.0.3+
            transformTemplate.transform("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$ConsumerDecorator", new TransformCallback() {
                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                    if (addConsumerHandleDeliveryInterceptor(target)) {
                        return target.toBytecode();
                    }
                    return null;
                }
            });
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
