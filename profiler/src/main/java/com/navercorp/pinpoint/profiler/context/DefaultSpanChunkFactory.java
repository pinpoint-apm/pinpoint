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

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultSpanChunkFactory implements SpanChunkFactory {

    private final TraceRoot traceRoot;

    public DefaultSpanChunkFactory(TraceRoot traceRoot) {
        this.traceRoot = Assert.requireNonNull(traceRoot, "traceRoot");
    }

    @Override
    public SpanChunk newSpanChunk(List<SpanEvent> spanEventList) {
        return new DefaultSpanChunk(traceRoot, spanEventList);
    }
}
