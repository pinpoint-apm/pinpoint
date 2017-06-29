/*
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.tomcat;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.resolver.ConditionProvider;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 *
 */
public class TomcatPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin#setUp(com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext)
     */
    @Override
    public void setup(ProfilerPluginSetupContext context) {

        final TomcatConfig config = new TomcatConfig(context.getConfig());
        if (logger.isInfoEnabled()) {
            logger.info("TomcatPlugin config:{}", config);
        }
        if (!config.isTomcatEnable()) {
            logger.info("TomcatPlugin disabled");
            return;
        }

        TomcatDetector tomcatDetector = new TomcatDetector(config.getTomcatBootstrapMains());
        context.addApplicationTypeDetector(tomcatDetector);

        if (shouldAddTransformers(config)) {
            logger.info("Adding Tomcat transformers");
            addTransformers(config);
        } else {
            logger.info("Not adding Tomcat transfomers");
        }
    }

    private boolean shouldAddTransformers(TomcatConfig config) {
        // Transform if conditional check is disabled
        if (!config.isTomcatConditionalTransformEnable()) {
            return true;
        }
        // Only transform if it's a Tomcat application or SpringBoot application
        ConditionProvider conditionProvider = ConditionProvider.DEFAULT_CONDITION_PROVIDER;
        boolean isTomcatApplication = conditionProvider.checkMainClass(config.getTomcatBootstrapMains());
        boolean isSpringBootApplication = conditionProvider.checkMainClass(config.getSpringBootBootstrapMains());
        return isTomcatApplication || isSpringBootApplication;
}

    private void addTransformers(TomcatConfig config) {
        if (config.isTomcatHidePinpointHeader()) {
            addRequestFacadeEditor();
        }

        addRequestEditor();
        addStandardHostValveEditor();
        addStandardServiceEditor();
        addTomcatConnectorEditor();
        addWebappLoaderEditor();

        addAsyncContextImpl();
    }

    private void addRequestEditor() {
        transformTemplate.transform("org.apache.catalina.connector.Request", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(TomcatConstants.TRACE_ACCESSOR);
                target.addField(TomcatConstants.ASYNC_ACCESSOR);

                // clear request.
                InstrumentMethod recycleMethodEditorBuilder = target.getDeclaredMethod("recycle");
                if (recycleMethodEditorBuilder != null) {
                    recycleMethodEditorBuilder.addInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.RequestRecycleInterceptor");
                }

                // trace asynchronous process.
                InstrumentMethod startAsyncMethodEditor = target.getDeclaredMethod("startAsync", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
                if (startAsyncMethodEditor != null) {
                    startAsyncMethodEditor.addInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.RequestStartAsyncInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addRequestFacadeEditor() {
        transformTemplate.transform("org.apache.catalina.connector.RequestFacade", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                if (target != null) {
                    target.weave("com.navercorp.pinpoint.plugin.tomcat.aspect.RequestFacadeAspect");
                    return target.toBytecode();
                }

                return null;
            }
        });
    }

    private void addStandardHostValveEditor() {
        transformTemplate.transform("org.apache.catalina.core.StandardHostValve", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                InstrumentMethod method = target.getDeclaredMethod("invoke", "org.apache.catalina.connector.Request", "org.apache.catalina.connector.Response");
                if (method != null) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.StandardHostValveInvokeInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addStandardServiceEditor() {
        transformTemplate.transform("org.apache.catalina.core.StandardService", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                // Tomcat 6
                InstrumentMethod startEditor = target.getDeclaredMethod("start");
                if (startEditor != null) {
                    startEditor.addInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.StandardServiceStartInterceptor");
                }

                // Tomcat 7
                InstrumentMethod startInternalEditor = target.getDeclaredMethod("startInternal");
                if (startInternalEditor != null) {
                    startInternalEditor.addInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.StandardServiceStartInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addTomcatConnectorEditor() {
        transformTemplate.transform("org.apache.catalina.connector.Connector", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                // Tomcat 6
                InstrumentMethod initializeEditor = target.getDeclaredMethod("initialize");
                if (initializeEditor != null) {
                    initializeEditor.addInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.ConnectorInitializeInterceptor");
                }

                // Tomcat 7
                InstrumentMethod initInternalEditor = target.getDeclaredMethod("initInternal");
                if (initInternalEditor != null) {
                    initInternalEditor.addScopedInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.ConnectorInitializeInterceptor", TomcatConstants.TOMCAT_SERVLET_ASYNC_SCOPE);
                }

                return target.toBytecode();
            }
        });
    }

    private void addWebappLoaderEditor() {
        transformTemplate.transform("org.apache.catalina.loader.WebappLoader", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                InstrumentMethod startMethod = null;
                if (target.hasDeclaredMethod("start")) {
                    // Tomcat 6 - org.apache.catalina.loader.WebappLoader.start()
                    startMethod = target.getDeclaredMethod("start");
                } else if (target.hasDeclaredMethod("startInternal")) {
                    // Tomcat 7, 8 - org.apache.catalina.loader.WebappLoader.startInternal()
                    startMethod = target.getDeclaredMethod("startInternal");
                }

                if (startMethod != null) {
                    startMethod.addInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.WebappLoaderStartInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addAsyncContextImpl() {
        transformTemplate.transform("org.apache.catalina.core.AsyncContextImpl", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(AsyncContextAccessor.class.getName());
                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("dispatch"))) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.AsyncContextImplDispatchMethodInterceptor");
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
