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
import com.navercorp.pinpoint.bootstrap.resolver.condition.MainClassCondition;
import com.navercorp.pinpoint.bootstrap.util.jdk.ThreadLocalRandom;
import com.navercorp.pinpoint.plugin.jdk.exec.interceptor.JustRetransform;

import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
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
       String mainClass = new MainClassCondition().getValue();
       logger.info("JdkExec: main class: " + mainClass);
       transformTemplate.transform(mainClass, new TransformCallback() {
           @Override
           public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
               InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
               TransformCallback transformer = new TransformCallback() {
                   @Override
                   public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                       logger.info("JdkExec: register retransform FutureTask");
                       InterceptorScope scope = instrumentor.getInterceptorScope(SCOPE_NAME);
                       InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                       InstrumentMethod ctor1 = target.getConstructor(Callable.class.getCanonicalName());
                       InstrumentMethod ctor2 = target.getConstructor(Runnable.class.getCanonicalName(), Object.class.getCanonicalName());

                       //constructing async task
                       for(InstrumentMethod method: new InstrumentMethod[]{ctor1, ctor2}) {
                           logger.info("JdkExec: instrumenting constructor: " + method.getName()
                                   + ", arg num: " + method.getParameterTypes().length);
                           method.addScopedInterceptor("com.navercorp.pinpoint.plugin.jdk.exec.interceptor.AsyncInitiatorInterceptor", scope);
                       }

                       //running async task
                       target.getDeclaredMethod("run").addInterceptor("com.navercorp.pinpoint.plugin.jdk.exec.interceptor.WorkerRunInterceptor");

                       logger.info("JdkExec: register retransform FutureTask done");
                       return target.toBytecode();
                   }
               };

               InstrumentMethod method = target.getDeclaredMethod("main", "java.lang.String[]");
               method.addInterceptor(JustRetransform.class.getName(), va(transformer));
               return target.toBytecode();
           }
       });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
