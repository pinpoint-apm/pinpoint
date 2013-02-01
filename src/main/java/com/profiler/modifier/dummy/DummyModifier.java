package com.profiler.modifier.dummy;

import java.security.ProtectionDomain;
import java.util.logging.Logger;

import com.profiler.Agent;
import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.logging.LoggingUtils;
import com.profiler.modifier.AbstractModifier;

/**
 * @author netspider
 */
public class DummyModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(DummyModifier.class.getName());
    private final boolean isDebug = LoggingUtils.isDebug(logger);

	public DummyModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "com/nhn/hippo/testweb/service/DummyService";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (isDebug) {
			logger.fine("Modifing. " + javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			Interceptor doSomethingInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.method.interceptors.MethodInterceptor");
			Interceptor aInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.method.interceptors.MethodInterceptor");
			Interceptor bInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.method.interceptors.MethodInterceptor");
			Interceptor baInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.method.interceptors.MethodInterceptor");
			Interceptor bbInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.method.interceptors.MethodInterceptor");
			Interceptor baaInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.method.interceptors.MethodInterceptor");
			Interceptor cInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.method.interceptors.MethodInterceptor");
			Interceptor caInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.method.interceptors.MethodInterceptor");

			setTraceContext(doSomethingInterceptor);
			setTraceContext(aInterceptor);
			setTraceContext(bInterceptor);
			setTraceContext(baInterceptor);
			setTraceContext(bbInterceptor);
			setTraceContext(baaInterceptor);
			setTraceContext(cInterceptor);
			setTraceContext(caInterceptor);

			InstrumentClass dummyService = byteCodeInstrumentor.getClass(javassistClassName);

			dummyService.addInterceptor("doSomething", null, doSomethingInterceptor);
			dummyService.addInterceptor("a", null, aInterceptor);
			dummyService.addInterceptor("b", null, bInterceptor);
			dummyService.addInterceptor("ba", null, baInterceptor);
			dummyService.addInterceptor("bb", null, bbInterceptor);
			dummyService.addInterceptor("baa", null, baaInterceptor);
			dummyService.addInterceptor("c", null, cInterceptor);
			dummyService.addInterceptor("ca", null, caInterceptor);

			return dummyService.toBytecode();
		} catch (InstrumentException e) {
			return null;
		}
	}
}