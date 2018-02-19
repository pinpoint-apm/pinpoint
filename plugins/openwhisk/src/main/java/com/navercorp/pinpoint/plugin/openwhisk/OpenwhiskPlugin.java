package com.navercorp.pinpoint.plugin.openwhisk;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.openwhisk.interceptor.PinpointHeaderAccessor;

import java.security.ProtectionDomain;

public class OpenwhiskPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final OpenwhiskConfig config = new OpenwhiskConfig(context.getConfig());
        if (!config.isEnable()) {
            return;
        }
        transformTemplate.transform("whisk.http.BasicHttpService$$anonfun$assignId$1$$anonfun$apply$12", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                final InstrumentMethod method = target.getDeclaredMethod("apply", "akka.http.scaladsl.server.RequestContext");
                method.addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.TransactionIdCreateInterceptor");
                return target.toBytecode();
            }
        });

        transformTemplate.transform("whisk.common.TransactionMetadata", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(AsyncContextAccessor.class.getName());
                target.addField(PinpointHeaderAccessor.class.getName());
                return target.toBytecode();
            }
        });

        transformTemplate.transform("whisk.connector.kafka.KafkaProducerConnector", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                final InstrumentMethod method = target.getDeclaredMethod("send", "java.lang.String", "whisk.core.connector.Message", "int");
                method.addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.OpenwhiskKafkaProducerConnectorInterceptor");
                return target.toBytecode();
            }
        });

        transformTemplate.transform("whisk.core.invoker.InvokerReactive$$anonfun$processActivationMessage$5", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                final InstrumentMethod method = target.getDeclaredMethod("apply", "whisk.core.connector.ActivationMessage");
                method.addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.OpenwhiskInvokerReactiveInterceptor");
                return target.toBytecode();
            }
        });

        transformTemplate.transform("whisk.core.containerpool.ContainerProxy", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                InstrumentMethod method = target.getDeclaredMethod("initializeAndRun", "whisk.core.containerpool.Container", "whisk.core.containerpool.Run", "whisk.common.TransactionMetadata");
                method.addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.ContainerProxyInitializeAndRunInterceptor");
                return target.toBytecode();
            }
        });

        transformTemplate.transform("whisk.core.loadBalancer.ContainerPoolBalancer", new TransformCallback() {


            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                InstrumentMethod method = target.getDeclaredMethod("processActiveAck", "byte[]");
                method.addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.ContainerPoolBalancerProcessActiveAckInterceptor");
                return target.toBytecode();
            }
        });

        transformTemplate.transform("whisk.core.connector.ActivationMessage$", new TransformCallback() {


            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.weave("com.navercorp.pinpoint.plugin.openwhisk.aspect.InvokeReactiveAspect");
                return target.toBytecode();
            }
        });

        transformTemplate.transform("whisk.core.connector.CompletionMessage$", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.weave("com.navercorp.pinpoint.plugin.openwhisk.aspect.CompletionMessageAspect");
                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
