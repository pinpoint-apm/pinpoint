package com.profiler.modifier.method;

import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.Agent;
import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.AbstractModifier;

/**
 * 
 * @author netspider
 * 
 */
public class MethodModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(MethodModifier.class.getName());

	public MethodModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "*";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Modifing. " + javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

		try {
			InstrumentClass clazz = byteCodeInstrumentor.getClass(javassistClassName);

			if (!clazz.isInterceptable()) {
				return null;
			}

			CtMethod[] methods = clazz.getDeclaredMethods();

			for (CtMethod m : methods) {
				if (m.isEmpty()) {
					continue;
				}
				
				Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.method.interceptors.MethodInterceptor");
				setTraceContext(interceptor);

				CtClass[] paramClass = m.getParameterTypes();

				String[] params = new String[paramClass.length];
				for (int i = 0; i < paramClass.length; i++) {
					params[i] = paramClass[i].getName();
				}
                if(logger.isLoggable(Level.INFO)) {
                    logger.info("### c=" + javassistClassName + ", m=" + m.getName() + ", params=" + Arrays.toString(params));
                }
				clazz.addInterceptor(m.getName(), params, interceptor);
			}

			return clazz.toBytecode();
		} catch (Exception e) {
			logger.log(Level.WARNING, "modify fail. Cause:" + e.getMessage(), e);
			return null;
		}
	}
}