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

package com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mysql;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.SetDatabaseInfoInterceptor;

import java.util.ArrayList;
import java.util.List;

import static com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants.UNKNOWN_DATABASE;

public class MySqlConnectionConfigurationInterceptor extends SetDatabaseInfoInterceptor {
    @Override
    public DatabaseInfo getDatabaseInfo(Object target, Object[] args, Object result) {
        final Boolean isHost = ArrayArgumentUtils.getArgument(args, 0, Boolean.class);
        if (isHost == null) {
            return null;
        }
        final String domain = ArrayArgumentUtils.getArgument(args, 1, String.class);
        if (domain == null) {
            return null;
        }
        Integer port = ArrayArgumentUtils.getArgument(args, 2, Integer.class);
        if (port == null) {
            port = -1;
        }
        // 0.8.x
        String database = ArrayArgumentUtils.getArgument(args, 11, String.class);
        if (database == null) {
            // 0.9.x
            database = ArrayArgumentUtils.getArgument(args, 12, String.class);
        }
        if (database == null) {
            database = UNKNOWN_DATABASE;
        }

        List<String> hostList = new ArrayList<>();
        if (isHost) {
            hostList.add(HostAndPort.toHostAndPortString(domain, port));
        } else {
            hostList.add(domain);
        }
        final DatabaseInfo databaseInfo = new DefaultDatabaseInfo(SpringDataR2dbcConstants.SPRING_DATA_R2DBC_MYSQL, SpringDataR2dbcConstants.SPRING_DATA_R2DBC_MYSQL_EXECUTE_QUERY, null, null, hostList, database);
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
}
