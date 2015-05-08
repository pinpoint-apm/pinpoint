/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.modifier.db.oracle;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.BindValueTraceValue;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.ParsingResultTraceValue;
import com.navercorp.pinpoint.profiler.interceptor.GroupDelegateStaticInterceptor;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.PreparedStatementBindVariableInterceptor;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.PreparedStatementExecuteQueryInterceptor;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import com.navercorp.pinpoint.profiler.util.PreparedStatementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For ojdbc library without OraclePreparedStatementWrapper.
 * eg. ojdbc-10.0.x
 * 
 * @author HyunGil Jeong
 */
public class OraclePreparedStatementModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public OraclePreparedStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public Matcher getMatcher() {
        List<String> preparedStatement = Arrays.asList(OracleClassConstants.ORACLE_PREPARED_STATEMENT, OracleClassConstants.ORACLE_PREPARED_STATEMENT_WRAPPER);
        return Matchers.newMultiClassNameMatcher(preparedStatement);
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        // Do not modify if wrapper exists
        if (OracleClassConstants.ORACLE_PREPARED_STATEMENT.equals(javassistClassName)) {
            if (byteCodeInstrumentor.findClass(classLoader, OracleClassConstants.ORACLE_PREPARED_STATEMENT_WRAPPER)) {
                return null;
            }
        }
        return this.modifyStatement(classLoader, javassistClassName, protectedDomain, classFileBuffer);
    }

    public byte[] modifyStatement(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifying. {}", className);
        }
        try {
            InstrumentClass preparedStatement = byteCodeInstrumentor.getClass(classLoader, className, classFileBuffer);

            Interceptor execute = new PreparedStatementExecuteQueryInterceptor();
            preparedStatement.addGroupInterceptor("execute", null, execute, OracleScope.SCOPE_NAME);
            Interceptor executeQuery = new PreparedStatementExecuteQueryInterceptor();
            preparedStatement.addGroupInterceptor("executeQuery", null, executeQuery, OracleScope.SCOPE_NAME);
            Interceptor executeUpdate = new PreparedStatementExecuteQueryInterceptor();
            preparedStatement.addGroupInterceptor("executeUpdate", null, executeUpdate, OracleScope.SCOPE_NAME);

            preparedStatement.addTraceValue(DatabaseInfoTraceValue.class);
            preparedStatement.addTraceValue(ParsingResultTraceValue.class);
            preparedStatement.addTraceValue(BindValueTraceValue.class, "new java.util.HashMap();");
            bindVariableIntercept(preparedStatement, classLoader, protectedDomain);

            return preparedStatement.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
            }
            return null;
        }
    }

    private void bindVariableIntercept(InstrumentClass preparedStatement, ClassLoader classLoader, ProtectionDomain protectedDomain) throws InstrumentException {
        List<Method> bindMethod = PreparedStatementUtils.findBindVariableSetMethod();
        final InterceptorGroupInvocation scope = byteCodeInstrumentor.getInterceptorGroupTransaction(OracleScope.SCOPE_NAME);
        Interceptor interceptor = new GroupDelegateStaticInterceptor(new PreparedStatementBindVariableInterceptor(), scope);
        int interceptorId = -1;
        for (Method method : bindMethod) {
            String methodName = method.getName();
            String[] parameterType = JavaAssistUtils.getParameterType(method.getParameterTypes());
            try {
                if (interceptorId == -1) {
                    interceptorId = preparedStatement.addInterceptor(methodName, parameterType, interceptor);
                } else {
                    preparedStatement.reuseInterceptor(methodName, parameterType, interceptorId);
                }
            } catch (NotFoundInstrumentException e) {
                // Cannot find bind variable setter method. This is not an error. logging will be enough.
                if (logger.isDebugEnabled()) {
                    logger.debug("bindVariable api not found. method:{} param:{} Cause:{}", methodName, Arrays.toString(parameterType), e.getMessage());
                }
            }
        }
    }

}
