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

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentableClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.StatementExecuteQueryInterceptor;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.StatementExecuteUpdateInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For ojdbc library without OracleStatementWrapper.
 * eg. ojdbc-10.0.x
 * 
 * @author HyunGil Jeong
 */
public class OracleStatementModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public OracleStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public Matcher getMatcher() {
        return Matchers.newMultiClassNameMatcher(OracleClassConstants.ORACLE_STATEMENT, OracleClassConstants.ORACLE_STATEMENT_WRAPPER);
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
     // Do not modify if wrapper exists
        if(OracleClassConstants.ORACLE_STATEMENT.equals(javassistClassName)) {
            if (byteCodeInstrumentor.findClass(classLoader, OracleClassConstants.ORACLE_STATEMENT_WRAPPER)) {
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
            InstrumentableClass statementClass = byteCodeInstrumentor.getClass(classLoader, className, classFileBuffer);
            Interceptor executeQuery = new StatementExecuteQueryInterceptor();
            statementClass.addGroupInterceptor("executeQuery", new String[]{"java.lang.String"}, executeQuery, OracleScope.SCOPE_NAME);

            // FIXME
            Interceptor executeUpdateInterceptor1 = new StatementExecuteUpdateInterceptor();
            statementClass.addGroupInterceptor("executeUpdate", new String[]{"java.lang.String"}, executeUpdateInterceptor1, OracleScope.SCOPE_NAME);


            Interceptor executeUpdateInterceptor2 = new StatementExecuteUpdateInterceptor();
            statementClass.addGroupInterceptor("executeUpdate", new String[]{"java.lang.String", "int"}, executeUpdateInterceptor2, OracleScope.SCOPE_NAME);

            Interceptor executeInterceptor1 = new StatementExecuteUpdateInterceptor();
            statementClass.addGroupInterceptor("execute", new String[]{"java.lang.String"}, executeInterceptor1, OracleScope.SCOPE_NAME);

            Interceptor executeInterceptor2 = new StatementExecuteUpdateInterceptor();
            statementClass.addGroupInterceptor("execute", new String[]{"java.lang.String", "int"}, executeInterceptor2, OracleScope.SCOPE_NAME);

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
