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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceType;

/**
 * @author emeroad
 */
public interface TraceFactory {
    Trace currentTraceObject();

    Trace currentRpcTraceObject();

    Trace currentRawTraceObject();

    Trace disableSampling();

    // picked as sampling target at remote
    Trace continueTraceObject(TraceId traceID);

    Trace continueTraceObject(Trace trace);

    Trace continueAsyncTraceObject(AsyncTraceId traceId, int asyncId, long startTime);

    Trace newTraceObject();

    Trace newTraceObject(TraceType traceType);

    Trace removeTraceObject();
}
