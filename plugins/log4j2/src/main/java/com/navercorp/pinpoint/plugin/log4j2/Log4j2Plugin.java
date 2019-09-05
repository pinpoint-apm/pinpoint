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

        transformAsyncLogger();
    }

    private void transformLogEvent() {

        transformTemplate.transform("org.apache.logging.log4j.core.impl.Log4jLogEvent", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?>
                    classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                if (haveMDCError(instrumentor, loader)) {
                    return null;
                }

                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                final String interceptorClassName = LoggingEventOfLog4j2Interceptor.class.getName();

                addConstructorInterceptor(target, new String[0], interceptorClassName);
                addConstructorInterceptor(target, new String[]{"long"}, interceptorClassName);
                addConstructorInterceptor(target, new String[]{"java.lang.String", "org.apache.logging.log4j.Marker", "java.lang.String", "org.apache.logging.log4j.Level", "org.apache.logging.log4j.message.Message",
                        "java.lang.Throwable"}, interceptorClassName);
                addConstructorInterceptor(target, new String[]{"java.lang.String", "org.apache.logging.log4j.Marker", "java.lang.String", "org.apache.logging.log4j.Level", "org.apache.logging.log4j.message.Message",
                        "java.util.List", "java.lang.Throwable"}, interceptorClassName);
                addConstructorInterceptor(target, new String[]{"java.lang.String", "org.apache.logging.log4j.Marker", "java.lang.String", "org.apache.logging.log4j.Level", "org.apache.logging.log4j.message.Message",
                        "java.lang.Throwable", "java.util.Map", "org.apache.logging.log4j.ThreadContext$ContextStack", "java.lang.String", "java.lang.StackTraceElement", "long"}, interceptorClassName);

                int majorVersion = 0;
                int minorVersion = 0;
                int patchVersion = 0;
                boolean gotCheckError = false;
                String version = getVersion(loader);
                if (version == null) {
                    gotCheckError = true;
                } else {
                    majorVersion = getVersionSection(version, 0);
                    minorVersion = getVersionSection(version, 1);
                    patchVersion = getVersionSection(version, 2);
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
                    addConstructorInterceptor(target, new String[]{"java.lang.String", "org.apache.logging.log4j.Marker", "java.lang.String",
                            "java.lang.StackTraceElement", "org.apache.logging.log4j.Level", "org.apache.logging.log4j.message.Message", "java.util.List", "java.lang.Throwable"}, interceptorClassName);
                }

                return target.toBytecode();
            }

            private void addConstructorInterceptor(InstrumentClass target, String[] parameterTypes, String interceptorClassName) throws InstrumentException {
                InstrumentMethod constructor = InstrumentUtils.findConstructor(target, parameterTypes);
                if (constructor != null) {
                    constructor.addInterceptor(interceptorClassName);
                }
            }
        });
    }

    private void transformAsyncLogger() {
        final String clazz = "org.apache.logging.log4j.core.async.AsyncLogger";
        transformTemplate.transform(clazz, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?>
                    classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                if (haveMDCError(instrumentor, loader)) {
                    return null;
                }
                String version = getVersion(loader);
                if (version == null) {
                    return null;
                }
                final int majorVersion = getVersionSection(version, 0);
                final int majorVersionConstraint = 2;
                if (majorVersion != majorVersionConstraint) {
                    return null;
                }

                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                final String interceptorClassName = LoggingEventOfLog4j2Interceptor.class.getName();

                //intercept logMessage(final String fqcn, final Level level, final Marker marker, final Message message, final Throwable thrown)
                addLogMethodInterceptor(target, new String[]{"java.lang.String", "org.apache.logging.log4j.Level", "org.apache.logging.log4j.Marker",
                        "org.apache.logging.log4j.message.Message", "java.lang.Throwable"}, interceptorClassName);

                return target.toBytecode();
            }

            private void addLogMethodInterceptor(InstrumentClass target, String[] parameterTypes, String interceptorClassName) throws InstrumentException {
                InstrumentMethod instrumentMethod = InstrumentUtils.findMethod(target, "logMessage", parameterTypes);
                if (instrumentMethod != null) {
                    instrumentMethod.addInterceptor(interceptorClassName);
                }
            }
        });
    }

    private String getVersion(ClassLoader loader) {
        try {
            String versionCheckClass = "org.apache.logging.log4j.core.LogEvent";
            Class<?> transformClazz = Class.forName(versionCheckClass, false, loader);
            return transformClazz.getPackage().getImplementationVersion();
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private int getVersionSection(String version, int index) {
        try {
            String[] versionsString = version.split("\\.");
            return index < versionsString.length ? Integer.parseInt(versionsString[index]) : -1;
        } catch (NumberFormatException e) {
            return -2;
        }
    }

    private boolean haveMDCError(Instrumentor instrumentor, ClassLoader loader) {
        InstrumentClass mdcClass = instrumentor.getInstrumentClass(loader, "org.apache.logging.log4j.ThreadContext", null);
        if (mdcClass == null) {
            logger.warn("Can not modify. Because org.apache.logging.log4j.ThreadContext does not exist.");
            return true;
        }
        if (!mdcClass.hasMethod("put", "java.lang.String", "java.lang.String")) {
            logger.warn("Can not modify. Because put method does not exist at org.apache.logging.log4j.ThreadContext class.");
            return true;
        }
        if (!mdcClass.hasMethod("remove", "java.lang.String")) {
            logger.warn("Can not modify. Because remove method does not exist at org.apache.logging.log4j.ThreadContext class.");
            return true;
        }
        return false;
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
