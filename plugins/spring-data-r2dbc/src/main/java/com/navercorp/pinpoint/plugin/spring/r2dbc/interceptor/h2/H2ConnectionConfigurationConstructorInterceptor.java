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

package com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.h2;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.SetDatabaseInfoInterceptor;

import java.util.List;

public class H2ConnectionConfigurationConstructorInterceptor extends SetDatabaseInfoInterceptor {

    @Override
    public DatabaseInfo getDatabaseInfo(Object target, Object[] args, Object result) {
        final String url = ArrayArgumentUtils.getArgument(args, 1, String.class);
        if (url == null) {
            return null;
        }

        final H2DatabaseInfoParser parser = new H2DatabaseInfoParser(url);
        final List<String> hostList = parser.getHostList();
        final String databaseId = parser.getDatabase();
        final DatabaseInfo databaseInfo = new DefaultDatabaseInfo(SpringDataR2dbcConstants.SPRING_DATA_R2DBC_H2, SpringDataR2dbcConstants.SPRING_DATA_R2DBC_H2_EXECUTE_QUERY, null, null, hostList, databaseId);
        ((DatabaseInfoAccessor) target)._$PINPOINT$_setDatabaseInfo(databaseInfo);
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
