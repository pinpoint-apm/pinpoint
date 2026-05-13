/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.jetty12;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.jetty12.ee10.interceptor.EE10ServletChannelHandleInterceptor;
import com.navercorp.pinpoint.plugin.jetty12.ee11.interceptor.EE11ServletChannelHandleInterceptor;
import com.navercorp.pinpoint.plugin.jetty12.interceptor.RequestStartAsyncInterceptor;

import java.security.ProtectionDomain;

public class Jetty12Plugin implements ProfilerPlugin, TransformTemplateAware {

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        Jetty12Configuration config = new Jetty12Configuration(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{} ", this.getClass().getSimpleName(), config);
        logger.info("version range=[12.0, 12.x]");

        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            Jetty12Detector detector = new Jetty12Detector(config.getBootstrapMains());
            if (detector.detect()) {
                logger.info("Detected application type : {}", Jetty12Constants.JETTY);
                if (!context.registerApplicationType(Jetty12Constants.JETTY)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), Jetty12Constants.JETTY);
                }
            }
        }

        logger.info("Adding Jetty 12 transformers");
        addEE10ServletChannelInterceptor();
        addEE11ServletChannelInterceptor();
        addEE10ServletApiRequestTransform();
        addEE11ServletApiRequestTransform();
    }

    private void addEE10ServletChannelInterceptor() {
        transformTemplate.transform("org.eclipse.jetty.ee10.servlet.ServletChannel", EE10ServletChannelTransform.class);
    }

    public static class EE10ServletChannelTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            final InstrumentMethod handleMethod = target.getDeclaredMethod("handle");
            if (handleMethod != null) {
                handleMethod.addInterceptor(EE10ServletChannelHandleInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void addEE11ServletChannelInterceptor() {
        transformTemplate.transform("org.eclipse.jetty.ee11.servlet.ServletChannel", EE11ServletChannelTransform.class);
    }

    public static class EE11ServletChannelTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            final InstrumentMethod handleMethod = target.getDeclaredMethod("handle");
            if (handleMethod != null) {
                handleMethod.addInterceptor(EE11ServletChannelHandleInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void addEE10ServletApiRequestTransform() {
        transformTemplate.transform("org.eclipse.jetty.ee10.servlet.ServletApiRequest", ServletApiRequestTransform.class);
    }

    private void addEE11ServletApiRequestTransform() {
        transformTemplate.transform("org.eclipse.jetty.ee11.servlet.ServletApiRequest", ServletApiRequestTransform.class);
    }

    public static class ServletApiRequestTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final Jetty12Configuration config = new Jetty12Configuration(instrumentor.getProfilerConfig());
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            if (config.isHidePinpointHeader()) {
                target.weave("com.navercorp.pinpoint.plugin.jetty12.aspect.RequestAspect");
            }
            final InstrumentMethod startAsyncMethod = target.getDeclaredMethod("startAsync");
            if (startAsyncMethod != null) {
                startAsyncMethod.addInterceptor(RequestStartAsyncInterceptor.class);
            }
            final InstrumentMethod startAsyncMethodEditor = target.getDeclaredMethod("startAsync", "jakarta.servlet.ServletRequest", "jakarta.servlet.ServletResponse");
            if (startAsyncMethodEditor != null) {
                startAsyncMethodEditor.addInterceptor(RequestStartAsyncInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
