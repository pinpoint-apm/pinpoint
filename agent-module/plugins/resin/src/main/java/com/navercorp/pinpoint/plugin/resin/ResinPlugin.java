package com.navercorp.pinpoint.plugin.resin;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.resin.interceptor.HttpServletRequestImplInterceptor;
import com.navercorp.pinpoint.plugin.resin.interceptor.ServletInvocationServiceInterceptor;
import com.navercorp.pinpoint.plugin.resin.interceptor.WebAppInterceptor;

import java.security.ProtectionDomain;

/**
 * @author huangpengjie@fang.com
 * @author jaehong.kim
 */
public class ResinPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final ResinConfig config = new ResinConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            final ResinDetector resinDetector = new ResinDetector(config.getBootstrapMains());
            if (resinDetector.detect()) {
                logger.info("Detected application type : {}", ResinConstants.RESIN);
                if (!context.registerApplicationType(ResinConstants.RESIN)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), ResinConstants.RESIN);
                }
            }
        }

        logger.info("Adding Resin transformers");
        addTransformers(config);
    }

    private void addTransformers(final ResinConfig config) {
        // Dispatch library & Add servlet request listener. Servlet 2.4
        addWebApp();
        // Add async listener. Servlet 3.0
        addHttpServletRequestImpl();
        // Record HTTP status code & Close trace
        addServletInvocation();
    }

    private void addWebApp() {
        transformTemplate.transform("com.caucho.server.webapp.WebApp", WebAppTransform.class);
    }

    public static class WebAppTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // Dispatch library & Add servlet request listener. Servlet 2.4
            final InstrumentMethod initMethodEditorBuilder = target.getDeclaredMethod("init");
            if (initMethodEditorBuilder != null) {
                initMethodEditorBuilder.addInterceptor(WebAppInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void addHttpServletRequestImpl() {
        transformTemplate.transform("com.caucho.server.http.HttpServletRequestImpl", HttpServletRequestImplTransform.class);
    }

    public static class HttpServletRequestImplTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            final ResinConfig config = new ResinConfig(instrumentor.getProfilerConfig());
            if (config.isHidePinpointHeader()) {
                // Hide pinpoint headers
                target.weave("com.navercorp.pinpoint.plugin.resin.aspect.HttpServletRequestImplAspect");
            }
            // Add async listener. Servlet 3.0
            final InstrumentMethod startAsyncMethodEditor = target.getDeclaredMethod("startAsync", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
            if (startAsyncMethodEditor != null) {
                startAsyncMethodEditor.addInterceptor(HttpServletRequestImplInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void addServletInvocation() {
        transformTemplate.transform("com.caucho.server.dispatch.ServletInvocation", ServletInvocationTransform.class);
    }

    public static class ServletInvocationTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // Record HTTP status code & Close trace
            final InstrumentMethod serviceMethodEditorBuilder = target.getDeclaredMethod("service", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
            if (serviceMethodEditorBuilder != null) {
                serviceMethodEditorBuilder.addScopedInterceptor(ServletInvocationServiceInterceptor.class, "RESIN_REQUEST", ExecutionPolicy.BOUNDARY);
            }
            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}