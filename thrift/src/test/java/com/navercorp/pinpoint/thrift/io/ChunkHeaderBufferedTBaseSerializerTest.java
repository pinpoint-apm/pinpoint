package com.nhn.pinpoint.thrift.io;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.thrift.TException;
import org.junit.Test;

import com.nhn.pinpoint.thrift.dto.TSpanChunk;

public class ChunkHeaderBufferedTBaseSerializerTest {

    private boolean flush = false;

    @Test
    public void add() throws TException {
        ChunkHeaderBufferedTBaseSerializer serializer = new ChunkHeaderBufferedTBaseSerializer(1024);
        serializer.setFlushHandler(new ChunkHeaderBufferedTBaseSerializerFlushHandler() {

            @Override
            public void handle(byte[] buffer, int offset, int length) {
                System.out.println("overflower buffer offset=" + offset + ", length= " + length + ", content=" + Arrays.toString(buffer));
                flush = true;
            }
        });

        // add and flush
        flush = false;
        TSpanChunk chunk = new TSpanMockBuilder().buildChunk(1, 1024);
        serializer.add(chunk);
        System.out.println(serializer);
        assertTrue(flush);

        // add and flush * 3
        flush = false;
        chunk = new TSpanMockBuilder().buildChunk(3, 1024);
        serializer.add(chunk);
        System.out.println(serializer);
        assertTrue(flush);

        // add
        flush = false;
        chunk = new TSpanMockBuilder().buildChunk(3, 10);
        serializer.add(chunk);
        System.out.println(serializer);
        assertFalse(flush);

        // flush
        serializer.flush();
        assertTrue(flush);
    }
}