package com.navercorp.pinpoint.plugin.jdk.exec;

import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.resolver.condition.MainClassCondition;
import com.navercorp.pinpoint.plugin.jdk.exec.interceptor.JustRetransform;

import java.security.ProtectionDomain;
import java.util.concurrent.Callable;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author hamlet-lee
 */
public class JdkExecPlugin implements ProfilerPlugin, TransformTemplateAware {
    private TransformTemplate transformTemplate;
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private static final String SCOPE_NAME = "JdkExec";

    @Override
    public void setup(ProfilerPluginSetupContext context) {
       boolean integrationTesting = false;

       String mainClass = new MainClassCondition().getValue();
       logger.info("JdkExec: main class: " + mainClass);
       if( mainClass.equals( "com.navercorp.pinpoint.test.plugin.ForkedPinpointPluginTest")) {
           logger.info("JdkExec: Integration Test strategy");
           integrationTesting = true;
       }else{
           logger.info("JdkExec: use main class strategy");
       }
       final boolean isIntegrationTesting = integrationTesting;
        
       final JdkExecConfig config = new JdkExecConfig(context.getConfig());
       if(!config.isProfile()){
           logger.info("JdkExec: not enabled");
           return;
       }else{
           logger.info("JdkExec: enabled");
       }

       TransformCallback transformCallback = new TransformCallback() {
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

                InstrumentMethod method = null;
                if(  isIntegrationTesting ) {
                    logger.info("JdkExec: instrument org.junit.runner.Runner ctor");
                    method = target.getConstructor();
                }else{
                    logger.info("JdkExec: instrument main");
                    method = target.getDeclaredMethod("main", "java.lang.String[]");
                }
                //InstrumentMethod method = target.getDeclaredMethod("checkAndLoadMain", "boolean", "int", "java.lang.String");
                method.addInterceptor(JustRetransform.class.getName(), va(transformer));
                return target.toBytecode();
            }
        };
       if( isIntegrationTesting ) {
           transformTemplate.transform("org.junit.runner.Runner", transformCallback);
       }else{
           transformTemplate.transform(mainClass, transformCallback);
       }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
