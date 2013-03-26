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
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TomcatProfiler implements ClassFileTransformer {

    private static final Logger logger = Logger.getLogger(TomcatProfiler.class.getName());
    private boolean isFine = logger.isLoggable(Level.FINE);

    private String agentArgString = "";

    private Instrumentation instrumentation;
    private final ByteCodeInstrumentor byteCodeInstrumentor;

    private final ModifierRegistry modifierRepository;

    private final ProfilerConfig profilerConfig;
    private final Agent agent;

    private AgentClassLoader agentClassLoader;

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        if (agentArgs != null) {
            logger.info("HIPPO agentArgs:" + agentArgs);
        }
        dumpSystemProperties();

        try {
            ProfilerConfig profilerConfig = new ProfilerConfig();
            profilerConfig.readConfigFile();
            if (!profilerConfig.isProfileEnable()) {
                logger.warning("Profiler Agent not started. profile.enable=" + profilerConfig.isProfileEnable());
                return;
            }
            Agent agent = new Agent(profilerConfig);
            new TomcatProfiler(agentArgs, instrumentation, agent, profilerConfig);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Profiler Agent start fail. Cause:" + e.getMessage(), e);
        }
    }

    private static void dumpSystemProperties() {
        if (logger.isLoggable(Level.FINE)) {
            Properties properties = System.getProperties();
            Set<String> strings = properties.stringPropertyNames();
            for (String key : strings) {
                logger.fine("SystemProperties " + key + "=" + properties.get(key));
            }
        }
    }

    public TomcatProfiler(String agentArgs, Instrumentation instrumentation, Agent agent, ProfilerConfig profilerConfig) {
        this.agentArgString = agentArgs;
        this.profilerConfig = profilerConfig;
        this.agent = agent;

        this.instrumentation = instrumentation;
        this.instrumentation.addTransformer(this);

        String[] paths = getTomcatlibPath();
        this.byteCodeInstrumentor = new JavaAssistByteCodeInstrumentor(paths);

        this.modifierRepository = createModifierRegistry();
    }

    private String[] getTomcatlibPath() {
        String catalinaHome = System.getProperty("catalina.home");

        if (catalinaHome == null) {
            logger.info("CATALINA_HOME is null");
            return null;
        }

        if (logger.isLoggable(Level.INFO)) {
            logger.info("CATALINA_HOME=" + catalinaHome);
        }

        // TODO This is draft. How can we support both Tomcat and BLOC without this configuration?
        String type = System.getProperty("hippo.servertype", "tomcat");

        if (type.equals("bloc")) {
            return new String[]{catalinaHome + "/server/lib/catalina.jar", catalinaHome + "/common/lib/servlet-api.jar"};
        } else {
            return new String[]{catalinaHome + "/lib/servlet-api.jar", catalinaHome + "/lib/catalina.jar"};
        }
    }

    private ModifierRegistry createModifierRegistry() {
        DefaultModifierRegistry modifierRepository = new DefaultModifierRegistry(byteCodeInstrumentor, agent, profilerConfig);

        modifierRepository.addMethodModifier();

        modifierRepository.addTomcatModifier();

        // jdbc
        modifierRepository.addJdbcModifier();

        // rpc
        modifierRepository.addConnectorModifier();

        // bloc
        modifierRepository.addBLOCModifier();

        return modifierRepository;
    }

    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
        // fast java class skip
        if (className.startsWith("java")) {
            if (className.startsWith("/", 4) || className.startsWith("x/", 4)) {
                return classFileBuffer;
            }
        }
        Modifier findModifier = this.modifierRepository.findModifier(className);

        if (findModifier == null) {
            // TODO : 디버그 용도로 추가함
            // TODO : modifier가 중복 적용되면 어떻게 되지???
            if (profilerConfig.isProfilableClass(className)) {
                // 테스트 장비에서 callstack view가 잘 보이는지 확인하려고 추가함.
                findModifier = this.modifierRepository.findModifier("*");
            } else {
                return null;
            }
        }

        if (isFine) {
            logger.fine("[transform] cl" + classLoader + " className:" + className + " Modifier:" + findModifier.getClass().getName());
        }
        String javassistClassName = className.replace('/', '.');

        try {
            return findModifier.modify(classLoader, javassistClassName, protectionDomain, classFileBuffer);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Modifier:" + findModifier.getTargetClass() + " modify fail. Cause:" + e.getMessage(), e);
            return null;
        }
    }
}
