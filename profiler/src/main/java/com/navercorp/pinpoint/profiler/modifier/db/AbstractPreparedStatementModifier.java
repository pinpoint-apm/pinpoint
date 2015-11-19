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

package com.navercorp.pinpoint.profiler.modifier.db;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.BindValueTraceValue;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.ParsingResultTraceValue;
import com.navercorp.pinpoint.profiler.interceptor.ScopeDelegateStaticInterceptor;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.PreparedStatementBindVariableInterceptor;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.PreparedStatementExecuteQueryInterceptor;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import com.navercorp.pinpoint.profiler.util.PreparedStatementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public abstract class AbstractPreparedStatementModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final boolean traceBindValue;

    public AbstractPreparedStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent, boolean traceBindValue) {
        super(byteCodeInstrumentor, agent);
        this.traceBindValue = traceBindValue;
    }

    protected abstract String getScope();

    @Override
    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifying. {}", javassistClassName);
        }
        try {
            InstrumentClass preparedStatementClass = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);
            String scope = this.getScope();

            for (String methodToIntercept : getMethodsToIntercept()) {
                Interceptor executeMethodInterceptor = new PreparedStatementExecuteQueryInterceptor();
                preparedStatementClass.addScopeInterceptor(methodToIntercept, null, executeMethodInterceptor, scope);
            }

            addTraceValues(preparedStatementClass);

            if (this.traceBindValue) {
                bindVariableInterceptors(preparedStatementClass);
            }

            return preparedStatementClass.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
            }
            return null;
        }
    }

    protected List<String> getMethodsToIntercept() {
        return Arrays.asList("execute", "executeQuery", "executeUpdate");
    }

    protected void addTraceValues(InstrumentClass preparedStatementClass) throws InstrumentException {
        preparedStatementClass.addTraceValue(DatabaseInfoTraceValue.class);
        preparedStatementClass.addTraceValue(ParsingResultTraceValue.class);
        preparedStatementClass.addTraceValue(BindValueTraceValue.class, "new java.util.HashMap();");
    }

    protected List<Method> getBindMethods() {
        return PreparedStatementUtils.findBindVariableSetMethod();
    }

    protected void bindVariableInterceptors(InstrumentClass preparedStatement) throws InstrumentException {
        List<Method> bindMethods = getBindMethods();
        final Scope scope = super.byteCodeInstrumentor.getScope(this.getScope());
        Interceptor interceptor = new ScopeDelegateStaticInterceptor(new PreparedStatementBindVariableInterceptor(), scope);
        int interceptorId = -1;
        for (Method method : bindMethods) {
            String methodName = method.getName();
            String[] parameterType = JavaAssistUtils.getParameterType(method.getParameterTypes());
            try {
                if (interceptorId == -1) {
                    interceptorId = preparedStatement.addInterceptor(methodName, parameterType, interceptor);
                } else {
                    preparedStatement.reuseInterceptor(methodName, parameterType, interceptorId);
                }
            } catch (NotFoundInstrumentException e) {
                // Cannot find bind variable setter method. This is not an error. Just some log will be enough.
                if (logger.isDebugEnabled()) {
                    logger.debug("bindVariable api not found. method:{} param:{} Cause:{}", methodName, Arrays.toString(parameterType), e.getMessage());
                }
            }
        }
    }

}
