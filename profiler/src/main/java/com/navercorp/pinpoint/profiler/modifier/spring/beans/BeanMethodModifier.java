package com.nhn.pinpoint.profiler.modifier.spring.beans;

import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

import javassist.bytecode.AccessFlag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.MethodFilter;
import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.modifier.Modifier;
import com.nhn.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor;

/**
 * 
 * @author netspider
 * @author emeroad
 * 
 */
public class BeanMethodModifier implements Modifier {
    static final MethodFilter METHOD_FILTER = new MethodFilter() {
        private static final int REQUIRED_ACCESS_FLAG = AccessFlag.PUBLIC;
        private static final int REJECTED_ACCESS_FLAG = AccessFlag.ABSTRACT | AccessFlag.BRIDGE | AccessFlag.NATIVE | AccessFlag.PRIVATE |
                AccessFlag.PROTECTED | AccessFlag.SYNTHETIC | AccessFlag.STATIC;

        @Override
        public boolean filter(MethodInfo ctMethod) {
            if (ctMethod.isConstructor()) {
                return false;
            }

            int access = ctMethod.getModifiers();

            return ((access & REQUIRED_ACCESS_FLAG) == 0) || ((access & REJECTED_ACCESS_FLAG) != 0);
        }
    };

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ByteCodeInstrumentor byteCodeInstrumentor;

    public BeanMethodModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
        this.byteCodeInstrumentor = byteCodeInstrumentor;
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modify {}", javassistClassName);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

        try {
            InstrumentClass clazz = byteCodeInstrumentor.getClass(javassistClassName);

            if (!clazz.isInterceptable()) {
                return null;
            }

            List<MethodInfo> methodList = clazz.getDeclaredMethods(METHOD_FILTER);
            for (MethodInfo method : methodList) {
                if (logger.isTraceEnabled()) {
                    logger.trace("### c={}, m={}, params={}", javassistClassName, method.getName(), Arrays.toString(method.getParameterTypes()));
                }

                MethodInterceptor interceptor = new MethodInterceptor();
                interceptor.setServiceType(ServiceType.SPRING_BEAN);
                
                clazz.addInterceptor(method.getName(), method.getParameterTypes(), interceptor);
            }

            return clazz.toBytecode();
        } catch (Exception e) {
            logger.warn("modify fail. Cause:{}", e.getMessage(), e);
            return null;
        }
    }
}