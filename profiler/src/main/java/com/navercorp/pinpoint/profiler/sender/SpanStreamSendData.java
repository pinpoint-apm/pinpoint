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

package com.navercorp.pinpoint.profiler.sender;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.profiler.util.ObjectPool;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;

/**
 * @author Taejin Koo
 */
public class SpanStreamSendData {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ByteBuffer[] components;

    private final SpanStreamSendDataSerializer encoder;

    private final List<HeaderTBaseSerializer> usedSerializerList;
    
    private final ObjectPool<HeaderTBaseSerializer> serializerPool;

    private final int maxBufferSize;
    private final int maxGatheringComponentCount;
    private final int maxAvaiableBufferCapacity;

    private int avaiableBufferCapacity;
    private int chunkCount = 0;
    private int componentsIndex = 0;

    private SpanStreamSendDataMode mode;

    private long firstAccessTime = -1;
    
    public SpanStreamSendData(int maxPacketSize, int maxGatheringComponentCount, ObjectPool<HeaderTBaseSerializer> serializerPool) {
        this.maxBufferSize = maxPacketSize;
        this.maxGatheringComponentCount = maxGatheringComponentCount - 2;
        this.maxAvaiableBufferCapacity = maxPacketSize - SpanStreamConstants.START_PROTOCOL_BUFFER_SIZE - SpanStreamConstants.END_PROTOCOL_BUFFER_SIZE;
        this.avaiableBufferCapacity = maxAvaiableBufferCapacity;

        if (this.maxAvaiableBufferCapacity <= 0) {
            throw new IllegalArgumentException("Illegal maxPacketSize(" + maxPacketSize + ")");
        }

        if (this.maxGatheringComponentCount <= 0) {
            throw new IllegalArgumentException("Illegal maxGatheringComponentCount(" + maxGatheringComponentCount + ")");
        }

        this.components = new ByteBuffer[maxGatheringComponentCount];
        
        this.encoder = new SpanStreamSendDataSerializer();

        this.usedSerializerList = new ArrayList<HeaderTBaseSerializer>();
        
        this.serializerPool = serializerPool;
        
        this.mode = SpanStreamSendDataMode.WAIT_BUFFER;
    }

    public boolean addBuffer(byte[] buffer) {
        return addBuffer(ByteBuffer.wrap(buffer), null);
    }

    public boolean addBuffer(byte[] buffer, HeaderTBaseSerializer headerTbaseSerializer) {
        return addBuffer(ByteBuffer.wrap(buffer), headerTbaseSerializer);
    }

    public boolean addBuffer(ByteBuffer buffer) {
        return addBuffer(buffer, null);
    }

    public boolean addBuffer(ByteBuffer buffer, HeaderTBaseSerializer headerTbaseSerializer) {
        ByteBuffer[] bufferArray = new ByteBuffer[1];
        bufferArray[0] = buffer;
        return addBuffer(bufferArray, null);
    }

    public boolean addBuffer(ByteBuffer[] buffers) {
        return addBuffer(buffers, null);
    }

    public boolean addBuffer(ByteBuffer[] buffers, HeaderTBaseSerializer headerTbaseSerializer) {
        if (firstAccessTime == -1) {
            firstAccessTime = System.currentTimeMillis();
        }
        
        if (!checkAddAvaiable(buffers)) {
            return false;
        }

        if (headerTbaseSerializer != null) {
            usedSerializerList.add(headerTbaseSerializer);
        }

        chunkCount++;

        ByteBuffer chunkFlagBuffer = encoder.createByteBuffer(SpanStreamConstants.DEFAULT_CHUNK_FLAG_BUFFER_SIZE);
        components[componentsIndex++] = chunkFlagBuffer;

        int usedBufferLength = 0;
        for (ByteBuffer buffer : buffers) {
            if (buffer.remaining() <= 0) {
                continue;
            }

            components[componentsIndex++] = buffer;
            usedBufferLength += buffer.remaining();
        }

        encoder.putShort(chunkFlagBuffer, (short) (usedBufferLength));
        chunkFlagBuffer.flip();

        this.avaiableBufferCapacity = this.avaiableBufferCapacity - usedBufferLength - SpanStreamConstants.DEFAULT_CHUNK_FLAG_BUFFER_SIZE;
        
        return true;
    }

    private boolean checkAddAvaiable(ByteBuffer[] bufferArray) {
        if (bufferArray == null || bufferArray.length == 0) {
            return false;
        }

        // check components count
        // input buffer + chunk buffer
        if (bufferArray.length + 1 > getAvaiableGatheringComponentsCount()) {
            return false;
        }

        int usedBufferLength = 0;
        for (ByteBuffer buffer : bufferArray) {
            usedBufferLength += buffer.remaining();
        }

        if (usedBufferLength == 0) {
            return false;
        }

        // check buffer total size
        return isAvaiableBufferCapacity(usedBufferLength);
    }
    
    public boolean isAvaiableBufferCapacity(int length) {
        logger.debug("inputdata-length:{}, chunk-length:{}, avaiable-length:{}", length, SpanStreamConstants.DEFAULT_CHUNK_FLAG_BUFFER_SIZE, avaiableBufferCapacity);
        if (length + SpanStreamConstants.DEFAULT_CHUNK_FLAG_BUFFER_SIZE < avaiableBufferCapacity) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAvaiableComponentsCount(int componentsCount) {
        int avaiableGatheringComponentsCount = getAvaiableGatheringComponentsCount();
        logger.debug("inputcomponents-count:{}, avaiablecomponents-count:{}", componentsCount, avaiableGatheringComponentsCount);

        if (componentsCount < avaiableGatheringComponentsCount) {
            return true;
        }
        return false;
    }

    public int getMaxBufferCapacity() {
        return maxBufferSize;
    }

    public int getAvaiableBufferCapacity() {
        return avaiableBufferCapacity;
    }

    public int getAvaiableGatheringComponentsCount() {
        return maxGatheringComponentCount - componentsIndex;
    }

    public ByteBuffer[] getSendBuffers() {
        int writeIndex = 0;

        ByteBuffer[] sendData = new ByteBuffer[2 + componentsIndex];

        sendData[writeIndex++] = encoder.createStartBuffer((byte) chunkCount);
        for (int i = 0; i < componentsIndex; i++) {
            sendData[writeIndex++] = components[i];
        }
        sendData[writeIndex++] = encoder.createEndBuffer();

        return sendData;
    }

    public void setWaitBufferAndFlushMode() {
        setFlushMode(SpanStreamSendDataMode.WAIT_BUFFER_AND_FLUSH);
    }

    public void setFlushMode() {
        setFlushMode(SpanStreamSendDataMode.FLUSH);
    }
    
    public void setFlushMode(SpanStreamSendDataMode mode) {
        this.mode = mode;
    }

    public SpanStreamSendDataMode getFlushMode() {
        return mode;
    }

    public void done() {
        for (HeaderTBaseSerializer eachSerializer : usedSerializerList) {
            serializerPool.returnObject(eachSerializer);
        }
    }

    public long getFirstAccessTime() {
        return firstAccessTime;
    }

    @Override
    public String toString() {
        return "SpanStreamSendData [components=" + Arrays.toString(components) + ", encoder=" + encoder + ", usedSerializerList=" + usedSerializerList
                + ", serializerPool=" + serializerPool + ", maxBufferSize=" + maxBufferSize + ", maxGatheringComponentCount=" + maxGatheringComponentCount
                + ", maxAvaiableBufferCapacity=" + maxAvaiableBufferCapacity + ", avaiableBufferCapacity=" + avaiableBufferCapacity + ", chunkCount="
                + chunkCount + ", componentsIndex=" + componentsIndex + ", mode=" + mode + "]";
    }

}
