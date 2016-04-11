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
import com.navercorp.pinpoint.profiler.sender.SpanStreamSendData;
import com.navercorp.pinpoint.profiler.sender.SpanStreamSendDataFactory;
import com.navercorp.pinpoint.profiler.sender.SpanStreamSendDataMode;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Taejin Koo
 */
public abstract class AbstractSpanStreamSendDataPlaner implements SendDataPlaner {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected PartitionedByteBufferLocator partitionedByteBufferLocator;

    private final SpanStreamSendDataFactory spanStreamSendDataFactory;

    public AbstractSpanStreamSendDataPlaner(PartitionedByteBufferLocator partitionedByteBufferLocator, SpanStreamSendDataFactory spanStreamSendDataFactory) {
        this.partitionedByteBufferLocator = partitionedByteBufferLocator;
        this.spanStreamSendDataFactory = spanStreamSendDataFactory;
    }

    @Override
    public Iterator<SpanStreamSendData> getSendDataIterator() throws IOException {
        return getSendDataIterator(spanStreamSendDataFactory.create());
    }

    @Override
    public Iterator<SpanStreamSendData> getSendDataIterator(SpanStreamSendData spanStreamSendData) throws IOException {
        return getSendDataIterator(spanStreamSendData, null);
    }

    @Override
    public Iterator<SpanStreamSendData> getSendDataIterator(SpanStreamSendData spanStreamSendData, HeaderTBaseSerializer serializer) throws IOException {
        logger.info("process");

        List<SpanStreamSendData> spanStreamSendDataList = new ArrayList<SpanStreamSendData>();
        SpanStreamSendData currentSpanStreamSendData = spanStreamSendData;

        FlushMode mode = plan(spanStreamSendData);
        if (mode == FlushMode.FLUSH_FIRST) {
            spanStreamSendDataList.add(currentSpanStreamSendData);
            currentSpanStreamSendData.setFlushMode(SpanStreamSendDataMode.WAIT_BUFFER_AND_FLUSH);
            currentSpanStreamSendData = spanStreamSendDataFactory.create();
        }

        int byteBufferCapacity = partitionedByteBufferLocator.getTotalByteBufferCapacity();
        if (currentSpanStreamSendData.isAvailableBufferCapacity(byteBufferCapacity)) {
            if (currentSpanStreamSendData.getAvailableGatheringComponentsCount() < 2) {
                currentSpanStreamSendData.setFlushMode();
            }
            
            currentSpanStreamSendData.addBuffer(partitionedByteBufferLocator.getByteBuffer(), serializer);
            
            spanStreamSendDataList.add(currentSpanStreamSendData);
        } else {
            int markFromPartitionIndex = 0;
            int markToPartitionIndex = -1;

            int flushBufferCapacity = 0;
            for (int i = 0; i < partitionedByteBufferLocator.getPartitionedCount(); i++) {
                
                int appendBufferSize = 0;
                if (!partitionedByteBufferLocator.isLastPartitionIndex(i)) {
                    appendBufferSize = getSpanChunkLength();
                }

                flushBufferCapacity += partitionedByteBufferLocator.getByteBufferCapacity(i);
                if (needFlush(currentSpanStreamSendData, flushBufferCapacity, appendBufferSize)) {
                    ByteBuffer addBuffer = getByteBuffer(partitionedByteBufferLocator, markFromPartitionIndex, markToPartitionIndex);
                    if (addBuffer != null) {
                        ByteBuffer[] byteBufferArray = new ByteBuffer[2];
                        byteBufferArray[0] = addBuffer;
                        byteBufferArray[1] = getSpanChunkBuffer();
                        
                        currentSpanStreamSendData.addBuffer(byteBufferArray);
                    }
                    
                    currentSpanStreamSendData.setFlushMode();
                    spanStreamSendDataList.add(currentSpanStreamSendData);

                    currentSpanStreamSendData = spanStreamSendDataFactory.create();
                    
                    markFromPartitionIndex = i;
                    flushBufferCapacity = partitionedByteBufferLocator.getByteBufferCapacity(i);
                } 

                markToPartitionIndex = i;

                if (partitionedByteBufferLocator.isLastPartitionIndex(i)) {
                    ByteBuffer addBuffer = getByteBuffer(partitionedByteBufferLocator, markFromPartitionIndex, markToPartitionIndex);
                    currentSpanStreamSendData.addBuffer(addBuffer, serializer);
                    
                    spanStreamSendDataList.add(currentSpanStreamSendData);
                }
            }
        }
        
        return spanStreamSendDataList.iterator();
    }

    private FlushMode plan(SpanStreamSendData spanStreamSendData) {
        int byteBufferCapacity = partitionedByteBufferLocator.getTotalByteBufferCapacity();

        if (byteBufferCapacity < spanStreamSendData.getAvailableBufferCapacity()) {
            return FlushMode.NORMAL;
        } else if (byteBufferCapacity < spanStreamSendData.getMaxBufferCapacity()) {
            return FlushMode.FLUSH_FIRST;
        } else {
            ByteBuffer spanChunkBuffer = getSpanChunkBuffer();

            for (int i = 0; i < partitionedByteBufferLocator.getPartitionedCount(); i++) {
                int currentBufferCapacity = partitionedByteBufferLocator.getByteBufferCapacity(i);

                if (partitionedByteBufferLocator.isLastPartitionIndex(i)) {
                    if (currentBufferCapacity > spanStreamSendData.getMaxBufferCapacity()) {
                        throw new IllegalStateException("BufferList has over size buffer. buffer length:" + currentBufferCapacity);
                    }
                } else {
                    if (currentBufferCapacity + spanChunkBuffer.remaining() > spanStreamSendData.getMaxBufferCapacity()) {
                        throw new IllegalStateException("BufferList has over size buffer. buffer length:" + currentBufferCapacity + ", maxCapacity:" + spanStreamSendData.getMaxBufferCapacity());
                    }
                }
            }

            return plan0(spanStreamSendData);
        }
    }

    private FlushMode plan0(SpanStreamSendData spanStreamSendData) {
        int normalModeChunkSize = calculateWithUsingCurrentSendData(spanStreamSendData);
        int flushFirstModeChunkSize = calculateWithoutUsingCurrentSendData(spanStreamSendData);

        if (normalModeChunkSize > flushFirstModeChunkSize) {
            return FlushMode.FLUSH_FIRST;
        } else {
            return FlushMode.NORMAL;
        }
    }

    private int calculateWithUsingCurrentSendData(SpanStreamSendData spanStreamSendData) {
        int chunkCount = 0;
        int availableBufferCapacity = spanStreamSendData.getAvailableBufferCapacity();

        return chunkCount + getNeedsChunkCount(spanStreamSendData, availableBufferCapacity);
    }

    private int calculateWithoutUsingCurrentSendData(SpanStreamSendData spanStreamSendData) {
        int chunkCount = 1;
        int availableBufferCapacity = spanStreamSendData.getMaxBufferCapacity();

        return chunkCount + getNeedsChunkCount(spanStreamSendData, availableBufferCapacity);
    }

    private int getNeedsChunkCount(SpanStreamSendData spanStreamSendData, int availableCurrentBufferCapacity) {
        int chunkCount = 1;
        int availableBufferCapacity = availableCurrentBufferCapacity;

        for (int i = 0; i < partitionedByteBufferLocator.getPartitionedCount(); i++) {
            int partitionByteBufferCapacity = partitionedByteBufferLocator.getByteBufferCapacity(i);

            if (partitionedByteBufferLocator.isLastPartitionIndex(i)) {
                if (partitionByteBufferCapacity > availableBufferCapacity) {
                    chunkCount++;
                }
            } else {
                if (partitionByteBufferCapacity + getSpanChunkLength() < availableBufferCapacity) {
                    availableBufferCapacity -= partitionByteBufferCapacity;
                } else {
                    chunkCount++;
                    availableBufferCapacity = spanStreamSendData.getMaxBufferCapacity();
                    availableBufferCapacity -= partitionByteBufferCapacity;
                }
            }
        }
        return chunkCount;
    }

    private boolean needFlush(SpanStreamSendData spanStreamSendData, int length, int delemeterBufferSize) {
        if (!spanStreamSendData.isAvailableBufferCapacity(length + delemeterBufferSize)) {
            return true;
        }

        int availableComponentsCount = 1;
        if (delemeterBufferSize > 0) {
            availableComponentsCount++;
        }

        if (!spanStreamSendData.isAvailableComponentsCount(availableComponentsCount)) {
            return true;
        }

        return false;
    }

    private ByteBuffer getByteBuffer(PartitionedByteBufferLocator partitionedByteBufferLocator, int fromPartitionIndex, int toPartitionIndex) {
        if (toPartitionIndex == -1) {
            return null;
        }

        return partitionedByteBufferLocator.getByteBuffer(fromPartitionIndex, toPartitionIndex);
    }

    abstract protected int getSpanChunkLength();

    abstract protected ByteBuffer getSpanChunkBuffer();

    private enum FlushMode {
        FLUSH_FIRST, NORMAL
    }

}
