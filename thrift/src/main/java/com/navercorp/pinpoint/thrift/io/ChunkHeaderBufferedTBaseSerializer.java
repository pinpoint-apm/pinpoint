package com.nhn.pinpoint.thrift.io;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;

import com.nhn.pinpoint.thrift.dto.TSpan;
import com.nhn.pinpoint.thrift.dto.TSpanChunk;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;

public class ChunkHeaderBufferedTBaseSerializer {
    private static final String FIELD_NAME_SPAN_EVENT_LIST = "spanEventList";

    // reuse byte buffer
    private final ByteArrayOutputStream out;
    // span event list serialized buffer
    private final TBaseStream eventStream;
    // header
    private final TBaseLocator locator;
    // reset chunk header
    private boolean writeChunkHeader = false;
    // flush size
    private final int flushSize;
    // flush handler
    private ChunkHeaderBufferedTBaseSerializerFlushHandler flushHandler;

    public ChunkHeaderBufferedTBaseSerializer(int flushSize) {
        out = new UnsafeByteArrayOutputStream(flushSize);
        eventStream = new TBaseStream(flushSize);
        locator = new DefaultTBaseLocator();
        this.flushSize = flushSize;
    }

    public void add(TBase<?, ?> base) throws TException {
        synchronized (out) {
            if (base instanceof TSpan) {
                addTSpan(base);
            } else if (base instanceof TSpanChunk) {
                addTSpanChunk(base);
            } else {
                write(base);
            }
        }
    }

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
            write(chunk, FIELD_NAME_SPAN_EVENT_LIST, eventStream.split(flushSize));
            while (!eventStream.isEmpty()) {
                write(chunk, FIELD_NAME_SPAN_EVENT_LIST, eventStream.split(flushSize));
            }
        } finally {
            eventStream.clear();
        }
    }

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
            write(span, FIELD_NAME_SPAN_EVENT_LIST, eventStream.split(flushSize));
            while (!eventStream.isEmpty()) {
                final TSpanChunk spanChunk = toSpanChunk(span);
                write(spanChunk, FIELD_NAME_SPAN_EVENT_LIST, eventStream.split(flushSize));
            }
        } finally {
            eventStream.clear();
        }
    }

    private void write(final TBase<?, ?> base, final String fieldName, final List<TBaseStreamNode> list) throws TException {
        final ReplaceListCompactProtocol protocol = new ReplaceListCompactProtocol(new ByteArrayOutputStreamTransport(out));

        // write chunk header
        writeChunkHeader(protocol);
        
        // write header
        writeHeader(protocol, locator.headerLookup(base));
        if (list != null && list.size() > 0) {
            protocol.addReplaceField(fieldName, list);
        }

        base.write(protocol);
        
        if (isOverflow()) {
            flush();
        }
    }

    private void write(final TBase<?, ?> base) throws TException {
        final TCompactProtocol protocol = new TCompactProtocol(new ByteArrayOutputStreamTransport(out));

        // write chunk header
        writeChunkHeader(protocol);

        // write header
        writeHeader(protocol, locator.headerLookup(base));

        base.write(protocol);
        
        if (isOverflow()) {
            flush();
        }
    }

    private boolean isOverflow() {
        return out.size() > flushSize;
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
        // 프로토콜 변경에 관계 없이 고정 사이즈의 데이터로 인코딩 하도록 변경.
        short type = header.getType();
        protocol.writeByte(BytesUtils.writeShort1(type));
        protocol.writeByte(BytesUtils.writeShort2(type));
    }

    // flush & clear
    public void flush() {
        synchronized (out) {
            if (flushHandler != null && out.size() > Header.HEADER_SIZE) {
                flushHandler.handle(out.toByteArray(), 0, out.size());
            }
            out.reset();
            writeChunkHeader = false;
        }
    }

    public ChunkHeaderBufferedTBaseSerializerFlushHandler getFlushHandler() {
        return flushHandler;
    }

    public void setFlushHandler(ChunkHeaderBufferedTBaseSerializerFlushHandler flushHandler) {
        this.flushHandler = flushHandler;
    }

    public String toString() {
        
        return toStringBinary(out.toByteArray(), 0, out.size());
//        StringBuilder sb = new StringBuilder();
//        sb.append("{");
//        sb.append("bufferSize=").append(out.size()).append(", ");
//        sb.append("flushSize=").append(flushSize);
//        sb.append("}");
//
//        return sb.toString();
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
        spanChunk.setServiceType(span.getServiceType());
        spanChunk.setServiceTypeIsSet(true);
        spanChunk.setTransactionId(span.getTransactionId());
        spanChunk.setTransactionIdIsSet(true);
        spanChunk.setSpanId(span.getSpanId());
        spanChunk.setSpanIdIsSet(true);
        spanChunk.setEndPoint(span.getEndPoint());
        spanChunk.setEndPointIsSet(true);

        return spanChunk;
    }
    
    
    
    static String toStringBinary(final byte[] b, int off, int len) {
        StringBuilder result = new StringBuilder();
        for (int i = off; i < off + len; ++i) {
            int ch = b[i] & 0xFF;
            if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || " `~!@#$%^&*()-_=+[]{}|;:'\",.<>/?".indexOf(ch) >= 0) {
                result.append((char) ch);
            } else {
                result.append(String.format("\\x%02X", ch));
            }
        }
        return result.toString();
    }

}