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
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.profiler.modifier.db.AbstractPreparedStatementModifier;
import com.navercorp.pinpoint.profiler.util.*;


import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 */
public class MySQLPreparedStatementJDBC4Modifier extends AbstractPreparedStatementModifier {

    public MySQLPreparedStatementJDBC4Modifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent, agent.getProfilerConfig().isJdbcProfileMySqlSqlBindValue());
    }

    @Override
    public String getTargetClass() {
        return "com/mysql/jdbc/JDBC4PreparedStatement";
    }

    @Override
    protected String getScope() {
        return MYSQLScope.SCOPE_NAME;
    }

    @Override
    protected List<String> getMethodsToIntercept() {
        // return nothing (taken care of in the parent class)
        return Collections.emptyList();
    }

    @Override
    protected void addTraceValues(InstrumentClass preparedStatementClass) throws InstrumentException {
        // do nothing (taken care of in the parent class)
        return;
    }

    @Override
    protected List<Method> getBindMethods() {
        BindVariableFilter excludedMethods = new IncludeBindVariableFilter(new String[]{"setRowId", "setNClob", "setSQLXML"});
        return PreparedStatementUtils.findBindVariableSetMethod(excludedMethods);
    }

}
