package com.profiler;

import com.profiler.config.ProfilerConfig;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.JavaAssistByteCodeInstrumentor;
import com.profiler.modifier.DefaultModifierRegistry;
import com.profiler.modifier.Modifier;
import com.profiler.modifier.ModifierRegistry;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TomcatProfiler implements ClassFileTransformer {

	private static final Logger logger = Logger.getLogger(TomcatProfiler.class.getName());

	private String agentArgString = "";
	private Instrumentation instrumentation;
	private ByteCodeInstrumentor byteCodeInstrumentor;

	private final ModifierRegistry modifierRepository;
	private ProfilerConfig profilerConfig;

	public static void premain(String agentArgs, Instrumentation inst) {
        try {
            ProfilerConfig profilerConfig = new ProfilerConfig();
            profilerConfig.readConfigFile();
            if (!profilerConfig.isProfileEnable()) {
                logger.warning("Profiler Agent not started. PROFILE_ENABLE=" + profilerConfig.isProfileEnable());
                return;
            }
		    new TomcatProfiler(agentArgs, inst, profilerConfig);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Profiler Agent start fail. Cause:" + e.getMessage(), e);
        }
	}

	public TomcatProfiler(String agentArgs, Instrumentation inst, ProfilerConfig profilerConfig) {
		this.agentArgString = agentArgs;
        this.profilerConfig = profilerConfig;
        this.instrumentation = inst;
        this.instrumentation.addTransformer(this);
        String[] paths = getTomcatlibPath();
        this.byteCodeInstrumentor = new JavaAssistByteCodeInstrumentor(paths);
        this.modifierRepository = createModifierRegistry(byteCodeInstrumentor);
	}

	private String[] getTomcatlibPath() {
		String catalinaHome = System.getProperty("catalina.home");

		if (catalinaHome == null) {
			return null;
		}

		if (logger.isLoggable(Level.INFO)) {
			logger.info("CATALINA_HOME=" + catalinaHome);
		}

		// TODO This is draft. How can we support both Tomcat and BLOC without this configuration?
		String type = System.getProperty("hippo.servertype", "tomcat");

		if (type.equals("bloc")) {
			return new String[] { catalinaHome + "/server/lib/catalina.jar", catalinaHome + "/common/lib/servlet-api.jar" };
		} else {
			return new String[] { catalinaHome + "/lib/servlet-api.jar", catalinaHome + "/lib/catalina.jar" };
		}
	}

    private ModifierRegistry createModifierRegistry(ByteCodeInstrumentor byteCodeInstrumentor) {
		DefaultModifierRegistry modifierRepository = new DefaultModifierRegistry(byteCodeInstrumentor, profilerConfig);

		modifierRepository.addTomcatModifier();

        // jdbc
		modifierRepository.addJdbcModifier();
        // rpc
		modifierRepository.addConnectorModifier();
		return modifierRepository;
	}

	@Override
	public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("[transform] cl" + classLoader + " className:" + className);
        }
		Modifier findModifier = this.modifierRepository.findModifier(className);
		if (findModifier == null) {
			return null;
		}

		String javassistClassName = className.replace('/', '.');

		return findModifier.modify(classLoader, javassistClassName, protectionDomain, classFileBuffer);
	}
}
