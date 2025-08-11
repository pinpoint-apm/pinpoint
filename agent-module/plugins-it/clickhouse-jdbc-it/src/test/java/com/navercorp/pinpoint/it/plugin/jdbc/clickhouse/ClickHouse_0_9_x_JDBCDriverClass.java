/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.jdbc.clickhouse;

import com.navercorp.pinpoint.it.plugin.utils.jdbc.AbstractJDBCDriverClass;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class ClickHouse_0_9_x_JDBCDriverClass extends AbstractJDBCDriverClass {
    @Override
    public Class<Driver> getDriver() {
        return forName("com.clickhouse.jdbc.Driver");
    }

    @Override
    public Class<Connection> getConnection() {
        return forName("com.clickhouse.jdbc.ConnectionImpl");
    }

    @Override
    public Class<Statement> getStatement() {
        return forName("com.clickhouse.jdbc.StatementImpl");
    }

    @Override
    public Class<PreparedStatement> getPreparedStatement() {
        return forName("com.clickhouse.jdbc.PreparedStatementImpl");
    }

    @Override
    public Class<CallableStatement> getCallableStatement() {
        // Unsupported
        return null;
    }
}
