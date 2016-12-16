/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.storage;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanChunkFactory;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.storage.flush.StorageFlusher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class BufferedStorage implements Storage {
    private static final Logger logger = LoggerFactory.getLogger(BufferedStorage.class);
    private static final boolean isDebug = logger.isDebugEnabled();

    private static final int DEFAULT_BUFFER_SIZE = 20;

    private final int bufferSize;

    private List<SpanEvent> storage;
    private final StorageFlusher flusher;
    private final SpanChunkFactory spanChunkFactory;

    public BufferedStorage(StorageFlusher flusher, SpanChunkFactory spanChunkFactory) {
        this(flusher, spanChunkFactory, DEFAULT_BUFFER_SIZE);
    }

    public BufferedStorage(StorageFlusher flusher, SpanChunkFactory spanChunkFactory, int bufferSize) {
        if (flusher == null) {
            throw new NullPointerException("flusher must not be null");
        }
        if (spanChunkFactory == null) {
            throw new NullPointerException("spanChunkFactory must not be null");
        }
        this.flusher = flusher;
        this.spanChunkFactory = spanChunkFactory;
        this.bufferSize = bufferSize;
        this.storage = new ArrayList<SpanEvent>(bufferSize);
    }

    @Override
    public void store(SpanEvent spanEvent) {
        List<SpanEvent> flushData = null;
        storage.add(spanEvent);
        if (storage.size() >= bufferSize) {
            // data copy
            flushData = storage;
            storage = new ArrayList<SpanEvent>(bufferSize);
        }

        if (flushData != null) {
            final SpanChunk spanChunk = spanChunkFactory.create(flushData);
            if (isDebug) {
                logger.debug("[BufferedStorage] Flush span-chunk {}", spanChunk);
            }
            flusher.flush(spanChunk);
        }
    }

    @Override
    public void store(Span span) {
        List<SpanEvent> spanEventList;
        spanEventList = storage;
        this.storage = new ArrayList<SpanEvent>(bufferSize);

        if (spanEventList != null && !spanEventList.isEmpty()) {
            span.setSpanEventList((List) spanEventList);
        }
        flusher.flush(span);

        if (isDebug) {
            logger.debug("[BufferedStorage] Flush span {}", span);
        }
    }

    public void flush() {
        List<SpanEvent> spanEventList;
        spanEventList = storage;
        this.storage = new ArrayList<SpanEvent>(bufferSize);

        if (spanEventList != null && !spanEventList.isEmpty()) {
            final SpanChunk spanChunk = spanChunkFactory.create(spanEventList);
            flusher.flush(spanChunk);
            if (isDebug) {
                logger.debug("flush span chunk {}", spanChunk);
            }
        }
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return "BufferedStorage{" + "bufferSize=" + bufferSize + ", flusher=" + flusher + '}';
    }

}