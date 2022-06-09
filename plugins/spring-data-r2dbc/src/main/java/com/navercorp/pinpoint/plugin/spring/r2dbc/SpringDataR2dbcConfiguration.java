/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.r2dbc;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcConfig;

public class SpringDataR2dbcConfiguration {

    private final boolean enabled;
    private final MssqlConfig mssqlConfig;
    private final OracleConfig oracleConfig;
    private final MariadbConfig mariadbConfig;
    private final MysqlConfig mysqlConfig;
    private final H2Config h2Config;
    private final PostgresqlConfig postgresqlConfig;

    public SpringDataR2dbcConfiguration(ProfilerConfig config) {
        this.enabled = config.readBoolean("profiler.spring.data.r2dbc.enable", true);

        this.mssqlConfig = new MssqlConfig(config.readBoolean("profiler.jdbc.mssql", false),
                config.readBoolean("profiler.jdbc.mssql.tracesqlbindvalue", config.isTraceSqlBindValue()),
                config.getMaxSqlBindValueSize());
        this.oracleConfig = new OracleConfig(config.readBoolean("profiler.jdbc.oracle", false),
                config.readBoolean("profiler.jdbc.oracle.tracesqlbindvalue", config.isTraceSqlBindValue()),
                config.getMaxSqlBindValueSize());
        this.mariadbConfig = new MariadbConfig(config.readBoolean("profiler.jdbc.mariadb", false),
                config.readBoolean("profiler.jdbc.mariadb.tracesqlbindvalue", config.isTraceSqlBindValue()),
                config.getMaxSqlBindValueSize());
        this.mysqlConfig = new MysqlConfig(config.readBoolean("profiler.jdbc.mysql", false),
                config.readBoolean("profiler.jdbc.mysql.tracesqlbindvalue", config.isTraceSqlBindValue()),
                config.getMaxSqlBindValueSize());
        this.h2Config = new H2Config(config.readBoolean("profiler.jdbc.h2", false),
                config.readBoolean("profiler.jdbc.h2.tracesqlbindvalue", config.isTraceSqlBindValue()),
                config.getMaxSqlBindValueSize());
        this.postgresqlConfig = new PostgresqlConfig(config.readBoolean("profiler.jdbc.postgresql", false),
                config.readBoolean("profiler.jdbc.postgresql.tracesqlbindvalue", config.isTraceSqlBindValue()),
                config.getMaxSqlBindValueSize());
    }

    public JdbcConfig getMssqlConfig() {
        return this.mssqlConfig;
    }

    public JdbcConfig getOracleConfig() {
        return oracleConfig;
    }

    public JdbcConfig getMariadbConfig() {
        return mariadbConfig;
    }

    public JdbcConfig getMysqlConfig() {
        return mysqlConfig;
    }

    public JdbcConfig getH2Config() {
        return h2Config;
    }

    public JdbcConfig getPostgresqlConfig() {
        return postgresqlConfig;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "SpringDataR2dbcConfiguration{" +
                "enabled=" + enabled +
                ", mssqlConfig=" + mssqlConfig +
                ", oracleConfig=" + oracleConfig +
                ", mariadbConfig=" + mariadbConfig +
                ", mysqlConfig=" + mysqlConfig +
                ", h2Config=" + h2Config +
                ", postgresqlConfig=" + postgresqlConfig +
                '}';
    }

    static class MssqlConfig extends JdbcConfig {
        public MssqlConfig(boolean pluginEnable, boolean traceSqlBindValue, int maxSqlBindValue) {
            super(pluginEnable, traceSqlBindValue, maxSqlBindValue);
        }
    }

    static class OracleConfig extends JdbcConfig {
        public OracleConfig(boolean pluginEnable, boolean traceSqlBindValue, int maxSqlBindValue) {
            super(pluginEnable, traceSqlBindValue, maxSqlBindValue);
        }
    }

    static class MariadbConfig extends JdbcConfig {
        public MariadbConfig(boolean pluginEnable, boolean traceSqlBindValue, int maxSqlBindValue) {
            super(pluginEnable, traceSqlBindValue, maxSqlBindValue);
        }
    }

    static class MysqlConfig extends JdbcConfig {
        public MysqlConfig(boolean pluginEnable, boolean traceSqlBindValue, int maxSqlBindValue) {
            super(pluginEnable, traceSqlBindValue, maxSqlBindValue);
        }
    }

    static class H2Config extends JdbcConfig {
        public H2Config(boolean pluginEnable, boolean traceSqlBindValue, int maxSqlBindValue) {
            super(pluginEnable, traceSqlBindValue, maxSqlBindValue);
        }
    }

    static class PostgresqlConfig extends JdbcConfig {
        public PostgresqlConfig(boolean pluginEnable, boolean traceSqlBindValue, int maxSqlBindValue) {
            super(pluginEnable, traceSqlBindValue, maxSqlBindValue);
        }
    }
}
