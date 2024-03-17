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

package com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.oracle;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.SetDatabaseInfoInterceptor;
import io.r2dbc.spi.ConnectionFactoryOptions;

import java.util.ArrayList;
import java.util.List;

import static com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants.UNKNOWN_DATABASE;

public class OracleConnectionFactoryImplConstructorInterceptor extends SetDatabaseInfoInterceptor {
    @Override
    public DatabaseInfo getDatabaseInfo(Object target, Object[] args, Object result) {
        final ConnectionFactoryOptions connectionFactoryOptions = ArrayArgumentUtils.getArgument(args, 0, ConnectionFactoryOptions.class);
        if (connectionFactoryOptions == null) {
            return null;
        }
        Object host = connectionFactoryOptions.getValue(ConnectionFactoryOptions.HOST);
        if (Boolean.FALSE == (host instanceof String)) {
            return null;
        }
        Object port = connectionFactoryOptions.getValue(ConnectionFactoryOptions.PORT);
        if (Boolean.FALSE == (port instanceof Integer)) {
            return null;
        }
        Object database = connectionFactoryOptions.getValue(ConnectionFactoryOptions.DATABASE);
        if (database == null) {
            database = UNKNOWN_DATABASE;
        }

        final List<String> hostList = new ArrayList<>();
        hostList.add(HostAndPort.toHostAndPortString((String) host, (Integer) port));
        final String databaseId = (String) database;
        final DatabaseInfo databaseInfo = new DefaultDatabaseInfo(SpringDataR2dbcConstants.SPRING_DATA_R2DBC_ORACLE, SpringDataR2dbcConstants.SPRING_DATA_R2DBC_ORACLE_EXECUTE_QUERY, null, null, hostList, databaseId);
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
