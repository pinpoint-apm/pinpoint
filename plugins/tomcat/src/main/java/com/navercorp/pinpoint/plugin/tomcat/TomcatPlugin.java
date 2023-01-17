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

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.jboss.JbossConfig;
import com.navercorp.pinpoint.plugin.jboss.JbossConstants;
import com.navercorp.pinpoint.plugin.jboss.JbossDetector;
import com.navercorp.pinpoint.plugin.tomcat.jakarta.interceptor.ConnectorInitializeInterceptor;
import com.navercorp.pinpoint.plugin.tomcat.jakarta.interceptor.RequestStartAsyncInterceptor;
import com.navercorp.pinpoint.plugin.tomcat.jakarta.interceptor.StandardHostValveInvokeInterceptor;
import com.navercorp.pinpoint.plugin.tomcat.jakarta.interceptor.StandardServiceStartInterceptor;
import com.navercorp.pinpoint.plugin.tomcat.jakarta.interceptor.WebappLoaderStartInterceptor;

import java.security.ProtectionDomain;

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
        transformTemplate.transform("org.apache.catalina.core.StandardService", StandardServiceTransform.class);
        transformTemplate.transform("org.apache.catalina.connector.Connector", ConnectorTransform.class);
        transformTemplate.transform("org.apache.catalina.loader.WebappLoader", WebappLoaderTransform.class);
        // Add async listener. Servlet 3.0
        transformTemplate.transform("org.apache.catalina.connector.Request", RequestTransform.class);
        // Hide pinpoint headers & Trace HTTP response status code
        transformTemplate.transform("org.apache.catalina.connector.RequestFacade", RequestFacadeTransform.class);
        // Entry Point
        transformTemplate.transform("org.apache.catalina.core.StandardHostValve", StandardHostValveTransform.class);
    }

    private static boolean hasClass(String className, ClassLoader cl) {
        try {
            Class.forName(className, false, cl);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static boolean hasJakarta(ClassLoader cl) {
        return hasClass("jakarta.servlet.http.HttpServletRequest", cl);
    }

    public static class StandardServiceTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            // Add server metadata
            // Tomcat 6
            final InstrumentMethod startEditor = target.getDeclaredMethod("start");
            if (startEditor != null) {
                startEditor.addInterceptor(getStandardServiceStartInterceptor(classLoader));
            }

            // Tomcat 7
            final InstrumentMethod startInternalEditor = target.getDeclaredMethod("startInternal");
            if (startInternalEditor != null) {
                startInternalEditor.addInterceptor(getStandardServiceStartInterceptor(classLoader));
            }
            return target.toBytecode();
        }

        private static Class<? extends Interceptor> getStandardServiceStartInterceptor(ClassLoader cl) {
            if (hasJakarta(cl)) {
                return StandardServiceStartInterceptor.class;
            } else {
                return com.navercorp.pinpoint.plugin.tomcat.javax.interceptor.StandardServiceStartInterceptor.class;
            }
        }
    }

    public static class ConnectorTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // Add server metadata
            // Tomcat 6
            final InstrumentMethod initializeEditor = target.getDeclaredMethod("initialize");
            if (initializeEditor != null) {
                initializeEditor.addInterceptor(getConnectorInitializeInterceptor(classLoader));
            }

            // Tomcat 7
            final InstrumentMethod initInternalEditor = target.getDeclaredMethod("initInternal");
            if (initInternalEditor != null) {
                initInternalEditor.addScopedInterceptor(getConnectorInitializeInterceptor(classLoader), TomcatConstants.TOMCAT_SERVLET_ASYNC_SCOPE);
            }
            return target.toBytecode();
        }

        private static Class<? extends Interceptor> getConnectorInitializeInterceptor(ClassLoader cl) {
            if (hasJakarta(cl)) {
                return ConnectorInitializeInterceptor.class;
            } else {
                return com.navercorp.pinpoint.plugin.tomcat.javax.interceptor.ConnectorInitializeInterceptor.class;
            }
        }
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
                startMethod.addInterceptor(getWebappLoaderStartInterceptor(classLoader));
            }
            return target.toBytecode();
        }

        private static Class<? extends Interceptor> getWebappLoaderStartInterceptor(ClassLoader cl) {
            if (hasJakarta(cl)) {
                return WebappLoaderStartInterceptor.class;
            } else {
                return com.navercorp.pinpoint.plugin.tomcat.javax.interceptor.WebappLoaderStartInterceptor.class;
            }
        }
    }

    public static class RequestTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            if (hasJakarta(classLoader)) {
                addInterceptorsForJakarta(target);
            } else {
                addInterceptorsForJavaX(target);
            }

            return target.toBytecode();
        }

        private static void addInterceptorsForJakarta(InstrumentClass target) throws InstrumentException {
            // Add async listener. Servlet 3.0
            final InstrumentMethod startAsyncMethodEditor = target.getDeclaredMethod("startAsync", "jakarta.servlet.ServletRequest", "jakarta.servlet.ServletResponse");
            if (startAsyncMethodEditor != null) {
                startAsyncMethodEditor.addInterceptor(RequestStartAsyncInterceptor.class);
            }
        }

        private static void addInterceptorsForJavaX(InstrumentClass target) throws InstrumentException {
            // Add async listener. Servlet 3.0
            final InstrumentMethod startAsyncMethodEditor = target.getDeclaredMethod("startAsync", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
            if (startAsyncMethodEditor != null) {
                startAsyncMethodEditor.addInterceptor(com.navercorp.pinpoint.plugin.tomcat.javax.interceptor.RequestStartAsyncInterceptor.class);
            }
        }
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

    public static class StandardHostValveTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // Remove bind trace
            final InstrumentMethod method = target.getDeclaredMethod("invoke", "org.apache.catalina.connector.Request", "org.apache.catalina.connector.Response");
            if (method != null) {
                method.addInterceptor(getStandardHostValveInvokeInterceptor(classLoader));
            }
            return target.toBytecode();
        }

        private static Class<? extends Interceptor> getStandardHostValveInvokeInterceptor(ClassLoader cl) {
            if (hasJakarta(cl)) {
                return StandardHostValveInvokeInterceptor.class;
            } else {
                return com.navercorp.pinpoint.plugin.tomcat.javax.interceptor.StandardHostValveInvokeInterceptor.class;
            }
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}