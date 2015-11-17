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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.profiler.sender.CompositeSpanStreamData;
import com.navercorp.pinpoint.profiler.sender.SpanStreamSendData;
import com.navercorp.pinpoint.profiler.sender.SpanStreamSendDataFactory;
import com.navercorp.pinpoint.profiler.sender.SpanStreamSendDataMode;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;

/**
 * @author Taejin Koo
 */
public abstract class AbstractSpanStreamSendDataPlaner implements SendDataPlaner {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected CompositeSpanStreamData compositeSpanStreamData;

    private final SpanStreamSendDataFactory spanStreamSendDataFactory;

    public AbstractSpanStreamSendDataPlaner(CompositeSpanStreamData compositeSpanStreamData, SpanStreamSendDataFactory spanStreamSendDataFactory) {
        this.compositeSpanStreamData = compositeSpanStreamData;
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

        int buffersLength = compositeSpanStreamData.getComponentsBufferCapacity();
        if (currentSpanStreamSendData.isAvailableBufferCapacity(buffersLength)) {
            if (currentSpanStreamSendData.getAvailableGatheringComponentsCount() < 2) {
                currentSpanStreamSendData.setFlushMode();
            }
            
            currentSpanStreamSendData.addBuffer(compositeSpanStreamData.getByteBuffer(), serializer);
            
            spanStreamSendDataList.add(currentSpanStreamSendData);
        } else {
            int markStartComponentIndex = 0;
            int markEndComponentIndex = -1;

            int flushBufferSize = 0;
            for (int i = 0; i < compositeSpanStreamData.getComponentsCount(); i++) {
                
                int appendBufferSize = 0;
                if (!compositeSpanStreamData.isLastComponentIndex(i)) {
                    appendBufferSize = getSpanChunkLength();
                }

                flushBufferSize += compositeSpanStreamData.getComponentBufferLength(i);
                if (needFlush(currentSpanStreamSendData, flushBufferSize, appendBufferSize)) {
                    ByteBuffer addBuffer = createByteBuffer(compositeSpanStreamData, markStartComponentIndex, markEndComponentIndex);
                    if (addBuffer != null) {
                        ByteBuffer[] byteBufferArray = new ByteBuffer[2];
                        byteBufferArray[0] = addBuffer;
                        byteBufferArray[1] = getSpanChunkBuffer();
                        
                        currentSpanStreamSendData.addBuffer(byteBufferArray);
                    }
                    
                    currentSpanStreamSendData.setFlushMode();
                    spanStreamSendDataList.add(currentSpanStreamSendData);

                    currentSpanStreamSendData = spanStreamSendDataFactory.create();
                    
                    markStartComponentIndex = i;
                    flushBufferSize = compositeSpanStreamData.getComponentBufferLength(i);
                } 

                markEndComponentIndex = i;

                if (compositeSpanStreamData.isLastComponentIndex(i)) {
                    ByteBuffer addBuffer = createByteBuffer(compositeSpanStreamData, markStartComponentIndex, markEndComponentIndex);
                    currentSpanStreamSendData.addBuffer(addBuffer, serializer);
                    
                    spanStreamSendDataList.add(currentSpanStreamSendData);
                }
            }
        }
        
        return spanStreamSendDataList.iterator();
    }

    private FlushMode plan(SpanStreamSendData spanStreamSendData) {
        int buffersLength = compositeSpanStreamData.getComponentsBufferCapacity();

        if (buffersLength < spanStreamSendData.getAvailableBufferCapacity()) {
            return FlushMode.NORMAL;
        } else if (buffersLength < spanStreamSendData.getMaxBufferCapacity()) {
            return FlushMode.FLUSH_FIRST;
        } else {
            ByteBuffer spanChunkBuffer = getSpanChunkBuffer();

            for (int i = 0; i < compositeSpanStreamData.getComponentsCount(); i++) {
                int currentComponentBufferLength = compositeSpanStreamData.getComponentBufferLength(i);

                if (compositeSpanStreamData.isLastComponentIndex(i)) {
                    if (currentComponentBufferLength > spanStreamSendData.getMaxBufferCapacity()) {
                        throw new IllegalStateException("BufferList has over size buffer. buffer length:" + currentComponentBufferLength);
                    }
                } else {
                    if (currentComponentBufferLength + spanChunkBuffer.remaining() > spanStreamSendData.getMaxBufferCapacity()) {
                        throw new IllegalStateException("BufferList has over size buffer. buffer length:" + currentComponentBufferLength + ", maxCapacity:" + spanStreamSendData.getMaxBufferCapacity());
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
        int chunkCount = 1;

        int availableBufferSize = spanStreamSendData.getAvailableBufferCapacity();

        for (int i = 0; i < compositeSpanStreamData.getComponentsCount(); i++) {
            int currentComponentBufferLength = compositeSpanStreamData.getComponentBufferLength(i);

            if (compositeSpanStreamData.isLastComponentIndex(i)) {
                if (currentComponentBufferLength > availableBufferSize) {
                    chunkCount++;
                }
            } else {
                if (currentComponentBufferLength + getSpanChunkLength() < availableBufferSize) {
                    availableBufferSize -= currentComponentBufferLength;
                } else {
                    chunkCount++;
                    availableBufferSize = spanStreamSendData.getMaxBufferCapacity();
                    availableBufferSize -= currentComponentBufferLength;
                }
            }
        }
        return chunkCount;
    }

    private int calculateWithoutUsingCurrentSendData(SpanStreamSendData spanStreamSendData) {
        int chunkCount = 2;

        int availableBufferSize = spanStreamSendData.getMaxBufferCapacity();

        for (int i = 0; i < compositeSpanStreamData.getComponentsCount(); i++) {
            int currentComponentBufferLength = compositeSpanStreamData.getComponentBufferLength(i);

            if (compositeSpanStreamData.isLastComponentIndex(i)) {
                if (currentComponentBufferLength > availableBufferSize) {
                    chunkCount++;
                }
            } else {
                if (currentComponentBufferLength + getSpanChunkLength() < availableBufferSize) {
                    availableBufferSize -= currentComponentBufferLength;
                } else {
                    chunkCount++;
                    availableBufferSize = spanStreamSendData.getMaxBufferCapacity();
                    availableBufferSize -= currentComponentBufferLength;
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

    private ByteBuffer createByteBuffer(CompositeSpanStreamData compositeSpanStreamData, int fromComponentBufferIndex, int toComponentBufferIndex) {
        if (toComponentBufferIndex == -1) {
            return null;
        }

        return compositeSpanStreamData.getByteBuffer(fromComponentBufferIndex, toComponentBufferIndex);
    }

    abstract protected int getSpanChunkLength();

    abstract protected ByteBuffer getSpanChunkBuffer();

    private enum FlushMode {
        FLUSH_FIRST, NORMAL
    }

}
