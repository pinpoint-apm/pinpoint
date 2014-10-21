package com.nhn.pinpoint.profiler.modifier.spring.beans;

import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

import javassist.CtMethod;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.MethodInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.Method;
import com.nhn.pinpoint.profiler.interceptor.bci.MethodFilter;
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
        public boolean filter(CtMethod ctMethod) {
            MethodInfo methodInfo = ctMethod.getMethodInfo();

            if (methodInfo.isConstructor()) {
                return false;
            }

            int access = methodInfo.getAccessFlags();

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

            List<Method> methodList = clazz.getDeclaredMethods(METHOD_FILTER);
            for (Method method : methodList) {
                if (logger.isTraceEnabled()) {
                    logger.trace("### c={}, m={}, params={}", javassistClassName, method.getMethodName(), Arrays.toString(method.getMethodParams()));
                }

                Interceptor interceptor = new MethodInterceptor();
                clazz.addInterceptor(method.getMethodName(), method.getMethodParams(), interceptor);
            }

            return clazz.toBytecode();
        } catch (Exception e) {
            logger.warn("modify fail. Cause:{}", e.getMessage(), e);
            return null;
        }
    }
}