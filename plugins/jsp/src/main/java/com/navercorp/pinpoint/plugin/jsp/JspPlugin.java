/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jsp;

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
import com.navercorp.pinpoint.plugin.jsp.interceptor.HttpJspBaseServiceMethodInterceptor;

import java.security.ProtectionDomain;

/**
 * @author jaehong.kim
 */
public class JspPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    /*
     * (non-Javadoc)
     *
     * @see com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin#setUp(com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext)
     */
    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final JspPluginConfig config = new JspPluginConfig(context.getConfig());
        if (logger.isInfoEnabled()) {
            logger.info("JspPlugin config={}", config);
        }

        if (!config.isEnable()) {
            if (logger.isInfoEnabled()) {
                logger.info("JspPlugin disabled");
            }
            return;
        }

        // Tomcat, Jetty, JBoss
        // Jasper 2 JSP Engine.
        if (logger.isInfoEnabled()) {
            logger.info("Adding Jasper 2 JSP Engine(Tomcat, Jetty, JBoss).");
        }
        addJasper2JspEngine();
    }

    private void addJasper2JspEngine() {
        transformTemplate.transform("org.apache.jasper.runtime.HttpJspBase", HttpJspBaseTransform.class);
    }

    public static class HttpJspBaseTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            final InstrumentMethod serviceMethod = target.getDeclaredMethod("service", "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse");
            if (serviceMethod != null) {
                serviceMethod.addInterceptor(HttpJspBaseServiceMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}