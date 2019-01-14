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

package com.navercorp.pinpoint.profiler.context.compress;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.TraceDataFormatVersion;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanPostProcessorV1 implements SpanPostProcessor<Context> {

    private static final byte V1 = TraceDataFormatVersion.V1.getVersion();

    @Override
    public Context newContext(Span span, TSpan tSpan) {
        if (tSpan.getVersion() == V1) {
            tSpan.setVersion(V1);
        }

        final long startTime = span.getStartTime();
        return new ContextV1(startTime);
    }

    @Override
    public Context newContext(SpanChunk spanChunk, TSpanChunk tSpanChunk) {
        if (tSpanChunk.getVersion() == V1) {
            tSpanChunk.setVersion(V1);
        }

        final List<SpanEvent> spanEventList = spanChunk.getSpanEventList();
        if (CollectionUtils.isEmpty(spanEventList)) {
            throw new IllegalStateException("spanEventList is empty");
        }
//        skip default version
//        tSpanChunk.setVersion(V1.getVersion());

        final TraceRoot traceRoot = spanChunk.getTraceRoot();
        final long traceStartTime = traceRoot.getTraceStartTime();
        return new ContextV1(traceStartTime);
    }

    @Override
    public void postProcess(Context context, SpanEvent spanEvent, TSpanEvent tSpanEvent) {
        final long startTime = spanEvent.getStartTime();
        final long keyTime = context.keyTime();
        final long startElapsedTime = startTime - keyTime;
        tSpanEvent.setStartElapsed((int) startElapsedTime);
    }



}
