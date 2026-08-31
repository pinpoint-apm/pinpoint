/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.test;

import com.navercorp.pinpoint.profiler.context.AsyncSpanChunk;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.SpanType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CallSteps {

    private static final Comparator<SpanEvent> SPANEVENT_SEQUENCE_COMPARATOR = Comparator.comparingInt(SpanEvent::getSequence);
    private static final Comparator<AsyncSpanChunk> ASYNC_SPANCHUNK_COMPARATOR = Comparator.comparingInt(o -> o.getLocalAsyncId().getSequence());

    private Span root;
    private final List<SpanChunk> spanChunkList = new ArrayList<>();
    private final List<AsyncSpanChunk> asyncSpanChunkList = new ArrayList<>();

    public CallSteps(OrderedSpanRecorder recorder) {
        for (SpanType item : recorder) {
            if (item instanceof Span) {
                root = (Span) item;
            } else if (item instanceof AsyncSpanChunk) {
                AsyncSpanChunk asyncSpanChunk = (AsyncSpanChunk) item;
                asyncSpanChunkList.add(asyncSpanChunk);
            } else if (item instanceof SpanChunk) {
                SpanChunk spanChunk = (SpanChunk) item;
                spanChunkList.add(spanChunk);
            }
        }
    }

    public List<SpanEvent> get() {
        List<SpanEvent> list = new ArrayList<>();
        if (root != null) {
            list.addAll(root.getSpanEventList());
        }
        for (SpanChunk spanChunk : spanChunkList) {
            list.addAll(spanChunk.getSpanEventList());
        }
        list.sort(SPANEVENT_SEQUENCE_COMPARATOR);

        List<SpanEvent> callSteps = new ArrayList<>();
        for (SpanEvent spanEvent : list) {
            populate(callSteps, spanEvent, 0);
        }

        return callSteps;
    }

    void populate(List<SpanEvent> callSteps, SpanChunk spanChunk, int parentDepth) {
        for (SpanEvent spanEvent : spanChunk.getSpanEventList()) {
            populate(callSteps, spanEvent, parentDepth);
        }
    }

    private void populate(List<SpanEvent> callSteps, SpanEvent spanEvent, int parentDepth) {
        int depth = uncompressDepth(callSteps, spanEvent);
        if (parentDepth != 0) {
            depth += parentDepth;
        }
        spanEvent.setDepth(depth);
        callSteps.add(spanEvent);

        if (spanEvent.getAsyncIdObject() != null) {
            List<AsyncSpanChunk> nextAsyncChunkList = findAsyncSpanChunk(asyncSpanChunkList, spanEvent.getAsyncIdObject().getAsyncId());
            for (AsyncSpanChunk asyncSpanChunk : nextAsyncChunkList) {
                populate(callSteps, asyncSpanChunk, depth);
            }
        }
    }

    int uncompressDepth(List<SpanEvent> steps, SpanEvent spanEvent) {
        if (spanEvent.getDepth() == -1) {
            for (int i = steps.size() - 1; i >= 0; i--) {
                SpanEvent prevSpanEvent = steps.get(i);
                if (prevSpanEvent.getDepth() != -1) {
                    return prevSpanEvent.getDepth();
                }
            }
            return 0;
        }
        return spanEvent.getDepth();
    }


    private List<AsyncSpanChunk> findAsyncSpanChunk(List<AsyncSpanChunk> asyncSpanChunkList, int asyncId) {
        final List<AsyncSpanChunk> list = new ArrayList<>();
        for (AsyncSpanChunk asyncSpanChunk : asyncSpanChunkList) {
            if (asyncSpanChunk.getLocalAsyncId().getAsyncId() == asyncId) {
                list.add(asyncSpanChunk);
            }
        }
        list.sort(ASYNC_SPANCHUNK_COMPARATOR);

        return list;
    }

    private void resetDepth(AsyncSpanChunk asyncSpanChunk, int depth) {
        for (SpanEvent spanEvent : asyncSpanChunk.getSpanEventList()) {
            int newDepth = spanEvent.getDepth() + depth;
            spanEvent.setDepth(newDepth);
        }
    }
}
