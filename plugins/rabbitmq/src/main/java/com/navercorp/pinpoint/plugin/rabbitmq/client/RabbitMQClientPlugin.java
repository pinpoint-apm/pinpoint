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
        if (config.isTraceRabbitMQClientProducer()) {
            addAmqpBasicPropertiesEditor();
            addChannelEditors();
        }
        if (config.isTraceRabbitMQClientConsumer()) {
            addConsumerTransformers(config.getConsumerClasses());
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

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final InstrumentMethod basicPublish = target.getDeclaredMethod("basicPublish", "java.lang.String", "java.lang.String", "boolean", "boolean", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
            if (basicPublish != null) {
                basicPublish.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.ChannelBasicPublishInterceptor", RabbitMQClientConstants.RABBITMQ_SCOPE);
            }
            return target.toBytecode();
        }
    }

    private void addChannelEditors() {
        transformTemplate.transform("com.rabbitmq.client.impl.ChannelN", new ChannelTransformCallback());
        transformTemplate.transform("com.rabbitmq.client.impl.recovery.AutorecoveringChannel", new ChannelTransformCallback());
    }

    private static class ConsumerHandleDeliveryTransformCallback implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final InstrumentMethod method = target.getDeclaredMethod("handleDelivery", "java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
            if (method == null) {
                return null;
            }
            method.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.ConsumerHandleDeliveryInterceptor", RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE);
            return target.toBytecode();
        }
    }

    private void addConsumerTransformers(List<String> customConsumers) {
        transformTemplate.transform("com.rabbitmq.client.QueueingConsumer", new ConsumerHandleDeliveryTransformCallback());
        transformTemplate.transform("com.rabbitmq.client.DefaultConsumer", new ConsumerHandleDeliveryTransformCallback());
        // Custom consumers
        for (String customConsumer : customConsumers) {
            transformTemplate.transform(customConsumer, new ConsumerHandleDeliveryTransformCallback() {
                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    byte[] transformedConsumerByteCode = super.doInTransform(instrumentor, loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
                    if (transformedConsumerByteCode != null) {
                        return transformedConsumerByteCode;
                    }
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                    // Check inner classes for consumer implementations
                    for (InstrumentClass potentialConsumer : target.getNestedClasses(ClassFilters.ACCEPT_ALL)) {
                        if (potentialConsumer.hasMethod("handleDelivery", "java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]")) {
                            instrumentor.transform(loader, potentialConsumer.getName(), new ConsumerHandleDeliveryTransformCallback());
                        }
                    }
                    return null;
                }
            });
        }
    }

    private void addSpringAmqpSupport() {
        transformTemplate.transform("org.springframework.amqp.core.Message", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addField(AsyncContextAccessor.class.getName());
                return target.toBytecode();
            }
        });
        transformTemplate.transform("org.springframework.amqp.rabbit.support.Delivery", new TransformCallback() {
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
        });
        transformTemplate.transform("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                InstrumentMethod handleMethod = target.getDeclaredMethod("handle", "org.springframework.amqp.rabbit.support.Delivery");
                if (handleMethod != null) {
                    handleMethod.addInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.BlockingQueueConsumerHandleInterceptor");
                }
                return target.toBytecode();
            }
        });
        transformTemplate.transform("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$InternalConsumer", new ConsumerHandleDeliveryTransformCallback());
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
