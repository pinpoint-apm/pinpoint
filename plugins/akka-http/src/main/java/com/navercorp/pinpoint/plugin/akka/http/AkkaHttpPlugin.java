package com.navercorp.pinpoint.plugin.akka.http;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import java.security.ProtectionDomain;


public class AkkaHttpPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        transformTemplate.transform("akka.http.scaladsl.server.directives.ExecutionDirectives", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("$anonfun$handleExceptions$2"))) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.akka.http.interceptor.ExecutionDirectivesHandleExceptionsInterceptor");
                    break;
                }
                return target.toBytecode();
            }
        });

        transformTemplate.transform("akka.http.scaladsl.server.directives.ExecutionDirectives$$anonfun$handleExceptions$1$$anonfun$apply$1", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                final InstrumentMethod method = target.getDeclaredMethod("apply", "akka.http.scaladsl.server.RequestContext");
                method.addInterceptor("com.navercorp.pinpoint.plugin.akka.http.interceptor.ExecutionDirectivesHandleExceptionsInterceptor");
                return target.toBytecode();
            }
        });

        transformTemplate.transform("akka.http.scaladsl.server.RequestContextImpl", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(AsyncContextAccessor.class.getName());
                final InstrumentMethod completeMethod = target.getDeclaredMethod("complete", "akka.http.scaladsl.marshalling.ToResponseMarshallable");
                completeMethod.addInterceptor("com.navercorp.pinpoint.plugin.akka.http.interceptor.RequestContextImplCompleteInterceptor");

                final InstrumentMethod redirectMethod = target.getDeclaredMethod("redirect", "akka.http.scaladsl.model.Uri", "akka.http.scaladsl.model.StatusCodes$Redirection");
                redirectMethod.addInterceptor("com.navercorp.pinpoint.plugin.akka.http.interceptor.RequestContextImplRedirectInterceptor");

                final InstrumentMethod failMethod = target.getDeclaredMethod("fail", "java.lang.Throwable");
                failMethod.addInterceptor("com.navercorp.pinpoint.plugin.akka.http.interceptor.RequestContextImplFailInterceptor");

                final InstrumentMethod copyMethod = target.getDeclaredMethod("copy", "akka.http.scaladsl.model.HttpRequest",
                        "akka.http.scaladsl.model.Uri$Path", "scala.concurrent.ExecutionContextExecutor", "akka.stream.Materializer", "akka.event.LoggingAdapter",
                        "akka.http.scaladsl.settings.RoutingSettings", "akka.http.scaladsl.settings.ParserSettings");
                copyMethod.addInterceptor("com.navercorp.pinpoint.plugin.akka.http.interceptor.RequestContextImplCopyInterceptor");
                return target.toBytecode();
            }
        });

        transformTemplate.transform("akka.http.javadsl.model.HttpRequest", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
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
