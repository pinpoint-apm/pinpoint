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

package com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.jasync;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.SetDatabaseInfoInterceptor;

import java.util.ArrayList;
import java.util.List;

import static com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants.UNKNOWN_DATABASE;

public class ConfigurationConstructorInterceptor extends SetDatabaseInfoInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public DatabaseInfo getDatabaseInfo(Object target, Object[] args, Object result) {
        final String host = ArrayArgumentUtils.getArgument(args, 1, String.class);
        if (host == null) {
            return null;
        }
        final Integer port = ArrayArgumentUtils.getArgument(args, 2, Integer.class);
        if (port == null) {
            return null;
        }
        String database = ArrayArgumentUtils.getArgument(args, 4, String.class);
        if (database == null) {
            database = UNKNOWN_DATABASE;
        }

        final List<String> hostList = new ArrayList<>();
        hostList.add(HostAndPort.toHostAndPortString(host, port));
        final DatabaseInfo databaseInfo = new DefaultDatabaseInfo(SpringDataR2dbcConstants.SPRING_DATA_R2DBC_MYSQL, SpringDataR2dbcConstants.SPRING_DATA_R2DBC_MYSQL_EXECUTE_QUERY, null, null, hostList, database);
        if (isDebug) {
            logger.debug("Create databaseInfo={}", databaseInfo);
        }
        return databaseInfo;

    }

    public boolean setDatabaseInfo(DatabaseInfo databaseInfo, Object target, Object[] args, Object result) {
        if (target instanceof DatabaseInfoAccessor) {
            ((DatabaseInfoAccessor) target)._$PINPOINT$_setDatabaseInfo(databaseInfo);
            return true;
        }
        return false;
    }
}
