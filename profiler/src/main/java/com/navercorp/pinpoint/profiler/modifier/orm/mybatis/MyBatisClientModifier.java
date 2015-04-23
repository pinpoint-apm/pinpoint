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

package com.navercorp.pinpoint.profiler.modifier.orm.mybatis;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.orm.mybatis.filter.SqlSessionMethodFilter;
import com.navercorp.pinpoint.profiler.modifier.orm.mybatis.interceptor.MyBatisScope;
import com.navercorp.pinpoint.profiler.modifier.orm.mybatis.interceptor.MyBatisSqlMapOperationInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author Hyun Jeong
 */
public abstract class MyBatisClientModifier extends AbstractModifier {

    private static final ServiceType serviceType = ServiceType.MYBATIS;
    private static final String SCOPE = MyBatisScope.SCOPE;
    private static final MethodFilter sqlSessionMethodFilter = new SqlSessionMethodFilter();

    protected final Logger logger;

    public MyBatisClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent, Class<? extends MyBatisClientModifier> childClazz) {
        super(byteCodeInstrumentor, agent);
        logger = LoggerFactory.getLogger(childClazz);
    }

    protected MethodFilter getSqlSessionMethodFilter() {
        return sqlSessionMethodFilter;
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
                myBatisClientImpl.addGroupInterceptor(method.getName(), method.getParameterTypes(), sqlSessionInterceptor, SCOPE);
            }

            return myBatisClientImpl.toBytecode();
        } catch (Throwable e) {
            logger.warn("{} modifier error. Cause:{}", javassistClassName, e.getMessage(), e);
            return null;
        }
    }


}
