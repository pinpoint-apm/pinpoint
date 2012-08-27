package com.profiler;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.JavaAssistByteCodeInstrumentor;
import com.profiler.modifier.DefaultModifierRegistry;
import com.profiler.modifier.Modifier;
import com.profiler.modifier.ModifierRegistry;

import com.profiler.config.TomcatProfilerConfig;

public class TomcatProfiler implements ClassFileTransformer {

	private static final Logger logger = Logger.getLogger(TomcatProfiler.class.getName());

	private String agentArgString = "";
	private Instrumentation instrumentation;
	private ByteCodeInstrumentor byteCodeInstrumentor;

	private final ModifierRegistry modifierRepository;
	private TomcatProfilerConfig tomcatProfilerConfig;

	public static void premain(String agentArgs, Instrumentation inst) {
		TomcatProfilerConfig tomcatProfilerConfig = TomcatProfilerConfig.readConfigFile();
		new TomcatProfiler(agentArgs, inst, tomcatProfilerConfig);
	}

	public TomcatProfiler(String agentArgs, Instrumentation inst, TomcatProfilerConfig tomcatProfilerConfig) {
		this.agentArgString = agentArgs;
		this.instrumentation = inst;
		this.instrumentation.addTransformer(this);
        String[] paths = getTomcatlibPath();
		this.byteCodeInstrumentor = new JavaAssistByteCodeInstrumentor(paths);
		this.modifierRepository = createModifierRegistry(byteCodeInstrumentor, tomcatProfilerConfig);
		this.tomcatProfilerConfig = tomcatProfilerConfig;

	}

    private String[] getTomcatlibPath() {
        String catalinaHome = System.getProperty("catalina.home");
        if (catalinaHome == null) {
            return null;
        }
        if (logger.isLoggable(Level.INFO)) {
            logger.info("CATALINA_HOME=" + catalinaHome);
        }
        return new String[] {catalinaHome + "/lib/servlet-api.jar", catalinaHome + "/lib/catalina.jar"};
    }

    private ModifierRegistry createModifierRegistry(ByteCodeInstrumentor byteCodeInstrumentor, TomcatProfilerConfig tomcatProfilerConfig) {
		DefaultModifierRegistry modifierRepository = new DefaultModifierRegistry(byteCodeInstrumentor);
		modifierRepository.addTomcatModifier();
		if (tomcatProfilerConfig.enableJdbcProfile()) {
			modifierRepository.addJdbcModifier();
		}
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
