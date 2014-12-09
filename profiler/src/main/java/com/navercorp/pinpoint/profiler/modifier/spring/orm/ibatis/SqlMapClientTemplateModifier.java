package com.navercorp.pinpoint.profiler.modifier.spring.orm.ibatis;

import java.security.ProtectionDomain;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.orm.ibatis.filter.SqlMapClientMethodFilter;
import com.navercorp.pinpoint.profiler.modifier.orm.ibatis.interceptor.IbatisScope;
import com.navercorp.pinpoint.profiler.modifier.orm.ibatis.interceptor.IbatisSqlMapOperationInterceptor;
import com.navercorp.pinpoint.profiler.util.DepthScope;

/**
 * SqlMapClientTemplate Modifier
 * <p/>
 * Hooks onto <i>org.springframework.orm.ibatis.SqlMapClientTemplate</i>
 * <p/>
 * 
 * @author Hyun Jeong
 * @see com.ibatis.sqlmap.client.SqlMapExecutor
 */
public final class SqlMapClientTemplateModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final ServiceType serviceType = ServiceType.SPRING_ORM_IBATIS;
	private static final DepthScope scope = IbatisScope.SCOPE;
	private static final MethodFilter sqlMapClientMethodFilter = new SqlMapClientMethodFilter();

	public static final String TARGET_CLASS_NAME = "org/springframework/orm/ibatis/SqlMapClientTemplate";

	public SqlMapClientTemplateModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	@Override
	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Modifying. {}", javassistClassName);
		}
		try {
			InstrumentClass sqlMapClientTemplate = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);
			List<MethodInfo> declaredMethods = sqlMapClientTemplate.getDeclaredMethods(sqlMapClientMethodFilter);
			
			for (MethodInfo method : declaredMethods) {
				Interceptor sqlMapClientTemplateInterceptor = new IbatisSqlMapOperationInterceptor(serviceType);
				sqlMapClientTemplate.addScopeInterceptor(method.getName(), method.getParameterTypes(), sqlMapClientTemplateInterceptor, scope);
			}
			
			return sqlMapClientTemplate.toBytecode();
		} catch (Throwable e) {
			this.logger.warn("{} modifier error. Cause:{}", javassistClassName, e.getMessage(), e);
			return null;
		}
	}

	@Override
	public String getTargetClass() {
		return TARGET_CLASS_NAME;
	}

}
