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
package com.navercorp.pinpoint.plugin.weblogic;


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

/**
 * @author andyspan
 * @author jaehong.kim
 */
public class WeblogicPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;
    protected PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final WeblogicConfiguration config = new WeblogicConfiguration(context.getConfig());
        if (!config.isEnable()) {
            logger.info("WeblogicPlugin disabled");
            return;
        }
        logger.info("WeblogicPlugin config={}", config);

        context.addApplicationTypeDetector(new WeblogicDetector(config.getBootstrapMains()));

        // Add servlet request listener. Servlet 2.4
        addHttpServer();
        // Add async listener. Servlet 3.0 & Hide pinpoint headers
        addServletRequestImpl(config);
        // Remove bind trace
        addServerInterceptor();
    }

    private void addHttpServer() {
        transformTemplate.transform("weblogic.servlet.internal.HttpServer", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // Add servlet request listener. Servlet 2.4
                final InstrumentMethod handleMethodEditorBuilder = target.getDeclaredMethod("loadWebApp", "weblogic.management.configuration.WebAppComponentMBean", "weblogic.servlet.internal.WebAppModule");
                if (handleMethodEditorBuilder != null) {
                    handleMethodEditorBuilder.addInterceptor("com.navercorp.pinpoint.plugin.weblogic.interceptor.HttpServerLoadWebAppInterceptor");
                }
                return target.toBytecode();
            }
        });
    }

    private void addServletRequestImpl(final WeblogicConfiguration config) {
        transformTemplate.transform("weblogic.servlet.internal.ServletRequestImpl", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                if (config.isHidePinpointHeader()) {
                    // Hide pinpoint headers
                    target.weave("com.navercorp.pinpoint.plugin.weblogic.aspect.ServletRequestImplAspect");
                }

                // Add async listener. Servlet 3.0
                final InstrumentMethod startAsyncMethodEditor = target.getDeclaredMethod("startAsync", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
                if (startAsyncMethodEditor != null) {
                    startAsyncMethodEditor.addInterceptor("com.navercorp.pinpoint.plugin.weblogic.interceptor.ServletRequestImplStartAsyncInterceptor");
                }
                return target.toBytecode();
            }
        });
    }

    private void addServerInterceptor() {
        transformTemplate.transform("weblogic.servlet.internal.WebAppServletContext", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // Remove bind trace
                final InstrumentMethod handleMethodEditorBuilder = target.getDeclaredMethod("execute", "weblogic.servlet.internal.ServletRequestImpl", "weblogic.servlet.internal.ServletResponseImpl");
                if (handleMethodEditorBuilder != null) {
                    handleMethodEditorBuilder.addInterceptor("com.navercorp.pinpoint.plugin.weblogic.interceptor.WebAppServletContextExecuteInterceptor");
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