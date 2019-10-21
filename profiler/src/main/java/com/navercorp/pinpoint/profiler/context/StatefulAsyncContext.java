/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.AsyncStateSupport;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

/**
 * @author Woonduk Kang(emeroad)
 */
public class StatefulAsyncContext extends DefaultAsyncContext implements AsyncStateSupport {


    private final AsyncState asyncState;


    public StatefulAsyncContext(AsyncTraceContext asyncTraceContext, TraceRoot traceRoot, AsyncId asyncId, int asyncMethodApiId, AsyncState asyncState) {
        super(asyncTraceContext, traceRoot, asyncId, asyncMethodApiId);
        this.asyncState = Assert.requireNonNull(asyncState, "asyncState");
    }

    @Override
    public AsyncState getAsyncState() {
        return asyncState;
    }

    @Override
    public String toString() {
        return "StatefulAsyncContext{" +
                "asyncState=" + asyncState +
                "} " + super.toString();
    }
}
