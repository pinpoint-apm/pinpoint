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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.MongoDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.plugin.mongo.HostListAccessor;
import com.navercorp.pinpoint.plugin.mongo.MongoConstants;
import com.navercorp.pinpoint.plugin.mongo.MongoUtil;
import com.navercorp.pinpoint.plugin.mongo.NormalizedBson;

import java.util.List;

public class MongoCollectionImplReadOperationInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private final boolean collectJson;
    private final boolean traceBsonBindValue;

    public MongoCollectionImplReadOperationInterceptor(TraceContext traceContext, MethodDescriptor descriptor, boolean collectJson, boolean traceBsonBindValue) {
        super(traceContext, descriptor);
        this.collectJson = collectJson;
        this.traceBsonBindValue = traceBsonBindValue;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordApi(methodDescriptor);

        if (Boolean.FALSE == (target instanceof HostListAccessor)) {
            return;
        }

        final List<String> hostList = ((HostListAccessor) target)._$PINPOINT$_getHostList();
        if (hostList == null) {
            return;
        }

        final DatabaseInfo databaseInfo = DatabaseInfoUtils.getDatabaseInfo(target, UnKnownDatabaseInfo.MONGO_INSTANCE);
        final MongoDatabaseInfo mongoDatabaseInfo = new MongoDatabaseInfo(MongoConstants.MONGODB, MongoConstants.MONGO_EXECUTE_QUERY,
                null, null, hostList, databaseInfo.getDatabaseId(), ((MongoDatabaseInfo) databaseInfo).getCollectionName(), ((MongoDatabaseInfo) databaseInfo).getReadPreference(), ((MongoDatabaseInfo) databaseInfo).getWriteConcern());

        recorder.recordServiceType(mongoDatabaseInfo.getExecuteQueryType());
        recorder.recordEndPoint(mongoDatabaseInfo.getMultipleHost());
        recorder.recordDestinationId(mongoDatabaseInfo.getDatabaseId());

        MongoUtil.recordMongoCollection(recorder, mongoDatabaseInfo.getCollectionName(), mongoDatabaseInfo.getReadPreference());
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (collectJson) {
            final boolean success = InterceptorUtils.isSuccess(throwable);
            if (success) {
                if (args != null) {
                    NormalizedBson parsedBson = MongoUtil.parseBson(args, traceBsonBindValue);
                    MongoUtil.recordParsedBson(recorder, parsedBson);
                }
            }
        }
        recorder.recordException(throwable);
        if (isAsynchronousInvocation(target, args, result, throwable)) {
            // Trace to Disposable object
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            ((AsyncContextAccessor) (result))._$PINPOINT$_setAsyncContext(asyncContext);
            if (isDebug) {
                logger.debug("Set asyncContext to result. asyncContext={}", asyncContext);
            }
        }
    }

    private boolean isAsynchronousInvocation(final Object target, final Object[] args, Object result, Throwable throwable) {
        if (throwable != null) {
            return false;
        }

        if (!(result instanceof AsyncContextAccessor)) {
            return false;
        }

        return true;
    }
}
