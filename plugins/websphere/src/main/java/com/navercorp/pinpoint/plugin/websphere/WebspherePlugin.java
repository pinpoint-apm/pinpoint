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
            logger.info("WebspherePlugin disabled");
            return;
        }
        logger.info("WebspherePlugin config:{}", config);

        context.addApplicationTypeDetector(new WebsphereDetector(config.getBootstrapMains()));

        // Hide pinpoint header & Add async listener. Servlet 3.0
        addSRTServletRequest(config);
        // Entry Point
        addWSWebContainer();
        // Set status code
        addWCCResponseImpl();
        addWSAsyncContextImpl();
    }

    private void addSRTServletRequest(final WebsphereConfiguration config) {
        transformTemplate.transform("com.ibm.ws.webcontainer.srt.SRTServletRequest", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                if (config.isHidePinpointHeader()) {
                    // Hide pinpoint headers
                    target.weave("com.navercorp.pinpoint.plugin.websphere.aspect.SRTServletRequestAspect");
                }

                // Add async listener. Servlet 3.0
                final InstrumentMethod startAsyncMethodEditor = target.getDeclaredMethod("startAsync", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
                if (startAsyncMethodEditor != null) {
                    startAsyncMethodEditor.addInterceptor("com.navercorp.pinpoint.plugin.websphere.interceptor.WCCRequestImplStartAsyncInterceptor");
                }
                return target.toBytecode();
            }
        });
    }

    private void addWSWebContainer() {
        transformTemplate.transform("com.ibm.ws.webcontainer.WSWebContainer", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // Clear remained trace - defense code
                final InstrumentMethod handleMethodEditorBuilder = target.getDeclaredMethod("handleRequest", "com.ibm.websphere.servlet.request.IRequest", "com.ibm.websphere.servlet.response.IResponse");
                if (handleMethodEditorBuilder != null) {
                    handleMethodEditorBuilder.addInterceptor("com.navercorp.pinpoint.plugin.websphere.interceptor.WebContainerHandleRequestInterceptor");
                }
                return target.toBytecode();
            }
        });
    }


    private void addWCCResponseImpl() {
        transformTemplate.transform("com.ibm.ws.webcontainer.channel.WCCResponseImpl", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(WebsphereConstants.STATUS_CODE_ACCESSOR);
                final InstrumentMethod setStatusCodeMethod = target.getDeclaredMethod("setStatusCode", "int");
                if (setStatusCodeMethod != null) {
                    setStatusCodeMethod.addInterceptor("com.navercorp.pinpoint.plugin.websphere.interceptor.WCCResponseImplInterceptor");
                }
                return target.toBytecode();
            }
        });
    }

    private void addWSAsyncContextImpl() {
        transformTemplate.transform("com.ibm.ws.webcontainer.async.WSAsyncContextImpl", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addGetter("com.navercorp.pinpoint.plugin.websphere.InitResponseGetter", "initResponse");
                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}