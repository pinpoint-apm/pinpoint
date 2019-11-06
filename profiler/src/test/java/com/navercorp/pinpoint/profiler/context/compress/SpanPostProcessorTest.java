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

import com.navercorp.pinpoint.bootstrap.context.TraceId;

import com.navercorp.pinpoint.profiler.context.DefaultSpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class SpanPostProcessorTest {

    private final String agentId = "agentId";
    private final long agentStartTime = System.currentTimeMillis();

    @Test
    public void postProcess1() {

        SpanProcessor<TSpan, TSpanChunk> spanChunkPostProcessor = new SpanProcessorV1();

        TraceRoot internalTraceId = newInternalTraceId();
        TSpanChunk tSpanChunk = new TSpanChunk();
        try {
            SpanChunk spanChunk = new DefaultSpanChunk(internalTraceId, new ArrayList<SpanEvent>());
            spanChunkPostProcessor.postProcess(spanChunk, tSpanChunk);
            Assert.fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void postProcess2() {

        SpanProcessor<TSpan, TSpanChunk> spanChunkPostProcessor = new SpanProcessorV1();

        TraceRoot internalTraceId = newInternalTraceId();
        List<TSpanEvent> tSpanEvents = new ArrayList<TSpanEvent>();
        TSpanChunk tSpanChunk = new TSpanChunk();
        tSpanChunk.setSpanEventList(tSpanEvents);

        List<SpanEvent> spanEvents = new ArrayList<SpanEvent>();
        SpanChunk spanChunk = new DefaultSpanChunk(internalTraceId, spanEvents);
        // one spanEvent
        addSpanEvent(tSpanEvents, spanEvents);
        spanChunkPostProcessor.postProcess(spanChunk, tSpanChunk);

        // two spanEvent
        addSpanEvent(tSpanEvents, spanEvents);
        spanChunkPostProcessor.postProcess(spanChunk, tSpanChunk);

        // three
        addSpanEvent(tSpanEvents, spanEvents);
        spanChunkPostProcessor.postProcess(spanChunk, tSpanChunk);

    }

    private void addSpanEvent(List<TSpanEvent> tSpanEvents, List<SpanEvent> spanEvents) {
        spanEvents.add(new SpanEvent());
        tSpanEvents.add(new TSpanEvent());
    }

    private TraceRoot newInternalTraceId() {
        TraceId traceId = new DefaultTraceId(agentId, agentStartTime, 100);
        return new DefaultTraceRoot(traceId, agentId, agentStartTime, 0);
    }
}
