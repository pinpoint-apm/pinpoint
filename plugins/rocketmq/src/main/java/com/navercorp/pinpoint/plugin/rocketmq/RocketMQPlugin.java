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
package com.navercorp.pinpoint.plugin.rocketmq;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.rocketmq.field.accessor.EndPointFieldAccessor;
import com.navercorp.pinpoint.plugin.rocketmq.interceptor.ConsumerMessageListenerConcurrentlyInterceptor;
import com.navercorp.pinpoint.plugin.rocketmq.interceptor.ConsumerMessageListenerOrderlyInterceptor;
import com.navercorp.pinpoint.plugin.rocketmq.interceptor.ProducerSendCallBackInterceptor.ConstructInterceptor;
import com.navercorp.pinpoint.plugin.rocketmq.interceptor.ProducerSendCallBackInterceptor.OnExceptionInterceptor;
import com.navercorp.pinpoint.plugin.rocketmq.interceptor.ProducerSendCallBackInterceptor.OnSuccessInterceptor;
import com.navercorp.pinpoint.plugin.rocketmq.interceptor.ProducerSendInterceptor;
import com.navercorp.pinpoint.plugin.rocketmq.interceptor.UpdateNameServerAddressListInterceptor;

/**
 * @author messi-gao
 */
public class RocketMQPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final RocketMQConfig config = new RocketMQConfig(context.getConfig());
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        List<String> basePackageNames = new ArrayList<>();
        // basePackageName support
        String basePackageName = config.getConsumerBasePackage();
        if (StringUtils.isEmpty(basePackageName)) {
            logger.error("please config the [profiler.rocketmq.consumer.basePackage] in pinpoint.config");
            return;
        } else {
            basePackageNames.add(basePackageName);
        }
        if (config.isProducerEnable()) {
            transformTemplate.transform("org.apache.rocketmq.client.impl.MQClientAPIImpl",
                                        MQClientAPIImplTransform.class);
            final Matcher matcher = Matchers.newPackageBasedMatcher(basePackageNames,
                                                                    new InterfaceInternalNameMatcherOperand(
                                                                            "org.apache.rocketmq.client.producer.SendCallback",
                                                                            true));
            transformTemplate.transform(matcher, SendCallbackTransform.class);
        }

        if (config.isConsumerEnable()) {
            // rocketmq spring boot support
            basePackageNames.add("org.apache.rocketmq.spring.support");

            final Matcher matcher = Matchers.newPackageBasedMatcher(basePackageNames,
                                                                    new InterfaceInternalNameMatcherOperand(
                                                                            "org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently",
                                                                            true));
            transformTemplate.transform(matcher, MessageListenerConcurrentlyTransform.class);

            final Matcher orderlyMatcher = Matchers.newPackageBasedMatcher(basePackageNames,
                                                                           new InterfaceInternalNameMatcherOperand(
                                                                                   "org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly",
                                                                                   true));
            transformTemplate.transform(orderlyMatcher, MessageListenerOrderlyTransform.class);
        }

        if (enableConsumerTransform(config)) {
            if (StringUtils.hasText(config.getRocketmqEntryPoint())) {
                transformEntryPoint(config.getRocketmqEntryPoint());
            }
        }
    }

    public static class MQClientAPIImplTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className,
                                                                           classfileBuffer);
            target.addField(EndPointFieldAccessor.class);
            target.addField(AsyncContextAccessor.class);

            final List<InstrumentMethod> sendMessageMethods = target.getDeclaredMethods(
                    MethodFilters.name("sendMessage"));
            for (InstrumentMethod sendMessageMethod : sendMessageMethods) {
                if (sendMessageMethod.getParameterTypes().length == 12) {
                    sendMessageMethod.addInterceptor(ProducerSendInterceptor.class);
                }
            }

            InstrumentMethod updateNameServerAddressListMethod = target.getDeclaredMethod(
                    "updateNameServerAddressList", "java.lang.String");
            if (updateNameServerAddressListMethod != null) {
                updateNameServerAddressListMethod.addInterceptor(UpdateNameServerAddressListInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class SendCallbackTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className,
                                                                           classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            List<InstrumentMethod> constructors = target.getDeclaredConstructors();
            for (InstrumentMethod constructor : constructors) {
                constructor.addInterceptor(ConstructInterceptor.class);
            }
            final MethodFilter onSuccessFilter = MethodFilters.chain(
                    MethodFilters.name("onSuccess"),
                    MethodFilters.argAt(0, "org.apache.rocketmq.client.producer.SendResult"));
            final List<InstrumentMethod> onSuccessMethods = target.getDeclaredMethods(onSuccessFilter);
            for (InstrumentMethod instrumentMethod : onSuccessMethods) {
                instrumentMethod.addScopedInterceptor(OnSuccessInterceptor.class, RocketMQConstants.SCOPE);
            }

            final MethodFilter onExceptionFilter = MethodFilters.chain(
                    MethodFilters.name("onException"),
                    MethodFilters.argAt(0, "java.lang.Throwable"));
            final List<InstrumentMethod> onExceptions = target.getDeclaredMethods(onExceptionFilter);
            for (InstrumentMethod instrumentMethod : onExceptions) {
                instrumentMethod.addScopedInterceptor(OnExceptionInterceptor.class, RocketMQConstants.SCOPE);

            }
            return target.toBytecode();
        }
    }

    public static class MessageListenerConcurrentlyTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className,
                                                                           classfileBuffer);
            List<InstrumentMethod> consumeMessageMethods = target.getDeclaredMethods(
                    MethodFilters.name("consumeMessage"));
            for (InstrumentMethod consumeMessage : consumeMessageMethods) {
                consumeMessage.addScopedInterceptor(ConsumerMessageListenerConcurrentlyInterceptor.class,
                                                    RocketMQConstants.SCOPE, ExecutionPolicy.BOUNDARY);
            }
            return target.toBytecode();
        }
    }

    public static class MessageListenerOrderlyTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className,
                                                                           classfileBuffer);
            List<InstrumentMethod> consumeMessageMethods = target.getDeclaredMethods(
                    MethodFilters.name("consumeMessage"));
            for (InstrumentMethod consumeMessage : consumeMessageMethods) {
                consumeMessage.addScopedInterceptor(ConsumerMessageListenerOrderlyInterceptor.class,
                                                    RocketMQConstants.SCOPE, ExecutionPolicy.BOUNDARY);
            }

            return target.toBytecode();
        }
    }

    private static boolean enableConsumerTransform(RocketMQConfig config) {
        return config.isConsumerEnable() && StringUtils.hasText(config.getRocketmqEntryPoint());
    }

    public void transformEntryPoint(String entryPoint) {
        final String clazzName = toClassName(entryPoint);
//        transformTemplate.transform(clazzName, EntryPointTransform.class);
    }

    private String toClassName(String fullQualifiedMethodName) {
        final int classEndPosition = fullQualifiedMethodName.lastIndexOf('.');
        if (classEndPosition <= 0) {
            throw new IllegalArgumentException(
                    "invalid full qualified method name(" + fullQualifiedMethodName + "). not found method");
        }

        return fullQualifiedMethodName.substring(0, classEndPosition);
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate matchableTransformTemplate) {
        this.transformTemplate = matchableTransformTemplate;
    }
}
