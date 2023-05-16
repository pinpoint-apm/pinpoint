/*
 * Copyright 2018 NAVER Corp.
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


import java.util.Objects;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

import java.util.List;


/**
 * @author emeroad
 */
public class DefaultSpanChunk implements SpanChunk {

    private final TraceRoot traceRoot;

    private final List<SpanEvent> spanEventList; // required


    public DefaultSpanChunk(TraceRoot traceRoot, List<SpanEvent> spanEventList) {
        this.traceRoot = Objects.requireNonNull(traceRoot, "traceRoot");
        this.spanEventList = Objects.requireNonNull(spanEventList, "spanEventList");
    }

    @Override
    public TraceRoot getTraceRoot() {
        return traceRoot;
    }


    @Override
    public List<SpanEvent> getSpanEventList() {
        return spanEventList;
    }


    @Override
    public String toString() {
        return "SpanChunk{" +
                "traceRoot=" + traceRoot +
                ", spanEventList=" + spanEventList +
                '}';
    }
}
