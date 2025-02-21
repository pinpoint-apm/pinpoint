/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mysql;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.SetDatabaseInfoInterceptor;

import java.util.List;

import static com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants.UNKNOWN_DATABASE;

public class AsyncerInitFlowInitHandshakeInterceptor extends SetDatabaseInfoInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public DatabaseInfo getDatabaseInfo(Object target, Object[] args, Object result) {
        final DatabaseInfoAccessor databaseInfoAccessor = ArrayArgumentUtils.getArgument(args, 0, DatabaseInfoAccessor.class);
        if (databaseInfoAccessor == null) {
            return null;
        }

        String database = ArrayArgumentUtils.getArgument(args, 2, String.class);
        if (database == null) {
            database = UNKNOWN_DATABASE;
        }

        final DatabaseInfo databaseInfo = databaseInfoAccessor._$PINPOINT$_getDatabaseInfo();
        if (databaseInfo == null) {
            return null;
        }

        final List<String> hostList = databaseInfo.getHost();
        final DatabaseInfo updatedDatabaseInfo = new DefaultDatabaseInfo(SpringDataR2dbcConstants.SPRING_DATA_R2DBC_MYSQL, SpringDataR2dbcConstants.SPRING_DATA_R2DBC_MYSQL_EXECUTE_QUERY, null, null, hostList, database);
        databaseInfoAccessor._$PINPOINT$_setDatabaseInfo(updatedDatabaseInfo);
        if (isDebug) {
            logger.debug("Update databaseInfo={}", updatedDatabaseInfo);
        }

        return updatedDatabaseInfo;
    }

    @Override
    public boolean setDatabaseInfo(DatabaseInfo databaseInfo, Object target, Object[] args, Object result) {
        return false;
    }
}