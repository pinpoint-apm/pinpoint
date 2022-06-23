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

package com.navercorp.pinpoint.plugin.mongo.interceptor;

import com.mongodb.MongoNamespace;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.MongoDatabaseInfo;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.mongo.MongoConstants;
import com.navercorp.pinpoint.plugin.mongo.MongoUtil;

import java.util.Collections;

public class ReactiveMongoOperationPublisherConstructorInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public ReactiveMongoOperationPublisherConstructorInterceptor() {
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (throwable != null) {
            return;
        }

        if (Boolean.FALSE == (target instanceof DatabaseInfoAccessor)) {
            logger.info("Unexpected target. The target is not a DatabaseInfoAccessor implementation. target={}", target);
            return;
        }

        final MongoNamespace mongoNamespace = ArrayArgumentUtils.getArgument(args, 0, MongoNamespace.class);
        if (mongoNamespace == null) {
            logger.info("Unexpected argument. The arg0 is not a MongoNamespace class. args={}", args);
            return;
        }
        final String databaseId = mongoNamespace.getDatabaseName();
        final String collectionName = mongoNamespace.getCollectionName();

        String readPreferenceName = "";
        final ReadPreference readPreference = ArrayArgumentUtils.getArgument(args, 3, ReadPreference.class);
        if (readPreference != null) {
            readPreferenceName = readPreference.getName();
        }

        String writeConcernName = "";
        final WriteConcern writeConcern = ArrayArgumentUtils.getArgument(args, 5, WriteConcern.class);
        if (writeConcern != null) {
            writeConcernName = MongoUtil.getWriteConcern0(writeConcern);
        }

        final DatabaseInfo databaseInfo = new MongoDatabaseInfo(MongoConstants.MONGODB, MongoConstants.MONGO_EXECUTE_QUERY,
                null, null, Collections.EMPTY_LIST, databaseId, collectionName, readPreferenceName, writeConcernName);
        ((DatabaseInfoAccessor) target)._$PINPOINT$_setDatabaseInfo(databaseInfo);
        if (isDebug) {
            logger.debug("Set databaseInfo={}", databaseInfo);
        }
    }
}
