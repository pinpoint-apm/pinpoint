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

package com.navercorp.pinpoint.profiler.modifier.db.cubrid;

import java.security.ProtectionDomain;

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

/**
 * @author emeroad
 */
public class CubridStatementModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public CubridStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "cubrid/jdbc/driver/CUBRIDStatement";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }
        try {
            InstrumentClass statementClass = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

            Interceptor executeQueryInterceptor = new StatementExecuteQueryInterceptor();
            statementClass.addGroupInterceptor("executeQuery", new String[]{"java.lang.String"}, executeQueryInterceptor, CubridScope.SCOPE_NAME);

            Interceptor executeUpdateInterceptor1 = new StatementExecuteUpdateInterceptor();
            statementClass.addGroupInterceptor("executeUpdate", new String[]{"java.lang.String"}, executeUpdateInterceptor1, CubridScope.SCOPE_NAME);

            Interceptor executeUpdateInterceptor2 = new StatementExecuteUpdateInterceptor();
            statementClass.addGroupInterceptor("executeUpdate", new String[]{"java.lang.String", "int"}, executeUpdateInterceptor2, CubridScope.SCOPE_NAME);

            Interceptor executeInterceptor1 = new StatementExecuteUpdateInterceptor();
            statementClass.addGroupInterceptor("execute", new String[]{"java.lang.String"}, executeInterceptor1, CubridScope.SCOPE_NAME);

            Interceptor executeInterceptor2 = new StatementExecuteUpdateInterceptor();
            statementClass.addGroupInterceptor("execute", new String[]{"java.lang.String", "int"}, executeInterceptor2, CubridScope.SCOPE_NAME);

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
