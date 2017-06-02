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

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class PartitionedByteBufferLocator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final byte[] baseBuffer;
    private final List<Index> partitionIndexList;

    private final int partitionedBufferCapacity;

    private PartitionedByteBufferLocator(byte[] buffer, List<Index> partitionIndexList) {
        if (buffer == null) {
            throw new NullPointerException("buffer must not be null.");
        }
        if (CollectionUtils.isEmpty(partitionIndexList)) {
            throw new NullPointerException("buffer must not be null or zero.");
        }

        Index firstPartitionIndex = ListUtils.getFirst(partitionIndexList);
        Index lastPartitionIndex = ListUtils.getLast(partitionIndexList);

        int partitionedBufferCapacity = lastPartitionIndex.getEndPosition() - firstPartitionIndex.getStartPosition();
        if (partitionedBufferCapacity > buffer.length) {
            throw new IllegalArgumentException("partitionedBufferCapacity(" + partitionedBufferCapacity + ") > bufferCapacity(" + buffer.length + ").");
        }

        if (lastPartitionIndex.getEndPosition() > buffer.length) {
            throw new IllegalArgumentException("lastPartitionEndPosition(" + lastPartitionIndex.getEndPosition() + ") > bufferCapacity(" + buffer.length + ").");
        }

        this.baseBuffer = buffer;

        this.partitionIndexList = new ArrayList<Index>(partitionIndexList);
        this.partitionedBufferCapacity = partitionedBufferCapacity;
    }

    public int getPartitionedCount() {
        return partitionIndexList.size();
    }

    public int getTotalByteBufferCapacity() {
        return partitionedBufferCapacity;
    }

    public int getByteBufferCapacity(int partitionIndex) {
        if (partitionIndex < 0) {
            throw new IllegalArgumentException("partitionIndex = " + partitionIndex);
        }
        if (partitionIndex >= getPartitionedCount()) {
            throw new IllegalArgumentException("partitionIndex(" + partitionIndex + ") >= partitionedCount(" + getPartitionedCount() + ").");
        }

        return partitionIndexList.get(partitionIndex).getCapacity();
    }

    public int getByteBufferCapacity(int fromPartitionIndex, int toPartitionIndex) {
        checkRangeValidation(fromPartitionIndex, toPartitionIndex, getPartitionedCount());

        int capacity = 0;
        for (int i = fromPartitionIndex; i < toPartitionIndex; i++) {
            capacity += partitionIndexList.get(i).getCapacity();
        }

        return capacity;
    }

    public ByteBuffer getByteBuffer() {
        return getByteBuffer(0, getPartitionedCount() - 1);
    }

    public ByteBuffer getByteBuffer(int partitionIndex) {
        if (partitionIndex < 0) {
            throw new IllegalArgumentException("partitionIndex = " + partitionIndex);
        }

        if (partitionIndex >= getPartitionedCount()) {
            throw new IllegalArgumentException("partitionIndex(" + partitionIndex + ") >= partitionedCount(" + getPartitionedCount() + ").");
        }

        Index fromIndex = partitionIndexList.get(partitionIndex);
        Index toIndex = partitionIndexList.get(partitionIndex);

        int startPosition = fromIndex.getStartPosition();
        int endPosition = toIndex.getEndPosition();

        logger.debug("getByteBuffer baseBuffer-length:{}, {}~{}.", baseBuffer.length, startPosition, endPosition);

        return ByteBuffer.wrap(baseBuffer, fromIndex.getStartPosition(), toIndex.getEndPosition() - fromIndex.getStartPosition());
    }

    public ByteBuffer getByteBuffer(int fromPartitionIndex, int toPartitionIndex) {
        checkRangeValidation(fromPartitionIndex, toPartitionIndex, getPartitionedCount());

        Index fromIndex = partitionIndexList.get(fromPartitionIndex);
        Index toIndex = partitionIndexList.get(toPartitionIndex);

        int startPosition = fromIndex.getStartPosition();
        int endPosition = toIndex.getEndPosition();

        logger.debug("getByteBuffer baseBuffer-length:{}, {}~{}.", baseBuffer.length, startPosition, endPosition);
        return ByteBuffer.wrap(baseBuffer, fromIndex.getStartPosition(), toIndex.getEndPosition() - fromIndex.getStartPosition());
    }

    private void checkRangeValidation(int fromPartitionIndex, int toPartitionIndex, int partitionedCount) {
        if (fromPartitionIndex < 0) {
            throw new IllegalArgumentException("fromPartitionIndex = " + fromPartitionIndex);
        }

        if (fromPartitionIndex > toPartitionIndex) {
            throw new IllegalArgumentException("fromPartitionIndex(" + fromPartitionIndex + ") > toPartitionIndex(" + toPartitionIndex + ").");
        }

        if (toPartitionIndex >= partitionedCount) {
            throw new IllegalArgumentException("toPartitionIndex(" + toPartitionIndex + ") >= partitionedCount(" + partitionedCount + ").");
        }
    }

    public boolean isLastPartitionIndex(int partitionIndex) {
        if (partitionIndex == partitionIndexList.size() - 1) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "PartitionedByteBufferLocator [baseBuffer-length=" + baseBuffer.length + ", partitionedCount=" + partitionIndexList.size()
                + ", partitionedBufferCapacity=" + partitionedBufferCapacity + "]";
    }

    public static class Builder {

        private byte[] buffer;
        private List<Index> indexList = new ArrayList<Index>();

        public void setBuffer(byte[] buffer) {
            this.buffer = buffer;
        }

        public void addIndex(List<Index> indexList) {
            if (indexList == null) {
                return;
            }

            for (Index index : indexList) {
                addIndex(index);
            }
        }

        public void addIndex(int startBufferPosition, int endBufferPosition) {
            addIndex(new Index(startBufferPosition, endBufferPosition));
        }

        public void addIndex(Index index) {
            Index lastPartitionIndex = ListUtils.getLast(indexList);

            int partitionedEndPosition = 0;
            if (lastPartitionIndex != null) {
                partitionedEndPosition = lastPartitionIndex.getEndPosition();
            }
            chechRangeValidation(index, partitionedEndPosition);

            indexList.add(index);
        }

        private void chechRangeValidation(Index index, int partitionedEndPosition) {
            int startPosition = index.getStartPosition();
            int endPosition = index.getEndPosition();

            if (startPosition < 0) {
                throw new IllegalArgumentException("startPosition = " + startPosition);
            }

            if (startPosition > endPosition) {
                throw new IllegalArgumentException("startPosition(" + startPosition + ") > endPosition(" + endPosition + ").");
            }

            if (startPosition != partitionedEndPosition) {
                throw new IllegalArgumentException("support only stream buffer index. startPosition(" + startPosition + ") != partitionedEndPosition(" + partitionedEndPosition + ").");
            }
        }

        public PartitionedByteBufferLocator build() {
            return new PartitionedByteBufferLocator(buffer, indexList);
        }

    }

    static class Index {

        private final int startPosition;
        private final int endPosition;
        private final int capacity;

        Index(int startPosition, int endPosition) {
            this.startPosition = startPosition;
            this.endPosition = endPosition;
            this.capacity = endPosition - startPosition;
        }

        public int getStartPosition() {
            return startPosition;
        }

        public int getEndPosition() {
            return endPosition;
        }

        public int getCapacity() {
            return capacity;
        }

        @Override
        public String toString() {
            return "Index [startPosition=" + startPosition + ", endPosition=" + endPosition + ", capacity=" + capacity + "]";
        }

    }

}
