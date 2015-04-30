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

package com.navercorp.pinpoint.profiler.sender;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SpanStreamConstants;

/**
 * @author Taejin Koo
 */
public class SpanStreamSendDataSerializer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    ByteBuffer createStartBuffer(byte chunkCount) {
        ByteBuffer startBuffer = ByteBuffer.allocate(SpanStreamConstants.START_PROTOCOL_BUFFER_SIZE);

        startBuffer.put(SpanStreamConstants.Protocol.SPAN_STREAM_SIGNATURE);
        startBuffer.put(SpanStreamConstants.Protocol.SPAN_STREAM_VERSION);
        startBuffer.put(chunkCount);

        startBuffer.flip();

        return startBuffer;
    }

    ByteBuffer createEndBuffer() {
        ByteBuffer endBuffer = ByteBuffer.allocate(SpanStreamConstants.END_PROTOCOL_BUFFER_SIZE);

        endBuffer.put(SpanStreamConstants.Protocol.SPAN_STREAM_END);
        endBuffer.flip();

        return endBuffer;
    }

    ByteBuffer createByteBuffer(int capacity) {
        return ByteBuffer.allocate(capacity);
    }

    void putShort(ByteBuffer byteBuffer, short value) {
        byteBuffer.put((byte) (value >> 8));
        byteBuffer.put((byte) (value));
    }

    public CompositeSpanStreamData serializeSpanStream(HeaderTBaseSerializer serializer, TSpan span) {
        CompositeSpanStreamData.Builder compositeSpanStreamDataBuilder = new CompositeSpanStreamData.Builder();

        List<TSpanEvent> spanEventList = span.getSpanEventList();
        if (spanEventList != null) {
            for (TSpanEvent spanEvent : spanEventList) {
                int bufferStartIndex = serializer.getInterBufferSize();
                try {
                    byte[] buffer = serializer.continueSerialize(spanEvent);
                    int bufferEndIndex = serializer.getInterBufferSize();

                    compositeSpanStreamDataBuilder.addComponentsIndex(bufferStartIndex, bufferEndIndex);
                } catch (TException e) {
                    logger.warn("Serialize fail. value:{}.", spanEvent, e);
                    serializer.reset(bufferStartIndex);
                }
            }
        }

        TSpan copiedSpan = copySpanWithoutSpanEvent(span);
        try {
            int bufferStartIndex = serializer.getInterBufferSize();
            byte[] buffer = serializer.continueSerialize(copiedSpan);
            int bufferEndIndex = serializer.getInterBufferSize();

            compositeSpanStreamDataBuilder.addComponentsIndex(bufferStartIndex, bufferEndIndex);

            compositeSpanStreamDataBuilder.setBuffer(buffer);

            return compositeSpanStreamDataBuilder.build();
        } catch (TException e) {
            logger.warn("Serialize fail. value:{}.", copiedSpan, e);
        }

        return null;
    }

    private TSpan copySpanWithoutSpanEvent(TSpan span) {
        TSpan copiedSpan = span.deepCopy();
        copiedSpan.setSpanEventList(Collections.EMPTY_LIST);
        return copiedSpan;
    }

    public CompositeSpanStreamData serializeSpanChunkStream(HeaderTBaseSerializer serializer, TSpanChunk spanChunk) {
        CompositeSpanStreamData.Builder compositeSpanStreamDataBuilder = new CompositeSpanStreamData.Builder();

        List<TSpanEvent> spanEventList = spanChunk.getSpanEventList();
        if (spanEventList != null) {
            for (TSpanEvent spanEvent : spanEventList) {
                int bufferStartIndex = serializer.getInterBufferSize();
                try {
                    byte[] buffer = serializer.continueSerialize(spanEvent);
                    int bufferEndIndex = serializer.getInterBufferSize();

                    compositeSpanStreamDataBuilder.addComponentsIndex(bufferStartIndex, bufferEndIndex);
                } catch (TException e) {
                    logger.warn("Serialize fail. value:{}.", spanEvent, e);
                    serializer.reset(bufferStartIndex);
                }
            }
        }

        TSpanChunk copiedSpanChunk = copySpanChunkWithoutSpanEvent(spanChunk);

        try {
            int bufferStartIndex = serializer.getInterBufferSize();
            byte[] buffer = serializer.continueSerialize(copiedSpanChunk);
            int bufferEndIndex = serializer.getInterBufferSize();

            compositeSpanStreamDataBuilder.addComponentsIndex(bufferStartIndex, bufferEndIndex);

            compositeSpanStreamDataBuilder.setBuffer(buffer);

            return compositeSpanStreamDataBuilder.build();
        } catch (TException e) {
            logger.warn("Serialize fail. value:{}.", copiedSpanChunk, e);
        }

        return null;
    }

    private TSpanChunk copySpanChunkWithoutSpanEvent(TSpanChunk spanChunk) {
        TSpanChunk copiedSpanChunk = spanChunk.deepCopy();
        copiedSpanChunk.setSpanEventList(Collections.EMPTY_LIST);
        return copiedSpanChunk;
    }

}
