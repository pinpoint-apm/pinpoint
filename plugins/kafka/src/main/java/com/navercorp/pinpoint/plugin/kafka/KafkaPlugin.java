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

package com.navercorp.pinpoint.plugin.kafka;

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
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.EndPointFieldAccessor;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.RemoteAddressFieldAccessor;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.SocketChannelListFieldAccessor;
import com.navercorp.pinpoint.plugin.kafka.field.getter.ApiVersionsGetter;
import com.navercorp.pinpoint.plugin.kafka.field.getter.SelectorGetter;
import com.navercorp.pinpoint.plugin.kafka.interceptor.ConsumerConstructorInterceptor;
import com.navercorp.pinpoint.plugin.kafka.interceptor.ConsumerMultiRecordEntryPointInterceptor;
import com.navercorp.pinpoint.plugin.kafka.interceptor.ConsumerPollInterceptor;
import com.navercorp.pinpoint.plugin.kafka.interceptor.ConsumerRecordEntryPointInterceptor;
import com.navercorp.pinpoint.plugin.kafka.interceptor.ConsumerRecordsInterceptor;
import com.navercorp.pinpoint.plugin.kafka.interceptor.ProducerAddHeaderInterceptor;
import com.navercorp.pinpoint.plugin.kafka.interceptor.NetworkClientPollInterceptor;
import com.navercorp.pinpoint.plugin.kafka.interceptor.ProducerConstructorInterceptor;
import com.navercorp.pinpoint.plugin.kafka.interceptor.ProducerSendInterceptor;
import com.navercorp.pinpoint.plugin.kafka.interceptor.SocketChannelRegisterInterceptor;

import java.security.ProtectionDomain;
import java.util.List;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Harris Gwag (gwagdalf)
 * @author Taejin Koo
 */
public class KafkaPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final KafkaConfig config = new KafkaConfig(context.getConfig());
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        if (config.isProducerEnable()) {
            transformTemplate.transform("org.apache.kafka.clients.producer.KafkaProducer", KafkaProducerTransform.class);
            transformTemplate.transform("org.apache.kafka.clients.producer.internals.TransactionManager", TransactionManagerTransform.class);
        }

        if (enableConsumerTransform(config)) {
            transformTemplate.transform("org.apache.kafka.clients.consumer.KafkaConsumer", KafkaConsumerTransform.class);

            transformTemplate.transform("org.apache.kafka.clients.consumer.ConsumerRecord", ConsumerRecordTransform.class);

            // for getting local addresses
            transformTemplate.transform("org.apache.kafka.common.network.Selector", KafkaSelectorTransform.class);
            transformTemplate.transform("org.apache.kafka.clients.NetworkClient", NetworkClientTransform.class);
            transformTemplate.transform("org.apache.kafka.common.TopicPartition", TopicPartitionTransform.class);
            transformTemplate.transform("org.apache.kafka.clients.consumer.ConsumerRecords", ConsumerRecordsTransform.class);

            if (config.isSpringConsumerEnable()) {
                transformTemplate.transform("org.springframework.kafka.listener.adapter.RecordMessagingMessageListenerAdapter", AcknowledgingConsumerAwareMessageListenerTransform.class);
                transformTemplate.transform("org.springframework.kafka.listener.adapter.BatchMessagingMessageListenerAdapter", BatchMessagingMessageListenerAdapterTransform.class);

                // Spring Cloud Starter Stream Kafka 2.2.x is supported
                transformTemplate.transform("org.springframework.kafka.listener.adapter.RetryingMessageListenerAdapter", AcknowledgingConsumerAwareMessageListenerTransform.class);

                // for MessagingGatewaySupport in spring-integration-kafka
                transformTemplate.transform("org.springframework.integration.kafka.inbound.KafkaInboundGateway$IntegrationRecordMessageListener", AcknowledgingConsumerAwareMessageListenerTransform.class);

                // for MessageProducerSupport in spring-integration-kafka
                transformTemplate.transform("org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter$IntegrationRecordMessageListener", AcknowledgingConsumerAwareMessageListenerTransform.class);
                transformTemplate.transform("org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter$IntegrationBatchMessageListener", BatchMessagingMessageListenerAdapterTransform.class);
            }

            if (StringUtils.hasText(config.getKafkaEntryPoint())) {
                transformEntryPoint(config.getKafkaEntryPoint());
            }
        }
    }

    public static class KafkaProducerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            // Version 2.3.0+ is supported.
            InstrumentMethod constructor = target.getConstructor("java.util.Map",
                    "org.apache.kafka.common.serialization.Serializer", "org.apache.kafka.common.serialization.Serializer",
                    "org.apache.kafka.clients.producer.internals.ProducerMetadata", "org.apache.kafka.clients.KafkaClient",
                    "org.apache.kafka.clients.producer.internals.ProducerInterceptors", "org.apache.kafka.common.utils.Time");

            if (constructor == null) {
                // Version 2.2.0+ is supported.
                constructor = target.getConstructor("java.util.Map",
                        "org.apache.kafka.common.serialization.Serializer", "org.apache.kafka.common.serialization.Serializer",
                        "org.apache.kafka.clients.Metadata", "org.apache.kafka.clients.KafkaClient",
                        "org.apache.kafka.clients.producer.internals.ProducerInterceptors", "org.apache.kafka.common.utils.Time");
            }

            // Version 2.0.0+ is supported.
            if (constructor == null) {
                constructor = target.getConstructor("org.apache.kafka.clients.producer.ProducerConfig",
                        "org.apache.kafka.common.serialization.Serializer", "org.apache.kafka.common.serialization.Serializer",
                        "org.apache.kafka.clients.Metadata", "org.apache.kafka.clients.KafkaClient");
            }

            if (constructor == null) {
                constructor = target.getConstructor("org.apache.kafka.clients.producer.ProducerConfig",
                        "org.apache.kafka.common.serialization.Serializer", "org.apache.kafka.common.serialization.Serializer");
            }

            if (constructor != null) {
                constructor.addInterceptor(ProducerConstructorInterceptor.class);
            }

            InstrumentMethod sendMethod = target.getDeclaredMethod("send", "org.apache.kafka.clients.producer.ProducerRecord", "org.apache.kafka.clients.producer.Callback");
            if (sendMethod != null) {
                sendMethod.addInterceptor(ProducerSendInterceptor.class);
            }

            // Version 0.11.0+ is supported.
            InstrumentMethod setReadOnlyMethod = target.getDeclaredMethod("setReadOnly", "org.apache.kafka.common.header.Headers");
            if (setReadOnlyMethod != null) {
                setReadOnlyMethod.addInterceptor(ProducerAddHeaderInterceptor.class);
            }
            if (target.hasField("apiVersions")) {
                target.addGetter(ApiVersionsGetter.class, "apiVersions");
            }

            target.addField(RemoteAddressFieldAccessor.class);

            return target.toBytecode();
        }

    }

    public static class TransactionManagerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            InstrumentMethod beginTransactionMethod = target.getDeclaredMethod("beginTransaction");
            if (beginTransactionMethod != null) {
                beginTransactionMethod.addInterceptor(BasicMethodInterceptor.class, va(KafkaConstants.KAFKA_CLIENT_INTERNAL));
            }

            InstrumentMethod beginCommitMethod = target.getDeclaredMethod("beginCommit");
            if (beginCommitMethod != null) {
                beginCommitMethod.addInterceptor(BasicMethodInterceptor.class, va(KafkaConstants.KAFKA_CLIENT_INTERNAL));
            }

            InstrumentMethod beginAbortMethod = target.getDeclaredMethod("beginAbort");
            if (beginAbortMethod != null) {
                beginAbortMethod.addInterceptor(BasicMethodInterceptor.class, va(KafkaConstants.KAFKA_CLIENT_INTERNAL));
            }

            return target.toBytecode();
        }

    }

    public static class KafkaConsumerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            InstrumentMethod constructor = target.getConstructor("org.apache.kafka.clients.consumer.ConsumerConfig",
                    "org.apache.kafka.common.serialization.Deserializer", "org.apache.kafka.common.serialization.Deserializer");
            if(constructor != null) {
                constructor.addInterceptor(ConsumerConstructorInterceptor.class);
            }

            // Version 2.2.0+ is supported.
            InstrumentMethod pollMethod = target.getDeclaredMethod("poll", "org.apache.kafka.common.utils.Timer", "boolean");

            // Version 2.0.0+ is supported.
            if (pollMethod == null) {
                pollMethod = target.getDeclaredMethod("poll", "long", "boolean");
            }

            // Version 2.0.0-
            if (pollMethod == null) {
                pollMethod = target.getDeclaredMethod("poll", "long");
            }

            if (pollMethod != null) {
                pollMethod.addInterceptor(ConsumerPollInterceptor.class);
            }

            target.addField(RemoteAddressFieldAccessor.class);

            return target.toBytecode();
        }

    }

    public static class ConsumerRecordTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(RemoteAddressFieldAccessor.class);
            target.addField(EndPointFieldAccessor.class);
            return target.toBytecode();
        }

    }

    public static class AcknowledgingConsumerAwareMessageListenerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            MethodFilter methodFilter = MethodFilters.chain(MethodFilters.name("onMessage"), MethodFilters.argAt(0, "org.apache.kafka.clients.consumer.ConsumerRecord"));
            List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(methodFilter);
            for (InstrumentMethod declaredMethod : declaredMethods) {
                declaredMethod.addScopedInterceptor(ConsumerRecordEntryPointInterceptor.class, va(0), KafkaConstants.SCOPE, ExecutionPolicy.BOUNDARY);
            }

            return target.toBytecode();
        }

    }

    public static class BatchMessagingMessageListenerAdapterTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            MethodFilter methodFilter = MethodFilters.chain(MethodFilters.name("onMessage"), MethodFilters.argAt(0, "org.apache.kafka.clients.consumer.ConsumerRecords"));
            List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(methodFilter);
            for (InstrumentMethod declaredMethod : declaredMethods) {
                declaredMethod.addScopedInterceptor(ConsumerMultiRecordEntryPointInterceptor.class, va(0), KafkaConstants.SCOPE, ExecutionPolicy.BOUNDARY);
            }

            methodFilter = MethodFilters.chain(MethodFilters.name("onMessage"), MethodFilters.argAt(0, "java.util.List"));
            declaredMethods = target.getDeclaredMethods(methodFilter);
            for (InstrumentMethod declaredMethod : declaredMethods) {
                declaredMethod.addScopedInterceptor(ConsumerMultiRecordEntryPointInterceptor.class, va(0), KafkaConstants.SCOPE, ExecutionPolicy.BOUNDARY);
            }

            return target.toBytecode();
        }

    }

    private boolean enableConsumerTransform(KafkaConfig config) {
        if (config.isConsumerEnable() && StringUtils.hasText(config.getKafkaEntryPoint())) {
            return true;
        }

        return config.isSpringConsumerEnable();
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    public void transformEntryPoint(String entryPoint) {
        final String clazzName = toClassName(entryPoint);

        transformTemplate.transform(clazzName, EntryPointTransform.class);
    }

    public static class EntryPointTransform implements TransformCallback {
        private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            final KafkaConfig config = new KafkaConfig(instrumentor.getProfilerConfig());
            final String methodName = toMethodName(config.getKafkaEntryPoint());
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name(methodName))) {
                try {
                    String[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes == null) {
                        continue;
                    }

                    for (int i = 0; i < parameterTypes.length; i++) {
                        String parameterType = parameterTypes[i];

                        if (KafkaConstants.CONSUMER_RECORD_CLASS_NAME.equals(parameterType)) {
                            method.addInterceptor(ConsumerRecordEntryPointInterceptor.class, va(i));
                            break;
                        } else if (KafkaConstants.CONSUMER_MULTI_RECORD_CLASS_NAME.equals(parameterType)) {
                            method.addInterceptor(ConsumerMultiRecordEntryPointInterceptor.class, va(i));
                            break;
                        }
                    }
                } catch (Exception e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unsupported method " + method, e);
                    }
                }
            }
            return target.toBytecode();
        }

        private String toMethodName(String fullQualifiedMethodName) {
            final int methodBeginPosition = fullQualifiedMethodName.lastIndexOf('.');
            if (methodBeginPosition <= 0 || methodBeginPosition + 1 >= fullQualifiedMethodName.length()) {
                throw new IllegalArgumentException("invalid full qualified method name(" + fullQualifiedMethodName + "). not found method");
            }

            return fullQualifiedMethodName.substring(methodBeginPosition + 1);
        }

    }

    private String toClassName(String fullQualifiedMethodName) {
        final int classEndPosition = fullQualifiedMethodName.lastIndexOf('.');
        if (classEndPosition <= 0) {
            throw new IllegalArgumentException("invalid full qualified method name(" + fullQualifiedMethodName + "). not found method");
        }

        return fullQualifiedMethodName.substring(0, classEndPosition);
    }

    public static class KafkaSelectorTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            // for v1.0.0+
            InstrumentMethod registerMethod = target.getDeclaredMethod("registerChannel", "java.lang.String", "java.nio.channels.SocketChannel", "int");
            if (registerMethod == null) {
                // for v1.0.0
                registerMethod = target.getDeclaredMethod("buildChannel", "java.nio.channels.SocketChannel", "java.lang.String", "java.nio.channels.SelectionKey");
            }

            if (registerMethod != null) {
                registerMethod.addInterceptor(SocketChannelRegisterInterceptor.class);

                target.addField(SocketChannelListFieldAccessor.class);
            }

            return target.toBytecode();
        }
    }

    public static class NetworkClientTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            InstrumentMethod pollMethod = target.getDeclaredMethod("poll", "long", "long");

            if (pollMethod != null) {
                pollMethod.addInterceptor(NetworkClientPollInterceptor.class);
                target.addGetter(SelectorGetter.class, "selector");
            }

            return target.toBytecode();
        }

    }

    public static class TopicPartitionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            target.addField(EndPointFieldAccessor.class);

            return target.toBytecode();
        }

    }

    public static class ConsumerRecordsTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            InstrumentMethod constructor = target.getConstructor("java.util.Map");
            if (constructor != null) {
                constructor.addInterceptor(ConsumerRecordsInterceptor.class);
            }
            return target.toBytecode();
        }

    }

}
