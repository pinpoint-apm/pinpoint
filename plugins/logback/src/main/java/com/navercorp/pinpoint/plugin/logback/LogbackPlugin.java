/*
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

import java.security.ProtectionDomain;
import java.util.Arrays;

import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;

/**
 * This modifier support slf4j 1.4.1 version and logback 0.9.8 version, or greater.
 * Because package name of MDC class is different on under those version 
 * and under those version is too old.
 * By the way slf4j 1.4.0 version release on May 2007.
 * Refer to url http://mvnrepository.com/artifact/org.slf4j/slf4j-api for detail.
 * 
 * @author minwoo.jung
 */
public class LogbackPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private TransformTemplate transformTemplate;
    
    @Override
    public void setup(ProfilerPluginSetupContext context) {
        
        final LogbackConfig config = new LogbackConfig(context.getConfig());
        if (logger.isInfoEnabled()) {
            logger.info("LogbackPlugin config:{}", config);
        }
        
        if (!config.isLogbackLoggingTransactionInfo()) {
            logger.info("Logback plugin is not executed because logback transform enable config value is false.");
            return;
        }
        
        transformTemplate.transform("ch.qos.logback.classic.spi.LoggingEvent", new TransformCallback() {
            
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass mdcClass = instrumentor.getInstrumentClass(loader, "org.slf4j.MDC", null);
                
                if (mdcClass == null) {
                    logger.warn("Can not modify. Because org.slf4j.MDC does not exist.");
                    return null;
                }
                
                if (!mdcClass.hasMethod("put", "java.lang.String", "java.lang.String")) {
                    logger.warn("Can not modify. Because put method does not exist at org.slf4j.MDC class.");
                    return null;
                }
                if (!mdcClass.hasMethod("remove", "java.lang.String")) {
                    logger.warn("Can not modify. Because remove method does not exist at org.slf4j.MDC class.");
                    return null;
                }
                
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                
                if (!target.hasConstructor()) {
                    logger.warn("Can not modify. Because constructor to modify not exist at ch.qos.logback.classic.spi.LoggingEvent class."
                                + "\nconstructor prototype : LoggingEvent();");
                    return null;
                }
                if (!target.hasConstructor("java.lang.String", "ch.qos.logback.classic.Logger", "ch.qos.logback.classic.Level", "java.lang.String", "java.lang.Throwable", "java.lang.Object[]")) {
                    logger.warn("Can not modify. Because constructor to modify not exist at ch.qos.logback.classic.spi.LoggingEvent class."
                                + "\nconstructor prototype : LoggingEvent(String fqcn, Logger logger, Level level, String message, Throwable throwable, Object[] argArray);");
                    return null;
                }

                final String interceptorClassName = "com.navercorp.pinpoint.plugin.logback.interceptor.LoggingEventOfLogbackInterceptor";
                addInterceptor(target, new String[0], interceptorClassName);
                addInterceptor(target, new String[]{"java.lang.String", "ch.qos.logback.classic.Logger", "ch.qos.logback.classic.Level", "java.lang.String", "java.lang.Throwable", "java.lang.Object[]"}, interceptorClassName);

                return target.toBytecode();
            }

            private void addInterceptor(InstrumentClass target, String[] parameterTypes, String interceptorClassName) throws InstrumentException {
                InstrumentMethod constructor = InstrumentUtils.findConstructor(target, parameterTypes);
                if (constructor == null) {
                    throw new NotFoundInstrumentException("Cannot find constructor with parameter types: " + Arrays.toString(parameterTypes));
                }
                constructor.addInterceptor(interceptorClassName);
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
