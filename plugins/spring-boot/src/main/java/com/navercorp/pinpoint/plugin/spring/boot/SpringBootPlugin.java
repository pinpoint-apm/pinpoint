/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.boot;

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
import com.navercorp.pinpoint.plugin.spring.boot.interceptor.LauncherLaunchInterceptor;

import java.security.ProtectionDomain;

/**
 * @author HyunGil Jeong
 */
public class SpringBootPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        SpringBootConfiguration config = new SpringBootConfiguration(context.getConfig());
        if (!config.isSpringBootEnabled()) {
            logger.info("SpringBootPlugin disabled");
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            SpringBootDetector springBootDetector = new SpringBootDetector(config.getSpringBootBootstrapMains());
            if (springBootDetector.detect()) {
                logger.info("Detected application type : {}", SpringBootConstants.SERVICE_TYPE);
                if (!context.registerApplicationType(SpringBootConstants.SERVICE_TYPE)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), SpringBootConstants.SERVICE_TYPE);
                }
            }
        }

        logger.info("Adding SpringBoot transformers");
        addLauncherEditor();
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    private void addLauncherEditor() {
        transformTemplate.transform("org.springframework.boot.loader.Launcher", LauncherTransform.class);
    }

    public static class LauncherTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
        byte[] classfileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("launch", "java.lang.String[]", "java.lang.String", "java.lang.ClassLoader");
            if (method != null) {
                method.addInterceptor(LauncherLaunchInterceptor.class);
            }

            return target.toBytecode();
        }

    }
}