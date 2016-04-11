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

import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SpanStreamConstants;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

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

    public PartitionedByteBufferLocator serializeSpanStream(HeaderTBaseSerializer serializer, TSpan span) {
        PartitionedByteBufferLocator.Builder partitionedByteBufferLocatorBuilder = new PartitionedByteBufferLocator.Builder();

        List<TSpanEvent> spanEventList = span.getSpanEventList();
        if (spanEventList != null) {
            for (TSpanEvent spanEvent : spanEventList) {
                int bufferStartIndex = serializer.getInterBufferSize();
                try {
                    byte[] buffer = serializer.continueSerialize(spanEvent);
                    int bufferEndIndex = serializer.getInterBufferSize();

                    partitionedByteBufferLocatorBuilder.addIndex(bufferStartIndex, bufferEndIndex);
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

            partitionedByteBufferLocatorBuilder.addIndex(bufferStartIndex, bufferEndIndex);

            partitionedByteBufferLocatorBuilder.setBuffer(buffer);

            return partitionedByteBufferLocatorBuilder.build();
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

    public PartitionedByteBufferLocator serializeSpanChunkStream(HeaderTBaseSerializer serializer, TSpanChunk spanChunk) {
        PartitionedByteBufferLocator.Builder partitionedByteBufferLocatorBuilder = new PartitionedByteBufferLocator.Builder();

        List<TSpanEvent> spanEventList = spanChunk.getSpanEventList();
        if (spanEventList != null) {
            for (TSpanEvent spanEvent : spanEventList) {
                int bufferStartIndex = serializer.getInterBufferSize();
                try {
                    byte[] buffer = serializer.continueSerialize(spanEvent);
                    int bufferEndIndex = serializer.getInterBufferSize();

                    partitionedByteBufferLocatorBuilder.addIndex(bufferStartIndex, bufferEndIndex);
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

            partitionedByteBufferLocatorBuilder.addIndex(bufferStartIndex, bufferEndIndex);

            partitionedByteBufferLocatorBuilder.setBuffer(buffer);

            return partitionedByteBufferLocatorBuilder.build();
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
