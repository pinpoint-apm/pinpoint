package com.nhn.pinpoint.profiler.modifier.arcus;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.*;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author netspider
 */
public class MemcachedClientModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(MemcachedClientModifier.class.getName());

	public MemcachedClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "net/spy/memcached/MemcachedClient";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName,
			ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}

		try {
			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

			aClass.addTraceVariable("__serviceCode", "__setServiceCode",
                    "__getServiceCode", "java.lang.String");

			Interceptor addOpInterceptor = byteCodeInstrumentor
					.newInterceptor(classLoader, protectedDomain,
							"com.nhn.pinpoint.profiler.modifier.arcus.interceptor.AddOpInterceptor");
			aClass.addInterceptor("addOp", new String[]{"java.lang.String",
                    "net.spy.memcached.ops.Operation"}, addOpInterceptor,
                    Type.before);

			// 모든 public 메소드에 ApiInterceptor를 적용한다.
			String[] ignored = new String[] { "__", "shutdown" };
			for (Method method : aClass.getDeclaredMethods(new ArcusMethodFilter(ignored))) {
				Interceptor apiInterceptor = byteCodeInstrumentor
						.newInterceptor(classLoader, protectedDomain,
								"com.nhn.pinpoint.profiler.modifier.arcus.interceptor.ApiInterceptor");
				aClass.addInterceptor(method.getMethodName(), method.getMethodParams(), apiInterceptor, Type.around);
			}
			return aClass.toBytecode();
		} catch (Exception e) {
			if (logger.isWarnEnabled()) {
				logger.warn(e.getMessage(), e);
			}
			return null;
		}
	}
}