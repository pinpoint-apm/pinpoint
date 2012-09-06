package com.profiler.modifier;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.bci.InstrumentException;
import javassist.ClassPool;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;

public abstract class AbstractModifier implements Modifier {

	private final Logger logger = Logger.getLogger(AbstractModifier.class.getName());

	protected final ClassPool classPool;
	protected ByteCodeInstrumentor byteCodeInstrumentor;

	public AbstractModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		this.byteCodeInstrumentor = byteCodeInstrumentor;
		this.classPool = byteCodeInstrumentor.getClassPool();
	}

	public void printClassConvertComplete(String javassistClassName) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info(javassistClassName + " class is converted.");
		}
	}

	public void checkLibrary(ClassLoader classLoader, String javassistClassName) {
		this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
	}

	protected Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN) throws InstrumentException {
		Class<?> aClass = this.byteCodeInstrumentor.defineClass(classLoader, interceptorFQCN, protectedDomain);
		try {
            return (Interceptor) aClass.newInstance();
		} catch (InstantiationException e) {
			throw new InstrumentException(aClass + " instance create fail Cause:" + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new InstrumentException(aClass + " instance create fail Cause:" + e.getMessage(), e);
		}
	}
}
