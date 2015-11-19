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
import com.navercorp.pinpoint.profiler.modifier.db.AbstractPreparedStatementModifier;

/**
 * For ojdbc library without OraclePreparedStatementWrapper.
 * eg. ojdbc-10.0.x
 * 
 * @author HyunGil Jeong
 */
public class OraclePreparedStatementModifier extends AbstractPreparedStatementModifier {

    public OraclePreparedStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent, agent.getProfilerConfig().isJdbcProfileOracleSqlBindValue());
    }

    @Override
    public String getTargetClass() {
        return OracleClassConstants.ORACLE_PREPARED_STATEMENT;
    }

    @Override
    protected String getScope() {
        return OracleScope.SCOPE_NAME;
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        // Do not modify if wrapper exists
        if (byteCodeInstrumentor.findClass(classLoader, OracleClassConstants.ORACLE_PREPARED_STATEMENT_WRAPPER)) {
            return null;
        }
        return super.modify(classLoader, javassistClassName, protectedDomain, classFileBuffer);
    }

}
