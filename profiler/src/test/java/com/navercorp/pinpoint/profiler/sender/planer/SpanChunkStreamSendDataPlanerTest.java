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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.profiler.context.DefaultSpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessor;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessorV1;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.thrift.DefaultTransactionIdEncoder;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TransactionIdEncoder;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.context.thrift.SpanThriftMessageConverter;
import com.navercorp.pinpoint.profiler.sender.HeaderTBaseSerializerPoolFactory;
import com.navercorp.pinpoint.profiler.sender.PartitionedByteBufferLocator;
import com.navercorp.pinpoint.profiler.sender.SpanStreamSendData;
import com.navercorp.pinpoint.profiler.sender.SpanStreamSendDataFactory;
import com.navercorp.pinpoint.profiler.sender.SpanStreamSendDataSerializer;
import com.navercorp.pinpoint.profiler.util.ObjectPool;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.mock;

public class SpanChunkStreamSendDataPlanerTest {

    private final String applicationName = "applicationName";
    private final String agentId = "agentId";
    private final long agentStartTime = System.currentTimeMillis();
    private final ServiceType applicationServiceType = ServiceType.STAND_ALONE;
    private final TransactionIdEncoder encoder = new DefaultTransactionIdEncoder(agentId, agentStartTime);

    private SpanProcessor<TSpan, TSpanChunk> spanPostProcessor = new SpanProcessorV1();
    private TraceRoot traceRoot;
    private MessageConverter<TBase<?, ?>> messageConverter =
            new SpanThriftMessageConverter(applicationName, agentId, agentStartTime, applicationServiceType.getCode(), encoder, spanPostProcessor);

    private ObjectPool<HeaderTBaseSerializer> objectPool;

    @Before
    public void before() {
        HeaderTBaseSerializerPoolFactory serializerFactory = new HeaderTBaseSerializerPoolFactory(true, 1000, true);
        this.objectPool = new ObjectPool<HeaderTBaseSerializer>(serializerFactory, 16);

        this.traceRoot = newTraceRoot();
    }

    private TraceRoot newTraceRoot() {
        TraceId traceId = new DefaultTraceId(agentId, 0, 100);
        return new DefaultTraceRoot(traceId, agentId, System.currentTimeMillis(), 0);
    }

    @Test
    public void spanChunkStreamSendDataPlanerTest() throws Exception {
        int spanEventSize = 10;

        SpanStreamSendDataSerializer serializer = new SpanStreamSendDataSerializer();

        HeaderTBaseSerializerFactory headerTBaseSerializerFactory = new HeaderTBaseSerializerFactory();

        List<SpanEvent> originalSpanEventList = createSpanEventList(spanEventSize);
        SpanChunk spanChunk = new DefaultSpanChunk(traceRoot, originalSpanEventList);
        TBase<?, ?> tSpanChunk = messageConverter.toMessage(spanChunk);

        PartitionedByteBufferLocator partitionedByteBufferLocator = serializer.serializeSpanChunkStream(headerTBaseSerializerFactory.createSerializer(), (TSpanChunk) tSpanChunk);
        SpanStreamSendDataFactory factory = new SpanStreamSendDataFactory(100, 50, objectPool);
        List<TSpanEvent> spanEventList = getSpanEventList(partitionedByteBufferLocator, factory);

        partitionedByteBufferLocator = serializer.serializeSpanChunkStream(headerTBaseSerializerFactory.createSerializer(), (TSpanChunk) tSpanChunk);
        factory = new SpanStreamSendDataFactory(objectPool);
        List<TSpanEvent> spanEventList2 = getSpanEventList(partitionedByteBufferLocator, factory);

        Assert.assertEquals(spanEventSize, spanEventList.size());
        Assert.assertEquals(spanEventSize, spanEventList2.size());
    }

    private List<SpanEvent> createSpanEventList(int size) {

        List<SpanEvent> spanEventList = new ArrayList<SpanEvent>(size);
        for (int i = 0; i < size; i++) {
            SpanEvent spanEvent = new SpanEvent();
            spanEvent.markStartTime();
            spanEvent.setElapsedTime(1);

            spanEventList.add(spanEvent);
        }

        return spanEventList;
    }

    private List<TSpanEvent> getSpanEventList(PartitionedByteBufferLocator spanData, SpanStreamSendDataFactory factory) throws Exception {
        List<TSpanEvent> spanEventList = new ArrayList<TSpanEvent>();

        SpanChunkStreamSendDataPlaner planer = new SpanChunkStreamSendDataPlaner(spanData, factory);
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
