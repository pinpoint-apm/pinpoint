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
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

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
        addChannelEditors(config.isTraceRabbitMQClientProducer(), config.isTraceRabbitMQClientConsumer());
        if (config.isTraceRabbitMQClientProducer()) {
            addAmqpBasicPropertiesEditor();
        }
        if (config.isTraceRabbitMQClientConsumer()) {
            addAMQChannelEditor(config.getExcludeExchangeFilter());
            addConsumerDispatchEditor(config.getExcludeExchangeFilter());
            addConsumerEditors(config.getConsumerClasses());
            addSpringAmqpSupport();
        }
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
                final InstrumentMethod basicPublish = target.getDeclaredMethod("basicPublish", "java.lang.String", "java.lang.String", "boolean", "boolean", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
                if (basicPublish != null) {
                    basicPublish.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.ChannelBasicPublishInterceptor", RabbitMQClientConstants.RABBITMQ_SCOPE);
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

    private void addChannelEditors(final boolean traceProducer, final boolean traceConsumer) {
        if (!traceProducer && !traceConsumer) {
            return;
        }
        transformTemplate.transform("com.rabbitmq.client.impl.ChannelN", new ChannelTransformCallback(traceProducer, traceConsumer));
        transformTemplate.transform("com.rabbitmq.client.impl.recovery.AutorecoveringChannel", new ChannelTransformCallback(traceProducer, traceConsumer));
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

    private InstrumentClass addConsumerHandleDeliveryInterceptor(InstrumentClass target) throws InstrumentException {
        if (target == null) {
            return null;
        }
        final InstrumentMethod handleDelivery = target.getDeclaredMethod("handleDelivery", "java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
        if (handleDelivery == null) {
            return null;
        }
        handleDelivery.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.ConsumerHandleDeliveryInterceptor", RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE);
        target.addField(AsyncContextAccessor.class.getName());
        return target;
    }

    private void addConsumerEditors(List<String> customConsumers) {
        // DefaultConsumer
        transformTemplate.transform("com.rabbitmq.client.DefaultConsumer", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target = addConsumerHandleDeliveryInterceptor(target);
                if (target == null) {
                    return null;
                }
                return target.toBytecode();
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
                target = addConsumerHandleDeliveryInterceptor(target);
                if (target == null) {
                    return null;
                }
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
        });
        // Custom consumers
        for (String customConsumer : customConsumers) {
            transformTemplate.transform(customConsumer, new TransformCallback() {
                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                    InstrumentClass instrumentedTarget = addConsumerHandleDeliveryInterceptor(target);
                    if (instrumentedTarget != null) {
                        return instrumentedTarget.toBytecode();
                    }
                    // Check inner classes for consumer implementations
                    for (InstrumentClass potentialConsumer : target.getNestedClasses(ClassFilters.ACCEPT_ALL)) {
                        if (potentialConsumer.hasMethod("handleDelivery", "java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]")) {
                            instrumentor.transform(loader, potentialConsumer.getName(), new TransformCallback() {
                                @Override
                                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                    target = addConsumerHandleDeliveryInterceptor(target);
                                    if (target == null) {
                                        return null;
                                    }
                                    return target.toBytecode();
                                }
                            });
                        }
                    }
                    return null;
                }
            });
        }
    }

    private void addSpringAmqpSupport() {
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
        TransformCallback deliveryTransformCallback = new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                InstrumentMethod constructor = target.getConstructor("java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
                if (constructor == null) {
                    return null;
                }
                constructor.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.DeliveryConstructInterceptor", RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE, ExecutionPolicy.INTERNAL);
                target.addField(AsyncContextAccessor.class.getName());
                return target.toBytecode();
            }
        };
        // spring-rabbit pre-1.7.0
        transformTemplate.transform("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$Delivery", deliveryTransformCallback);
        // spring-rabbit 1.7.0+
        transformTemplate.transform("org.springframework.amqp.rabbit.support.Delivery", deliveryTransformCallback);

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
                InstrumentMethod handle = target.getDeclaredMethod("handle", "org.springframework.amqp.rabbit.support.Delivery");
                if (handle != null) {
                    handle.addInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.QueueingConsumerHandleInterceptor");
                }
                return target.toBytecode();
            }
        });
        transformTemplate.transform("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$InternalConsumer", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target = addConsumerHandleDeliveryInterceptor(target);
                if (target == null) {
                    return null;
                }
                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
