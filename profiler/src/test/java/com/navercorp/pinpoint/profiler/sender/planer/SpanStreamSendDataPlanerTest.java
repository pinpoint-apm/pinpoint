/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.sender.planer;

import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.sender.HeaderTBaseSerializerPoolFactory;
import com.navercorp.pinpoint.profiler.sender.PartitionedByteBufferLocator;
import com.navercorp.pinpoint.profiler.sender.SpanStreamSendData;
import com.navercorp.pinpoint.profiler.sender.SpanStreamSendDataFactory;
import com.navercorp.pinpoint.profiler.sender.SpanStreamSendDataSerializer;
import com.navercorp.pinpoint.profiler.util.ObjectPool;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.mock;

public class SpanStreamSendDataPlanerTest {

    private static ObjectPool<HeaderTBaseSerializer> objectPool;

    @BeforeClass
    public static void setUp() {

        HeaderTBaseSerializerPoolFactory serializerFactory = new HeaderTBaseSerializerPoolFactory(true, 1000, true);
        objectPool = new ObjectPool<HeaderTBaseSerializer>(serializerFactory, 16);

    }

    @Test
    public void spanStreamSendDataPlanerTest() throws Exception {
        int spanEventSize = 10;

        SpanStreamSendDataSerializer serializer = new SpanStreamSendDataSerializer();

        HeaderTBaseSerializerFactory headerTBaseSerializerFactory = new HeaderTBaseSerializerFactory();

        List<TSpanEvent> originalSpanEventList = createSpanEventList(spanEventSize);
        TSpan span = createSpan(originalSpanEventList);

        PartitionedByteBufferLocator partitionedByteBufferLocator = serializer.serializeSpanStream(headerTBaseSerializerFactory.createSerializer(), span);
        SpanStreamSendDataFactory factory = new SpanStreamSendDataFactory(100, 50, objectPool);
        List<TSpanEvent> spanEventList = getSpanEventList(partitionedByteBufferLocator, factory);

        partitionedByteBufferLocator = serializer.serializeSpanStream(headerTBaseSerializerFactory.createSerializer(), span);
        factory = new SpanStreamSendDataFactory(objectPool);
        List<TSpanEvent> spanEventList2 = getSpanEventList(partitionedByteBufferLocator, factory);

        Assert.assertEquals(spanEventSize, spanEventList.size());
        Assert.assertEquals(spanEventSize, spanEventList2.size());
    }

    private TSpan createSpan(List<TSpanEvent> spanEventList) {
        final String agentId = "agentId";


        final TSpan span = new TSpan();
        span.setAgentId(agentId);

        for (TSpanEvent spanEvent : spanEventList) {
            span.addToSpanEventList(spanEvent);
        }


        return span;
    }

    private List<TSpanEvent> createSpanEventList(int size) {
        List<TSpanEvent> spanEventList = new ArrayList<TSpanEvent>(size);
        for (int i = 0; i < size; i++) {
            TSpanEvent spanEvent = new TSpanEvent();
            spanEvent.setStartElapsed(1);
            spanEvent.setEndElapsed(2);

            spanEventList.add(spanEvent);
        }

        return spanEventList;
    }

    private List<TSpanEvent> getSpanEventList(PartitionedByteBufferLocator partitionedByteBufferLocator, SpanStreamSendDataFactory factory) throws Exception {
        List<TSpanEvent> spanEventList = new ArrayList<TSpanEvent>();

        SpanChunkStreamSendDataPlaner planer = new SpanChunkStreamSendDataPlaner(partitionedByteBufferLocator, factory);
        Iterator<SpanStreamSendData> iterator = planer.getSendDataIterator();
        while (iterator.hasNext()) {
            SpanStreamSendData data = iterator.next();

            ByteBuffer[] sendBuffers = data.getSendBuffers();
            byte[] relatedBuffer = getSpanRelatedBuffer(sendBuffers);

            List<TSpanEvent> result = deserialize(relatedBuffer);

            spanEventList.addAll(result);
        }

        return spanEventList;
    }

    private byte[] getSpanRelatedBuffer(ByteBuffer[] buffers) {
        int totalBufferSize = 0;

        for (ByteBuffer buffer : buffers) {
            totalBufferSize += buffer.remaining();
        }

        byte[] relatedBuffer = new byte[totalBufferSize];

        int offset = 0;
        for (ByteBuffer buffer : buffers) {
            int bufferlength = buffer.remaining();

            buffer.get(relatedBuffer, offset, bufferlength);
            offset += bufferlength;
        }

        return relatedBuffer;
    }


    private List<TSpanEvent> deserialize(byte[] data) throws TException {
        ByteBuffer bb = ByteBuffer.wrap(data);

        bb.get();
        bb.get();
        int chunkSize = bb.get();

        List<TSpanEvent> eventList = new ArrayList<TSpanEvent>();

        for (int i = 0; i < chunkSize; i++) {
            short componentSize = bb.getShort();
            byte[] component = new byte[componentSize];
            bb.get(component);

            HeaderTBaseDeserializer deserialize = new HeaderTBaseDeserializerFactory().createDeserializer();
            List<Message<TBase<?, ?>>> value = deserialize.deserializeList(component);

            for (Message<TBase<?, ?>> request : value) {
                TBase<?, ?> tbase = request.getData();
                if (tbase instanceof TSpanEvent) {
                    eventList.add((TSpanEvent) tbase);
                }
            }

        }
        return eventList;
    }


}
