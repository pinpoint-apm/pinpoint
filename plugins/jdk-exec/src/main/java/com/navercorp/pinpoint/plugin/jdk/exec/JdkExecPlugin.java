package com.navercorp.pinpoint.plugin.jdk.exec;

import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.util.jdk.ThreadLocalRandom;
import com.navercorp.pinpoint.plugin.jdk.exec.interceptor.JustRetransform;

import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author lisn
 */
public class JdkExecPlugin implements ProfilerPlugin, TransformTemplateAware {
    private TransformTemplate transformTemplate;
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private static final String SCOPE_NAME = "JdkExec";

    @Override
    public void setup(ProfilerPluginSetupContext context) {
       String mainClass = "analyzer2.web.Application";
       transformTemplate.transform(mainClass, new TransformCallback() {
           @Override
           public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
               InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
               TransformCallback transformer1 = new TransformCallback() {
                   @Override
                   public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                       InterceptorScope scope = instrumentor.getInterceptorScope(SCOPE_NAME);
                       InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                       for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("newTaskFor"))) {
                           method.addScopedInterceptor("com.navercorp.pinpoint.plugin.jdk.exec.interceptor.AsyncInitiatorInterceptor", scope);
                       }
                       return target.toBytecode();
                   }
               };
               TransformCallback transformer2 = new TransformCallback() {
                   @Override
                   public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                       InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                       target.getDeclaredMethod("run").addInterceptor("com.navercorp.pinpoint.plugin.jdk.exec.interceptor.WorkerRunInterceptor");
                       return target.toBytecode();
                   }
               };

               for(InstrumentMethod method: target.getDeclaredMethods(MethodFilters.name("main"))) {
                   method.addInterceptor(JustRetransform.class.getName(), va(transformer1, transformer2));
               }

               return target.toBytecode();
           }
       });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
