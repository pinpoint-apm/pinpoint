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

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.MongoDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
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

            DatabaseInfo databaseInfo = null;
            List<String> emptyList = new ArrayList<String>();
            if (args[0] instanceof String) {
                final List<String> hostList = getHostList(args);
                databaseInfo = createDatabaseInfo(hostList, "", "");
            } else if (args[0] instanceof ReadPreference) {
                DatabaseInfo dbInfo = getDbInfo(target);
                ReadPreference readPreference = (ReadPreference) args[0];
                if (dbInfo == null) {
                    dbInfo = createDatabaseInfo(emptyList, readPreference.getName(), "");
                }
                databaseInfo = cloneDatabaseInfoWithReadPreference(dbInfo, readPreference);
            } else if (args[0] instanceof WriteConcern) {
                DatabaseInfo dbInfo = getDbInfo(target);
                WriteConcern writeConcern = (WriteConcern) args[0];
                if (dbInfo == null) {
                    dbInfo = createDatabaseInfo(emptyList, "", writeConcern.getWString());
                }
                databaseInfo = cloneDatabaseInfoWithWriteConcern(dbInfo, (WriteConcern) args[0]);
            }

            if (databaseInfo == null) {
                databaseInfo = UnKnownDatabaseInfo.MONGO_INSTANCE;
            }

            if (target instanceof DatabaseInfoAccessor) {
                ((DatabaseInfoAccessor) target)._$PINPOINT$_setDatabaseInfo(databaseInfo);
            }
        }
    }

    /**
     * @param target
     * @param databaseInfo
     * @return
     */
    private DatabaseInfo getDbInfo(Object target) {
        DatabaseInfo databaseInfo = null;
        if (target instanceof DatabaseInfoAccessor) {
            databaseInfo = ((DatabaseInfoAccessor) target)._$PINPOINT$_getDatabaseInfo();
        }
        return databaseInfo;
    }

    private void logBeforeInterceptor(Object target, Object[] args) {
        logger.beforeInterceptor(target, args);
    }

    private void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        logger.afterInterceptor(target, args, result, throwable);
    }

    private DatabaseInfo createDatabaseInfo(List<String> hostList, String readPreference, String writeConcern) {

        DatabaseInfo databaseInfo = new MongoDatabaseInfo(MongoConstants.MONGODB, MongoConstants.MONGO_EXECUTE_QUERY,
                null, null, hostList, null, null, readPreference, writeConcern);

        if (isDebug) {
            logger.debug("parse DatabaseInfo:{}", databaseInfo);
        }

        return databaseInfo;
    }

    private DatabaseInfo cloneDatabaseInfoWithReadPreference(DatabaseInfo dbInfo, ReadPreference readPreference) {
        MongoDatabaseInfo originalMongoDbInfo = (MongoDatabaseInfo) dbInfo;
        DatabaseInfo databaseInfo = new MongoDatabaseInfo(originalMongoDbInfo.getType(),
                originalMongoDbInfo.getExecuteQueryType(), null, null, originalMongoDbInfo.getHost(), null, null,
                readPreference.getName(), originalMongoDbInfo.getWriteConcern());

        if (isDebug) {
            logger.debug("parse DatabaseInfo:{}", databaseInfo);
        }

        return databaseInfo;
    }

    private DatabaseInfo cloneDatabaseInfoWithWriteConcern(DatabaseInfo dbInfo, WriteConcern writeConcern) {
        MongoDatabaseInfo originalMongoDbInfo = (MongoDatabaseInfo) dbInfo;
        DatabaseInfo databaseInfo = new MongoDatabaseInfo(originalMongoDbInfo.getType(),
                originalMongoDbInfo.getExecuteQueryType(), null, null, originalMongoDbInfo.getHost(), null, null,
                originalMongoDbInfo.getReadPreference(), writeConcern.getWString());

        if (isDebug) {
            logger.debug("parse DatabaseInfo:{}", databaseInfo);
        }

        return databaseInfo;
    }

    private List<String> getHostList(Object[] args) {
        final List<String> hostList = new ArrayList<String>();
        hostList.add((String) args[0] + ":" + args[1]);
        return hostList;
    }
}
