/*
 * Copyright 2016 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.profiler.context.storage.flush;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.rpc.util.ListUtils;

/**
 * @author Taejin Koo
 */
public class SpanEventThresholdCondition implements SpanChunkFlushCondition, SpanFlushCondition {

    private final int threshold;

    public SpanEventThresholdCondition(int threshold) {
        this.threshold = threshold;
    }

    public SpanEventThresholdCondition(int maxSize, int percentage) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive number");
        }

        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("percentage number must be between 0 and 100");
        }

        int threshold = (int) (maxSize * (percentage / 100.0f));
        threshold = Math.max(0, threshold);
        threshold = Math.min(threshold, maxSize);

        this.threshold = threshold;
    }

    @Override
    public boolean matches(Span span, StorageFlusher flusher) {
        int size = ListUtils.size(span.getSpanEventList());
        if (size <= threshold) {
            return true;
        }
        return false;
    }

    @Override
    public boolean matches(SpanChunk spanChunk, StorageFlusher flusher) {
        int size = ListUtils.size(spanChunk.getSpanEventList());
        if (size <= threshold) {
            return true;
        }
        return false;
    }

    public int getThreshold() {
        return threshold;
    }

}
