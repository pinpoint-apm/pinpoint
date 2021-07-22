/*
 * Copyright 2021 NAVER Corp.
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
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.rocketmq.field.accessor.ChannelFutureGetter;
import com.navercorp.pinpoint.plugin.rocketmq.field.accessor.ChannelTablesAccessor;
import com.navercorp.pinpoint.plugin.rocketmq.field.accessor.ChannelTablesGetter;
import com.navercorp.pinpoint.plugin.rocketmq.field.accessor.EndPointFieldAccessor;
import com.navercorp.pinpoint.plugin.rocketmq.field.accessor.MQClientInstanceGetter;
import com.navercorp.pinpoint.plugin.rocketmq.interceptor.ConsumerMessageListenerConcurrentlyInterceptor;
import com.navercorp.pinpoint.plugin.rocketmq.interceptor.ConsumerMessageListenerOrderlyInterceptor;
import com.navercorp.pinpoint.plugin.rocketmq.interceptor.DefaultMQPushConsumerImplStartInterceptor;
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

        final List<String> basePackageNames = new ArrayList<>();
        // rocketmq spring boot support
        basePackageNames.add("org.apache.rocketmq.spring.support");
        // rocketmq spring cloud stream support
        basePackageNames.add("com.alibaba.cloud.stream.binder.rocketmq");

        final List<String> basePackages = config.getBasePackages();
        if (!basePackages.isEmpty()) {
            basePackageNames.addAll(basePackages);
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
            transformTemplate.transform("org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl",
                                        ConsumerTransform.class);
            transformTemplate.transform("org.apache.rocketmq.remoting.netty.NettyRemotingClient",
                                        RemotingTransform.class);
            transformTemplate.transform("org.apache.rocketmq.remoting.netty.NettyRemotingClient$ChannelWrapper",
                                        ChannelWrapperTransform.class);
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
    }

    public static class MQClientAPIImplTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className,
                                                                           classfileBuffer);
            target.addField(EndPointFieldAccessor.class);

            final List<InstrumentMethod> sendMessageMethods = target.getDeclaredMethods(
                    MethodFilters.name("sendMessage"));
            for (InstrumentMethod sendMessageMethod : sendMessageMethods) {
                if (sendMessageMethod.getParameterTypes().length == 12) {
                    sendMessageMethod.addInterceptor(ProducerSendInterceptor.class);
                }
            }

            final InstrumentMethod updateNameServerAddressListMethod = target.getDeclaredMethod(
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
            final List<InstrumentMethod> constructors = target.getDeclaredConstructors();
            for (InstrumentMethod constructor : constructors) {
                constructor.addInterceptor(ConstructInterceptor.class);
            }
            final InstrumentMethod onSuccessMethod = target.getDeclaredMethod("onSuccess",
                                                                              "org.apache.rocketmq.client.producer.SendResult");
            onSuccessMethod.addScopedInterceptor(OnSuccessInterceptor.class, RocketMQConstants.SCOPE);

            final InstrumentMethod onExceptionsMethod = target.getDeclaredMethod("onException",
                                                                                 "java.lang.Throwable");
            onExceptionsMethod.addScopedInterceptor(OnExceptionInterceptor.class, RocketMQConstants.SCOPE);
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
            final InstrumentMethod consumeMessageMethod = target.getDeclaredMethod("consumeMessage",
                                                                                   "java.util.List",
                                                                                   "org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext");
            consumeMessageMethod.addInterceptor(ConsumerMessageListenerConcurrentlyInterceptor.class);
            target.addField(ChannelTablesAccessor.class);
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
            final InstrumentMethod consumeMessageMethod = target.getDeclaredMethod("consumeMessage",
                                                                                   "java.util.List",
                                                                                   "org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext");
            consumeMessageMethod.addInterceptor(ConsumerMessageListenerOrderlyInterceptor.class);
            target.addField(ChannelTablesAccessor.class);
            return target.toBytecode();
        }
    }

    public static class ConsumerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className,
                                                                           classfileBuffer);
            target.addGetter(MQClientInstanceGetter.class, "mQClientFactory");

            final InstrumentMethod consumeStartMethod = target.getDeclaredMethod("start");
            consumeStartMethod.addInterceptor(DefaultMQPushConsumerImplStartInterceptor.class);

            return target.toBytecode();
        }
    }

    public static class RemotingTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className,
                                                                           classfileBuffer);
            target.addGetter(ChannelTablesGetter.class, "channelTables");
            return target.toBytecode();
        }
    }

    public static class ChannelWrapperTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className,
                                                                           classfileBuffer);
            target.addGetter(ChannelFutureGetter.class, "channelFuture");
            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate matchableTransformTemplate) {
        this.transformTemplate = matchableTransformTemplate;
    }
}
