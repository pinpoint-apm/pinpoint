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
public class ArcusClientModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public ArcusClientModifier(ByteCodeInstrumentor byteCodeInstrumentor,
			Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "net/spy/memcached/ArcusClient";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName,
			ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
		}

		try {
			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

			Interceptor setCacheManagerInterceptor = byteCodeInstrumentor
					.newInterceptor(
							classLoader,
							protectedDomain,
							"com.nhn.pinpoint.profiler.modifier.arcus.interceptor.SetCacheManagerInterceptor");
			aClass.addInterceptor("setCacheManager",
					new String[] { "net.spy.memcached.CacheManager" },
					setCacheManagerInterceptor, Type.before);

			// 모든 public 메소드에 ApiInterceptor를 적용한다.
			String[] ignored = new String[] { "__", "shutdown" };
			for (Method method : aClass.getDeclaredMethods(new ArcusMethodFilter(ignored))) {
				Interceptor apiInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain,
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