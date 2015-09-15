/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.logback;

import static com.navercorp.pinpoint.common.trace.HistogramSchema.*;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginInstrumentContext;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.PinpointClassFileTransformer;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;

public class LogbackPlugin implements ProfilerPlugin {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    
    public static final ServiceType GSON_SERVICE_TYPE = ServiceType.of(5010, "GSON", NORMAL_SCHEMA);
    public static final AnnotationKey GSON_ANNOTATION_KEY_JSON_LENGTH = new AnnotationKey(9000, "gson.json.length");

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        context.addClassFileTransformer("ch.qos.logback.classic.spi.LoggingEvent", new PinpointClassFileTransformer() {
            
            @Override
            public byte[] transform(ProfilerPluginInstrumentContext pluginContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass mdcClass = pluginContext.getInstrumentClass(loader, "org.slf4j.MDC", null);
                
                if (mdcClass == null) {
                    logger.warn("modify fail. Because org.slf4j.MDC does not exist.");
                    return null;
                }
                
                if (!mdcClass.hasMethod("put", "java.lang.String", "java.lang.String")) {
                    logger.warn("modify fail. Because put method does not exist at org.slf4j.MDC class.");
                    return null;
                }
                if (!mdcClass.hasMethod("remove", "java.lang.String")) {
                    logger.warn("modify fail. Because remove method does not exist at org.slf4j.MDC class.");
                    return null;
                }
                
                InstrumentClass target = pluginContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addInterceptor("com.navercorp.pinpoint.plugin.logback.interceptor.LoggingEventOfLogbackInterceptor");
                
                return target.toBytecode();
            }
        });
    }
}
