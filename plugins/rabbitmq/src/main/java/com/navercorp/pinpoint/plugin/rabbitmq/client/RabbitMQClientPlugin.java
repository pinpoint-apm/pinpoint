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
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.rabbitmq.client.field.accessor.LocalAddressAccessor;
import com.navercorp.pinpoint.plugin.rabbitmq.client.field.accessor.RemoteAddressAccessor;
import com.navercorp.pinpoint.plugin.rabbitmq.client.field.getter.ChannelGetter;
import com.navercorp.pinpoint.plugin.rabbitmq.client.field.setter.HeadersFieldSetter;
import com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.AMQCommandConstructInterceptor;
import com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.ChannelBasicGetInterceptor;
import com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.ChannelBasicPublishInterceptor;
import com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.DeliveryConstructInterceptor;
import com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.QueueingConsumerHandleInterceptor;
import com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.QueueingConsumerOnNextInterceptor;
import com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.RabbitMQConsumerDispatchInterceptor;
import com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.RabbitMQConsumerHandleCompleteInboundCommandInterceptor;
import com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.SocketChannelFrameHandlerConstructInterceptor;
import com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.SocketFrameHandlerConstructInterceptor;

import java.security.ProtectionDomain;
import java.util.List;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Jinkai.Ma
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
public class RabbitMQClientPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        RabbitMQClientPluginConfig config = new RabbitMQClientPluginConfig(context.getConfig());

        if (!config.isTraceRabbitMQClient()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        addCommonEditors(config.isTraceRabbitMQClientProducer(), config.isTraceRabbitMQClientConsumer());
        if (config.isTraceRabbitMQClientProducer()) {
            addAmqpBasicPropertiesEditor();
        }
        if (config.isTraceRabbitMQClientConsumer()) {
            addAMQChannelEditor();
            addConsumerDispatchEditor();
            addConsumerEditors();
            addCustomConsumerEditors(config.getConsumerClasses());
        }

        addSpringAmqpSupport(config.isTraceRabbitMQClientProducer(), config.isTraceRabbitMQClientConsumer());
    }

    private void addAmqpBasicPropertiesEditor() {
        transformTemplate.transform("com.rabbitmq.client.AMQP$BasicProperties", BasicPropertiesTransform.class);
    }

    public static class BasicPropertiesTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addSetter(HeadersFieldSetter.class, "headers");
            return target.toBytecode();
        }
    }

    public static class ChannelTransform implements TransformCallback {

        public ChannelTransform() {
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final RabbitMQClientPluginConfig config = new RabbitMQClientPluginConfig(instrumentor.getProfilerConfig());
            final boolean traceProducer = config.isTraceRabbitMQClientProducer();
            final boolean traceConsumer = config.isTraceRabbitMQClientConsumer();

            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (traceProducer) {
                // copy AMQP.BasicProperties
                target.weave("com.navercorp.pinpoint.plugin.rabbitmq.client.aspect.ChannelAspect");

                final InstrumentMethod basicPublish = target.getDeclaredMethod("basicPublish", "java.lang.String", "java.lang.String", "boolean", "boolean", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
                if (basicPublish != null) {
                    basicPublish.addScopedInterceptor(ChannelBasicPublishInterceptor.class, RabbitMQClientConstants.RABBITMQ_PRODUCER_SCOPE, ExecutionPolicy.BOUNDARY);
                }
            }
            if (traceConsumer) {
                final InstrumentMethod basicGet = target.getDeclaredMethod("basicGet", "java.lang.String", "boolean");
                if (basicGet != null) {
                    basicGet.addScopedInterceptor(ChannelBasicGetInterceptor.class, RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE);
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
        transformTemplate.transform("com.rabbitmq.client.impl.ChannelN", ChannelTransform.class);
        transformTemplate.transform("com.rabbitmq.client.impl.recovery.AutorecoveringChannel", ChannelTransform.class);
        // FrameHandler implementations for end point and remote address
        transformTemplate.transform("com.rabbitmq.client.impl.SocketFrameHandler", SocketFrameHandlerTransform.class);
        transformTemplate.transform("com.rabbitmq.client.impl.nio.SocketChannelFrameHandler", SocketChannelFrameHandlerTransform.class);
        // Envelope - for asynchrnous trace propagation for consumers
        transformTemplate.transform("com.rabbitmq.client.Envelope", EnvelopeTransform.class);
        // AMQCommand - for pinpoint header propagation
        transformTemplate.transform("com.rabbitmq.client.impl.AMQCommand", AMQCommandTransform.class);
    }

    public static class SocketFrameHandlerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(LocalAddressAccessor.class);
            target.addField(RemoteAddressAccessor.class);
            final InstrumentMethod constructor1 = target.getConstructor("java.net.Socket");
            if (constructor1 != null) {
                constructor1.addScopedInterceptor(SocketFrameHandlerConstructInterceptor.class, RabbitMQClientConstants.RABBITMQ_FRAME_HANDLER_CREATION_SCOPE);
            }
            final InstrumentMethod constructor2 = target.getConstructor("java.net.Socket", "java.util.concurrent.ExecutorService");
            if (constructor2 != null) {
                constructor2.addScopedInterceptor(SocketFrameHandlerConstructInterceptor.class, RabbitMQClientConstants.RABBITMQ_FRAME_HANDLER_CREATION_SCOPE);
            }
            return target.toBytecode();
        }
    }

    public static class SocketChannelFrameHandlerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(LocalAddressAccessor.class);
            target.addField(RemoteAddressAccessor.class);
            final InstrumentMethod constructor = target.getConstructor("com.rabbitmq.client.impl.nio.SocketChannelFrameHandlerState");
            if (constructor != null) {
                constructor.addScopedInterceptor(SocketChannelFrameHandlerConstructInterceptor.class, RabbitMQClientConstants.RABBITMQ_FRAME_HANDLER_CREATION_SCOPE);
            }
            return target.toBytecode();
        }
    }

    public static class EnvelopeTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            return target.toBytecode();
        }
    }

    public static class AMQCommandTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod constructor = target.getConstructor("com.rabbitmq.client.Method", "com.rabbitmq.client.impl.AMQContentHeader", "byte[]");
            if (constructor != null) {
                constructor.addScopedInterceptor(AMQCommandConstructInterceptor.class, RabbitMQClientConstants.RABBITMQ_PRODUCER_SCOPE, ExecutionPolicy.INTERNAL);
            }
            return target.toBytecode();
        }
    }


    private void addAMQChannelEditor() {
        transformTemplate.transform("com.rabbitmq.client.impl.AMQChannel", AMQChannelTransform.class);
    }

    public static class AMQChannelTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final InstrumentMethod handleCompleteInboundCommand = target.getDeclaredMethod("handleCompleteInboundCommand", "com.rabbitmq.client.impl.AMQCommand");
            if (handleCompleteInboundCommand != null) {
                RabbitMQClientPluginConfig config = new RabbitMQClientPluginConfig(instrumentor.getProfilerConfig());
                Filter<String> excludeExchangeFilter = config.getExcludeExchangeFilter();
                handleCompleteInboundCommand.addInterceptor(RabbitMQConsumerHandleCompleteInboundCommandInterceptor.class, va(excludeExchangeFilter));
            }
            return target.toBytecode();
        }
    }

    private void addConsumerDispatchEditor() {
        transformTemplate.transform("com.rabbitmq.client.impl.ConsumerDispatcher", ConsumerDispatcherTransform.class);
    }

    public static class ConsumerDispatcherTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final InstrumentMethod handleDelivery = target.getDeclaredMethod("handleDelivery", "com.rabbitmq.client.Consumer", "java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
            if (handleDelivery == null) {
                return null;
            }
            RabbitMQClientPluginConfig config = new RabbitMQClientPluginConfig(instrumentor.getProfilerConfig());
            Filter<String> excludeExchangeFilter = config.getExcludeExchangeFilter();
            handleDelivery.addInterceptor(RabbitMQConsumerDispatchInterceptor.class, va(excludeExchangeFilter));
            target.addGetter(ChannelGetter.class, "channel");
            return target.toBytecode();
        }
    }


    private void addConsumerEditors() {
        // DefaultConsumer
        transformTemplate.transform("com.rabbitmq.client.DefaultConsumer", DefaultConsumerTransform.class);
        // QueueingConsumer
        transformTemplate.transform("com.rabbitmq.client.QueueingConsumer$Delivery", QueueingConsumerDeliveryTransform.class);
        transformTemplate.transform("com.rabbitmq.client.QueueingConsumer", QueueingConsumerTransform.class);
    }

    public static class DefaultConsumerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (RabbitMQUtils.addConsumerHandleDeliveryInterceptor(target)) {
                return target.toBytecode();
            }
            return null;
        }
    }

    public static class QueueingConsumerDeliveryTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod constructor = target.getConstructor("com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
            if (constructor == null) {
                return null;
            }
            constructor.addScopedInterceptor(DeliveryConstructInterceptor.class, RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE, ExecutionPolicy.INTERNAL);
            target.addField(AsyncContextAccessor.class);
            return target.toBytecode();
        }
    }

    public static class QueueingConsumerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (RabbitMQUtils.addConsumerHandleDeliveryInterceptor(target)) {
                InstrumentMethod nextDelivery = target.getDeclaredMethod("nextDelivery");
                if (nextDelivery != null) {
                    nextDelivery.addScopedInterceptor(QueueingConsumerOnNextInterceptor.class, RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE);
                }
                InstrumentMethod nextDeliveryTimeout = target.getDeclaredMethod("nextDelivery", "long");
                if (nextDeliveryTimeout != null) {
                    nextDeliveryTimeout.addScopedInterceptor(QueueingConsumerOnNextInterceptor.class, RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE);
                }
                InstrumentMethod handle = target.getDeclaredMethod("handle", "com.rabbitmq.client.QueueingConsumer$Delivery");
                if (handle != null) {
                    handle.addInterceptor(QueueingConsumerHandleInterceptor.class);
                }
                return target.toBytecode();
            }
            return null;
        }
    }

    private void addCustomConsumerEditors(List<String> customConsumers) {
        for (String customConsumer : customConsumers) {
            transformTemplate.transform(customConsumer, CustomConsumerTransform.class);
        }
    }

    public static class CustomConsumerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (RabbitMQUtils.addConsumerHandleDeliveryInterceptor(target)) {
                return target.toBytecode();
            }
            // Check inner classes for consumer implementations
            for (InstrumentClass potentialConsumer : target.getNestedClasses(ClassFilters.ACCEPT_ALL)) {
                if (potentialConsumer.hasMethod("handleDelivery", "java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]")) {
                    instrumentor.transform(loader, potentialConsumer.getName(), PotentialConsumerTransform.class);
                }
            }
            return null;
        }
    }

    public static class PotentialConsumerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (RabbitMQUtils.addConsumerHandleDeliveryInterceptor(target)) {
                return target.toBytecode();
            }
            return null;
        }
    }

    private void addSpringAmqpSupport(final boolean traceProducer, final boolean traceConsumer) {
        if (!traceProducer && !traceConsumer) {
            return;
        }

        transformTemplate.transform("org.springframework.amqp.rabbit.core.RabbitTemplate", RabbitTemplateTransform.class);

        // Spring-amqp rabbit client transformation
        if (traceConsumer) {
            // Message
            transformTemplate.transform("org.springframework.amqp.core.Message", MessageTransform.class);

            // Delivery
            // spring-rabbit pre-1.7.0
            transformTemplate.transform("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$Delivery", BlockingQueueConsumerDeliveryTransform.class);
            // spring-rabbit 1.7.0+
            transformTemplate.transform("org.springframework.amqp.rabbit.support.Delivery", DeliveryTransform.class);

            // BlockingQueueConsumer
            transformTemplate.transform("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer", BlockingQueueConsumerTransform.class);
            transformTemplate.transform("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$InternalConsumer", BlockingQueueConsumerInternalConsumerTransform.class);
            // Spring-rabbit 1.7.7+, 2.0.3+
            transformTemplate.transform("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$ConsumerDecorator", BlockingQueueConsumerConsumerDecoratorTransform.class);
        }
    }

    public static class RabbitTemplateTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            for (InstrumentMethod publicApi : target.getDeclaredMethods(RabbitMQUtils.getPublicApiFilter())) {
                publicApi.addScopedInterceptor(BasicMethodInterceptor.class, va(RabbitMQClientConstants.RABBITMQ_CLIENT_INTERNAL), RabbitMQClientConstants.RABBITMQ_TEMPLATE_API_SCOPE);
            }
            InstrumentMethod invoke = target.getDeclaredMethod("invoke", "org.springframework.amqp.rabbit.core.RabbitOperations$OperationsCallback");
            if (invoke != null) {
                invoke.addInterceptor(BasicMethodInterceptor.class, va(RabbitMQClientConstants.RABBITMQ_CLIENT_INTERNAL));
            }

            final RabbitMQClientPluginConfig config = new RabbitMQClientPluginConfig(instrumentor.getProfilerConfig());
            final boolean traceConsumer = config.isTraceRabbitMQClientConsumer();
            if (traceConsumer) {
                // Internal consumer implementations
                if (!RabbitMQUtils.addConsumerHandleDeliveryInterceptor(target)) {
                    // Check inner classes for consumer implementations
                    for (InstrumentClass potentialConsumer : target.getNestedClasses(ClassFilters.ACCEPT_ALL)) {
                        if (potentialConsumer.hasMethod("handleDelivery", "java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]")) {
                            instrumentor.transform(loader, potentialConsumer.getName(), PotentialConsumerTransform.class);
                        }
                    }
                }
            }

            return target.toBytecode();
        }
    }

    public static class MessageTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            return target.toBytecode();
        }
    }

    public static class BlockingQueueConsumerDeliveryTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // spring-rabbit pre-1.4.2
            InstrumentMethod constructor1 = target.getConstructor("com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
            if (constructor1 != null) {
                constructor1.addScopedInterceptor(DeliveryConstructInterceptor.class, RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE, ExecutionPolicy.INTERNAL);
            }
            // spring-rabbit 1.4.2 to 1.6.x
            InstrumentMethod constructor2 = target.getConstructor("java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
            if (constructor2 != null) {
                constructor2.addScopedInterceptor(DeliveryConstructInterceptor.class, RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE, ExecutionPolicy.INTERNAL);
            }
            target.addField(AsyncContextAccessor.class);
            return target.toBytecode();
        }
    }

    public static class DeliveryTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod constructor = target.getConstructor("java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
            if (constructor != null) {
                constructor.addScopedInterceptor(DeliveryConstructInterceptor.class, RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE, ExecutionPolicy.INTERNAL);
            }
            // spring-rabbit 2.1.0+
            InstrumentMethod constructor_2_1_0 = target.getConstructor("java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]", "java.lang.String");
            if (constructor_2_1_0 != null) {
                constructor_2_1_0.addScopedInterceptor(DeliveryConstructInterceptor.class, RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE, ExecutionPolicy.INTERNAL);
            }
            target.addField(AsyncContextAccessor.class);
            return target.toBytecode();
        }
    }

    public static class BlockingQueueConsumerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod nextMessage = target.getDeclaredMethod("nextMessage");
            if (nextMessage != null) {
                nextMessage.addScopedInterceptor(QueueingConsumerOnNextInterceptor.class, RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE);
            }
            InstrumentMethod nextMessageTimeout = target.getDeclaredMethod("nextMessage", "long");
            if (nextMessageTimeout != null) {
                nextMessageTimeout.addScopedInterceptor(QueueingConsumerOnNextInterceptor.class, RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE);
            }
            // spring-rabbit pre-1.7.0
            InstrumentMethod handle1 = target.getDeclaredMethod("handle", "org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$Delivery");
            if (handle1 != null) {
                handle1.addInterceptor(QueueingConsumerHandleInterceptor.class);
            }
            // spring-rabbit 1.7.0+
            InstrumentMethod handle2 = target.getDeclaredMethod("handle", "org.springframework.amqp.rabbit.support.Delivery");
            if (handle2 != null) {
                handle2.addInterceptor(QueueingConsumerHandleInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class BlockingQueueConsumerInternalConsumerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (RabbitMQUtils.addConsumerHandleDeliveryInterceptor(target)) {
                return target.toBytecode();
            }
            return null;
        }
    }

    public static class BlockingQueueConsumerConsumerDecoratorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (RabbitMQUtils.addConsumerHandleDeliveryInterceptor(target)) {
                return target.toBytecode();
            }
            return null;
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
