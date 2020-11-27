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
import java.util.List;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.rocketmq.field.accessor.RemoteAddressFieldAccessor;
import com.navercorp.pinpoint.plugin.rocketmq.interceptor.MessageListenerConcurrentlyInterceptor;
import com.navercorp.pinpoint.plugin.rocketmq.interceptor.MessageListenerOrderlyInterceptor;
import com.navercorp.pinpoint.plugin.rocketmq.interceptor.ProducerSendCallBackInterceptor;
import com.navercorp.pinpoint.plugin.rocketmq.interceptor.ProducerSendInterceptor;
import com.navercorp.pinpoint.plugin.rocketmq.interceptor.UpdateNameServerAddressListInterceptor;

/**
 * @author messi-gao
 */
public class RocketMQPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final RocketMQConfig config = new RocketMQConfig(context.getConfig());
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        if (config.isProducerEnable()) {
            transformTemplate.transform("org.apache.rocketmq.client.impl.MQClientAPIImpl",
                                        MQClientAPIImplTransform.class);
            transformTemplate.transform("org.apache.rocketmq.client.producer.SendCallback",
                                        SendCallbackTransform.class);
        }

        if (config.isConsumerEnable()) {
            transformTemplate.transform(
                    "org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently",
                    MessageListenerConcurrentlyTransform.class);
            transformTemplate.transform("org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly",
                                        MessageListenerOrderlyTransform.class);
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

            target.addField(RemoteAddressFieldAccessor.class);

            final List<InstrumentMethod> sendMessageMethods = target.getDeclaredMethods(
                    MethodFilters.name("sendMessage"));
            for (InstrumentMethod sendMessageMethod : sendMessageMethods) {
                if (sendMessageMethod.getParameterTypes().length == 12) {
                    sendMessageMethod.addInterceptor(ProducerSendInterceptor.class);
                }
            }

            InstrumentMethod updateNameServerAddressListMethod = target.getDeclaredMethod(
                    "updateNameServerAddressList");
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
            final MethodFilter onSuccessFilter = MethodFilters.chain(
                    MethodFilters.name("onSuccess"),
                    MethodFilters.argAt(0, "org.apache.rocketmq.client.producer.SendResult"));
            final List<InstrumentMethod> onSuccessMethods = target.getDeclaredMethods(onSuccessFilter);
            for (InstrumentMethod instrumentMethod : onSuccessMethods) {
                instrumentMethod.addInterceptor(ProducerSendCallBackInterceptor.OnSuccessInterceptor.class);
            }

            final MethodFilter onExceptionFilter = MethodFilters.chain(
                    MethodFilters.name("onException"),
                    MethodFilters.argAt(0, "java.lang.Throwable"));
            final List<InstrumentMethod> onExceptions = target.getDeclaredMethods(onExceptionFilter);
            for (InstrumentMethod instrumentMethod : onExceptions) {
                instrumentMethod.addInterceptor(ProducerSendCallBackInterceptor.OnExceptionInterceptor.class);
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
            target.getDeclaredMethod("consumeMessage")
                  .addInterceptor(MessageListenerConcurrentlyInterceptor.class);
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
            target.getDeclaredMethod("consumeMessage")
                  .addInterceptor(MessageListenerOrderlyInterceptor.class);
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
}
