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

package com.navercorp.pinpoint.thrift.io;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;

import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;

/**
 * ChunkHeaderBufferedTBaseSerializer
 * - need flush handler
 * 
 * @author jaehong.kim
 */
public class ChunkHeaderBufferedTBaseSerializer {
    private static final String FIELD_NAME_SPAN_EVENT_LIST = "spanEventList";
    private static final int DEFAULT_CHUNK_SIZE = 1024 * 16;

    // span event list serialized buffer
    private final TBaseStream eventStream;
    // header
    private final TBaseLocator locator;
    private final TProtocolFactory protocolFactory;
    private final ByteArrayOutputStreamTransport transport;

    // reset chunk header
    private boolean writeChunkHeader = false;
    // flush size
    private int chunkSize = DEFAULT_CHUNK_SIZE;
    // flush handler
    private ChunkHeaderBufferedTBaseSerializerFlushHandler flushHandler;

    public ChunkHeaderBufferedTBaseSerializer(final ByteArrayOutputStream out, final TProtocolFactory protocolFactory, final TBaseLocator locator) {
        transport = new ByteArrayOutputStreamTransport(out);
        eventStream = new TBaseStream(protocolFactory);

        this.protocolFactory = protocolFactory;
        this.locator = locator;
    }

    public void add(TBase<?, ?> base) throws TException {
        synchronized (transport) {
            if (base instanceof TSpan) {
                addTSpan(base);
            } else if (base instanceof TSpanChunk) {
                addTSpanChunk(base);
            } else {
                write(base);
            }
        }
    }

    // TSpanChunk = TSpanChunk + TSpanChunk
    private void addTSpanChunk(TBase<?, ?> base) throws TException {
        final TSpanChunk chunk = (TSpanChunk) base;
        if (chunk.getSpanEventList() == null) {
            write(base);
            return;
        }

        try {
            for (TSpanEvent e : chunk.getSpanEventList()) {
                eventStream.write(e);
            }
            write(chunk, FIELD_NAME_SPAN_EVENT_LIST, eventStream.split(chunkSize));
            while (!eventStream.isEmpty()) {
                write(chunk, FIELD_NAME_SPAN_EVENT_LIST, eventStream.split(chunkSize));
            }
        } finally {
            eventStream.clear();
        }
    }

    // TSpan = TSpan + TSpanChunk
    private void addTSpan(TBase<?, ?> base) throws TException {
        final TSpan span = (TSpan) base;
        if (span.getSpanEventList() == null) {
            write(base);
            return;
        }

        try {
            for (TSpanEvent e : span.getSpanEventList()) {
                eventStream.write(e);
            }
            write(span, FIELD_NAME_SPAN_EVENT_LIST, eventStream.split(chunkSize));
            while (!eventStream.isEmpty()) {
                final TSpanChunk spanChunk = toSpanChunk(span);
                write(spanChunk, FIELD_NAME_SPAN_EVENT_LIST, eventStream.split(chunkSize));
            }
        } finally {
            eventStream.clear();
        }
    }

    // write chunk header + header + body
    private void write(final TBase<?, ?> base, final String fieldName, final List<ByteArrayOutput> list) throws TException {
        final TReplaceListProtocol protocol = new TReplaceListProtocol(protocolFactory.getProtocol(transport));

        // write chunk header
        writeChunkHeader(protocol);

        // write header
        writeHeader(protocol, locator.headerLookup(base));
        if (list != null && !list.isEmpty()) {
            protocol.addReplaceField(fieldName, list);
        }

        base.write(protocol);

        if (isNeedFlush()) {
            flush();
        }
    }

    // write chunk header + header + body
    private void write(final TBase<?, ?> base) throws TException {
        final TProtocol protocol = protocolFactory.getProtocol(transport);

        // write chunk header
        writeChunkHeader(protocol);

        // write header
        writeHeader(protocol, locator.headerLookup(base));

        base.write(protocol);

        if (isNeedFlush()) {
            flush();
        }
    }

    private boolean isNeedFlush() {
        return flushHandler != null && transport.getBufferPosition() > chunkSize;
    }

    private void writeChunkHeader(TProtocol protocol) throws TException {
        if (writeChunkHeader) {
            return;
        }

        // write chunk header
        writeHeader(protocol, locator.getChunkHeader());
        writeChunkHeader = true;
    }

    private void writeHeader(final TProtocol protocol, final Header header) throws TException {
        protocol.writeByte(header.getSignature());
        protocol.writeByte(header.getVersion());
        short type = header.getType();
        protocol.writeByte(BytesUtils.writeShort1(type));
        protocol.writeByte(BytesUtils.writeShort2(type));
    }

    // flush & clear
    public void flush() throws TException {
        synchronized (transport) {
            if (flushHandler != null && transport.getBufferPosition() > Header.HEADER_SIZE) {
                flushHandler.handle(transport.getBuffer(), 0, transport.getBufferPosition());
            }
            transport.flush();
            writeChunkHeader = false;
        }
    }

    public ChunkHeaderBufferedTBaseSerializerFlushHandler getFlushHandler() {
        return flushHandler;
    }

    public void setFlushHandler(final ChunkHeaderBufferedTBaseSerializerFlushHandler flushHandler) {
        this.flushHandler = flushHandler;
    }

    public TTransport getTransport() {
        return transport;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("transport=").append(transport).append(", ");
        sb.append("chunkSize=").append(chunkSize);
        sb.append("}");

        return sb.toString();
    }

    TSpanChunk toSpanChunk(TSpan span) {
        // create TSpanChunk
        final TSpanChunk spanChunk = new TSpanChunk();
        spanChunk.setSpanEventList(span.getSpanEventList());
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