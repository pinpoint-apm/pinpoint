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
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.pulsar.field.accessor.TopicInfoAccessor;
import com.navercorp.pinpoint.plugin.pulsar.interceptor.ConsumerConstructorInterceptor;
import com.navercorp.pinpoint.plugin.pulsar.interceptor.ConsumerImplInterceptor;
import com.navercorp.pinpoint.plugin.pulsar.interceptor.SendAsyncInterceptor;
import com.navercorp.pinpoint.plugin.pulsar.interceptor.SendCompleteInterceptor;

import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author zhouzixin@apache.org
 */
public class PulsarPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {

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

        if (config.isProducerEnable()) {
            transformTemplate.transform(
                    "org.apache.pulsar.client.impl.ProducerImpl",
                    ProducerImplTransform.class
            );
            final Matcher matcher = Matchers.newPackageBasedMatcher(
                    "org.apache.pulsar", new InterfaceInternalNameMatcherOperand(
                            "org.apache.pulsar.client.impl.SendCallback", true)
            );
            transformTemplate.transform(matcher, SendCallbackTransform.class);
        }

        if (config.isConsumerEnable()) {
            logger.info("{} add consumer interceptor", this.getClass().getSimpleName());
            transformTemplate.transform(
                    "org.apache.pulsar.client.impl.ConsumerImpl",
                    ConsumerImplTransform.class
            );
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
            target.getDeclaredMethod("sendAsync",
                            "org.apache.pulsar.client.api.Message",
                            "org.apache.pulsar.client.impl.SendCallback")
                    .addInterceptor(SendAsyncInterceptor.class);

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

            final InstrumentMethod sendCompleteMethod = target.getDeclaredMethod("sendComplete",
                    "java.lang.Throwable", "org.apache.pulsar.client.impl.OpSendMsgStats");
            if (sendCompleteMethod != null) {
                sendCompleteMethod.addScopedInterceptor(SendCompleteInterceptor.class, PulsarConstants.SCOPE);
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
