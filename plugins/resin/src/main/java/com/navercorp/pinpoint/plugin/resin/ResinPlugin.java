package com.navercorp.pinpoint.plugin.resin;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

/**
 * 
 * @author huangpengjie@fang.com
 * 
 */
public class ResinPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final ResinConfig config = new ResinConfig(context.getConfig());
        if (logger.isInfoEnabled()) {
            logger.info("ResinPlugin config:{}", config);
        }
        if (!config.isResinEnable()) {
            logger.info("ResinPlugin disabled");
            return;
        }

        ResinDetector resinDetector = new ResinDetector(config.getResinBootstrapMains());
        context.addApplicationTypeDetector(resinDetector);

        logger.info("Adding Resin transformers");
        addTransformers(config);
    }

    private void addTransformers(ResinConfig config) {
        addWebApp();
        addServletInvocation();

        // handle async request
        addHttpServletRequestImpl();
        addAsyncContextImpl();
        addCauchoRequestWrapper();

        addErrorPageManager();
    }

    private void addCauchoRequestWrapper() {
        transformTemplate.transform("com.caucho.server.http.CauchoRequestWrapper", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                target.addGetter("com.navercorp.pinpoint.plugin.resin.HttpServletRequestGetter", "_request");
                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    private void addErrorPageManager() {
        transformTemplate.transform("com.caucho.server.webapp.ErrorPageManager", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                InstrumentMethod sendServletErrorMethodEditorBuilder = target.getDeclaredMethod("sendServletError", "java.lang.Throwable", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
                if (sendServletErrorMethodEditorBuilder != null) {
                    sendServletErrorMethodEditorBuilder.addInterceptor("com.navercorp.pinpoint.plugin.resin.interceptor.ErrorPageManagerInterceptor");
                }
                return target.toBytecode();
            }
        });
    }

    private void addWebApp() {
        transformTemplate.transform("com.caucho.server.webapp.WebApp", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                InstrumentMethod initMethodEditorBuilder = target.getDeclaredMethod("init");
                if (initMethodEditorBuilder != null) {
                    initMethodEditorBuilder.addInterceptor("com.navercorp.pinpoint.plugin.resin.interceptor.WebAppInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addServletInvocation() {
        transformTemplate.transform("com.caucho.server.dispatch.ServletInvocation", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                InterceptorScope scope = instrumentor.getInterceptorScope(ResinConstants.RESIN_SERVLET_SCOPE);

                // trace request.
                InstrumentMethod serviceMethodEditorBuilder = target.getDeclaredMethod("service", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
                if (serviceMethodEditorBuilder != null) {
                    serviceMethodEditorBuilder.addScopedInterceptor("com.navercorp.pinpoint.plugin.resin.interceptor.ServletInvocationInterceptor", scope, ExecutionPolicy.BOUNDARY);
                }

                // resin4 isAsyncSupported
                InstrumentMethod isAsyncSupportedMethodEditorBuilder = target.getDeclaredMethod("isAsyncSupported");
                if (isAsyncSupportedMethodEditorBuilder != null) {
                    target.addField(ResinConstants.VERSION_ACCESSOR);
                }

                return target.toBytecode();
            }
        });
    }

    private void addHttpServletRequestImpl() {
        transformTemplate.transform("com.caucho.server.http.HttpServletRequestImpl", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                // trace asynchronous process.
                InstrumentMethod startAsyncMethodEditor = target.getDeclaredMethod("startAsync", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
                if (startAsyncMethodEditor != null) {
                    target.addField(ResinConstants.TRACE_ACCESSOR);
                    target.addField(ResinConstants.ASYNC_ACCESSOR);
                    startAsyncMethodEditor.addInterceptor("com.navercorp.pinpoint.plugin.resin.interceptor.HttpServletRequestImplInterceptor");
                }

                // clear request. 4.x
                InstrumentMethod finishRequestInvocationMethodEditor = target.getDeclaredMethod("finishRequest");
                if (finishRequestInvocationMethodEditor != null) {
                    finishRequestInvocationMethodEditor.addInterceptor("com.navercorp.pinpoint.plugin.resin.interceptor.HttpRequestInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addAsyncContextImpl() {
        transformTemplate.transform("com.caucho.server.http.AsyncContextImpl", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(AsyncContextAccessor.class.getName());
                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("dispatch"))) {
                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.resin.interceptor.AsyncContextImplDispatchMethodInterceptor", ResinConstants.RESIN_SERVLET_ASYNC_SCOPE);
                }

                return target.toBytecode();
            }
        });
    }

}
