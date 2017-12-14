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

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;


/**
 * @author Woonduk Kang(emeroad)
 */
public interface AsyncTraceContext {

//    Reference<Trace> continueAsyncTraceObject(TraceRoot traceRoot, int asyncId, short asyncSequence);

    Reference<Trace> continueAsyncTraceObject(TraceRoot traceRoot, int asyncId, short asyncSequence);

    Trace newAsyncTraceObject(TraceRoot traceRoot, int asyncId, short asyncSequence);

    Reference<Trace> continueAsyncTraceObject(AsyncTraceId asyncTraceId, int asyncId, long startTime);

    Reference<Trace> currentRawTraceObject();

    Reference<Trace> currentTraceObject();

    void removeTraceObject();

    int nextAsyncId();

}
