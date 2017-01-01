/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.storage.flush;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class DispatcherFlusher implements StorageFlusher {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile boolean closed;

    private final SpanFlushCondition spanFlushCondition;
    private final StorageFlusher spanStorageFlusher;

    private final SpanChunkFlushCondition spanChunkFlushCondition;
    private final StorageFlusher spanChunkStorageFlusher;

    private final StorageFlusher defaultFlusher;

    public DispatcherFlusher(StorageFlusher defaultFlusher, SpanFlushCondition spanFlushCondition, StorageFlusher spanStorageFlusher, SpanChunkFlushCondition spanChunkFlushCondition, StorageFlusher spanChunkStorageFlusher) {
        if (defaultFlusher == null) {
            throw new NullPointerException("defaultFlusher may not be null");
        }
        if (spanFlushCondition == null) {
            throw new NullPointerException("spanFlushCondition must not be null");
        }
        if (spanStorageFlusher == null) {
            throw new NullPointerException("spanStorageFlusher must not be null");
        }
        if (spanChunkFlushCondition == null) {
            throw new NullPointerException("spanChunkFlushCondition must not be null");
        }
        if (spanChunkStorageFlusher == null) {
            throw new NullPointerException("spanChunkStorageFlusher must not be null");
        }
        this.defaultFlusher = defaultFlusher;

        this.spanFlushCondition = spanFlushCondition;
        this.spanStorageFlusher = spanStorageFlusher;
        this.spanChunkFlushCondition = spanChunkFlushCondition;
        this.spanChunkStorageFlusher = spanStorageFlusher;
    }

    @Override
    public void flush(SpanChunk spanChunk) {
        if (closed) {
            logger.warn("Already closed.");
            return;
        }

        SpanChunkFlushCondition condition = this.spanChunkFlushCondition;
        StorageFlusher flusher = this.spanChunkStorageFlusher;
        if (condition.matches(spanChunk, flusher)) {
            flusher.flush(spanChunk);
            return;
        }

        defaultFlusher.flush(spanChunk);
    }

    @Override
    public void flush(Span span) {
        if (closed) {
            logger.warn("Already closed.");
            return;
        }

        SpanFlushCondition condition = this.spanFlushCondition;
        StorageFlusher flusher = this.spanStorageFlusher;
        if (condition.matches(span, flusher)) {
            flusher.flush(span);
            return;
        }

        defaultFlusher.flush(span);
    }

    @Override
    public void stop() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }

        spanChunkStorageFlusher.stop();
        spanStorageFlusher.stop();
        defaultFlusher.stop();
    }

}
