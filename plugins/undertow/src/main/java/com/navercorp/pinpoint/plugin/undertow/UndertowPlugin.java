/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.undertow;

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
 * @author jaehong.kim
 */
public class UndertowPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final UndertowConfig config = new UndertowConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("UndertowPlugin disabled");
            return;
        }

        if (logger.isInfoEnabled()) {
            logger.info("UndertowPlugin config:{}", config);
        }
        // Entry Point
        addConnectors();
        // Add async listener. Servlet 3.0
        addHttpServletRequestImpl();
    }


    private void addConnectors() {
        transformTemplate.transform("io.undertow.server.Connectors", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // Entry Point
                final InstrumentMethod connectorsExecuteRootHandlerMethod = target.getDeclaredMethod("executeRootHandler", "io.undertow.server.HttpHandler", "io.undertow.server.HttpServerExchange");
                if (connectorsExecuteRootHandlerMethod != null) {
                    connectorsExecuteRootHandlerMethod.addInterceptor("com.navercorp.pinpoint.plugin.undertow.interceptor.ConnectorsExecuteRootHandlerInterceptor");
                }
                return target.toBytecode();
            }
        });
    }

    private void addHttpServletRequestImpl() {
        transformTemplate.transform("io.undertow.servlet.spec.HttpServletRequestImpl", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // Add async listener. Servlet 3.0
                final InstrumentMethod startAsyncMethod = target.getDeclaredMethod("startAsync", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
                if (startAsyncMethod != null) {
                    startAsyncMethod.addInterceptor("com.navercorp.pinpoint.plugin.undertow.interceptor.HttpServletRequestImplStartAsyncInterceptor");
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