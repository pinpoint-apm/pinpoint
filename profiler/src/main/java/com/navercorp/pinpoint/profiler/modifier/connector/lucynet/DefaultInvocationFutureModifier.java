package com.nhn.pinpoint.profiler.modifier.connector.lucynet;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.modifier.DedicatedModifier;
import com.nhn.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor;

/**
 * target lib = lucy-net-1.5.4
 * 
 * @author netspider
 * 
 */
public class DefaultInvocationFutureModifier extends DedicatedModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public DefaultInvocationFutureModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "com/nhncorp/lucy/net/invoker/DefaultInvocationFuture";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

			// FIXME 이렇게 하면 api type이 internal method로 보이는데 사실 NPC_CLIENT, NIMM_CLIENT로 보여야함. servicetype으로 넣기에 애매해서. 어떻게 수정할 것인지는 나중에 고민.
			aClass.addInterceptor("getReturnValue", null, new MethodInterceptor());
			aClass.addInterceptor("get", null, new MethodInterceptor());

			return aClass.toBytecode();
		} catch (Throwable e) {
			logger.warn("NimmInvoker modifier error. Caused:{}", e.getMessage(), e);
			return null;
		}
	}
}