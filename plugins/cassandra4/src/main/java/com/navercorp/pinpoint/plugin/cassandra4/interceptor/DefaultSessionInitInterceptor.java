/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.cassandra4.interceptor;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessorUtils;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.cassandra4.CassandraConstants;
import com.navercorp.pinpoint.plugin.cassandra4.HostListAccessor;

import java.util.List;

public class DefaultSessionInitInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (throwable != null) {
            return;
        }

        if (Boolean.FALSE == (target instanceof HostListAccessor)) {
            return;
        }

        final List<String> hostList = ((HostListAccessor) target)._$PINPOINT$_getHostList();
        if (hostList == null) {
            return;
        }

        try {
            String keyspace = "UNKNOWN";
            final CqlIdentifier cqlIdentifier = ArrayArgumentUtils.getArgument(args, 0, CqlIdentifier.class);
            if (cqlIdentifier != null) {
                keyspace = cqlIdentifier.asInternal();
            }
            // Set databaseInfo
            final DatabaseInfo databaseInfo = createDatabaseInfo(hostList, keyspace);
            DatabaseInfoAccessorUtils.setDatabaseInfo(databaseInfo, target);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        }
    }

    private DatabaseInfo createDatabaseInfo(List<String> hostList, String keyspace) {
        DatabaseInfo databaseInfo = new DefaultDatabaseInfo(CassandraConstants.CASSANDRA, CassandraConstants.CASSANDRA_EXECUTE_QUERY, null, null, hostList, keyspace);
        return databaseInfo;
    }
}