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
package com.navercorp.pinpoint.profiler.modifier.db.mssql.sqlserver;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.profiler.modifier.db.mssql.MsSqlResultSetModifier;

/**
 *
 * @author Barney Kim
 */
public class SqlServerResultSetModifier extends MsSqlResultSetModifier {

    /**
     * @param byteCodeInstrumentor
     * @param agent
     */
    public SqlServerResultSetModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.profiler.modifier.AbstractModifier#getTargetClass()
     */
    @Override
    public String getTargetClass() {
        return "com/microsoft/sqlserver/jdbc/SQLServerResultSet";
    }

    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.profiler.modifier.db.Scopeable#getScopeName()
     */
    @Override
    public String getScopeName() {
        return SqlServerScope.SCOPE_NAME;
    }

}
