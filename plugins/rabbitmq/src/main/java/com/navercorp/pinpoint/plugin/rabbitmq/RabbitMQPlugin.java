package com.navercorp.pinpoint.plugin.rabbitmq;

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
public class RabbitMQPlugin implements ProfilerPlugin, TransformTemplateAware {

    private static final String PUBLISHER_INTERCEPTOR_FQCN = "com.navercorp.pinpoint.plugin.rabbitmq.interceptor.RabbitMQPublishInterceptor";
    private static final String CONSUMER_INTERCEPTOR_FQCN = "com.navercorp.pinpoint.plugin.rabbitmq.interceptor.RabbitMQConsumeInterceptor";

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        RabbitMQClientPluginConfig config = new RabbitMQClientPluginConfig(context.getConfig());
        if (!config.isTraceRabbitMQClient()) {
            return;
        }
        if (config.isTraceRabbitMQClientConsumer() || config.isTraceRabbitMQClientProducer()) {
            if (config.isTraceRabbitMQClientProducer()) {
                addPublisher();
            }
            if (config.isTraceRabbitMQClientConsumer()) {
                addConsumer(config.getConsumerClasses());
                addSpringAmqpSupport();
            }
        }
    }

    private void addPublisher() {
        transformTemplate.transform("com.rabbitmq.client.impl.recovery.AutorecoveringChannel", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                final InstrumentMethod method = target.getDeclaredMethod("basicPublish", "java.lang.String", "java.lang.String", "boolean", "boolean", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
                if (method != null) {
                    method.addScopedInterceptor(PUBLISHER_INTERCEPTOR_FQCN, RabbitMQConstants.RABBITMQ_SCOPE);
                }

                return target.toBytecode();
            }
        });
        transformTemplate.transform("com.rabbitmq.client.impl.ChannelN", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                final InstrumentMethod method = target.getDeclaredMethod("basicPublish", "java.lang.String", "java.lang.String", "boolean", "boolean", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
                if (method != null) {
                    method.addScopedInterceptor(PUBLISHER_INTERCEPTOR_FQCN, RabbitMQConstants.RABBITMQ_SCOPE);
                }

                return target.toBytecode();
            }
        });
        transformTemplate.transform("com.rabbitmq.client.AMQP$BasicProperties", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addSetter("com.navercorp.pinpoint.plugin.rabbitmq.field.setter.HeadersFieldSetter", "headers");

                return target.toBytecode();
            }
        });
    }

    private static class ConsumerTransformCallback implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod method = target.getDeclaredMethod("handleDelivery", "java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
            if (method == null) {
                return null;
            }
            method.addScopedInterceptor(CONSUMER_INTERCEPTOR_FQCN, RabbitMQConstants.RABBITMQ_CONSUMER_SCOPE);
            return target.toBytecode();
        }
    }

    private void addConsumer(List<String> customConsumers) {
        transformTemplate.transform("com.rabbitmq.client.QueueingConsumer", new ConsumerTransformCallback());
        transformTemplate.transform("com.rabbitmq.client.DefaultConsumer", new ConsumerTransformCallback());
        // Custom consumers
        for (String customConsumer : customConsumers) {
            transformTemplate.transform(customConsumer, new ConsumerTransformCallback() {
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
                            instrumentor.transform(loader, potentialConsumer.getName(), new ConsumerTransformCallback());
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
                constructor.addScopedInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.interceptor.DeliveryConstructInterceptor", RabbitMQConstants.RABBITMQ_CONSUMER_SCOPE, ExecutionPolicy.INTERNAL);
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
                    handleMethod.addInterceptor("com.navercorp.pinpoint.plugin.rabbitmq.interceptor.BlockingQueueConsumerHandleInterceptor");
                }
                return target.toBytecode();
            }
        });
        transformTemplate.transform("org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$InternalConsumer", new ConsumerTransformCallback());
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
