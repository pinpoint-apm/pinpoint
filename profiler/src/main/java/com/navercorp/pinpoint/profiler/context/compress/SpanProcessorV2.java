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

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.TraceDataFormatVersion;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanProcessorV2 implements SpanProcessor<TSpan, TSpanChunk> {

    private static final byte V2 = TraceDataFormatVersion.V2.getVersion();

    // TODO refactor injector
    private final static Comparator<SpanEvent> SEQUENCE_COMPARATOR = SpanEventSequenceComparator.INSTANCE;

    public SpanProcessorV2() {
    }

    @Override
    public void preProcess(Span span, TSpan tSpan) {
        tSpan.setVersion(V2);

        final List<SpanEvent> spanEventList = span.getSpanEventList();
        Collections.sort(spanEventList, SEQUENCE_COMPARATOR);
    }

    @Override
    public void preProcess(SpanChunk spanChunk, TSpanChunk tSpanChunk) {
        tSpanChunk.setVersion(V2);

        final List<SpanEvent> spanEventList = spanChunk.getSpanEventList();
        Collections.sort(spanEventList, SEQUENCE_COMPARATOR);
    }

    @Override
    public void postProcess(SpanChunk span, TSpanChunk tSpan) {
        final List<SpanEvent> spanEventList = span.getSpanEventList();
        final List<TSpanEvent> tSpanEventList = tSpan.getSpanEventList();
        postEventProcess(spanEventList, tSpanEventList);
    }

    @Override
    public void postProcess(Span span, TSpan tSpan) {
        final List<SpanEvent> spanEventList = span.getSpanEventList();
        final List<TSpanEvent> tSpanEventList = tSpan.getSpanEventList();
        postEventProcess(spanEventList, tSpanEventList);
    }

    void postEventProcess(List<SpanEvent> spanEventList, List<TSpanEvent> tSpanEventList) {
        if (!(CollectionUtils.nullSafeSize(spanEventList) == CollectionUtils.nullSafeSize(tSpanEventList))) {
            throw new IllegalStateException("list size not same");
        }

        final Iterator<TSpanEvent> tSpanEventIterator = tSpanEventList.iterator();

        long keyTime = getKeyTime(spanEventList);
        int prevDepth = 0;
        boolean first = true;
        for (SpanEvent spanEvent : spanEventList) {
            final TSpanEvent tSpanEvent = tSpanEventIterator.next();

            final long startTime = spanEvent.getStartTime();
            final long startElapsedTime = startTime - keyTime;
            tSpanEvent.setStartElapsed((int) startElapsedTime);
            keyTime = startTime;

            if (first) {
                first = false;
                int depth = spanEvent.getDepth();
                prevDepth = depth;
                tSpanEvent.setDepth(depth);
            } else {
                int currentDepth = spanEvent.getDepth();

                if (currentDepth == prevDepth) {
                    // skip
                    tSpanEvent.setDepth(0);
                } else {
                    tSpanEvent.setDepth(currentDepth);
                }
                prevDepth = currentDepth;
            }
        }
    }


    private long getKeyTime(List<SpanEvent> spanEventList) {
        if (CollectionUtils.isEmpty(spanEventList)) {
            throw new IllegalArgumentException("spanEventList is empty.");
        }
        final SpanEvent first = spanEventList.get(0);
        if (first == null) {
            throw new IllegalStateException("first SpanEvent is null");
        }

        return first.getStartTime();
    }

}
