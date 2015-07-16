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

package com.navercorp.pinpoint.profiler.modifier.db.interceptor;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.RecordableTrace;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.interceptor.*;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValueUtils;

/**
 * @author emeroad
 */
public class TransactionSetAutoCommitInterceptor extends SpanEventSimpleAroundInterceptor {


    public TransactionSetAutoCommitInterceptor() {
        super(TransactionSetAutoCommitInterceptor.class);
    }


    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        DatabaseInfo databaseInfo = DatabaseInfoTraceValueUtils.__getTraceDatabaseInfo(target, UnKnownDatabaseInfo.INSTANCE);

        recorder.recordServiceType(databaseInfo.getType());
        recorder.recordEndPoint(databaseInfo.getMultipleHost());
        recorder.recordDestinationId(databaseInfo.getDatabaseId());


        recorder.recordApi(getMethodDescriptor(), args);
        recorder.recordException(throwable);
    }
}