package com.navercorp.pinpoint.plugin.jdk.exec;

import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import java.security.ProtectionDomain;
import java.util.List;
import java.util.concurrent.FutureTask;

/**
 * @author lisn
 */
public class JdkExecPlugin implements ProfilerPlugin, TransformTemplateAware {
    private TransformTemplate transformTemplate;
    private static final String SCOPE_NAME = "JdkExec";

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        transformTemplate.retransform(java.util.concurrent.AbstractExecutorService.class, new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InterceptorScope scope = instrumentor.getInterceptorScope(SCOPE_NAME);
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                //target.getDeclaredMethod("newTaskFor", "java.lang.Runnable","java.lang.Object")
                for(InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("newTaskFor"))){
                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.jdk.exec.interceptor.AsyncInitiatorInterceptor", scope);
                }
                return target.toBytecode();
            }
        });

        transformTemplate.retransform(FutureTask.class, new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.getDeclaredMethod("run").addInterceptor("com.navercorp.pinpoint.plugin.jdk.exec.interceptor.WorkerRunInterceptor");
                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
