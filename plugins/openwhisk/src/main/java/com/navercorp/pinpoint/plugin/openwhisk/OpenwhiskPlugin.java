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
import com.navercorp.pinpoint.plugin.openwhisk.accessor.PinpointTraceAccessor;

import java.security.ProtectionDomain;
import java.util.Optional;

/**
 * @author upgle (Seonghyun, Oh)
 */
public class OpenwhiskPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final OpenwhiskConfig config = new OpenwhiskConfig(context.getConfig());
        if (!config.isEnable()) {
            return;
        }

        OpenwhiskDetector openwhiskDetector = new OpenwhiskDetector();
        context.addApplicationTypeDetector(openwhiskDetector);

        transformTemplate.transform("whisk.http.BasicHttpService$$anonfun$assignId$1$$anonfun$apply$13", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                final InstrumentMethod method = target.getDeclaredMethod("apply", "akka.http.scaladsl.server.RequestContext");
                method.addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.TransactionIdCreateInterceptor");

                return target.toBytecode();
            }
        });

        transformTemplate.transform("whisk.common.TransactionId$", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                Optional<InstrumentMethod> method = target.getDeclaredMethods()
                        .stream()
                        .filter(instrumentMethod -> instrumentMethod.getDescriptor().getMethodName().equals("started$extension"))
                        .findFirst();

                if (method.isPresent()) {
                    method.get().addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.TransactionIdStartedInterceptor");
                }

                Optional<InstrumentMethod> method2 = target.getDeclaredMethods()
                        .stream()
                        .filter(instrumentMethod -> instrumentMethod.getDescriptor().getMethodName().equals("finished$extension"))
                        .findFirst();

                if (method2.isPresent()) {
                    method2.get().addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.TransactionIdFinishedInterceptor");
                }

                return target.toBytecode();
            }
        });

        transformTemplate.transform("whisk.common.StartMarker", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(AsyncContextAccessor.class.getName());
                target.addField(PinpointTraceAccessor.class.getName());

                Optional<InstrumentMethod> method = target.getDeclaredMethods()
                        .stream()
                        .filter(instrumentMethod -> instrumentMethod.getDescriptor().getMethodName().contains("copy"))
                        .findFirst();

                if (method.isPresent()) {
                    method.get().addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.StartMarkerCopyInterceptor");
                }

                return target.toBytecode();
            }
        });


        transformTemplate.transform("whisk.common.TransactionMetadata", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                InstrumentMethod constructor = target.getConstructor("java.lang.String", "java.time.Instant", "boolean");
                constructor.addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.TransactionMetadataInterceptor");

                target.addField(AsyncContextAccessor.class.getName());
                target.addSetter("com.navercorp.pinpoint.plugin.openwhisk.interceptor.IdSetter", "id", true);

                return target.toBytecode();
            }
        });

    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
