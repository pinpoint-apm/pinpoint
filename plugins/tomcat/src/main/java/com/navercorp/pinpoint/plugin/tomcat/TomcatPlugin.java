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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.jboss.JbossConfig;
import com.navercorp.pinpoint.plugin.jboss.JbossConstants;
import com.navercorp.pinpoint.plugin.jboss.JbossDetector;
import com.navercorp.pinpoint.plugin.tomcat.interceptor.ConnectorInitializeInterceptor;
import com.navercorp.pinpoint.plugin.tomcat.interceptor.RequestStartAsyncInterceptor;
import com.navercorp.pinpoint.plugin.tomcat.interceptor.StandardHostValveInvokeInterceptor;
import com.navercorp.pinpoint.plugin.tomcat.interceptor.StandardServiceStartInterceptor;
import com.navercorp.pinpoint.plugin.tomcat.interceptor.WebappLoaderStartInterceptor;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 */
public class TomcatPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final TomcatConfig config = new TomcatConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            final TomcatDetector tomcatDetector = new TomcatDetector(config.getBootstrapMains());
            if (tomcatDetector.detect()) {
                logger.info("Detected application type : {}", TomcatConstants.TOMCAT);
                if (!context.registerApplicationType(TomcatConstants.TOMCAT)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), TomcatConstants.TOMCAT);
                }
            }
        }

        if (shouldAddTransformers(context)) {
            logger.info("Adding Tomcat transformers");
            addTransformers(config);
        } else {
            logger.info("Not adding Tomcat transformers");
        }
    }

    private boolean shouldAddTransformers(ProfilerPluginSetupContext context) {
        final ServiceType configuredApplicationType = context.getConfiguredApplicationType();
        if (TomcatConstants.TOMCAT.equals(configuredApplicationType)) {
            return true;
        }
        final JbossConfig jbossConfig = new JbossConfig(context.getConfig());
        final JbossDetector jbossDetector = new JbossDetector(jbossConfig.getBootstrapMains());
        if (jbossDetector.detect()) {
            logger.info("Detected application type : {}", JbossConstants.JBOSS);
            return false;
        }
        return true;
    }

    public void addTransformers(TomcatConfig config) {
        // Add server metadata
        addStandardService();
        addTomcatConnector();
        addWebappLoader();

        // Add async listener. Servlet 3.0
        addRequest();
        // Hide pinpoint headers & Trace HTTP response status code
        addRequestFacade(config);
        // Entry Point
        addStandardHostValve();
    }


    private void addStandardService() {
        transformTemplate.transform("org.apache.catalina.core.StandardService", StandardServiceTransform.class);
    }

    public static class StandardServiceTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // Add server metadata
            // Tomcat 6
            final InstrumentMethod startEditor = target.getDeclaredMethod("start");
            if (startEditor != null) {
                startEditor.addInterceptor(StandardServiceStartInterceptor.class);
            }

            // Tomcat 7
            final InstrumentMethod startInternalEditor = target.getDeclaredMethod("startInternal");
            if (startInternalEditor != null) {
                startInternalEditor.addInterceptor(StandardServiceStartInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void addTomcatConnector() {
        transformTemplate.transform("org.apache.catalina.connector.Connector", ConnectorTransform.class);
    }
    public static class ConnectorTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // Add server metadata
            // Tomcat 6
            final InstrumentMethod initializeEditor = target.getDeclaredMethod("initialize");
            if (initializeEditor != null) {
                initializeEditor.addInterceptor(ConnectorInitializeInterceptor.class);
            }

            // Tomcat 7
            final InstrumentMethod initInternalEditor = target.getDeclaredMethod("initInternal");
            if (initInternalEditor != null) {
                initInternalEditor.addScopedInterceptor(ConnectorInitializeInterceptor.class, TomcatConstants.TOMCAT_SERVLET_ASYNC_SCOPE);
            }
            return target.toBytecode();
        }
    }


    private void addWebappLoader() {
        transformTemplate.transform("org.apache.catalina.loader.WebappLoader", WebappLoaderTransform.class);
    }

    public static class WebappLoaderTransform implements TransformCallback {

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
                startMethod.addInterceptor(WebappLoaderStartInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void addRequest() {
        transformTemplate.transform("org.apache.catalina.connector.Request", RequestTransform.class);
    }

    public static class RequestTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // Add async listener. Servlet 3.0
            final InstrumentMethod startAsyncMethodEditor = target.getDeclaredMethod("startAsync", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
            if (startAsyncMethodEditor != null) {
                startAsyncMethodEditor.addInterceptor(RequestStartAsyncInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void addRequestFacade(final TomcatConfig config) {
        transformTemplate.transform("org.apache.catalina.connector.RequestFacade", RequestFacadeTransform.class);
    }

    public static class RequestFacadeTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            final TomcatConfig config = new TomcatConfig(instrumentor.getProfilerConfig());
            if (config.isHidePinpointHeader()) {
                // Hide pinpoint headers
                target.weave("com.navercorp.pinpoint.plugin.tomcat.aspect.RequestFacadeAspect");
            }
            return target.toBytecode();
        }
    }

    private void addStandardHostValve() {
        transformTemplate.transform("org.apache.catalina.core.StandardHostValve", StandardHostValveTransform.class);
    }

    public static class StandardHostValveTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // Remove bind trace
            final InstrumentMethod method = target.getDeclaredMethod("invoke", "org.apache.catalina.connector.Request", "org.apache.catalina.connector.Response");
            if (method != null) {
                method.addInterceptor(StandardHostValveInvokeInterceptor.class);
            }
            return target.toBytecode();
        }
    }


    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}