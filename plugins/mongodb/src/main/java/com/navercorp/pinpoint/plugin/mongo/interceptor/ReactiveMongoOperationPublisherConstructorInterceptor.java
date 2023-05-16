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

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoNamespace;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.internal.MongoClientImpl;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.MongoDatabaseInfo;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.mongo.MongoConstants;
import com.navercorp.pinpoint.plugin.mongo.MongoUtil;
import com.navercorp.pinpoint.plugin.mongo.ReactiveMongoClientImplGetter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
            return;
        }

        try {
            final MongoNamespace mongoNamespace = ArrayArgumentUtils.getArgument(args, 0, MongoNamespace.class);
            if (mongoNamespace == null) {
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

            List<String> hostList = getHostList(args);
            if (hostList.isEmpty()) {
                hostList = Arrays.asList("UNKNOWN");
            }

            final DatabaseInfo databaseInfo = new MongoDatabaseInfo(MongoConstants.MONGODB, MongoConstants.MONGO_EXECUTE_QUERY,
                    null, null, hostList, databaseId, collectionName, readPreferenceName, writeConcernName);
            ((DatabaseInfoAccessor) target)._$PINPOINT$_setDatabaseInfo(databaseInfo);
            if (isDebug) {
                logger.debug("Set databaseInfo={}", databaseInfo);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        }
    }

    List<String> getHostList(Object[] args) {
        final ReactiveMongoClientImplGetter reactiveMongoClientImplGetter = ArrayArgumentUtils.getArgument(args, 9, ReactiveMongoClientImplGetter.class);
        if (reactiveMongoClientImplGetter != null) {
            final MongoClientImpl mongoClientImpl = reactiveMongoClientImplGetter._$PINPOINT$_getMongoClientImpl();
            if (mongoClientImpl != null) {
                final MongoClientSettings mongoClientSettings = mongoClientImpl.getSettings();
                if (mongoClientSettings != null) {
                    return MongoUtil.getHostList(mongoClientSettings);
                }
            }
        }
        return Collections.EMPTY_LIST;
    }
}
