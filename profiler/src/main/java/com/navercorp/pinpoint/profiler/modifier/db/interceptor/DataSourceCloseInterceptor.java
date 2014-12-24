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
import com.navercorp.pinpoint.bootstrap.interceptor.*;
import com.navercorp.pinpoint.common.ServiceType;

/**
 * Datasource의 get을 추적해야 될것으로 예상됨.
 * @author emeroad
 */
public class DataSourceCloseInterceptor extends SpanEventSimpleAroundInterceptor {



    public DataSourceCloseInterceptor() {
        super(DataSourceCloseInterceptor.class);
    }

//    @Override
//    protected void prepareBeforeTrace(Object target, Object[] args) {
//        // 예외 케이스 : getConnection()에서 Driver.connect()가 호출되는지 알고 싶으므로 push만 한다.
//        scope.push();
//    }

    @Override
    public void doInBeforeTrace(RecordableTrace trace, final Object target, Object[] args) {
        trace.markBeforeTime();
    }

//    @Override
//    protected void prepareAfterTrace(Object target, Object[] args, Object result, Throwable throwable) {
//        // 예외 케이스 : getConnection()에서 Driver.connect()가 호출되는지 알고 싶으므로 pop만 한다.
//        scope.pop();
//    }

    @Override
    public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
        trace.recordServiceType(ServiceType.DBCP);
        trace.recordApi(getMethodDescriptor());
        trace.recordException(throwable);

        trace.markAfterTime();
    }
}
