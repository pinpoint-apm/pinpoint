package com.navercorp.pinpoint.plugin.log4j2;

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
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;
import com.navercorp.pinpoint.plugin.log4j2.interceptor.LoggingEventOfLog4j2Interceptor;

import java.security.ProtectionDomain;

/**
 * @author licoco
 * @author King Jin
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

        transformLogEvent();
    }

    private void transformLogEvent() {
        transformTemplate.transform("org.apache.logging.log4j.core.impl.Log4jLogEvent", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?>
                    classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

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
                final String interceptorClassName = LoggingEventOfLog4j2Interceptor.class.getName();

                addInterceptor(target, new String[0], interceptorClassName);
                addInterceptor(target, new String[]{"long"}, interceptorClassName);
                addInterceptor(target, new String[]{"java.lang.String", "org.apache.logging.log4j.Marker", "java.lang.String", "org.apache.logging.log4j.Level", "org.apache.logging.log4j.message.Message",
                        "java.lang.Throwable"}, interceptorClassName);
                addInterceptor(target, new String[]{"java.lang.String", "org.apache.logging.log4j.Marker", "java.lang.String", "org.apache.logging.log4j.Level", "org.apache.logging.log4j.message.Message",
                        "java.util.List", "java.lang.Throwable"}, interceptorClassName);
                addInterceptor(target, new String[]{"java.lang.String", "org.apache.logging.log4j.Marker", "java.lang.String", "org.apache.logging.log4j.Level", "org.apache.logging.log4j.message.Message",
                        "java.lang.Throwable", "java.util.Map", "org.apache.logging.log4j.ThreadContext$ContextStack", "java.lang.String", "java.lang.StackTraceElement", "long"}, interceptorClassName);

                int majorVersion = 0;
                int minorVersion = 0;
                int patchVersion = 0;
                boolean gotCheckError = false;
                try {
                    String versionCheckClass = "org.apache.logging.log4j.core.LogEvent";
                    Class<?> transformClazz = Class.forName(versionCheckClass, false, loader);
                    String implementationVersion = transformClazz.getPackage().getImplementationVersion();
                    String[] versionsString = implementationVersion.split("\\.");
                    majorVersion = Integer.parseInt(versionsString[0]);
                    minorVersion = Integer.parseInt(versionsString[1]);
                    final int maxVersionLength = 3;
                    patchVersion = versionsString.length >= maxVersionLength ? Integer.parseInt(versionsString[2]) : 0;
                } catch (ClassNotFoundException e) {
                    gotCheckError = true;
                } catch (NumberFormatException e) {
                    gotCheckError = true;
                }

                final int majorVersionConstraint = 2;
                if (majorVersion != majorVersionConstraint) {
                    gotCheckError = true;
                }
                if (gotCheckError) {
                    return target.toBytecode();
                }
                /*
                  for log4j2 >=2.12.1 Constructor
                 */
                int minorVersionCheck = 12;
                int patchVersionCheck = 1;
                boolean extraConstructor = (minorVersion == minorVersionCheck && patchVersion >= patchVersionCheck) || (minorVersion > minorVersionCheck);
                if (extraConstructor) {
                    addInterceptor(target, new String[]{"java.lang.String", "org.apache.logging.log4j.Marker", "java.lang.String",
                            "java.lang.StackTraceElement", "org.apache.logging.log4j.Level", "org.apache.logging.log4j.message.Message", "java.util.List", "java.lang.Throwable"}, interceptorClassName);
                }

                return target.toBytecode();
            }

            private void addInterceptor(InstrumentClass target, String[] parameterTypes, String interceptorClassName) throws InstrumentException {
                InstrumentMethod constructor = InstrumentUtils.findConstructor(target, parameterTypes);
                if (constructor != null) {
                    constructor.addInterceptor(interceptorClassName);
                }
            }

        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
