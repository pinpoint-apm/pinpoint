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
            return;
        }
        if (config.isTraceActiveMQClientConsumer() || config.isTraceActiveMQClientProducer()) {
            this.addTransportEditor();
            this.addConnectionEditor();
//            this.addMessageDispatchChannelEditor();
            Filter<String> excludeDestinationFilter = config.getExcludeDestinationFilter();
            if (config.isTraceActiveMQClientProducer()) {
                this.addProducerEditor(excludeDestinationFilter);
            }
            if (config.isTraceActiveMQClientConsumer()) {
                boolean traceActiveMQTextMessage = config.isTraceActiveMQTextMessage();
                this.addConsumerEditor(traceActiveMQTextMessage, excludeDestinationFilter);
            }
        }
    }

    private void addTransportEditor() {

        transformTemplate.transform("org.apache.activemq.transport.failover.FailoverTransport", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addGetter("com.navercorp.pinpoint.plugin.activemq.client.field.getter.URIGetter", "connectedTransportURI");

                return target.toBytecode();
            }
        });

        transformTemplate.transform("org.apache.activemq.transport.tcp.TcpTransport", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addGetter("com.navercorp.pinpoint.plugin.activemq.client.field.getter.SocketGetter", "socket");

                return target.toBytecode();
            }
        });
    }

    // ActiveMQConnection.getTransport() method has been made public in version 5.1.0.
    // Inject transport field getter to cover for prior versions.
    private void addConnectionEditor() {
        transformTemplate.transform("org.apache.activemq.ActiveMQConnection", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addGetter("com.navercorp.pinpoint.plugin.activemq.client.field.getter.TransportGetter", "transport");

                return target.toBytecode();
            }
        });
    }

    private void addProducerEditor(final Filter<String> excludeDestinationFilter) {
        final MethodFilter methodFilter = MethodFilters.chain(
                MethodFilters.name("send"),
                MethodFilters.argAt(0, "javax.jms.Destination"),
                MethodFilters.argAt(1, "javax.jms.Message")
        );
        transformTemplate.transform("org.apache.activemq.ActiveMQMessageProducer", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addGetter("com.navercorp.pinpoint.plugin.activemq.client.field.getter.ActiveMQSessionGetter", "session");

                for (InstrumentMethod method : target.getDeclaredMethods(methodFilter)) {
                    try {
                        method.addScopedInterceptor("com.navercorp.pinpoint.plugin.activemq.client.interceptor.ActiveMQMessageProducerSendInterceptor", va(excludeDestinationFilter), ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE);
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

    private void addConsumerEditor(final boolean traceActiveMQTextMessage, final Filter<String> excludeDestinationFilter) {
        transformTemplate.transform("org.apache.activemq.ActiveMQMessageConsumer", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addGetter("com.navercorp.pinpoint.plugin.activemq.client.field.getter.ActiveMQSessionGetter", "session");

                final InstrumentMethod dispatchMethod = target.getDeclaredMethod("dispatch", "org.apache.activemq.command.MessageDispatch");
                if (dispatchMethod != null) {
                    dispatchMethod.addScopedInterceptor("com.navercorp.pinpoint.plugin.activemq.client.interceptor.ActiveMQMessageConsumerDispatchInterceptor", va(excludeDestinationFilter), ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE);
                }

                InstrumentMethod receive = target.getDeclaredMethod("receive");
                if (receive != null) {
                    receive.addScopedInterceptor("com.navercorp.pinpoint.plugin.activemq.client.interceptor.ActiveMQMessageConsumerReceiveInterceptor", va(traceActiveMQTextMessage), ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE);
                }
                InstrumentMethod receiveWithParam = target.getDeclaredMethod("receive", "long");
                if (receiveWithParam != null) {
                    receiveWithParam.addScopedInterceptor("com.navercorp.pinpoint.plugin.activemq.client.interceptor.ActiveMQMessageConsumerReceiveInterceptor", va(traceActiveMQTextMessage), ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE);
                }
                InstrumentMethod receiveNoWait = target.getDeclaredMethod("receiveNoWait");
                if (receiveNoWait != null) {
                    receiveNoWait.addScopedInterceptor("com.navercorp.pinpoint.plugin.activemq.client.interceptor.ActiveMQMessageConsumerReceiveInterceptor", va(traceActiveMQTextMessage), ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE);
                }

                InstrumentMethod createActiveMQMessage = target.getDeclaredMethod("createActiveMQMessage", "org.apache.activemq.command.MessageDispatch");
                if (createActiveMQMessage != null) {
                    createActiveMQMessage.addInterceptor("com.navercorp.pinpoint.plugin.activemq.client.interceptor.ActiveMQMessageConsumerCreateActiveMQMessageInterceptor");
                }

                return target.toBytecode();
            }
        });

        transformTemplate.transform("org.apache.activemq.command.MessageDispatch", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addField(AsyncContextAccessor.class.getName());
                return target.toBytecode();
            }
        });

        transformTemplate.transform("org.apache.activemq.command.ActiveMQMessage", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addField(AsyncContextAccessor.class.getName());
                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
