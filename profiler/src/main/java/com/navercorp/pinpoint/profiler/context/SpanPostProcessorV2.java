/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.profiler.context.compress.SpanEventCompressor;
import com.navercorp.pinpoint.profiler.context.compress.SpanEventCompressorV2;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanPostProcessorV2 implements SpanPostProcessor {

    private static final TraceDataFormatVersion V2 = TraceDataFormatVersion.V2;

    // TODO refactor injector
    private final SpanEventCompressor<Long> spanEventCompressor = new SpanEventCompressorV2();

    @Override
    public Span postProcess(Span span, List<SpanEvent> spanEventList) {

        span.setVersion(V2.getVersion());
        span.finish();

        long spanStartTime = span.getStartTime();

        spanEventCompressor.compress(spanEventList, spanStartTime);

        span.setSpanEventList((List)spanEventList);

        return span;
    }
}
