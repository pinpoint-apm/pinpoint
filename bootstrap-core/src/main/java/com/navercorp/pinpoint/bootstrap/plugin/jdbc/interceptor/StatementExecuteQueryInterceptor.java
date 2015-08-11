/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdbc.common.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.TargetMethod;
import com.navercorp.pinpoint.plugin.jdbc.common.JdbcDriverConstants;
import com.navercorp.pinpoint.plugin.jdbc.common.UnKnownDatabaseInfo;

/**
 * @author netspider
 * @author emeroad
 */
@TargetMethod(name="executeQuery", paramTypes={ "java.lang.String" })
public class StatementExecuteQueryInterceptor extends SpanEventSimpleAroundInterceptorForPlugin implements JdbcDriverConstants {
    private final MetadataAccessor databaseInfoAccessor;
    
    public StatementExecuteQueryInterceptor(TraceContext traceContext, MethodDescriptor descriptor, @Name(DATABASE_INFO) MetadataAccessor databaseInfoAccessor) {
        super(traceContext, descriptor);
        this.databaseInfoAccessor = databaseInfoAccessor;
    }


    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, final Object target, Object[] args) {
        /**
         * If method was not called by request handler, we skip tagging.
         */
        DatabaseInfo databaseInfo = databaseInfoAccessor.get(target, UnKnownDatabaseInfo.INSTANCE);

        recorder.recordServiceType(databaseInfo.getExecuteQueryType());
        recorder.recordEndPoint(databaseInfo.getMultipleHost());
        recorder.recordDestinationId(databaseInfo.getDatabaseId());

    }


    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        if (args.length > 0) {
            Object arg = args[0];
            if (arg instanceof String) {
                recorder.recordSqlInfo((String) arg);
                // TODO more parsing result processing
            }
        }
        recorder.recordException(throwable);
    }
}
