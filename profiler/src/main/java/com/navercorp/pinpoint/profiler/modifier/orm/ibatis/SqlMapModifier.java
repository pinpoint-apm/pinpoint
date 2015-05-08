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

package com.navercorp.pinpoint.profiler.modifier.orm.ibatis;

import java.security.ProtectionDomain;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.profiler.modifier.orm.ibatis.filter.SqlMapClientMethodFilter;
import com.navercorp.pinpoint.profiler.modifier.orm.ibatis.filter.SqlMapSessionMethodFilter;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.slf4j.Logger;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.orm.ibatis.interceptor.IbatisScope;
import com.navercorp.pinpoint.profiler.modifier.orm.ibatis.interceptor.IbatisSqlMapOperationInterceptor;
import org.slf4j.LoggerFactory;

/**
 * Base class for modifying iBatis client classes
 *  
 * @author Hyun Jeong
 */
public class SqlMapModifier extends AbstractModifier {

    private static final ServiceType serviceType = ServiceType.IBATIS;
    private static final String SCOPE = IbatisScope.SCOPE;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String SQLMAP_CLIENT_CLASS_NAME = "com/ibatis/sqlmap/engine/impl/SqlMapClientImpl";

    public static final String SQLMAP_SESSION_CLASS_NAME = "com/ibatis/sqlmap/engine/impl/SqlMapSessionImpl";

    public SqlMapModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifying. {}", javassistClassName);
        }
        try {
            InstrumentClass ibatisClientImpl = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);
            final MethodFilter ibatisApiMethodFilter = getIbatisApiMethodFilter(javassistClassName);
            List<MethodInfo> declaredMethods = ibatisClientImpl.getDeclaredMethods(ibatisApiMethodFilter);

            for (MethodInfo method : declaredMethods) {
                Interceptor ibatisApiInterceptor = new IbatisSqlMapOperationInterceptor(serviceType);
                ibatisClientImpl.addGroupInterceptor(method.getName(), method.getParameterTypes(), ibatisApiInterceptor, SCOPE);
            }

            return ibatisClientImpl.toBytecode();
        } catch (Throwable e) {
            this.logger.warn("{} modifier error. Cause:{}", javassistClassName, e.getMessage(), e);
            return null;
        }
    }

    private MethodFilter getIbatisApiMethodFilter(String className) {
        final String jvmClassName = JavaAssistUtils.javaNameToJvmName(className);
        if (SQLMAP_CLIENT_CLASS_NAME.equals(jvmClassName)) {
            return new SqlMapClientMethodFilter();
        }
        if (SQLMAP_SESSION_CLASS_NAME.equals(jvmClassName)) {
            return new SqlMapSessionMethodFilter();
        }
        throw new IllegalArgumentException("unsupported className:" + className);
    }

    @Override
    public Matcher getMatcher() {
        return Matchers.newMultiClassNameMatcher(SQLMAP_CLIENT_CLASS_NAME, SQLMAP_SESSION_CLASS_NAME);
    }

}
