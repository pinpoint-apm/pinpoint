/*
 * Copyright 2016 Pinpoint contributors and NAVER Corp.
 *
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
package com.navercorp.pinpoint.plugin.jboss;

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
 * The Class JbossPlugin.
 *
 * @author <a href="mailto:suraj.raturi89@gmail.com">Suraj Raturi</a>
 */
public class JbossPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    /** The transform template. */
    private TransformTemplate transformTemplate;

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin#setup(com.navercorp.pinpoint.bootstrap.plugin.
     * ProfilerPluginSetupContext)
     */
    @Override
    public void setup(final ProfilerPluginSetupContext context) {
        final JbossConfig jbossConfig = new JbossConfig(context.getConfig());
        if (logger.isInfoEnabled()) {
            logger.info("JBossPlugin config:{}", jbossConfig);
        }
        if (!jbossConfig.isJbossEnable()) {
            logger.info("JBossPlugin disabled");
            return;
        }

        JbossDetector jbossDetector = new JbossDetector(jbossConfig.getJbossBootstrapMains());
        context.addApplicationTypeDetector(jbossDetector);

        if (shouldAddTransformers(jbossConfig)) {
            logger.info("Adding JBoss transformers");
            addTransformers(jbossConfig);
        } else {
            logger.info("Not adding JBoss transformers");
        }
    }

    private boolean shouldAddTransformers(JbossConfig jbossConfig) {
        // Transform if conditional check is disabled
        if (!jbossConfig.isJbossConditionalTransformEnable()) {
            return true;
        }
        // Only transform if it's a JBoss application
        ConditionProvider conditionProvider = ConditionProvider.DEFAULT_CONDITION_PROVIDER;
        boolean isJbossApplication = conditionProvider.checkMainClass(jbossConfig.getJbossBootstrapMains());
        return isJbossApplication;
    }

    private void addTransformers(JbossConfig jbossConfig) {
        // Instrumenting class on the base of ejb based application or rest based application.
        if (jbossConfig.isJbossTraceEjb()) {
            addMethodInvocationMessageHandlerEditor();
        } else {
            addStandardHostValveEditor();
            addContextInvocationEditor();
        }
    }

    /**
     * Adds the method invoke message handler editor.
     */
    private void addMethodInvocationMessageHandlerEditor() {
        transformTemplate.transform("org.jboss.as.ejb3.remote.protocol.versionone.MethodInvocationMessageHandler", new TransformCallback() {

            @Override
            public byte[] doInTransform(final Instrumentor instrumentor, final ClassLoader classLoader, final String className, final Class<?> classBeingRedefined,
                final ProtectionDomain protectionDomain,
                final byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                final InstrumentMethod method =
                    target.getDeclaredMethod("invokeMethod", new String[] { "short", "org.jboss.as.ee.component.ComponentView", "java.lang.reflect.Method", "java.lang.Object[]",
                        "org.jboss.ejb.client.EJBLocator", "java.util.Map" });
                if (method != null) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.jboss.interceptor.MethodInvocationHandlerInterceptor");
                }

                return target.toBytecode();
            }
        });

    }

    /**
     * Adds the context invocation editor.
     * 
     * 
     */
    private void addContextInvocationEditor() {
        transformTemplate.transform("org.jboss.as.ejb3.tx.EjbBMTInterceptor", new TransformCallback() {

            @Override
            public byte[] doInTransform(final Instrumentor instrumentor, final ClassLoader classLoader, final String className, final Class<?> classBeingRedefined,
                final ProtectionDomain protectionDomain,
                final byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                final InstrumentMethod method = target.getDeclaredMethod("handleInvocation", "org.jboss.invocation.InterceptorContext");
                if (method != null) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.jboss.interceptor.ContextInvocationInterceptor");
                }

                return target.toBytecode();
            }
        });

    }

    /**
     * Adds the standard host valve editor.
     *
     */
    private void addStandardHostValveEditor() {
        transformTemplate.transform("org.apache.catalina.core.StandardHostValve", new TransformCallback() {

            @Override
            public byte[] doInTransform(final Instrumentor instrumentor, final ClassLoader classLoader, final String className, final Class<?> classBeingRedefined,
                final ProtectionDomain protectionDomain,
                final byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                final InstrumentMethod method = target.getDeclaredMethod("invoke", "org.apache.catalina.connector.Request", "org.apache.catalina.connector.Response");
                if (method != null) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.jboss.interceptor.StandardHostValveInvokeInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware#setTransformTemplate(com.navercorp.
     * pinpoint.bootstrap.instrument.transformer.TransformTemplate)
     */
    @Override
    public void setTransformTemplate(final TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
