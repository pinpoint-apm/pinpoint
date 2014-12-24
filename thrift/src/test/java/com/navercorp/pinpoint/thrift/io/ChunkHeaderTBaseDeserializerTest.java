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

import java.util.List;

import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.junit.Test;

import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.io.ChunkHeaderBufferedTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.ChunkHeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;
import com.navercorp.pinpoint.thrift.io.TBaseLocator;
import com.navercorp.pinpoint.thrift.io.UnsafeByteArrayOutputStream;

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