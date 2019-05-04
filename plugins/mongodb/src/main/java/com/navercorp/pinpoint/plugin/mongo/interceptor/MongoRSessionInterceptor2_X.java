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

import com.mongodb.DBCollection;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.MongoDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.plugin.mongo.MongoUtil;
import com.navercorp.pinpoint.plugin.mongo.NormalizedBson;

/**
 * @author Community
 */
public class MongoRSessionInterceptor2_X extends SpanEventSimpleAroundInterceptorForPlugin {

    private final boolean collectJson;
    private final boolean traceBsonBindValue;

    public MongoRSessionInterceptor2_X(TraceContext traceContext, MethodDescriptor descriptor, boolean collectJson,
            boolean traceBsonBindValue) {
        super(traceContext, descriptor);
        this.collectJson = collectJson;
        this.traceBsonBindValue = traceBsonBindValue;
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        StringBuilder builder = new StringBuilder();
        if (args != null)
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                builder.append(arg.getClass());
                builder.append("|");
            }

        DatabaseInfo databaseInfo = DatabaseInfoUtils.getDatabaseInfo(target, UnKnownDatabaseInfo.MONGO_INSTANCE);

        recorder.recordServiceType(databaseInfo.getExecuteQueryType());
        recorder.recordEndPoint(databaseInfo.getMultipleHost());
        recorder.recordDestinationId(databaseInfo.getDatabaseId());

        recorder.recordApi(methodDescriptor);
        String readPreference = ((MongoDatabaseInfo) databaseInfo).getReadPreference();
        if (readPreference == null || readPreference.trim().equals("")) {
            DBCollection collection = (DBCollection) target;
            readPreference = collection.getReadPreference().getName();
        }
        MongoUtil.recordMongoCollection(recorder, ((MongoDatabaseInfo) databaseInfo).getCollectionName(),
                readPreference);
    }

    @Override
    protected void prepareAfterTrace(Object target, Object[] args, Object result, Throwable throwable) {
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result,
            Throwable throwable) {
        if (collectJson) {
            final boolean success = InterceptorUtils.isSuccess(throwable);
            if (success) {
                if (args != null) {
                    NormalizedBson parsedBson = MongoUtil.parseJson(args, traceBsonBindValue);
                    MongoUtil.recordParsedBson(recorder, parsedBson);
                }
            }
        }
        recorder.recordException(throwable);
    }
}
