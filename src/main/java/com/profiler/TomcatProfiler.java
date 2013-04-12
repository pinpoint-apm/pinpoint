package com.profiler;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Properties;
import java.util.Set;

import com.profiler.common.ServiceType;
import com.profiler.config.ProfilerConfig;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerBinder;
import com.profiler.logging.LoggerFactory;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.JavaAssistByteCodeInstrumentor;
import com.profiler.modifier.ModifierRegistry;

public class TomcatProfiler {

    private static final Logger logger = LoggerFactory.getLogger(TomcatProfiler.class.getName());
    private boolean isFine = logger.isDebugEnabled();

    private String agentArgString = "";

    private Instrumentation instrumentation;
    private final ByteCodeInstrumentor byteCodeInstrumentor;

    private final ModifierRegistry modifierRepository;

    private final ProfilerConfig profilerConfig;
    private final Agent agent;


    public void boot(Instrumentation instrumentation, String agentArgs) {

        if (agentArgs != null) {
            logger.info("HIPPO agentArgs:" + agentArgs);
        }

        try {

            LoggerBinder loggerBinder = null;
            loggerBinder.getLogger("LoggerFactory initialize start");
            LoggerFactory.initialize(loggerBinder);
            com.profiler.logging.Logger tomcatLogger = LoggerFactory.getLogger(TomcatProfiler.class);
            tomcatLogger.info("LoggerFactory initialize end");
        } catch (Exception e) {
            logger.warn("boot class not found", e);
        }

        try {
            ProfilerConfig profilerConfig = null;
            if (!profilerConfig.isProfileEnable()) {
                logger.warn("Profiler Agent not started. profile.enable=" + profilerConfig.isProfileEnable());
                return;
            }
            Agent agent = new DefaultAgent(agentArgs, instrumentation, profilerConfig);
//            new TomcatProfiler(agentArgs, instrumentation, agent, profilerConfig);
        } catch (Exception e) {
            logger.error("Profiler Agent start fail. Cause:" + e.getMessage(), e);
        }
    }



    private static void dumpSystemProperties() {
        if (logger.isDebugEnabled()) {
            Properties properties = System.getProperties();
            Set<String> strings = properties.stringPropertyNames();
            for (String key : strings) {
                logger.debug("SystemProperties " + key + "=" + properties.get(key));
            }
        }
    }

    public TomcatProfiler(String agentArgs, Instrumentation instrumentation, DefaultAgent agent, ProfilerConfig profilerConfig) {
        this.agentArgString = agentArgs;
        this.profilerConfig = profilerConfig;
        this.agent = agent;


        String[] paths = getTomcatlibPath();
        this.byteCodeInstrumentor = new JavaAssistByteCodeInstrumentor(paths, agent);

        this.modifierRepository = null;
        ClassFileTransformer classFileTransformerDispatcher = new ClassFileTransformerDispatcher(agent);
        this.instrumentation.addTransformer(classFileTransformerDispatcher);
    }

    public static String findHippoConfigFormSystemProeprty() {
        String hippoConfigFileName = System.getProperty("hippo.config");
		if (hippoConfigFileName == null) {
            return null;
        }
        logger.info("hippo.config property found. " + hippoConfigFileName);
        return hippoConfigFileName;
    }

    private String[] getTomcatlibPath() {
        String catalinaHome = System.getProperty("catalina.home");

        if (catalinaHome == null) {
            logger.info("CATALINA_HOME is null");
            return null;
        }

        if (logger.isInfoEnabled()) {
            logger.info("CATALINA_HOME={}", catalinaHome);
        }

		if (profilerConfig.getServiceType() == ServiceType.BLOC) {
			return new String[] { catalinaHome + "/server/lib/catalina.jar", catalinaHome + "/common/lib/servlet-api.jar" };
		} else {
			return new String[] { catalinaHome + "/lib/servlet-api.jar", catalinaHome + "/lib/catalina.jar" };
		}
    }



}
