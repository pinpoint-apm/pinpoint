/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.mongo.interceptor;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.ServerAddress;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.MongoDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.mongo.MongoConstants;

/**
 * @author Community
 */
public class MongoDriverConnectInterceptor2_X implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public MongoDriverConnectInterceptor2_X() {
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logBeforeInterceptor(target, args);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logAfterInterceptor(target, args, result, throwable);
        }

        final boolean success = InterceptorUtils.isSuccess(throwable);
        // Must not check if current transaction is trace target or not. Connection can
        // be made by other thread.

        if (success) {
            if (args == null) {
                return;
            }

            final List<String> hostList = getHostList(args[0]);

            DatabaseInfo databaseInfo = createDatabaseInfo(hostList);

            if (target instanceof DatabaseInfoAccessor) {
                ((DatabaseInfoAccessor) target)._$PINPOINT$_setDatabaseInfo(databaseInfo);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("databaseInfo couldn't be constructed ");
            }
        }
    }

    private void logBeforeInterceptor(Object target, Object[] args) {
        logger.beforeInterceptor(target, args);
    }

    private void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        logger.afterInterceptor(target, args, result, throwable);
    }

    private DatabaseInfo createDatabaseInfo(List<String> hostList) {

        DatabaseInfo databaseInfo = new MongoDatabaseInfo(MongoConstants.MONGODB, MongoConstants.MONGO_EXECUTE_QUERY,
                null, null, hostList, null, null, null, null);

        if (isDebug) {
            logger.debug("parse DatabaseInfo:{}", databaseInfo);
        }

        return databaseInfo;
    }

    private List<String> getHostList(Object args) {
        final List<String> hostList = new ArrayList<String>();
        if (args instanceof List) {
            @SuppressWarnings("unchecked")
            List<ServerAddress> hosts = (List<ServerAddress>) args;
            for (ServerAddress serverAddress : hosts) {
                hostList.add(HostAndPort.toHostAndPortString(serverAddress.getHost(), serverAddress.getPort()));
            }
        } else {
            ServerAddress serverAddress = (ServerAddress) args;
            hostList.add(HostAndPort.toHostAndPortString(serverAddress.getHost(), serverAddress.getPort()));
        }

        return hostList;
    }
}
