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

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeProvider;

public final class SpringDataR2dbcConstants {
    private SpringDataR2dbcConstants() {
    }

    public static final String UNKNOWN_DATABASE = "unknown";

    public static final ServiceType SPRING_DATA_R2DBC = ServiceTypeProvider.getByName("SPRING_DATA_R2DBC");
    public static final ServiceType SPRING_DATA_R2DBC_MSSQL_JDBC = ServiceTypeProvider.getByName("R2DBC_MSSQL_JDBC");
    public static final ServiceType SPRING_DATA_R2DBC_MSSQL_JDBC_EXECUTE_QUERY = ServiceTypeProvider.getByName("R2DBC_MSSQL_JDBC_EXECUTE_QUERY");
    public static final ServiceType SPRING_DATA_R2DBC_ORACLE = ServiceTypeProvider.getByName("R2DBC_ORACLE");
    public static final ServiceType SPRING_DATA_R2DBC_ORACLE_EXECUTE_QUERY = ServiceTypeProvider.getByName("R2DBC_ORACLE_EXECUTE_QUERY");
    public static final ServiceType SPRING_DATA_R2DBC_MARIADB = ServiceTypeProvider.getByName("R2DBC_MARIADB");
    public static final ServiceType SPRING_DATA_R2DBC_MARIADB_EXECUTE_QUERY = ServiceTypeProvider.getByName("R2DBC_MARIADB_EXECUTE_QUERY");
    public static final ServiceType SPRING_DATA_R2DBC_MYSQL = ServiceTypeProvider.getByName("R2DBC_MYSQL");
    public static final ServiceType SPRING_DATA_R2DBC_MYSQL_EXECUTE_QUERY = ServiceTypeProvider.getByName("R2DBC_MYSQL_EXECUTE_QUERY");
    public static final ServiceType SPRING_DATA_R2DBC_H2 = ServiceTypeProvider.getByName("R2DBC_H2");
    public static final ServiceType SPRING_DATA_R2DBC_H2_EXECUTE_QUERY = ServiceTypeProvider.getByName("R2DBC_H2_EXECUTE_QUERY");
    public static final ServiceType SPRING_DATA_R2DBC_POSTGRESQL = ServiceTypeProvider.getByName("R2DBC_POSTGRESQL");
    public static final ServiceType SPRING_DATA_R2DBC_POSTGRESQL_EXECUTE_QUERY = ServiceTypeProvider.getByName("R2DBC_POSTGRESQL_EXECUTE_QUERY");

}
