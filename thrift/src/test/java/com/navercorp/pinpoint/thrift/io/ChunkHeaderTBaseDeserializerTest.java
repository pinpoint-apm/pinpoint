package com.nhn.pinpoint.thrift.io;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.junit.Test;

import com.nhn.pinpoint.thrift.dto.TSpanChunk;

public class ChunkHeaderTBaseDeserializerTest {
    private static final TBaseLocator DEFAULT_TBASE_LOCATOR = new DefaultTBaseLocator();
    private static final TProtocolFactory DEFAULT_PROTOCOL_FACTORY = new TCompactProtocol.Factory();

    @Test
    public void deserialize() throws Exception {
        final ChunkHeaderTBaseDeserializer deserializer = new ChunkHeaderTBaseDeserializer(DEFAULT_PROTOCOL_FACTORY, DEFAULT_TBASE_LOCATOR);

        UnsafeByteArrayOutputStream out = new UnsafeByteArrayOutputStream();
        ChunkHeaderBufferedTBaseSerializer serializer = new ChunkHeaderBufferedTBaseSerializer(out, DEFAULT_PROTOCOL_FACTORY, DEFAULT_TBASE_LOCATOR);
        TSpanChunk chunk = new TSpanMockBuilder().buildChunk(3, 10);
        serializer.add(chunk);

        List<TBase<?, ?>> list = deserializer.deserialize(serializer.getTransport().getBuffer(), 0, serializer.getTransport().getBufferPosition());
        assertEquals(1, list.size());
        TSpanChunk result = (TSpanChunk) list.get(0);
        assertEquals(3, result.getSpanEventList().size());
    }
}