/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.pulsar;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.pulsar.field.accessor.ClientConfigAccessor;
import com.navercorp.pinpoint.plugin.pulsar.field.accessor.TopicInfoAccessor;
import com.navercorp.pinpoint.plugin.pulsar.field.getter.RetryCountGetter;
import com.navercorp.pinpoint.plugin.pulsar.field.getter.SequenceIdGetter;
import com.navercorp.pinpoint.plugin.pulsar.field.getter.ServiceUrlGetter;
import com.navercorp.pinpoint.plugin.pulsar.interceptor.ClientConfigConstructorInterceptor;
import com.navercorp.pinpoint.plugin.pulsar.interceptor.ConsumerConstructorInterceptor;
import com.navercorp.pinpoint.plugin.pulsar.interceptor.ConsumerImplInterceptor;
import com.navercorp.pinpoint.plugin.pulsar.interceptor.MessageBuilderSendAsyncInterceptor;
import com.navercorp.pinpoint.plugin.pulsar.interceptor.SendAsyncInterceptor;
import com.navercorp.pinpoint.plugin.pulsar.interceptor.SendCompleteInterceptor;

import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author zhouzixin@apache.org
 */
public class PulsarPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {

    // 4.x and the unshaded pulsar-client-original keep the original package,
    // the shaded pulsar-client / pulsar-client-all 5.x relocate the implementation.
    private static final String[] BASE_PACKAGES = {
            PulsarClientUtils.PACKAGE,
            PulsarClientUtils.SHADED_PACKAGE
    };

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setTransformTemplate(final MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    @Override
    public void setup(final ProfilerPluginSetupContext context) {
        final PulsarConfig config = new PulsarConfig(context.getConfig());
        if (Boolean.FALSE == config.isEnable()) {
            logger.info("{} disabled {}", this.getClass().getSimpleName(), "profiler.pulsar.enable=false");
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        if (config.isProducerEnable() || config.isConsumerEnable()) {
            // The service URL is read from ClientConfigurationData, which the client hands to its producers and consumers
            for (final String basePackage : BASE_PACKAGES) {
                transformTemplate.transform(
                        basePackage + ".client.impl.conf.ClientConfigurationData",
                        ClientConfigurationDataTransform.class
                );
                transformTemplate.transform(
                        basePackage + ".client.impl.PulsarClientImpl",
                        ClientConfigHolderTransform.class
                );
            }
        }

        if (config.isProducerEnable()) {
            for (final String basePackage : BASE_PACKAGES) {
                transformTemplate.transform(
                        basePackage + ".client.impl.ProducerBase",
                        ClientConfigHolderTransform.class
                );
                transformTemplate.transform(
                        basePackage + ".client.impl.TypedMessageBuilderImpl",
                        TypedMessageBuilderImplTransform.class
                );
                transformTemplate.transform(
                        basePackage + ".client.impl.ProducerImpl",
                        ProducerImplTransform.class
                );
                transformTemplate.transform(
                        basePackage + ".client.impl.OpSendMsgStatsImpl",
                        OpSendMsgStatsImplTransform.class
                );
                final Matcher matcher = Matchers.newPackageBasedMatcher(
                        basePackage, new InterfaceInternalNameMatcherOperand(
                                basePackage + ".client.impl.SendCallback", true)
                );
                transformTemplate.transform(matcher, SendCallbackTransform.class);
            }
        }

        if (config.isConsumerEnable()) {
            logger.info("{} add consumer interceptor", this.getClass().getSimpleName());
            for (final String basePackage : BASE_PACKAGES) {
                transformTemplate.transform(
                        basePackage + ".client.impl.ConsumerImpl",
                        ConsumerImplTransform.class
                );
            }
        }
    }

    private static void addClientConfigHolder(final InstrumentClass target) throws InstrumentException {
        target.addField(ClientConfigAccessor.class);
        // the first parameter of every constructor is the object the client configuration is taken from
        for (final InstrumentMethod constructor : target.getDeclaredConstructors()) {
            if (constructor.getParameterTypes().length > 0) {
                constructor.addInterceptor(ClientConfigConstructorInterceptor.class);
            }
        }
    }

    public static class ClientConfigurationDataTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(
                final Instrumentor instrumentor,
                final ClassLoader classLoader,
                final String className,
                final Class<?> classBeingRedefined,
                final ProtectionDomain protectionDomain,
                final byte[] classfileBuffer
        ) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            if (target.hasField("serviceUrl", "java.lang.String")) {
                target.addGetter(ServiceUrlGetter.class, "serviceUrl");
            }
            return target.toBytecode();
        }
    }

    /**
     * PulsarClientImpl(ClientConfigurationData, ...) and ProducerBase(PulsarClientImpl, ...)
     */
    public static class ClientConfigHolderTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(
                final Instrumentor instrumentor,
                final ClassLoader classLoader,
                final String className,
                final Class<?> classBeingRedefined,
                final ProtectionDomain protectionDomain,
                final byte[] classfileBuffer
        ) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            addClientConfigHolder(target);
            return target.toBytecode();
        }
    }

    public static class TypedMessageBuilderImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(
                final Instrumentor instrumentor,
                final ClassLoader classLoader,
                final String className,
                final Class<?> classBeingRedefined,
                final ProtectionDomain protectionDomain,
                final byte[] classfileBuffer
        ) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // TypedMessageBuilderImpl(ProducerBase, ...)
            addClientConfigHolder(target);

            // send() calls sendAsync(), and so do Producer.send(T) and Producer.sendAsync(T)
            final InstrumentMethod sendAsyncMethod = target.getDeclaredMethod("sendAsync");
            if (sendAsyncMethod != null) {
                sendAsyncMethod.addScopedInterceptor(MessageBuilderSendAsyncInterceptor.class,
                        PulsarConstants.SEND_SCOPE, ExecutionPolicy.ALWAYS);
            }
            return target.toBytecode();
        }
    }

    public static class ProducerImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(
                final Instrumentor instrumentor,
                final ClassLoader classLoader,
                final String className,
                final Class<?> classBeingRedefined,
                final ProtectionDomain protectionDomain,
                final byte[] classfileBuffer
        ) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // The SendCallback parameter lives in the package of ProducerImpl, relocated or not
            final String implPackage = PulsarClientUtils.getPackageName(className);
            final InstrumentMethod sendAsyncMethod = target.getDeclaredMethod("sendAsync",
                    "org.apache.pulsar.client.api.Message",
                    implPackage + ".SendCallback");
            if (sendAsyncMethod != null) {
                sendAsyncMethod.addScopedInterceptor(SendAsyncInterceptor.class,
                        PulsarConstants.SEND_SCOPE, ExecutionPolicy.ALWAYS);
            }

            return target.toBytecode();
        }
    }

    public static class OpSendMsgStatsImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(
                final Instrumentor instrumentor,
                final ClassLoader classLoader,
                final String className,
                final Class<?> classBeingRedefined,
                final ProtectionDomain protectionDomain,
                final byte[] classfileBuffer
        ) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            if (target.hasField("sequenceId", "long")) {
                target.addGetter(SequenceIdGetter.class, "sequenceId");
            }
            if (target.hasField("retryCount", "int")) {
                target.addGetter(RetryCountGetter.class, "retryCount");
            }
            return target.toBytecode();
        }
    }

    public static class SendCallbackTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(
                final Instrumentor instrumentor,
                final ClassLoader classLoader,
                final String className,
                final Class<?> classBeingRedefined,
                final ProtectionDomain protectionDomain,
                final byte[] classfileBuffer
        ) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            for (final String basePackage : BASE_PACKAGES) {
                final InstrumentMethod sendCompleteMethod = target.getDeclaredMethod("sendComplete",
                        "java.lang.Throwable", basePackage + ".client.impl.OpSendMsgStats");
                if (sendCompleteMethod != null) {
                    sendCompleteMethod.addScopedInterceptor(SendCompleteInterceptor.class, PulsarConstants.SCOPE);
                }
            }
            // Newer clients (seen in 4.2.5 and 5.0.1) complete ProducerImpl$DefaultSendMessageCallback through
            // its private sendComplete(Throwable), bypassing the interface method.
            // The scope keeps a single event when the interface method delegates to it.
            final InstrumentMethod internalSendCompleteMethod = target.getDeclaredMethod("sendComplete",
                    "java.lang.Throwable");
            if (internalSendCompleteMethod != null) {
                internalSendCompleteMethod.addScopedInterceptor(SendCompleteInterceptor.class, PulsarConstants.SCOPE);
            }

            return target.toBytecode();
        }
    }

    public static class ConsumerImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(
                final Instrumentor instrumentor,
                final ClassLoader classLoader,
                final String className,
                final Class<?> classBeingRedefined,
                final ProtectionDomain protectionDomain,
                final byte[] classfileBuffer
        ) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // ConsumerConstructorInterceptor fills both fields: a method takes a single interceptor
            target.addField(ClientConfigAccessor.class);
            target.addField(TopicInfoAccessor.class);
            List<InstrumentMethod> consumerConstructors = target.getDeclaredConstructors();
            for (final InstrumentMethod consumerConstructor : consumerConstructors) {
                if (consumerConstructor.getParameterTypes().length == 13) {
                    consumerConstructor.addInterceptor(ConsumerConstructorInterceptor.class);
                }
            }

            final InstrumentMethod messageProcessedMethod = target.getDeclaredMethod(
                    "messageProcessed",
                    "org.apache.pulsar.client.api.Message");
            if (messageProcessedMethod != null) {
                messageProcessedMethod.addInterceptor(ConsumerImplInterceptor.class);
            }
            return target.toBytecode();
        }
    }
}
