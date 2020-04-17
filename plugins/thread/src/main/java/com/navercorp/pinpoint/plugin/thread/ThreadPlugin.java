package com.navercorp.pinpoint.plugin.thread;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.thread.interceptor.ThreadCallInterceptor;
import com.navercorp.pinpoint.plugin.thread.interceptor.ThreadConstructorInterceptor;

import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author echo
 * <p>
 * this plugin for record async thread trace
 */
public class ThreadPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        ThreadConfig threadConfig = new ThreadConfig(context.getConfig());

        logger.info("init {},config:{}", this.getClass().getSimpleName(), threadConfig);
        if (StringUtils.isEmpty(threadConfig.getThreadMatchPackage())) {
            logger.info("thread plugin package is empty,skip it");
            return;
        }
        addRunnableInterceptor(threadConfig);
        addCallableInterceptor(threadConfig);
    }

    private void addRunnableInterceptor(ThreadConfig threadConfig) {
        Matcher matcher = Matchers.newPackageBasedMatcher(threadConfig.getThreadMatchPackage(), new InterfaceInternalNameMatcherOperand("java.lang.Runnable", true));
        transformTemplate.transform(matcher, RunnableTransformCallback.class);
    }

    public static class RunnableTransformCallback implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, protectionDomain, classfileBuffer);
            List<InstrumentMethod> allConstructor = target.getDeclaredConstructors();
            for (int i = 0; i < allConstructor.size(); i++) {
                InstrumentMethod instrumentMethod = allConstructor.get(i);
                instrumentMethod.addScopedInterceptor(ThreadConstructorInterceptor.class, ThreadConstants.SCOPE_NAME);
            }
            target.addField(AsyncContextAccessor.class);
            final InstrumentMethod callMethod = target.getDeclaredMethod("run");
            if (callMethod != null) {
                callMethod.addInterceptor(ThreadCallInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void addCallableInterceptor(ThreadConfig threadConfig) {
        Matcher matcher = Matchers.newPackageBasedMatcher(threadConfig.getThreadMatchPackage(), new InterfaceInternalNameMatcherOperand("java.util.concurrent.Callable", true));
        transformTemplate.transform(matcher, CallableTransformCallback.class);
    }

    public static class CallableTransformCallback implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, protectionDomain, classfileBuffer);
            List<InstrumentMethod> allConstructor = target.getDeclaredConstructors();
            for (int i = 0; i < allConstructor.size(); i++) {
                InstrumentMethod instrumentMethod = allConstructor.get(i);
                instrumentMethod.addScopedInterceptor(ThreadConstructorInterceptor.class, ThreadConstants.SCOPE_NAME);
            }
            target.addField(AsyncContextAccessor.class);
            final InstrumentMethod callMethod = target.getDeclaredMethod("call");
            if (callMethod != null) {
                callMethod.addInterceptor(ThreadCallInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}