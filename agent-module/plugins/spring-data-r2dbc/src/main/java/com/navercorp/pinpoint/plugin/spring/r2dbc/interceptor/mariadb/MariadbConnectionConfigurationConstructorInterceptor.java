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

package com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mariadb;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.SetDatabaseInfoInterceptor;
import org.mariadb.r2dbc.util.HostAddress;

import java.util.ArrayList;
import java.util.List;

import static com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants.UNKNOWN_DATABASE;

public class MariadbConnectionConfigurationConstructorInterceptor extends SetDatabaseInfoInterceptor {
    @Override
    public DatabaseInfo getDatabaseInfo(Object target, Object[] args, Object result) {
        String database = ArrayArgumentUtils.getArgument(args, 5, String.class);
        if (database == null) {
            database = UNKNOWN_DATABASE;
        }

        final String host = ArrayArgumentUtils.getArgument(args, 6, String.class);
        final Integer port = ArrayArgumentUtils.getArgument(args, 10, Integer.class);
        final List<String> hostList = new ArrayList<>();
        final List<HostAddress> hostAddressList = ArrayArgumentUtils.getArgument(args, 11, List.class);
        if (hostAddressList != null) {
            for (HostAddress hostAddress : hostAddressList) {
                hostList.add(HostAndPort.toHostAndPortString(hostAddress.getHost(), hostAddress.getPort()));
            }
        } else {
            if (host == null || port == null) {
                return null;
            }
            hostList.add(HostAndPort.toHostAndPortString(host, port));
        }

        final DatabaseInfo databaseInfo = new DefaultDatabaseInfo(SpringDataR2dbcConstants.SPRING_DATA_R2DBC_MARIADB, SpringDataR2dbcConstants.SPRING_DATA_R2DBC_MARIADB_EXECUTE_QUERY, null, null, hostList, database);
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
