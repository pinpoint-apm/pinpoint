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

package com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.postgresql;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.SetDatabaseInfoInterceptor;

import java.util.Arrays;
import java.util.List;

import static com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants.UNKNOWN_DATABASE;

public class PostgresqlConnectionConfigurationInterceptor extends SetDatabaseInfoInterceptor {

    @Override
    public DatabaseInfo getDatabaseInfo(Object target, Object[] args, Object result) {
        // 0.8.x
        String host = parseHost08(args);
        if (host == null) {
            // 0.9.x
            host = parseHost09(args);
        }

        if (host == null) {
            return null;
        }
        final List<String> hostList = Arrays.asList(host);
        String database = ArrayArgumentUtils.getArgument(args, 4, String.class);
        if (database == null) {
            database = UNKNOWN_DATABASE;
        }
        DatabaseInfo databaseInfo = new DefaultDatabaseInfo(SpringDataR2dbcConstants.SPRING_DATA_R2DBC_POSTGRESQL, SpringDataR2dbcConstants.SPRING_DATA_R2DBC_POSTGRESQL_EXECUTE_QUERY, null, null, hostList, database);
        if (isDebug) {
            logger.debug("Create databaseInfo={}", databaseInfo);
        }
        return databaseInfo;
    }

    @Override
    public boolean setDatabaseInfo(DatabaseInfo databaseInfo, Object target, Object[] args, Object result) {
        if (target instanceof DatabaseInfoAccessor) {
            ((DatabaseInfoAccessor) target)._$PINPOINT$_setDatabaseInfo(databaseInfo);
            return true;
        }
        return false;
    }

    // Parse 0.8.x
    String parseHost08(Object[] args) {
        final String host = ArrayArgumentUtils.getArgument(args, 8, String.class);
        final int port = ArrayArgumentUtils.getArgument(args, 13, Integer.class, -1);
        final String socket = ArrayArgumentUtils.getArgument(args, 17, String.class);

        return parseHost(host, port, socket);
    }

    // Parse 0.9.x
    String parseHost09(Object[] args) {
        final String host = ArrayArgumentUtils.getArgument(args, 9, String.class);
        final int port = ArrayArgumentUtils.getArgument(args, 15, Integer.class, -1);
        final String socket = ArrayArgumentUtils.getArgument(args, 19, String.class);

        return parseHost(host, port, socket);
    }

    String parseHost(String host, int port, String socket) {
        if (host == null && socket == null) {
            return null;
        }
        if (socket != null) {
            return socket;
        }
        if (host == null) {
            return null;
        }

        if (port > 0) {
            return host + ":" + port;
        }
        return host;
    }
}
