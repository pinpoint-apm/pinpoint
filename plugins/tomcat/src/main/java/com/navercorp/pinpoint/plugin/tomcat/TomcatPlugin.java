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

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
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
        if (!config.isEnable()) {
            logger.info("TomcatPlugin disabled");
            return;
        }

        if (logger.isInfoEnabled()) {
            logger.info("TomcatPlugin config:{}", config);
        }

        final TomcatDetector tomcatDetector = new TomcatDetector(config.getBootstrapMains());
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
        if (!config.isConditionalTransformEnable()) {
            return true;
        }
        // Only transform if it's a Tomcat application or SpringBoot application
        final ConditionProvider conditionProvider = ConditionProvider.DEFAULT_CONDITION_PROVIDER;
        final boolean isTomcatApplication = conditionProvider.checkMainClass(config.getBootstrapMains());
        final boolean isSpringBootApplication = conditionProvider.checkMainClass(config.getSpringBootBootstrapMains());
        return isTomcatApplication || isSpringBootApplication;
    }

    private void addTransformers(TomcatConfig config) {
        // Add server metadata
        addStandardService();
        addTomcatConnector();
        addWebappLoader();

        // Add servlet request listener. Servlet 2.4
        addStandardContext();
        // Add async listener. Servlet 3.0
        addRequest();
        // Hide pinpoint headers & Trace HTTP response status code
        addRequestFacade(config);
        // Remove bind trace
        addStandardHostValve();
    }


    private void addStandardService() {
        transformTemplate.transform("org.apache.catalina.core.StandardService", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // Add server metadata
                // Tomcat 6
                final InstrumentMethod startEditor = target.getDeclaredMethod("start");
                if (startEditor != null) {
                    startEditor.addInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.StandardServiceStartInterceptor");
                }

                // Tomcat 7
                final InstrumentMethod startInternalEditor = target.getDeclaredMethod("startInternal");
                if (startInternalEditor != null) {
                    startInternalEditor.addInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.StandardServiceStartInterceptor");
                }
                return target.toBytecode();
            }
        });
    }

    private void addTomcatConnector() {
        transformTemplate.transform("org.apache.catalina.connector.Connector", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // Add server metadata
                // Tomcat 6
                final InstrumentMethod initializeEditor = target.getDeclaredMethod("initialize");
                if (initializeEditor != null) {
                    initializeEditor.addInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.ConnectorInitializeInterceptor");
                }

                // Tomcat 7
                final InstrumentMethod initInternalEditor = target.getDeclaredMethod("initInternal");
                if (initInternalEditor != null) {
                    initInternalEditor.addScopedInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.ConnectorInitializeInterceptor", TomcatConstants.TOMCAT_SERVLET_ASYNC_SCOPE);
                }
                return target.toBytecode();
            }
        });
    }

    private void addWebappLoader() {
        transformTemplate.transform("org.apache.catalina.loader.WebappLoader", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // Add servlet information
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


    private void addStandardContext() {
        transformTemplate.transform("org.apache.catalina.core.StandardContext", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // Add servlet request listener. Servlet 2.4
                final InstrumentMethod listenerStartMethod = target.getDeclaredMethod("listenerStart");
                if (listenerStartMethod != null) {
                    listenerStartMethod.addInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.StandardContextListenerStartInterceptor");
                }
                return target.toBytecode();
            }
        });
    }

    private void addRequest() {
        transformTemplate.transform("org.apache.catalina.connector.Request", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // RequestFacade for tomcat 6/8/9
                final InstrumentMethod getRequestMethod = target.getDeclaredMethod("getRequest");
                if (getRequestMethod != null) {
                    getRequestMethod.addInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.RequestGetRequestInterceptor");
                }

                // Add async listener. Servlet 3.0
                final InstrumentMethod startAsyncMethodEditor = target.getDeclaredMethod("startAsync", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
                if (startAsyncMethodEditor != null) {
                    startAsyncMethodEditor.addInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.RequestStartAsyncInterceptor");
                }
                return target.toBytecode();
            }
        });
    }

    private void addRequestFacade(final TomcatConfig config) {
        transformTemplate.transform("org.apache.catalina.connector.RequestFacade", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // Trace HTTP response status code
                target.addField(TomcatConstants.STATUS_CODE_ACCESSOR);
                if (config.isHidePinpointHeader()) {
                    // Hide pinpoint headers
                    target.weave("com.navercorp.pinpoint.plugin.tomcat.aspect.RequestFacadeAspect");
                }
                return target.toBytecode();
            }
        });
    }

    private void addStandardHostValve() {
        transformTemplate.transform("org.apache.catalina.core.StandardHostValve", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // Remove bind trace
                final InstrumentMethod method = target.getDeclaredMethod("invoke", "org.apache.catalina.connector.Request", "org.apache.catalina.connector.Response");
                if (method != null) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.tomcat.interceptor.StandardHostValveInvokeInterceptor");
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
