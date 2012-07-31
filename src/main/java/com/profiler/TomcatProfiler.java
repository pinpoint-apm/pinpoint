package com.profiler;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.modifier.DefaultModifierRegistry;
import com.profiler.modifier.Modifier;
import com.profiler.modifier.ModifierRegistry;
import javassist.ClassPool;
import javassist.NotFoundException;

import com.profiler.config.TomcatProfilerConfig;

public class TomcatProfiler implements ClassFileTransformer {

    private static final Logger logger = Logger.getLogger(TomcatProfiler.class.getName());

    private String agentArgString = "";
    private Instrumentation instrumentation;
    private ClassPool classPool;

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
        this.classPool = createClassPool();
        this.modifierRepository = createModifierRegistry(this.classPool, tomcatProfilerConfig);
        this.tomcatProfilerConfig = tomcatProfilerConfig;
    }

    private ModifierRegistry createModifierRegistry(ClassPool classPool, TomcatProfilerConfig tomcatProfilerConfig) {
        DefaultModifierRegistry modifierRepository = new DefaultModifierRegistry(classPool);
        modifierRepository.addTomcatModifier();
        if (tomcatProfilerConfig.enableJdbcProfile()) {
            modifierRepository.addJdbcModifier();
        }
        return modifierRepository;
    }

    private ClassPool createClassPool() {
        ClassPool classPool = new ClassPool(null);
        classPool.appendSystemPath();

        String catalinaHome = System.getProperty("catalina.home");
        if (catalinaHome != null) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("CATALINA_HOME=" + catalinaHome);
            }

            appendClassPath(classPool, catalinaHome + "/lib/servlet-api.jar");
            appendClassPath(classPool, catalinaHome + "/lib/catalina.jar");
        }
        return classPool;
    }

    private void appendClassPath(ClassPool classPool, String pathName) {
        try {
            classPool.appendClassPath(pathName);
        } catch (NotFoundException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("lib not found. " + e.getMessage());
            }

        }
    }

    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
        Modifier findModifier = this.modifierRepository.findModifier(className);
        if (findModifier == null) {
            return null;
        } 
        String javassistClassName = className.replace('/', '.');
        
        return findModifier.modify(classLoader, javassistClassName, classFileBuffer);
    }
}
