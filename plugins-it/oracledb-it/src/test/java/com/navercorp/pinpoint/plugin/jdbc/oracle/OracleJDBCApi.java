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
package com.navercorp.pinpoint.plugin.jdbc.oracle;

import com.navercorp.pinpoint.pluginit.jdbc.DefaultJDBCApi;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCDriverClass;

import java.lang.reflect.Method;
import java.sql.Driver;
import java.util.Objects;
import java.util.Properties;

public class OracleJDBCApi extends DefaultJDBCApi {
    public OracleJDBCApi(JDBCDriverClass jdbcDriverClass) {
        super(jdbcDriverClass);
    }

    @Override
    public OracleDriverClass getDriver() {
        return new OracleJDBCApi.OracleDriverClass(getJDBCDriverClass());
    }


    public static class OracleDriverClass implements DriverClass {
        private final JDBCDriverClass jdbcDriverClass;

        public OracleDriverClass(JDBCDriverClass jdbcDriverClass) {
            this.jdbcDriverClass = Objects.requireNonNull(jdbcDriverClass, "jdbcDriverClass");
        }

        protected Class<Driver> getConnection() {
            return jdbcDriverClass.getDriver();
        }

        @Override
        public Method getConnect() {
            final Class<Driver> connection = getConnection();
            return getDeclaredMethod(connection, "connect", String.class, Properties.class);
        }

        public Method getConnectionWithGssCredential() {
            final Class<Driver> connection = getConnection();
            return getDeclaredMethod(connection, "connect", String.class, Properties.class, org.ietf.jgss.GSSCredential.class);
        }
    }
}
