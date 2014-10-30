package com.nhn.pinpoint.thrift.io;

import static org.junit.Assert.*;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.junit.Test;

import com.nhn.pinpoint.thrift.dto.TSpanChunk;

public class ChunkHeaderTBaseDeserializerTest {
    private static final TBaseLocator DEFAULT_TBASE_LOCATOR = new DefaultTBaseLocator();
    private static final TProtocolFactory DEFAULT_PROTOCOL_FACTORY = new TCompactProtocol.Factory();

    @Test
    public void test() throws Exception {

        final ChunkHeaderTBaseDeserializer deserializer = new ChunkHeaderTBaseDeserializer(DEFAULT_PROTOCOL_FACTORY, DEFAULT_TBASE_LOCATOR);

        ChunkHeaderBufferedTBaseSerializer serializer = new ChunkHeaderBufferedTBaseSerializer(1024);
        serializer.setFlushHandler(new ChunkHeaderBufferedTBaseSerializerFlushHandler() {
            @Override
            public void handle(byte[] buffer, int offset, int length) {
                try {
                    deserializer.deserialize(buffer, offset, length);
                } catch (TException e) {
                    fail("Failed to deserialize. " + e.getMessage());
                }
            }
        });

        TSpanChunk chunk = new TSpanMockBuilder().buildChunk(3, 10);
        serializer.add(chunk);
        serializer.flush();
    }
}