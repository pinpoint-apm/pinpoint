package com.navercorp.pinpoint.profiler.modifier.orm.mybatis;

import java.security.ProtectionDomain;
import java.util.List;

import org.slf4j.Logger;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.orm.mybatis.filter.SqlSessionMethodFilter;
import com.navercorp.pinpoint.profiler.modifier.orm.mybatis.interceptor.MyBatisScope;
import com.navercorp.pinpoint.profiler.modifier.orm.mybatis.interceptor.MyBatisSqlMapOperationInterceptor;
import com.navercorp.pinpoint.profiler.util.DepthScope;

/**
 * @author Hyun Jeong
 */
public abstract class MyBatisClientModifier extends AbstractModifier {

	private static final ServiceType serviceType = ServiceType.MYBATIS;
	private static final DepthScope scope = MyBatisScope.SCOPE;
	private static final MethodFilter sqlSessionMethodFilter = new SqlSessionMethodFilter();
    protected Logger logger;


    protected MethodFilter getSqlSessionMethodFilter() {
		return sqlSessionMethodFilter;
	}
	
	public MyBatisClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	@Override
	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
            logger.info("Modifying. {}", javassistClassName);
		}
		try {
			InstrumentClass myBatisClientImpl = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);
			List<MethodInfo> declaredMethods = myBatisClientImpl.getDeclaredMethods(getSqlSessionMethodFilter());			
			for (MethodInfo method : declaredMethods) {
				Interceptor sqlSessionInterceptor = new MyBatisSqlMapOperationInterceptor(serviceType);
				myBatisClientImpl.addScopeInterceptor(method.getName(), method.getParameterTypes(), sqlSessionInterceptor, scope);
			}
			
			return myBatisClientImpl.toBytecode();
		} catch (Throwable e) {
            logger.warn("{} modifier error. Cause:{}", javassistClassName, e.getMessage(), e);
			return null;
		}
	}

	
}
