package com.nhn.pinpoint.profiler.modifier.orm.mybatis;

import java.security.ProtectionDomain;
import java.util.List;

import org.slf4j.Logger;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.Method;
import com.nhn.pinpoint.profiler.interceptor.bci.MethodFilter;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.orm.mybatis.filter.SqlSessionMethodFilter;
import com.nhn.pinpoint.profiler.modifier.orm.mybatis.interceptor.MyBatisScope;
import com.nhn.pinpoint.profiler.modifier.orm.mybatis.interceptor.MyBatisSqlMapOperationInterceptor;
import com.nhn.pinpoint.profiler.util.DepthScope;

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
		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			InstrumentClass myBatisClientImpl = byteCodeInstrumentor.getClass(javassistClassName);
			List<Method> declaredMethods = myBatisClientImpl.getDeclaredMethods(getSqlSessionMethodFilter());			
			for (Method method : declaredMethods) {
				Interceptor sqlSessionInterceptor = new MyBatisSqlMapOperationInterceptor(serviceType);
				myBatisClientImpl.addScopeInterceptor(method.getMethodName(), method.getMethodParams(), sqlSessionInterceptor, scope);
			}
			
			return myBatisClientImpl.toBytecode();
		} catch (Throwable e) {
            logger.warn("{} modifier error. Cause:{}", javassistClassName, e.getMessage(), e);
			return null;
		}
	}

	
}
