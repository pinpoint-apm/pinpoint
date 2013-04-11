package com.profiler;

import com.profiler.config.ProfilerConfig;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;
import com.profiler.modifier.DefaultModifierRegistry;
import com.profiler.modifier.Modifier;
import com.profiler.modifier.ModifierRegistry;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 *
 */
public class ClassFileTransformerDispatcher implements ClassFileTransformer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final boolean isFine = logger.isDebugEnabled();

    private final ClassLoader agentClassLoader = this.getClass().getClassLoader();

    private ModifierRegistry modifierRegistry;

    private Agent agent;

    private ProfilerConfig profilerConfig;


    public ClassFileTransformerDispatcher(Agent agent) {
        if(agent == null) {
            throw new NullPointerException("agent must not be null");
        }
        this.agent = agent;
        this.profilerConfig = agent.getProfilerConfig();
        this.modifierRegistry = createModifierRegistry();
    }

    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
        // fast java class skip
        if (className.startsWith("java")) {
            if (className.startsWith("/", 4) || className.startsWith("x/", 4)) {
                return classFileBuffer;
            }
        }
        if (classLoader == agentClassLoader) {
            // agent의 clssLoader에 로드된 클래스는 스킵한다.
            return null;
        }

        Modifier findModifier = this.modifierRegistry.findModifier(className);
        if (findModifier == null) {
            // TODO : 디버그 용도로 추가함
            // TODO : modifier가 중복 적용되면 어떻게 되지???
            if (this.profilerConfig.isProfilableClass(className)) {
                // 테스트 장비에서 callstack view가 잘 보이는지 확인하려고 추가함.
                findModifier = this.modifierRegistry.findModifier("*");
            } else {
                return null;
            }
        }

        if (isFine) {
            logger.debug("[transform] cl" + classLoader + " className:" + className + " Modifier:" + findModifier.getClass().getName());
        }
        String javassistClassName = className.replace('/', '.');

        try {
            return findModifier.modify(classLoader, javassistClassName, protectionDomain, classFileBuffer);

        } catch (Throwable e) {
            logger.error("Modifier:" + findModifier.getTargetClass() + " modify fail. Cause:" + e.getMessage(), e);
            return null;
        }
    }


    private ModifierRegistry createModifierRegistry() {
        DefaultModifierRegistry modifierRepository = new DefaultModifierRegistry(agent);

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

}
