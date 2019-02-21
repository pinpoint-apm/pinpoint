/**
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.websphere;

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
import com.navercorp.pinpoint.plugin.websphere.interceptor.WCCRequestImplStartAsyncInterceptor;
import com.navercorp.pinpoint.plugin.websphere.interceptor.WCCResponseImplInterceptor;
import com.navercorp.pinpoint.plugin.websphere.interceptor.WebContainerHandleRequestInterceptor;

/**
 * @author sjmittal
 * @author jaehong.kim
 */
public class WebspherePlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final WebsphereConfiguration config = new WebsphereConfiguration(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);


        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            WebsphereDetector websphereDetector = new WebsphereDetector(config.getBootstrapMains());
            if (websphereDetector.detect()) {
                logger.info("Detected application type : {}", WebsphereConstants.WEBSPHERE);
                if (!context.registerApplicationType(WebsphereConstants.WEBSPHERE)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), WebsphereConstants.WEBSPHERE);
                }
            }
        }

        logger.info("Adding WebSphere transformers");
        // Hide pinpoint header & Add async listener. Servlet 3.0
        addSRTServletRequest();
        // Entry Point
        addWSWebContainer();
        // Set status code
        addWCCResponseImpl();
        addWSAsyncContextImpl();
    }

    private void addSRTServletRequest() {
        transformTemplate.transform("com.ibm.ws.webcontainer.srt.SRTServletRequest", SRTServletRequestTransform.class);
    }

    public static class SRTServletRequestTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            final WebsphereConfiguration config = new WebsphereConfiguration(instrumentor.getProfilerConfig());
            if (config.isHidePinpointHeader()) {
                // Hide pinpoint headers
                target.weave("com.navercorp.pinpoint.plugin.websphere.aspect.SRTServletRequestAspect");
            }

            // Add async listener. Servlet 3.0
            final InstrumentMethod startAsyncMethodEditor = target.getDeclaredMethod("startAsync", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
            if (startAsyncMethodEditor != null) {
                startAsyncMethodEditor.addInterceptor(WCCRequestImplStartAsyncInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void addWSWebContainer() {
        transformTemplate.transform("com.ibm.ws.webcontainer.WSWebContainer", WSWebContainerTransform.class);
    }

    public static class WSWebContainerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // Clear remained trace - defense code
            final InstrumentMethod handleMethodEditorBuilder = target.getDeclaredMethod("handleRequest", "com.ibm.websphere.servlet.request.IRequest", "com.ibm.websphere.servlet.response.IResponse");
            if (handleMethodEditorBuilder != null) {
                handleMethodEditorBuilder.addInterceptor(WebContainerHandleRequestInterceptor.class);
            }
            return target.toBytecode();
        }
    }


    private void addWCCResponseImpl() {
        transformTemplate.transform("com.ibm.ws.webcontainer.channel.WCCResponseImpl", WCCResponseImplTransform.class);
    }

    public static class WCCResponseImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(StatusCodeAccessor.class);
            final InstrumentMethod setStatusCodeMethod = target.getDeclaredMethod("setStatusCode", "int");
            if (setStatusCodeMethod != null) {
                setStatusCodeMethod.addInterceptor(WCCResponseImplInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void addWSAsyncContextImpl() {
        transformTemplate.transform("com.ibm.ws.webcontainer.async.WSAsyncContextImpl", WSAsyncContextImplTransform.class);
    }

    public static class WSAsyncContextImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addGetter(InitResponseGetter.class, "initResponse");
            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}