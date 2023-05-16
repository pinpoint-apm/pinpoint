/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.test.wrapper;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanType;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ActualTraceFactory {

    public static ActualTrace wrap(SpanType spanType) {
        ActualTrace actualTrace = wrapOrNull(spanType);
        if (actualTrace == null) {
            throw new IllegalArgumentException("Unexpected type: " + spanType.getClass());
        }
        return actualTrace;
    }

    public static ActualTrace wrapOrNull(SpanType spanType) {
        if (spanType instanceof Span) {
            final Span span = (Span) spanType;
            return new SpanFacade(span);
        } else if (spanType instanceof SpanChunk) {
            final SpanChunk spanChunk = (SpanChunk) spanType;
            return new SpanEventFacade(spanChunk);
        }
        return null;
    }
}
