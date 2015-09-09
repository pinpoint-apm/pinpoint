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

import com.navercorp.pinpoint.bootstrap.context.RecordableTrace;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.interceptor.*;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * Maybe we should trace get of Datasource.
 * @author emeroad
 */
public class DataSourceCloseInterceptor extends SpanEventSimpleAroundInterceptor {



    public DataSourceCloseInterceptor() {
        super(DataSourceCloseInterceptor.class);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, final Object target, Object[] args) {
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder trace, Object target, Object[] args, Object result, Throwable throwable) {
        trace.recordServiceType(ServiceType.DBCP);
        trace.recordApi(getMethodDescriptor());
        trace.recordException(throwable);
    }
}
