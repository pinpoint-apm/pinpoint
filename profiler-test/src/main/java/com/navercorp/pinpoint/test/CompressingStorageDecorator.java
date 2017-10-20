/*
 * Copyright 2017 NAVER Corp.
 *
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
 */

package com.navercorp.pinpoint.test;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.compress.SpanEventCompressor;
import com.navercorp.pinpoint.profiler.context.storage.Storage;

import java.util.Collections;

/**
 * @author HyunGil Jeong
 */
public class CompressingStorageDecorator implements Storage {

    private final Storage delegate;
    private final SpanEventCompressor<Long> spanEventCompressor;

    public CompressingStorageDecorator(Storage delegate, SpanEventCompressor<Long> spanEventCompressor) {
        if (delegate == null) {
            throw new NullPointerException("delegate must not be null");
        }
        if (spanEventCompressor == null) {
            throw new NullPointerException("spanEventCompressor must not be null");
        }
        this.delegate = delegate;
        this.spanEventCompressor = spanEventCompressor;
    }

    @Override
    public void store(SpanEvent spanEvent) {
        if (spanEvent == null) {
            throw new NullPointerException("spanEvent must not be null");
        }
        long keyTime = spanEvent.getTraceRoot().getTraceStartTime();
        spanEventCompressor.compress(Collections.singletonList(spanEvent), keyTime);
        delegate.store(spanEvent);
    }

    @Override
    public void store(Span span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }
        delegate.store(span);
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    @Override
    public void close() {
        delegate.close();
    }
}
