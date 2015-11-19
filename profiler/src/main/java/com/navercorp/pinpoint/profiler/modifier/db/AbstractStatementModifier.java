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
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.StatementExecuteQueryInterceptor;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.StatementExecuteUpdateInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.ProtectionDomain;

/**
 * @author HyunGil Jeong
 */
public abstract class AbstractStatementModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public AbstractStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    protected abstract String getScope();

    @Override
    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifying. {}", javassistClassName);
        }
        try {
            InstrumentClass statementClass = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

            Interceptor executeQueryInterceptor = new StatementExecuteQueryInterceptor();
            statementClass.addScopeInterceptor("executeQuery", new String[]{"java.lang.String"}, executeQueryInterceptor, this.getScope());

            Interceptor executeUpdateInterceptor1 = new StatementExecuteUpdateInterceptor();
            statementClass.addScopeInterceptor("executeUpdate", new String[]{"java.lang.String"}, executeUpdateInterceptor1, this.getScope());

            Interceptor executeUpdateInterceptor2 = new StatementExecuteUpdateInterceptor();
            statementClass.addScopeInterceptor("executeUpdate", new String[]{"java.lang.String", "int"}, executeUpdateInterceptor2, this.getScope());

            Interceptor executeInterceptor1 = new StatementExecuteUpdateInterceptor();
            statementClass.addScopeInterceptor("execute", new String[]{"java.lang.String"}, executeInterceptor1, this.getScope());

            Interceptor executeInterceptor2 = new StatementExecuteUpdateInterceptor();
            statementClass.addScopeInterceptor("execute", new String[]{"java.lang.String", "int"}, executeInterceptor2, this.getScope());

            statementClass.addTraceValue(DatabaseInfoTraceValue.class);

            return statementClass.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
            }
            return null;
        }
    }

}
