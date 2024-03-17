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

import com.navercorp.pinpoint.it.plugin.utils.jdbc.DefaultJDBCApi;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCDriverClass;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Objects;


/**
 * @author intr3p1d
 */
public class ClickHouseJDBCApi extends DefaultJDBCApi {

    public ClickHouseJDBCApi(JDBCDriverClass jdbcDriverClass) {
        super(jdbcDriverClass);
    }


    public ConnectionClass getConnection() {
        return new ClickHouseConnectionClass(getJDBCDriverClass());
    }


    public static class ClickHouseConnectionClass extends DefaultJDBCApi.DefaultConnectionClass {

        private final JDBCDriverClass jdbcDriverClass;

        public ClickHouseConnectionClass(JDBCDriverClass jdbcDriverClass) {
            super(jdbcDriverClass);
            this.jdbcDriverClass = Objects.requireNonNull(jdbcDriverClass, "jdbcDriverClass");
        }

        protected Class<Connection> getConnection() {
            return jdbcDriverClass.getConnection();
        }

        @Override
        public Method getPrepareStatement() {
            final Class<Connection> connection = getConnection();
            return getDeclaredMethod(connection, "prepareStatement", String.class, int.class, int.class, int.class);
        }

    }

}
