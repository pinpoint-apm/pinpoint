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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.weblogic.interceptor.ServletRequestImplStartAsyncInterceptor;
import com.navercorp.pinpoint.plugin.weblogic.interceptor.WebAppServletContextExecuteInterceptor;

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
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            final WeblogicDetector weblogicDetector = new WeblogicDetector(config.getBootstrapMains());
            if (weblogicDetector.detect()) {
                logger.info("Detected application type : {}", WeblogicConstants.WEBLOGIC);
                if (!context.registerApplicationType(WeblogicConstants.WEBLOGIC)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), WeblogicConstants.WEBLOGIC);
                }
            }
        }

        logger.info("Adding Weblogic transformers");
        // Add async listener. Servlet 3.0 & Hide pinpoint headers
        addServletRequestImpl(config);
        // Entry Point
        addServerInterceptor();
        // Add response getter
        addAsyncContextImpl();
    }

    private void addServletRequestImpl(final WeblogicConfiguration config) {
        transformTemplate.transform("weblogic.servlet.internal.ServletRequestImpl", ServletRequestImplTransform.class);
    }

    public static class ServletRequestImplTransform implements TransformCallback {
    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

        final WeblogicConfiguration config = new WeblogicConfiguration(instrumentor.getProfilerConfig());
        if (config.isHidePinpointHeader()) {
            // Hide pinpoint headers
            target.weave("com.navercorp.pinpoint.plugin.weblogic.aspect.ServletRequestImplAspect");
        }

        // Add async listener. Servlet 3.0
        final InstrumentMethod startAsyncMethodEditor = target.getDeclaredMethod("startAsync", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
        if (startAsyncMethodEditor != null) {
            startAsyncMethodEditor.addInterceptor(ServletRequestImplStartAsyncInterceptor.class);
        }
        return target.toBytecode();
    }
}

    private void addServerInterceptor() {
        transformTemplate.transform("weblogic.servlet.internal.WebAppServletContext", WebAppServletContextTransform.class);
    }

    public static class WebAppServletContextTransform implements TransformCallback {
    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        final InstrumentMethod handleMethodEditorBuilder = target.getDeclaredMethod("execute", "weblogic.servlet.internal.ServletRequestImpl", "weblogic.servlet.internal.ServletResponseImpl");
        if (handleMethodEditorBuilder != null) {
            handleMethodEditorBuilder.addInterceptor(WebAppServletContextExecuteInterceptor.class);
        }
        return target.toBytecode();
    }
}

    private void addAsyncContextImpl() {
        transformTemplate.transform("weblogic.servlet.internal.async.AsyncContextImpl", AsyncContextImplTransform.class);
    }

    public static class AsyncContextImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addGetter(ResponseGetter.class, "response");
            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}