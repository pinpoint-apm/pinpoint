/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.plugin.activemq.client;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.activemq.client.field.getter.ActiveMQSessionGetter;
import com.navercorp.pinpoint.plugin.activemq.client.field.getter.SocketGetter;
import com.navercorp.pinpoint.plugin.activemq.client.field.getter.TransportGetter;
import com.navercorp.pinpoint.plugin.activemq.client.field.getter.URIGetter;
import com.navercorp.pinpoint.plugin.activemq.client.interceptor.ActiveMQMessageConsumerCreateActiveMQMessageInterceptor;
import com.navercorp.pinpoint.plugin.activemq.client.interceptor.ActiveMQMessageConsumerDispatchInterceptor;
import com.navercorp.pinpoint.plugin.activemq.client.interceptor.ActiveMQMessageConsumerReceiveInterceptor;
import com.navercorp.pinpoint.plugin.activemq.client.interceptor.ActiveMQMessageProducerSendInterceptor;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author HyunGil Jeong
 */
public class ActiveMQClientPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        ActiveMQClientPluginConfig config = new ActiveMQClientPluginConfig(context.getConfig());
        if (!config.isTraceActiveMQClient()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        if (config.isTraceActiveMQClientConsumer() || config.isTraceActiveMQClientProducer()) {
            this.addTransportEditor();
            this.addConnectionEditor();
//            this.addMessageDispatchChannelEditor();
            if (config.isTraceActiveMQClientProducer()) {
                this.addProducerEditor();
            }
            if (config.isTraceActiveMQClientConsumer()) {
                this.addConsumerEditor();
            }
        }
    }

    private void addTransportEditor() {

        transformTemplate.transform("org.apache.activemq.transport.failover.FailoverTransport", FailoverTransportTransform.class);

        transformTemplate.transform("org.apache.activemq.transport.tcp.TcpTransport", TcpTransportTransform.class);
    }

    public static class FailoverTransportTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addGetter(URIGetter.class, "connectedTransportURI");

            return target.toBytecode();
        }
    }

    public static class TcpTransportTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addGetter(SocketGetter.class, "socket");

            return target.toBytecode();
        }
    }

    // ActiveMQConnection.getTransport() method has been made public in version 5.1.0.
    // Inject transport field getter to cover for prior versions.
    private void addConnectionEditor() {
        transformTemplate.transform("org.apache.activemq.ActiveMQConnection", ActiveMQConnectionTransform.class);
    }

    public static class ActiveMQConnectionTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addGetter(TransportGetter.class, "transport");

            return target.toBytecode();
        }
    }

    private void addProducerEditor() {

        transformTemplate.transform("org.apache.activemq.ActiveMQMessageProducer", ActiveMQMessageProducerTransform.class);
    }

    public static class ActiveMQMessageProducerTransform implements TransformCallback {

        private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            ActiveMQClientPluginConfig config = new ActiveMQClientPluginConfig(instrumentor.getProfilerConfig());
            Filter<String> excludeDestinationFilter = config.getExcludeDestinationFilter();

            target.addGetter(ActiveMQSessionGetter.class, "session");
            final MethodFilter methodFilter = MethodFilters.chain(
                    MethodFilters.name("send"),
                    MethodFilters.argAt(0, "javax.jms.Destination"),
                    MethodFilters.argAt(1, "javax.jms.Message")
            );

            for (InstrumentMethod method : target.getDeclaredMethods(methodFilter)) {
                try {
                    method.addScopedInterceptor(ActiveMQMessageProducerSendInterceptor.class, va(excludeDestinationFilter), ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE);
                } catch (Exception e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unsupported method " + method, e);
                    }
                }
            }

            return target.toBytecode();
        }
    }

    private void addConsumerEditor() {
        transformTemplate.transform("org.apache.activemq.ActiveMQMessageConsumer", ActiveMQMessageConsumerTransform.class);

        transformTemplate.transform("org.apache.activemq.command.MessageDispatch", AddAsyncContextAccessorTransform.class);

        transformTemplate.transform("org.apache.activemq.command.ActiveMQMessage", AddAsyncContextAccessorTransform.class);
    }

    public static class ActiveMQMessageConsumerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            ActiveMQClientPluginConfig config = new ActiveMQClientPluginConfig(instrumentor.getProfilerConfig());
            Filter<String> excludeDestinationFilter = config.getExcludeDestinationFilter();
            boolean traceActiveMQTextMessage = config.isTraceActiveMQTextMessage();

            target.addGetter(ActiveMQSessionGetter.class, "session");

            final InstrumentMethod dispatchMethod = target.getDeclaredMethod("dispatch", "org.apache.activemq.command.MessageDispatch");
            if (dispatchMethod != null) {
                dispatchMethod.addScopedInterceptor(ActiveMQMessageConsumerDispatchInterceptor.class, va(excludeDestinationFilter), ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE);
            }

            InstrumentMethod receive = target.getDeclaredMethod("receive");
            if (receive != null) {
                receive.addScopedInterceptor(ActiveMQMessageConsumerReceiveInterceptor.class, va(traceActiveMQTextMessage), ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE);
            }
            InstrumentMethod receiveWithParam = target.getDeclaredMethod("receive", "long");
            if (receiveWithParam != null) {
                receiveWithParam.addScopedInterceptor(ActiveMQMessageConsumerReceiveInterceptor.class, va(traceActiveMQTextMessage), ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE);
            }
            InstrumentMethod receiveNoWait = target.getDeclaredMethod("receiveNoWait");
            if (receiveNoWait != null) {
                receiveNoWait.addScopedInterceptor(ActiveMQMessageConsumerReceiveInterceptor.class, va(traceActiveMQTextMessage), ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE);
            }

            InstrumentMethod createActiveMQMessage = target.getDeclaredMethod("createActiveMQMessage", "org.apache.activemq.command.MessageDispatch");
            if (createActiveMQMessage != null) {
                createActiveMQMessage.addInterceptor(ActiveMQMessageConsumerCreateActiveMQMessageInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class AddAsyncContextAccessorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
