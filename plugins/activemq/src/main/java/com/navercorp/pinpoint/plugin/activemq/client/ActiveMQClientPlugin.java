package com.navercorp.pinpoint.plugin.activemq.client;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import java.security.ProtectionDomain;

/**
 * @author HyunGil Jeong
 */
public class ActiveMQClientPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        this.addTransportEditor();
        this.addMessageDispatchChannelEditor();
        this.addProducerEditor();
        this.addConsumerEditor();
    }

    private void addTransportEditor() {
        transformTemplate.transform(ActiveMQClientConstants.ACTIVEMQ_TCP_TRANSPORT_FQCN, new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addGetter(ActiveMQClientConstants.FIELD_GETTER_SOCKET, ActiveMQClientConstants.FIELD_TCP_TRANSPORT_SOCKET);

                return target.toBytecode();
            }
        });
    }

    private void addMessageDispatchChannelEditor() {
        String[] messageDispatchChannelImplsFqcn = {
                ActiveMQClientConstants.ACTIVEMQ_MESSAGE_DISPATCH_CHANNEL_FIFO_FQCN,
                ActiveMQClientConstants.ACTIVEMQ_MESSAGE_DISPATCH_CHANNEL_SIMPLE_PRIORITY_FQCN
        };
        for (String messageDispatchChannelImplFqcn : messageDispatchChannelImplsFqcn) {
            transformTemplate.transform(messageDispatchChannelImplFqcn, new TransformCallback() {

                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                    final InstrumentMethod enqueue = target.getDeclaredMethod("enqueue", "org.apache.activemq.command.MessageDispatch");
                    if (enqueue != null) {
                        enqueue.addInterceptor(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_DISPATCH_CHANNEL_ENQUEUE_INTERCEPTOR_FQCN);
                    }
                    final InstrumentMethod dequeue = target.getDeclaredMethod("dequeue", "long");
                    if (dequeue != null) {
                        dequeue.addInterceptor(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_DISPATCH_CHANNEL_DEQUEUE_INTERCEPTOR_FQCN);
                    }

                    return target.toBytecode();
                }
            });
        }
    }

    private void addProducerEditor() {
        transformTemplate.transform(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_PRODUCER_FQCN, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addGetter(ActiveMQClientConstants.FIELD_GETTER_ACTIVEMQ_SESSION, ActiveMQClientConstants.FIELD_ACTIVEMQ_MESSAGE_PRODUCER_SESSION);

                final InstrumentMethod method = target.getDeclaredMethod("send", "javax.jms.Destination", "javax.jms.Message", "int", "int", "long", "org.apache.activemq.AsyncCallback");
                if (method != null) {
                    method.addInterceptor(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_PRODUCER_SEND_INTERCEPTOR_FQCN);
                }

                return target.toBytecode();
            }
        });
    }

    private void addConsumerEditor() {
        transformTemplate.transform(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_CONSUMER_FQCN, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addGetter(ActiveMQClientConstants.FIELD_GETTER_ACTIVEMQ_SESSION, ActiveMQClientConstants.FIELD_ACTIVEMQ_MESSAGE_CONSUMER_SESSION);

                final InstrumentMethod dispatchMethod = target.getDeclaredMethod("dispatch", "org.apache.activemq.command.MessageDispatch");
                if (dispatchMethod != null) {
                    dispatchMethod.addInterceptor(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_CONSUMER_DISPATCH_INTERCEPTOR_FQCN);
                }

                target.addInterceptor(ActiveMQClientConstants.ACTIVEMQ_MESSAGE_CONSUMER_RECEIVE_INTERCEPTOR_FQCN);

                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}