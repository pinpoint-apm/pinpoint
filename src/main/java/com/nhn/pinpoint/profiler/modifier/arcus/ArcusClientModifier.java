package com.nhn.pinpoint.profiler.modifier.arcus;

import java.security.ProtectionDomain;
import java.util.List;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.*;
import com.nhn.pinpoint.profiler.interceptor.bci.*;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.arcus.interceptor.ArcusScope;
import com.nhn.pinpoint.profiler.modifier.arcus.interceptor.IndexParameterExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author netspider
 * @author emeroad
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
			InstrumentClass arcusClient = byteCodeInstrumentor.getClass(javassistClassName);

			final Interceptor setCacheManagerInterceptor = byteCodeInstrumentor.newInterceptor(classLoader,protectedDomain,"com.nhn.pinpoint.profiler.modifier.arcus.interceptor.SetCacheManagerInterceptor");
            final String[] args = {"net.spy.memcached.CacheManager"};
            arcusClient.addInterceptor("setCacheManager", args, setCacheManagerInterceptor, Type.before);

            List<Method> declaredMethods = arcusClient.getDeclaredMethods(new ArcusMethodFilter());
            for (Method method : declaredMethods) {

                SimpleAroundInterceptor apiInterceptor = (SimpleAroundInterceptor) byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain,
								"com.nhn.pinpoint.profiler.modifier.arcus.interceptor.ApiInterceptor");
                if (agent.getProfilerConfig().isArucsKeyTrace()) {
                    final int index = ParameterUtils.findFirstString(method, 3);
                    if (index != -1) {
                        ((ParameterExtractorSupport)apiInterceptor).setParameterExtractor(new IndexParameterExtractor(index));
                    }
                }
                ScopeDelegateSimpleInterceptor arcusScopeDelegateSimpleInterceptor = new ScopeDelegateSimpleInterceptor(apiInterceptor, ArcusScope.SCOPE);
                arcusClient.addInterceptor(method.getMethodName(), method.getMethodParams(), arcusScopeDelegateSimpleInterceptor, Type.around);
			}

			return arcusClient.toBytecode();
		} catch (Exception e) {
			if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
			}
			return null;
		}
	}


}