package com.navercorp.pinpoint.plugin.log4j2;

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
import com.navercorp.pinpoint.plugin.log4j2.interceptor.LoggingEventOfLog4j2Interceptor;

import java.security.ProtectionDomain;
import java.util.Arrays;

/**
 * @Author: https://github.com/licoco/pinpoint
 * @Date: 2019/1/4 10:52
 * @Version: 1.0
 */
public class Log4j2Plugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private TransformTemplate transformTemplate;


    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final Log4j2Config config = new Log4j2Config(context.getConfig());
        if (logger.isInfoEnabled()) {
            logger.info("Log4j2Plugin config:{}", config);
        }

        if (!config.isLog4j2LoggingTransactionInfo()) {
            logger.info("Log4j2 plugin is not executed because log4j2 transform enable config value is false.");
            return;
        }
        transformTemplate.transform("org.apache.logging.log4j.core.impl.Log4jLogEvent", ThreadContextTransform.class);
    }

    public static class ThreadContextTransform implements TransformCallback {
        private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass mdcClass = instrumentor.getInstrumentClass(loader, "org.apache.logging.log4j.ThreadContext", null);

            if (mdcClass == null) {
                logger.warn("Can not modify. Because org.apache.logging.log4j.ThreadContext does not exist.");
                return null;
            }

            if (!mdcClass.hasMethod("put", "java.lang.String", "java.lang.String")) {
                logger.warn("Can not modify. Because put method does not exist at org.apache.logging.log4j.ThreadContext class.");
                return null;
            }
            if (!mdcClass.hasMethod("remove", "java.lang.String")) {
                logger.warn("Can not modify. Because remove method does not exist at org.apache.logging.log4j.ThreadContext class.");
                return null;
            }

            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final Class<? extends Interceptor> interceptorClassName = LoggingEventOfLog4j2Interceptor.class;

            /**
             * support log4j2 2.7+ Constructor
             */
            addInterceptor(target, new String[]{"java.lang.String", "org.apache.logging.log4j.Marker", "java.lang.String", "org.apache.logging.log4j.Level", "org.apache.logging.log4j.message.Message", "java.lang.Throwable", "org.apache.logging.log4j.core.impl.ThrowableProxy", "org.apache.logging.log4j.util.StringMap", "org.apache.logging.log4j.ThreadContext$ContextStack", "long", "java.lang.String", "int", "java.lang.StackTraceElement", "long", "long"}, interceptorClassName);
            /**
             * log4j 2.1~2.7 Constructor .  This constructor will be removed in an upcoming release
             */
            addInterceptor(target, new String[]{"java.lang.String", "org.apache.logging.log4j.Marker", "java.lang.String", "org.apache.logging.log4j.Level", "org.apache.logging.log4j.message.Message", "java.lang.Throwable", "org.apache.logging.log4j.core.impl.ThrowableProxy", "java.util.Map", "org.apache.logging.log4j.ThreadContext$ContextStack", "java.lang.String", "java.lang.StackTraceElement", "long"}, interceptorClassName);
            return target.toBytecode();
        }

        private void addInterceptor(InstrumentClass target, String[] parameterTypes, Class<? extends Interceptor> interceptorClassName) throws InstrumentException {
            InstrumentMethod constructor = target.getConstructor(parameterTypes);
            if (constructor != null) {
                constructor.addInterceptor(interceptorClassName);
            }
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
