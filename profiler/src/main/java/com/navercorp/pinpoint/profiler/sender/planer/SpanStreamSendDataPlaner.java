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

package com.navercorp.pinpoint.profiler.sender.planer;

import com.navercorp.pinpoint.profiler.sender.PartitionedByteBufferLocator;
import com.navercorp.pinpoint.profiler.sender.SpanStreamSendDataFactory;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collections;

/**
 * @author Taejin Koo
 */
public class SpanStreamSendDataPlaner extends AbstractSpanStreamSendDataPlaner {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TSpan span;

    private byte[] spanChunkBuffer;
    private int spanChunkSize;

    public SpanStreamSendDataPlaner(PartitionedByteBufferLocator partitionedByteBufferLocator, SpanStreamSendDataFactory spanStreamSendDataFactory, TSpan span) {
        super(partitionedByteBufferLocator, spanStreamSendDataFactory);

        this.span = span;
    }

    protected int getSpanChunkLength() {
        getSpanChunkBuffer0();
        return spanChunkSize;
    }

    @Override
    protected ByteBuffer getSpanChunkBuffer() {
        return ByteBuffer.wrap(getSpanChunkBuffer0(), 0, spanChunkSize);
    }

    private byte[] getSpanChunkBuffer0() {
        if (spanChunkBuffer == null) {
            final TSpanChunk spanChunk = toSpanChunk(span);

            HeaderTBaseSerializer serializer = new HeaderTBaseSerializerFactory(false, SpanStreamSendDataFactory.DEFAULT_UDP_MAX_BUFFER_SIZE, false).createSerializer();
            byte[] spanChunkBuffer;
            try {
                spanChunkBuffer = serializer.serialize(spanChunk);
                this.spanChunkBuffer = spanChunkBuffer;
                this.spanChunkSize = serializer.getInterBufferSize();
            } catch (TException e) {
                logger.warn("Spanchunk serializer failed. {}.", spanChunk);
            }
        }

        if (spanChunkBuffer == null) {
            return new byte[0];
        }

        return spanChunkBuffer;
    }

    private TSpanChunk toSpanChunk(TSpan span) {
        final TSpanChunk spanChunk = new TSpanChunk();

        spanChunk.setSpanEventList(Collections.EMPTY_LIST);
        spanChunk.setSpanEventListIsSet(true);
        spanChunk.setAgentId(span.getAgentId());
        spanChunk.setAgentIdIsSet(true);
        spanChunk.setApplicationName(span.getApplicationName());
        spanChunk.setApplicationNameIsSet(true);
        spanChunk.setAgentStartTime(span.getStartTime());
        spanChunk.setAgentStartTimeIsSet(true);

        spanChunk.setTransactionId(span.getTransactionId());
        spanChunk.setTransactionIdIsSet(true);
        spanChunk.setSpanId(span.getSpanId());
        spanChunk.setSpanIdIsSet(true);
        spanChunk.setEndPoint(span.getEndPoint());
        spanChunk.setEndPointIsSet(true);

        return spanChunk;
    }

}
