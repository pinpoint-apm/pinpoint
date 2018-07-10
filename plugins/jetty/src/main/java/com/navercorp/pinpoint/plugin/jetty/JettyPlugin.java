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
package com.navercorp.pinpoint.plugin.jetty;

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

import java.security.ProtectionDomain;

/**
 * @author Chaein Jung
 * @author jaehong.kim
 */
public class JettyPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        JettyConfiguration config = new JettyConfiguration(context.getConfig());
        if (!config.isEnable()) {
            logger.info("Disabled JettyPlugin.");
            return;
        }
        // 8.0 <= x <= 9.4
        logger.info("Enable JettyPlugin. version range=[8.0, 9.4], config={}", config);
        context.addApplicationTypeDetector(new JettyDetector(config.getBootstrapMains()));

        // Add async listener. Servlet 3.0
        requestAspect(config);
        // Entry Point
        addServerInterceptor();
    }

    private void requestAspect(final JettyConfiguration config) {
        transformTemplate.transform("org.eclipse.jetty.server.Request", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                if (config.isHidePinpointHeader()) {
                    // Hide pinpoint header
                    target.weave("com.navercorp.pinpoint.plugin.jetty.aspect.RequestAspect");
                }
                // Add async listener. Servlet 3.0
                final InstrumentMethod startAsyncMethod = target.getDeclaredMethod("startAsync");
                if (startAsyncMethod != null) {
                    startAsyncMethod.addInterceptor("com.navercorp.pinpoint.plugin.jetty.interceptor.RequestStartAsyncInterceptor");
                }
                final InstrumentMethod startAsyncMethodEditor = target.getDeclaredMethod("startAsync", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
                if (startAsyncMethodEditor != null) {
                    startAsyncMethodEditor.addInterceptor("com.navercorp.pinpoint.plugin.jetty.interceptor.RequestStartAsyncInterceptor");
                }
                return target.toBytecode();
            }
        });
    }

    private void addServerInterceptor() {
        transformTemplate.transform("org.eclipse.jetty.server.Server", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // 9.x
                final InstrumentMethod handleMethodEditorBuilder = target.getDeclaredMethod("handle", "org.eclipse.jetty.server.HttpChannel");
                if (handleMethodEditorBuilder != null) {
                    handleMethodEditorBuilder.addInterceptor("com.navercorp.pinpoint.plugin.jetty.interceptor.Jetty9xServerHandleInterceptor");
                    return target.toBytecode();
                }
                // 8.0
                final InstrumentMethod jetty80HandleMethodEditorBuilder = target.getDeclaredMethod("handle", "org.eclipse.jetty.server.HttpConnection");
                if (jetty80HandleMethodEditorBuilder != null) {
                    jetty80HandleMethodEditorBuilder.addInterceptor("com.navercorp.pinpoint.plugin.jetty.interceptor.Jetty80ServerHandleInterceptor");
                    return target.toBytecode();
                }
                // 8.1, 8.2
                final InstrumentMethod jetty82HandleMethodEditorBuilder = target.getDeclaredMethod("handle", "org.eclipse.jetty.server.AbstractHttpConnection");
                if (jetty82HandleMethodEditorBuilder != null) {
                    jetty82HandleMethodEditorBuilder.addInterceptor("com.navercorp.pinpoint.plugin.jetty.interceptor.Jetty8xServerHandleInterceptor");
                    return target.toBytecode();
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