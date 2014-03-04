package com.nhn.pinpoint.profiler;

import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.modifier.DefaultModifierRegistry;
import com.nhn.pinpoint.profiler.modifier.Modifier;
import com.nhn.pinpoint.profiler.modifier.ModifierRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author emeroad
 * @author netspider
 */
public class ClassFileTransformerDispatcher implements ClassFileTransformer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ClassLoader agentClassLoader = this.getClass().getClassLoader();

    private final ModifierRegistry modifierRegistry;

    private final Agent agent;
    private final ByteCodeInstrumentor byteCodeInstrumentor;

    private ProfilerConfig profilerConfig;


    public ClassFileTransformerDispatcher(Agent agent, ByteCodeInstrumentor byteCodeInstrumentor) {
        if (agent == null) {
            throw new NullPointerException("agent must not be null");
        }
        if (byteCodeInstrumentor == null) {
            throw new NullPointerException("byteCodeInstrumentor must not be null");
        }
        this.agent = agent;
        this.byteCodeInstrumentor = byteCodeInstrumentor;
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
        // 자기 자신의 패키지도 제외
        // 향후 패키지명 변경에 의해 코드 변경이 필요함.
        if (className.startsWith("com/nhn/pinpoint/")) {
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

        if (isDebug) {
            logger.debug("[transform] cl:{} className:{} Modifier:{}", classLoader, className, findModifier.getClass().getName());
        }
        String javassistClassName = className.replace('/', '.');

        try {
            final Thread thread = Thread.currentThread();
            final ClassLoader before = getContextClassLoader(thread);
            thread.setContextClassLoader(this.agentClassLoader);
            try {
                return findModifier.modify(classLoader, javassistClassName, protectionDomain, classFileBuffer);
            } finally {
                thread.setContextClassLoader(before);
            }
        }
        catch (Throwable e) {
            logger.error("Modifier:{} modify fail. cl:{} ctxCl:{} agentCl:{} Cause:{}",
                    findModifier.getTargetClass(), classLoader, Thread.currentThread().getContextClassLoader(), agentClassLoader, e.getMessage(), e);
            return null;
        }
    }

    private ClassLoader getContextClassLoader(Thread thread) {
        try {
            return thread.getContextClassLoader();
        } catch (SecurityException se) {
            throw se;
        } catch (Throwable th) {
            if (isDebug) {
                logger.debug("getContextClassLoader(). Caused:{}", th.getMessage(), th);
            }
            return null;
        }
    }


    private ModifierRegistry createModifierRegistry() {
        DefaultModifierRegistry modifierRepository = new DefaultModifierRegistry(agent, byteCodeInstrumentor);

        modifierRepository.addMethodModifier();

        modifierRepository.addTomcatModifier();

        // jdbc
        modifierRepository.addJdbcModifier();

        // rpc
        modifierRepository.addConnectorModifier();

        // arcus, memcached
        modifierRepository.addArcusModifier();

        // bloc
        modifierRepository.addBLOCModifier();

        // npc
        modifierRepository.addNpcModifier();
        
        // nimm
        modifierRepository.addNimmModifier();
        
        // lucy-net
        modifierRepository.addLucyNetModifier();
        
        return modifierRepository;
    }

}
