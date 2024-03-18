/*
 * Copyright 2023 NAVER Corp.
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

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.SpanType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SpanRecorder {
    final Recorder<SpanType> recorder;

    public SpanRecorder(Recorder<SpanType> recorder) {
        this.recorder = Objects.requireNonNull(recorder, "recorder");
    }

    public List<SpanEvent> getCurrentSpanEvents() {
        List<SpanEvent> spanEvents = new ArrayList<>();
        for (SpanType value : this.recorder) {
            if (value instanceof SpanChunk) {
                final SpanChunk spanChunk = (SpanChunk) value;
                spanEvents.addAll(spanChunk.getSpanEventList());
            }
        }
        return spanEvents;
    }

    public List<Span> getCurrentSpans() {
        List<Span> rootSpans = new ArrayList<>();
        for (Object value : this.recorder) {
            if (value instanceof Span) {
                Span span = (Span) value;
                rootSpans.add(span);
            }
        }
        return rootSpans;
    }
}
