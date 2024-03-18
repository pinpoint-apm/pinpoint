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

package com.navercorp.pinpoint.profiler.context.storage;

import com.navercorp.pinpoint.common.profiler.message.DataSender;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanChunkFactory;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.SpanType;
import com.navercorp.pinpoint.profiler.util.queue.ArrayBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class BufferedStorage implements Storage {
    private static final Logger logger = LogManager.getLogger(BufferedStorage.class);
    private static final boolean isDebug = logger.isDebugEnabled();

    private static final int DEFAULT_BUFFER_SIZE = 20;

    private final SpanChunkFactory spanChunkFactory;
    private final DataSender<SpanType> dataSender;
    private final ArrayBuffer<SpanEvent> buffer;


    public BufferedStorage(SpanChunkFactory spanChunkFactory, DataSender<SpanType> dataSender, int bufferSize) {
        this.spanChunkFactory = Objects.requireNonNull(spanChunkFactory, "spanChunkFactory");
        this.dataSender = Objects.requireNonNull(dataSender, "dataSender");
        this.buffer = new ArrayBuffer<>(bufferSize);
    }

    @Override
    public void store(SpanEvent spanEvent) {
        this.buffer.put(spanEvent);

        if (this.buffer.isOverflow()) {
            final List<SpanEvent> flushData = this.buffer.drain();
            sendSpanChunk(flushData);
        }
    }


    @Override
    public void store(Span span) {
        final List<SpanEvent> spanEventList = this.buffer.drain();
        span.setSpanEventList(spanEventList);
        span.finish();

        if (isDebug) {
            logger.debug("Flush {}", span);
        }
        final boolean success = this.dataSender.send(span);
        if (!success) {
            // WARN : Do not call span.toString ()
            // concurrentmodificationexceptionr may occur in spanProcessV2
            logger.debug("send fail");
        }
    }

    @Override
    public void flush() {
        final List<SpanEvent> spanEventList = this.buffer.drain();
        if (CollectionUtils.hasLength(spanEventList)) {
            sendSpanChunk(spanEventList);
        }
    }

    private void sendSpanChunk(List<SpanEvent> spanEventList) {
        final SpanChunk spanChunk = this.spanChunkFactory.newSpanChunk(spanEventList);

        if (isDebug) {
            logger.debug("Flush {}", spanChunk);
        }
        final boolean success = this.dataSender.send(spanChunk);
        if (!success) {
            // WARN : Do not call span.toString ()
            // concurrentmodificationexceptionr may occur in spanProcessV2
            logger.debug("send fail");
        }
    }


    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return "BufferedStorage{" + "buffer=" + buffer + ", dataSender=" + dataSender + '}';
    }
}