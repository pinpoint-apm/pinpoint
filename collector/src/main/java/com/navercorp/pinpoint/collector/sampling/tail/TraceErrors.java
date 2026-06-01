/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.sampling.tail;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;

import java.util.List;

/**
 * Detects whether a span or span chunk carries an error, for keep-on-error (B) tail sampling.
 * Any span/chunk in a trace that errors marks the whole trace for 100% retention.
 */
public final class TraceErrors {

    private TraceErrors() {
    }

    public static boolean hasError(SpanBo spanBo) {
        if (spanBo.hasError() || spanBo.hasException()) {
            return true;
        }
        return anyEventException(spanBo.getSpanEventBoList());
    }

    public static boolean hasError(SpanChunkBo spanChunkBo) {
        // SpanChunkBo carries no span-level error code; errors surface as span-event exceptions.
        return anyEventException(spanChunkBo.getSpanEventBoList());
    }

    private static boolean anyEventException(List<SpanEventBo> events) {
        if (events == null) {
            return false;
        }
        for (SpanEventBo event : events) {
            if (event.hasException()) {
                return true;
            }
        }
        return false;
    }
}
