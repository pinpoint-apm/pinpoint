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

import com.navercorp.pinpoint.profiler.context.compress.SpanProcessor;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessorV1;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * @author emeroad
 */
public class SpanEventTest {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final SpanProcessor<TSpan, TSpanChunk> compressorV1 = new SpanProcessorV1();

    @Test
    public void testMarkStartTime() {
        final DefaultTraceId traceId = new DefaultTraceId("agentId", 0, 0);
        TraceRoot traceRoot = TraceRoot.remote(traceId, "agentId", System.currentTimeMillis(), 0);

        Span span = new Span(traceRoot);
        span.markBeforeTime();

        span.setElapsedTime((int) (span.getStartTime() + 10));
        logger.debug("span:{}", span);

        final SpanEvent spanEvent = new SpanEvent();
        long currentTime = System.currentTimeMillis();
        spanEvent.setStartTime(currentTime);

        spanEvent.setElapsedTime(10);
        logger.debug("spanEvent:{}", spanEvent);
        span.setSpanEventList(Arrays.asList(spanEvent));

        TSpan tSpan = new TSpan();
        TSpanEvent tSpanEvent = new TSpanEvent();
        tSpan.addToSpanEventList(tSpanEvent);
        compressorV1.preProcess(span, tSpan);
        compressorV1.postProcess(span, tSpan);

        Assertions.assertEquals(span.getStartTime() + tSpanEvent.getStartElapsed(), spanEvent.getStartTime(), "startTime");
        Assertions.assertEquals(span.getStartTime() + tSpanEvent.getStartElapsed() + spanEvent.getElapsedTime(), spanEvent.getAfterTime(), "endTime");
    }

    @Test
    public void testGetStartTime() throws Exception {

    }

    @Test
    public void testMarkEndTime() throws Exception {

    }

    @Test
    public void testGetEndTime() throws Exception {

    }
}
