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

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;

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
            this.addMessageDispatchChannelEditor();
            Filter<String> excludeDestinationFilter = config.getExcludeDestinationFilter();
            if (config.isTraceActiveMQClientProducer()) {
                this.addProducerEditor(excludeDestinationFilter);
            }
            if (config.isTraceActiveMQClientConsumer()) {
                this.addConsumerEditor(excludeDestinationFilter);
            }
        }
    }

    private void addTransportEditor() {

        transformTemplate.transform(ActiveMQClientConstants.ACTIVEMQ_FAILOVER_TRANSPORT_FQCN, new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addGetter(ActiveMQClientConstants.FIELD_GETTER_URI, ActiveMQClientConstants.FIELD_URI_TRANSPORT_SOCKET);

                return target.toBytecode();
            }
        });

        transformTemplate.transform(ActiveMQClientConstants.ACTIVEMQ_TCP_TRANSPORT_FQCN, new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addGetter(ActiveMQClientConstants.FIELD_GETTER_SOCKET, ActiveMQClientConstants.FIELD_TCP_TRANSPORT_SOCKET);

                return target.toBytecode();
            }
        });
    }

    // ActiveMQConnection.getTransport() method has been made public in version 5.1.0.
    // Inject transport field getter to cover for prior versions.
    private void addConnectionEditor() {
        transformTemplate.transform(ActiveMQClientConstants.ACTIVEMQ_CONNECTION_FQCN, new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addGetter(ActiveMQClientConstants.FIELD_GETTER_TRANSPORT, ActiveMQClientConstants.FIELD_ACTIVEMQ_CONNECTION_TRANSPORT);

                return target.toBytecode();
            }
        });
    }

    private void addMessageDispatchChannelEditor() {
        TransformCallback messageDispatchChannelTransformer = new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                // MessageDispatchChannel is an interface (5.4.0+)
                if (!target.isInterceptable()) {
                    return null;
                }

                final InstrumentMethod enqueue = target.getDeclaredMethod("enqueue", "org.apache.activemq.command.MessageDispatch");
                if (enqueue != null) {
                    enqueue.addScopedInterceptor(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_DISPATCH_CHANNEL_ENQUEUE_INTERCEPTOR_FQCN, ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE, ExecutionPolicy.INTERNAL);
                }
                final InstrumentMethod dequeue = target.getDeclaredMethod("dequeue", "long");
                if (dequeue != null) {
                    dequeue.addScopedInterceptor(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_DISPATCH_CHANNEL_DEQUEUE_INTERCEPTOR_FQCN, ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE, ExecutionPolicy.INTERNAL);
                }

                return target.toBytecode();
            }
        };
        transformTemplate.transform(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_DISPATCH_CHANNEL_FQCN, messageDispatchChannelTransformer);
        transformTemplate.transform(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_DISPATCH_CHANNEL_FIFO_FQCN, messageDispatchChannelTransformer);
        transformTemplate.transform(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_DISPATCH_CHANNEL_SIMPLE_PRIORITY_FQCN, messageDispatchChannelTransformer);
    }

    private void addProducerEditor(final Filter<String> excludeDestinationFilter) {
        final MethodFilter methodFilter = MethodFilters.chain(
                MethodFilters.name("send"),
                MethodFilters.argAt(0, "javax.jms.Destination"),
                MethodFilters.argAt(1, "javax.jms.Message")
        );
        transformTemplate.transform(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_PRODUCER_FQCN, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addGetter(ActiveMQClientConstants.FIELD_GETTER_ACTIVEMQ_SESSION, ActiveMQClientConstants.FIELD_ACTIVEMQ_MESSAGE_PRODUCER_SESSION);

                for (InstrumentMethod method : target.getDeclaredMethods(methodFilter)) {
                    try {
                        method.addScopedInterceptor(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_PRODUCER_SEND_INTERCEPTOR_FQCN, va(excludeDestinationFilter), ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE);
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

    private void addConsumerEditor(final Filter<String> excludeDestinationFilter) {
        transformTemplate.transform(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_CONSUMER_FQCN, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addGetter(ActiveMQClientConstants.FIELD_GETTER_ACTIVEMQ_SESSION, ActiveMQClientConstants.FIELD_ACTIVEMQ_MESSAGE_CONSUMER_SESSION);

                final InstrumentMethod dispatchMethod = target.getDeclaredMethod("dispatch", "org.apache.activemq.command.MessageDispatch");
                if (dispatchMethod != null) {
                    dispatchMethod.addScopedInterceptor(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_CONSUMER_DISPATCH_INTERCEPTOR_FQCN, va(excludeDestinationFilter), ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE);
                }

                InstrumentMethod receive = InstrumentUtils.findMethod(target, "receive");
                receive.addScopedInterceptor(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_CONSUMER_RECEIVE_INTERCEPTOR_FQCN, ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE);
                InstrumentMethod receiveWithParam = InstrumentUtils.findMethod(target, "receive", "long");
                receiveWithParam.addScopedInterceptor(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_CONSUMER_RECEIVE_INTERCEPTOR_FQCN, ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE);
                InstrumentMethod receiveNoWait = InstrumentUtils.findMethod(target, "receiveNoWait");
                receiveNoWait.addScopedInterceptor(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_CONSUMER_RECEIVE_INTERCEPTOR_FQCN, ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE);

                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
