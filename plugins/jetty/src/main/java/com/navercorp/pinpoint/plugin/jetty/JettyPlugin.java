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

import static com.navercorp.pinpoint.common.util.VarArgs.va;

public class JettyPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        JettyConfiguration config = new JettyConfiguration(context.getConfig());
        if (!config.isJettyEnabled()) {
            logger.info("JettyPlugin disabled");
            return;
        }

        context.addApplicationTypeDetector(new JettyDetector(config.getJettyBootstrapMains()));

        addServerInterceptor(config);
    }

    private void addServerInterceptor(final JettyConfiguration config){
        transformTemplate.transform("org.eclipse.jetty.server.Server",  new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                InstrumentMethod handleMethodEditorBuilder = target.getDeclaredMethod("handle", "org.eclipse.jetty.server.HttpChannel");
                if (handleMethodEditorBuilder != null) {
                    handleMethodEditorBuilder.addInterceptor("com.navercorp.pinpoint.plugin.jetty.interceptor.ServerHandleInterceptor", va(config.getJettyExcludeUrlFilter()));
                    return target.toBytecode();
                }

                InstrumentMethod jetty8HandleMethodEditorBuilder = target.getDeclaredMethod("handle", "org.eclipse.jetty.server.AbstractHttpConnection");
                if (jetty8HandleMethodEditorBuilder != null) {
                    jetty8HandleMethodEditorBuilder.addInterceptor("com.navercorp.pinpoint.plugin.jetty.interceptor.Jetty8ServerHandleInterceptor", va(config.getJettyExcludeUrlFilter()));
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
