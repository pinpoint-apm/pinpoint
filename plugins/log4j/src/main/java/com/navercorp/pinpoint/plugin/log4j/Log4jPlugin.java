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
package com.navercorp.pinpoint.plugin.log4j;

import java.security.ProtectionDomain;
import java.util.Arrays;

import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;
import com.navercorp.pinpoint.plugin.log4j.interceptor.LoggingEventOfLog4jInterceptor;

/**
 * This modifier support log4j 1.2.15 version, or greater.
 * Because under 1.2.15 version is not exist MDC function and the number of constructor is different
 * and under 1.2.15 version is too old.
 * For reference 1.2.14 version release on Sep. 2006.
 * Refer to url http://mvnrepository.com/artifact/log4j/log4j for detail.
 * 
 * @author minwoo.jung
 */
public class Log4jPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private TransformTemplate transformTemplate;
    

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final Log4jConfig config = new Log4jConfig(context.getConfig());
        if (!config.isLog4jLoggingTransactionInfo()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        transformTemplate.transform("org.apache.log4j.spi.LoggingEvent", LoggingEventTransform.class);
    }

    public static class LoggingEventTransform implements TransformCallback {
        private final PLogger logger = PLoggerFactory.getLogger(getClass());
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass mdcClass = instrumentor.getInstrumentClass(loader, "org.apache.log4j.MDC", null);

            if (mdcClass == null) {
                logger.warn("Can not modify. Because org.apache.log4j.MDC does not exist.");
                return null;
            }

            if (!mdcClass.hasMethod("put", "java.lang.String", "java.lang.Object")) {
                logger.warn("Can not modify. Because put method does not exist at org.apache.log4j.MDC class.");
                return null;
            }
            if (!mdcClass.hasMethod("remove", "java.lang.String")) {
                logger.warn("Can not modify. Because remove method does not exist at org.apache.log4j.MDC class.");
                return null;
            }

            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.hasConstructor("java.lang.String", "org.apache.log4j.Category", "org.apache.log4j.Priority", "java.lang.Object", "java.lang.Throwable")) {
                logger.warn("Can not modify. Because constructor to modify not exist at org.apache.log4j.MDC class."
                        + "\nThere is a strong presumption that your application use under version 1.2.14 log4j."
                        + "\nconstructor prototype : LoggingEvent(String fqnOfCategoryClass, Category logger, Priority level, Object message, Throwable throwable);");
                return null;
            }
            if (!target.hasConstructor("java.lang.String", "org.apache.log4j.Category", "long", "org.apache.log4j.Priority", "java.lang.Object", "java.lang.Throwable")) {
                logger.warn("Can not modify. Because constructor to modify not exist at org.apache.log4j.MDC class."
                        + "\nThere is a strong presumption that your application use under version 1.2.14 log4j."
                        + "\nconstructor prototype : LoggingEvent(String fqnOfCategoryClass, Category logger, long timeStamp, Priority level, Object message, Throwable throwable);");
                return null;
            }
            if (!target.hasConstructor("java.lang.String", "org.apache.log4j.Category", "long", "org.apache.log4j.Level", "java.lang.Object", "java.lang.String", "org.apache.log4j.spi.ThrowableInformation", "java.lang.String", "org.apache.log4j.spi.LocationInfo", "java.util.Map")) {
                logger.warn("Can not modify. Because constructor to modify not exist at org.apache.log4j.MDC class. "
                        + "\nThere is a strong presumption that your application use under version 1.2.14 log4j."
                        + "\nconstructor prototype : LoggingEvent(final String fqnOfCategoryClass, final Category logger, final long timeStamp, final Level level, final Object message, final String threadName, final ThrowableInformation throwable, final String ndc, final LocationInfo info, final java.util.Map properties);");
                return null;
            }

            final Class<? extends Interceptor> interceptorClassName = LoggingEventOfLog4jInterceptor.class;
            addInterceptor(target, new String[]{"java.lang.String", "org.apache.log4j.Category", "org.apache.log4j.Priority", "java.lang.Object", "java.lang.Throwable"}, interceptorClassName);
            addInterceptor(target, new String[]{"java.lang.String", "org.apache.log4j.Category", "long", "org.apache.log4j.Priority", "java.lang.Object", "java.lang.Throwable"}, interceptorClassName);
            addInterceptor(target, new String[]{"java.lang.String", "org.apache.log4j.Category", "long", "org.apache.log4j.Level", "java.lang.Object", "java.lang.String", "org.apache.log4j.spi.ThrowableInformation", "java.lang.String", "org.apache.log4j.spi.LocationInfo", "java.util.Map"}, interceptorClassName);

            return target.toBytecode();
        }

        private void addInterceptor(InstrumentClass target, String[] parameterTypes, Class<? extends Interceptor> interceptorClassName) throws InstrumentException {
            InstrumentMethod constructor = InstrumentUtils.findConstructor(target, parameterTypes);
            if (constructor == null) {
                throw new NotFoundInstrumentException("Cannot find constructor with parameter types: " + Arrays.toString(parameterTypes));
            }
            constructor.addInterceptor(interceptorClassName);
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
