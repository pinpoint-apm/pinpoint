/*
 * Copyright 2019 NAVER Corp.
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

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.TraceDataFormatVersion;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;

import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanProcessorV1 implements SpanProcessor<TSpan, TSpanChunk> {

    private static final byte V1 = TraceDataFormatVersion.V1.getVersion();

    @Override
    public void preProcess(Span span, TSpan tSpan) {
        if (tSpan.getVersion() != V1) {
            tSpan.setVersion(V1);
        }
    }

    @Override
    public void preProcess(SpanChunk spanChunk, TSpanChunk tSpanChunk) {
        if (tSpanChunk.getVersion() != V1) {
            tSpanChunk.setVersion(V1);
        }
    }

    @Override
    public void postProcess(Span span, TSpan tSpan) {
        final TraceRoot traceRoot = span.getTraceRoot();
        final long keyTime = traceRoot.getTraceStartTime();

        List<SpanEvent> spanEventList = span.getSpanEventList();
        if (spanEventList == null) {
            spanEventList = Collections.emptyList();
        }

        List<TSpanEvent> tSpanEventList = tSpan.getSpanEventList();
        if (tSpanEventList == null) {
            tSpanEventList = Collections.emptyList();
        }

        postEventProcess(spanEventList, tSpanEventList, keyTime);
    }

    @Override
    public void postProcess(SpanChunk spanChunk, TSpanChunk tSpanChunk) {
        final TraceRoot traceRoot = spanChunk.getTraceRoot();
        final long keyTime = traceRoot.getTraceStartTime();

        final List<SpanEvent> spanEventList = spanChunk.getSpanEventList();
        if (CollectionUtils.isEmpty(spanEventList)) {
            throw new IllegalStateException("SpanChunk.spanEventList is empty");
        }
        final List<TSpanEvent> tSpanEventList = tSpanChunk.getSpanEventList();
        if (CollectionUtils.isEmpty(tSpanEventList)) {
            throw new IllegalStateException("TSpanChunk.spanEventList is empty");
        }
        postEventProcess(spanEventList, tSpanEventList, keyTime);
    }

    @VisibleForTesting
    public void postEventProcess(List<SpanEvent> spanEventList, List<TSpanEvent> tSpanEventList, long keyTime) {
        if (CollectionUtils.isEmpty(spanEventList)) {
            return;
        }
        if (!(CollectionUtils.nullSafeSize(spanEventList) == CollectionUtils.nullSafeSize(tSpanEventList))) {
            throw new IllegalStateException("list size not same, spanEventList=" + CollectionUtils.nullSafeSize(spanEventList) + ", tSpanEventList=" + CollectionUtils.nullSafeSize(tSpanEventList));
        }
        // check list type
        assert spanEventList instanceof RandomAccess;

        final int listSize = spanEventList.size();
        for (int i = 0; i < listSize; i++) {
            final SpanEvent spanEvent = spanEventList.get(i);
            final TSpanEvent tSpanEvent = tSpanEventList.get(i);

            final long startTime = spanEvent.getStartTime();
            final long startElapsedTime = startTime - keyTime;
            tSpanEvent.setStartElapsed((int) startElapsedTime);
        }
    }
}
