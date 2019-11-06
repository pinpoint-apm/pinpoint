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

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.annotations.InterfaceAudience;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface TraceFactory {

    Trace currentTraceObject();

    Trace currentRawTraceObject();

    Trace removeTraceObject();

    Trace disableSampling();

    // picked as sampling target at remote
    Trace continueTraceObject(TraceId traceId);

    Trace continueTraceObject(Trace trace);

    @InterfaceAudience.LimitedPrivate("vert.x")
    Trace continueAsyncTraceObject(TraceId traceId);

    Trace newTraceObject();

    @InterfaceAudience.LimitedPrivate("vert.x")
    Trace newAsyncTraceObject();
}
