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

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.junit.Test;

import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.io.ChunkHeaderBufferedTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.ChunkHeaderBufferedTBaseSerializerFlushHandler;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;
import com.navercorp.pinpoint.thrift.io.TBaseLocator;
import com.navercorp.pinpoint.thrift.io.UnsafeByteArrayOutputStream;

public class ChunkHeaderBufferedTBaseSerializerTest {
    private static final TBaseLocator DEFAULT_TBASE_LOCATOR = new DefaultTBaseLocator();
    private static final TProtocolFactory DEFAULT_PROTOCOL_FACTORY = new TCompactProtocol.Factory();

    private AtomicInteger flushCounter = new AtomicInteger(0);

    @Test
    public void add() throws TException {
        final int chunkSize = 1024;
        
        UnsafeByteArrayOutputStream out = new UnsafeByteArrayOutputStream();
        ChunkHeaderBufferedTBaseSerializer serializer = new ChunkHeaderBufferedTBaseSerializer(out, DEFAULT_PROTOCOL_FACTORY, DEFAULT_TBASE_LOCATOR);
        serializer.setChunkSize(1024);
        serializer.setFlushHandler(new ChunkHeaderBufferedTBaseSerializerFlushHandler() {

            @Override
            public void handle(byte[] buffer, int offset, int length) {
                flushCounter.incrementAndGet();
            }
        });

        // add and flush
        flushCounter.set(0);
        TSpanChunk chunk = new TSpanMockBuilder().buildChunk(1, 1024);
        serializer.add(chunk);
        assertEquals(1, flushCounter.get());

        // add and flush * 3
        flushCounter.set(0);
        chunk = new TSpanMockBuilder().buildChunk(3, 1024);
        serializer.add(chunk);
        assertEquals(3, flushCounter.get());

        // add
        flushCounter.set(0);
        chunk = new TSpanMockBuilder().buildChunk(3, 10);
        serializer.add(chunk);
        assertEquals(0, flushCounter.get());

        // flush
        flushCounter.set(0);
        serializer.flush();
        assertEquals(1, flushCounter.get());
    }
}