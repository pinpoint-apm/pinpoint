/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.jdbc.postgresql;


import com.navercorp.pinpoint.pluginit.jdbc.DefaultJDBCApi;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCDriverClass;

import java.lang.reflect.Method;
import java.sql.Statement;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PostgreSqlJDBCApi extends DefaultJDBCApi {
    public PostgreSqlJDBCApi(PostgreSqlJDBCDriverClass jdbcDriverClass) {
        super(jdbcDriverClass);
    }

    @Override
    public PostgreSqlStatementClass getStatement() {
        return new PostgreSqlStatementClass(getJDBCDriverClass());
    }

    public static class PostgreSqlStatementClass extends DefaultStatementClass {
        private final PostgreSqlJDBCDriverClass postgreSqlJDBCDriverClass;
        public PostgreSqlStatementClass(JDBCDriverClass jdbcDriverClass) {
            super(jdbcDriverClass);
            postgreSqlJDBCDriverClass = (PostgreSqlJDBCDriverClass) jdbcDriverClass;
        }

        public Method getStatementForExecuteUpdate() {
            Class<Statement> statementForExecuteUpdate = postgreSqlJDBCDriverClass.getStatementForExecuteUpdate();
            return getDeclaredMethod(statementForExecuteUpdate, "executeUpdate", String.class, int.class);
        }
    }
}
