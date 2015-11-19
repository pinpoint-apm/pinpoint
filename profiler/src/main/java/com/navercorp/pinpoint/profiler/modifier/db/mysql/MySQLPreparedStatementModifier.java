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

package com.navercorp.pinpoint.profiler.modifier.db.mysql;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.profiler.modifier.db.AbstractPreparedStatementModifier;

/**
 * @author emeroad
 */
public class MySQLPreparedStatementModifier extends AbstractPreparedStatementModifier {

    public MySQLPreparedStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent, agent.getProfilerConfig().isJdbcProfileMySqlSqlBindValue());
    }

    @Override
    public String getTargetClass() {
        return "com/mysql/jdbc/PreparedStatement";
    }

    @Override
    protected String getScope() {
        return MYSQLScope.SCOPE_NAME;
    }

}
