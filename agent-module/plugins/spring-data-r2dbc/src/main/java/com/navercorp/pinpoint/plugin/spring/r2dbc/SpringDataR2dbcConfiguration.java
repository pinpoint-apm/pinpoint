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
    private final JdbcConfig mssqlConfig;
    private final JdbcConfig oracleConfig;
    private final JdbcConfig mariadbConfig;
    private final JdbcConfig mysqlConfig;
    private final JdbcConfig h2Config;
    private final JdbcConfig postgresqlConfig;

    public SpringDataR2dbcConfiguration(ProfilerConfig config) {
        this.enabled = config.readBoolean("profiler.spring.data.r2dbc.enable", true);

        this.mssqlConfig = JdbcConfig.of("mssql", config);
        this.oracleConfig = JdbcConfig.of("oracle", config);
        this.mariadbConfig = JdbcConfig.of("mariadb", config);
        this.mysqlConfig = JdbcConfig.of("mysql", config);
        this.h2Config = JdbcConfig.of("h2", config);
        this.postgresqlConfig = JdbcConfig.of("postgresql", config);
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
}
