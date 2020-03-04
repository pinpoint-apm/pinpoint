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
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.TraceDataFormatVersion;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.RandomAccess;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcSpanProcessorV2 implements SpanProcessor<PSpan.Builder, PSpanChunk.Builder> {

    private static final byte V2 = TraceDataFormatVersion.V2.getVersion();

    // TODO refactor injector
    private final static Comparator<SpanEvent> SEQUENCE_COMPARATOR = SpanEventSequenceComparator.INSTANCE;

    public GrpcSpanProcessorV2() {
    }

    @Override
    public void preProcess(Span span, PSpan.Builder pSpan) {
        pSpan.setVersion(V2);

        final List<SpanEvent> spanEventList = span.getSpanEventList();
        if (CollectionUtils.hasLength(spanEventList)) {
            Collections.sort(spanEventList, SEQUENCE_COMPARATOR);
        }
    }

    @Override
    public void preProcess(SpanChunk spanChunk, PSpanChunk.Builder pSpanChunk) {
        pSpanChunk.setVersion(V2);

        final List<SpanEvent> spanEventList = spanChunk.getSpanEventList();
        if (CollectionUtils.hasLength(spanEventList)) {
            Collections.sort(spanEventList, SEQUENCE_COMPARATOR);
        }
    }

    @Override
    public void postProcess(SpanChunk spanChunk, PSpanChunk.Builder pSpanChunk) {
        final List<SpanEvent> spanEventList = spanChunk.getSpanEventList();
        final List<PSpanEvent.Builder> tSpanEventList = pSpanChunk.getSpanEventBuilderList();
        long keyTime = getKeyTime(spanEventList);
        pSpanChunk.setKeyTime(keyTime);
        postProcess(keyTime, spanEventList, tSpanEventList);
    }

    @Override
    public void postProcess(Span span, PSpan.Builder pSpan) {
        final List<SpanEvent> spanEventList = span.getSpanEventList();
        final List<PSpanEvent.Builder> tSpanEventList = pSpan.getSpanEventBuilderList();
        long keyTime = span.getStartTime();
        postProcess(keyTime, spanEventList, tSpanEventList);
    }

    private void postProcess(long keyTime, List<SpanEvent> spanEventList, List<PSpanEvent.Builder> pSpanEventList) {
        if (CollectionUtils.isEmpty(spanEventList)) {
            return;
        }
        if (!(CollectionUtils.nullSafeSize(spanEventList) == CollectionUtils.nullSafeSize(pSpanEventList))) {
            throw new IllegalStateException("list size not same");
        }
        // check list type
        assert spanEventList instanceof RandomAccess;

        int prevDepth = 0;
        boolean first = true;

        final int listSize = spanEventList.size();
        for (int i = 0; i < listSize; i++) {
            final SpanEvent spanEvent = spanEventList.get(i);
            final PSpanEvent.Builder pSpanEvent = pSpanEventList.get(i);

            final long startTime = spanEvent.getStartTime();
            final long startElapsedTime = startTime - keyTime;
            pSpanEvent.setStartElapsed((int) startElapsedTime);
            keyTime = startTime;

            if (first) {
                first = false;
                int depth = spanEvent.getDepth();
                prevDepth = depth;
                pSpanEvent.setDepth(depth);
            } else {
                int currentDepth = spanEvent.getDepth();

                if (currentDepth == prevDepth) {
                    // skip
                    pSpanEvent.setDepth(0);
                } else {
                    pSpanEvent.setDepth(currentDepth);
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
