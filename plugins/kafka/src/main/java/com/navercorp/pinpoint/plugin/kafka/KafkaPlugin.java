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
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.security.ProtectionDomain;
import java.util.List;

import static com.navercorp.pinpoint.common.util.VarArgs.va;


public class KafkaPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final KafkaConfig config = new KafkaConfig(context.getConfig());

        if (config.isProducerEnable()) {
            transformTemplate.transform("org.apache.kafka.clients.producer.KafkaProducer", new TransformCallback() {

                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                    // Version 2.2.0+ is supported.
                    InstrumentMethod constructor = target.getConstructor("java.util.Map",
                            "org.apache.kafka.common.serialization.Serializer", "org.apache.kafka.common.serialization.Serializer",
                            "org.apache.kafka.clients.Metadata", "org.apache.kafka.clients.KafkaClient",
                            "org.apache.kafka.clients.producer.internals.ProducerInterceptors", "org.apache.kafka.common.utils.Time");

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

                    constructor.addInterceptor(KafkaConstants.PRODUCER_CONSTRUCTOR_INTERCEPTOR);

                    InstrumentMethod sendMethod = target.getDeclaredMethod("send", "org.apache.kafka.clients.producer.ProducerRecord", "org.apache.kafka.clients.producer.Callback");
                    sendMethod.addInterceptor(KafkaConstants.PRODUCER_SEND_INTERCEPTOR);

                    target.addField(KafkaConstants.REMOTE_ADDRESS_ACCESSOR);
                    return target.toBytecode();
                }

            });
        }


        if (enableConsumerTransform(config)) {
            transformTemplate.transform("org.apache.kafka.clients.consumer.KafkaConsumer", new TransformCallback() {

                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                    InstrumentMethod constructor = target.getConstructor("org.apache.kafka.clients.consumer.ConsumerConfig",
                            "org.apache.kafka.common.serialization.Deserializer", "org.apache.kafka.common.serialization.Deserializer");
                    constructor.addInterceptor(KafkaConstants.CONSUMER_CONSTRUCTOR_INTERCEPTOR);

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

                    pollMethod.addInterceptor(KafkaConstants.CONSUMER_POLL_INTERCEPTOR);

                    target.addField(KafkaConstants.REMOTE_ADDRESS_ACCESSOR);

                    return target.toBytecode();
                }

            });

            transformTemplate.transform("org.apache.kafka.clients.consumer.ConsumerRecord", new TransformCallback() {

                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                    target.addField(KafkaConstants.REMOTE_ADDRESS_ACCESSOR);
                    return target.toBytecode();
                }

            });

            if (config.isSpringConsumerEnable()) {
                transformTemplate.transform("org.springframework.kafka.listener.adapter.RecordMessagingMessageListenerAdapter", new TransformCallback() {

                    @Override
                    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                        final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                        MethodFilter methodFilter = MethodFilters.chain(MethodFilters.name("onMessage"), MethodFilters.argAt(0, "org.apache.kafka.clients.consumer.ConsumerRecord"));
                        List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(methodFilter);
                        for (InstrumentMethod declaredMethod : declaredMethods) {
                            declaredMethod.addScopedInterceptor(KafkaConstants.CONSUMER_RECORD_ENTRYPOINT_INTERCEPTOR, va(0), KafkaConstants.SCOPE, ExecutionPolicy.BOUNDARY);
                        }

                        return target.toBytecode();
                    }

                });

                transformTemplate.transform("org.springframework.kafka.listener.adapter.BatchMessagingMessageListenerAdapter", new TransformCallback() {

                    @Override
                    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                        final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                        MethodFilter methodFilter = MethodFilters.chain(MethodFilters.name("onMessage"), MethodFilters.argAt(0, "org.apache.kafka.clients.consumer.ConsumerRecords"));
                        List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(methodFilter);
                        for (InstrumentMethod declaredMethod : declaredMethods) {
                            declaredMethod.addScopedInterceptor(KafkaConstants.CONSUMER_MULTI_RECORD_ENTRYPOINT_INTERCEPTOR, va(0), KafkaConstants.SCOPE, ExecutionPolicy.BOUNDARY);
                        }

                        methodFilter = MethodFilters.chain(MethodFilters.name("onMessage"), MethodFilters.argAt(0, "java.util.List"));
                        declaredMethods = target.getDeclaredMethods(methodFilter);
                        for (InstrumentMethod declaredMethod : declaredMethods) {
                            declaredMethod.addScopedInterceptor(KafkaConstants.CONSUMER_MULTI_RECORD_ENTRYPOINT_INTERCEPTOR, va(0), KafkaConstants.SCOPE, ExecutionPolicy.BOUNDARY);
                        }

                        return target.toBytecode();
                    }

                });

            }

            if (StringUtils.hasText(config.getKafkaEntryPoint())) {
                transformEntryPoint(config.getKafkaEntryPoint());
            }
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
        final String methodName = toMethodName(entryPoint);

        transformTemplate.transform(clazzName, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name(methodName))) {
                    try {
                        String[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes == null) {
                            continue;
                        }

                        for (int i = 0; i < parameterTypes.length; i++) {
                            String parameterType = parameterTypes[i];

                            if (KafkaConstants.CONSUMER_RECORD_CLASS_NAME.equals(parameterType)) {
                                method.addInterceptor(KafkaConstants.CONSUMER_RECORD_ENTRYPOINT_INTERCEPTOR, va(i));
                                break;
                            } else if (KafkaConstants.CONSUMER_MULTI_RECORD_CLASS_NAME.equals(parameterType)) {
                                method.addInterceptor(KafkaConstants.CONSUMER_MULTI_RECORD_ENTRYPOINT_INTERCEPTOR, va(i));
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

        });
    }

    private String toClassName(String fullQualifiedMethodName) {
        final int classEndPosition = fullQualifiedMethodName.lastIndexOf('.');
        if (classEndPosition <= 0) {
            throw new IllegalArgumentException("invalid full qualified method name(" + fullQualifiedMethodName + "). not found method");
        }

        return fullQualifiedMethodName.substring(0, classEndPosition);
    }

    private String toMethodName(String fullQualifiedMethodName) {
        final int methodBeginPosition = fullQualifiedMethodName.lastIndexOf('.');
        if (methodBeginPosition <= 0 || methodBeginPosition + 1 >= fullQualifiedMethodName.length()) {
            throw new IllegalArgumentException("invalid full qualified method name(" + fullQualifiedMethodName + "). not found method");
        }

        return fullQualifiedMethodName.substring(methodBeginPosition + 1);
    }

}
