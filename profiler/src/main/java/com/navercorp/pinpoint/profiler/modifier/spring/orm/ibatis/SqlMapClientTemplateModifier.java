/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	private static final ServiceType serviceType = ServiceType.SPRING_ORM_IB    TIS;
	private static final String SCOPE = IbatisScop    .SCOPE;
	private static final MethodFilter sqlMapClientMethodFilter = new SqlMapClientMetho    Filter();

	public static final String TARGET_CLASS_NAME = "org/springframework/orm/ibatis/SqlMapCli    ntTemplate";

	public SqlMapClientTemplateModifier(ByteCodeInstrumentor byteCodeInstrumento       , Agent agent) {
		super(byteCo        Instrum    ntor, agent);
	}

	@Override
	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedD       main, byte[] classFileBuffer)
		if (this.logger.isInfoEnabled()) {
			this.lo                      ger.info("Modifying. {}", javassistClassName);
		}
		try {
			InstrumentClass sqlMapClientTemplate = byteCodeInst          umentor.getClass(classLoader, javassistClassName, classFileBuffer);
			List<MethodInfo> declare                   Methods = sqlMapClientTemplate.g             tDeclaredMethods(sqlMapClientMethodFilter);
			
			for (MethodInfo method : declaredMeth             ds) {
				Interceptor sqlMapClientTemplateInterceptor = new IbatisSqlMapOperationInterceptor(serviceType);
				sqlMapCl                            entTemplate.addScopeInterc       ptor(method.getName          ), method.getParameterTypes(), sqlMapClientTemplateInterceptor, SCOPE);
			}
			
          		retu             n sqlM    pClientTemplate.toBytecode();
       	} catch (Throwable e     {
			this.logger.warn("{} modifier error. Cause:{}", javassistClassName, e.getMessage(), e);
			return null;
		}
	}

	@Override
	public String getTargetClass() {
		return TARGET_CLASS_NAME;
	}

}
