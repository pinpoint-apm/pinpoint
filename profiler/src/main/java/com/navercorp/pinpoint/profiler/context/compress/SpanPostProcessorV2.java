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
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanPostProcessorV2 implements SpanPostProcessor<Context> {

    private static final byte V2 = TraceDataFormatVersion.V2.getVersion();

    // TODO refactor injector
    private final static Comparator<SpanEvent> SEQUENCE_COMPARATOR = SpanEventSequenceComparator.INSTANCE;

    public SpanPostProcessorV2() {
    }

    @Override
    public Context newContext(Span span, TSpan tSpan) {
        tSpan.setVersion(V2);

        final List<SpanEvent> spanEventList = span.getSpanEventList();
        Collections.sort(spanEventList, SEQUENCE_COMPARATOR);

        final long keyTime = getKeyTime(spanEventList);
        return new ContextV2(keyTime);
    }

    @Override
    public Context newContext(SpanChunk spanChunk, TSpanChunk tSpanChunk) {
        tSpanChunk.setVersion(V2);

        final List<SpanEvent> spanEventList = spanChunk.getSpanEventList();
        Collections.sort(spanEventList, SEQUENCE_COMPARATOR);

        final long keyTime = getKeyTime(spanEventList);

        tSpanChunk.setKeyTime(keyTime);
        return new ContextV2(keyTime);
    }

    @Override
    public void postProcess(Context context, SpanEvent spanEvent, TSpanEvent tSpanEvent) {
        final ContextV2 context2 = (ContextV2) context;

        final long startTime = spanEvent.getStartTime();
        final long keyTime = context.keyTime();
        final long startElapsedTime = startTime - keyTime;
        tSpanEvent.setStartElapsed((int) startElapsedTime);
        context2.setKeyTime(startTime);

        if (context2.getIndex() == 0) {
            int depth = spanEvent.getDepth();
            context2.setPrevDepth(depth);
            tSpanEvent.setDepth(depth);
        } else {
            int currentDepth = spanEvent.getDepth();
            int prevDepth = context2.getPrevDepth();
            if (currentDepth == prevDepth) {
                // skip
                tSpanEvent.setDepth(0);
            } else {
                tSpanEvent.setDepth(currentDepth);
            }
            context2.setPrevDepth(currentDepth);
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
