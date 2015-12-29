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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.util.ListUtils;

/**
 * @author Taejin Koo
 */
public class CompositeSpanStreamData {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final byte[] buffer;
    private final List<ComponentBufferIndex> componentsBufferIndexList;
    private final int componentBufferCapacity;

    private CompositeSpanStreamData(byte[] buffer, List<ComponentBufferIndex> componentsBufferIndexList) {
        if (buffer == null) {
            throw new NullPointerException("buffer may not be null.");
        }

        if (componentsBufferIndexList == null || componentsBufferIndexList.isEmpty()) {
            throw new NullPointerException("buffer may not be null or zero.");
        }

        ComponentBufferIndex firstIndex = ListUtils.getFirst(componentsBufferIndexList);
        ComponentBufferIndex lastIndex = ListUtils.getLast(componentsBufferIndexList);

        int componentBufferCapacity = lastIndex.getEndIndex() - firstIndex.getStartIndex();
        if (componentBufferCapacity > buffer.length) {
            throw new IllegalArgumentException("componentBufferCapacity(" + componentBufferCapacity + ") > buffer.length(" + buffer.length + ").");
        }

        if (lastIndex.getEndIndex() > buffer.length) {
            throw new IllegalArgumentException("componentBuffersEndPosition(" + lastIndex.getEndIndex() + ") > buffer.length(" + buffer.length + ").");
        }

        this.buffer = buffer;

        this.componentsBufferIndexList = new ArrayList<ComponentBufferIndex>(componentsBufferIndexList);
        this.componentBufferCapacity = componentBufferCapacity;
    }

    public int getComponentsCount() {
        return componentsBufferIndexList.size();
    }

    public int getComponentsBufferCapacity() {
        return componentBufferCapacity;
    }

    public int getComponentBufferLength(int compositeBufferIndex) {
        if (compositeBufferIndex < 0) {
            throw new IllegalArgumentException("compositeBufferIndex = " + compositeBufferIndex);
        }

        if (compositeBufferIndex >= getComponentsCount()) {
            throw new IllegalArgumentException("compositeBufferIndex(" + compositeBufferIndex + ") >= componentBufferListSize(" + getComponentsCount() + ").");
        }

        return componentsBufferIndexList.get(compositeBufferIndex).getLength();
    }

    public int getComponentBufferLength(int fromComponentBufferIndex, int toComponentBufferIndex) {
        getRangeCheck(fromComponentBufferIndex, toComponentBufferIndex, getComponentsCount());

        int componentBufferLength = 0;
        for (int i = fromComponentBufferIndex; i < toComponentBufferIndex; i++) {
            componentBufferLength += componentsBufferIndexList.get(i).getLength();
        }

        return componentBufferLength;
    }

    public ByteBuffer getByteBuffer() {
        return getByteBuffer(0, getComponentsCount() - 1);
    }

    public ByteBuffer getByteBuffer(int componentBufferIndex) {
        if (componentBufferIndex < 0) {
            throw new IllegalArgumentException("componentBufferIndex = " + componentBufferIndex);
        }

        if (componentBufferIndex >= getComponentsCount()) {
            throw new IllegalArgumentException("componentBufferIndex(" + componentBufferIndex + ") >= componentBufferListSize(" + getComponentsCount() + ").");
        }

        ComponentBufferIndex fromIndex = componentsBufferIndexList.get(componentBufferIndex);
        ComponentBufferIndex toIndex = componentsBufferIndexList.get(componentBufferIndex);

        int startBufferPosition = fromIndex.getStartIndex();
        int endBufferPosition = toIndex.getEndIndex();

        logger.debug("getByteBuffer buffer-length:{}, {}~{}.", buffer.length, startBufferPosition, endBufferPosition);

        return ByteBuffer.wrap(buffer, fromIndex.getStartIndex(), toIndex.getEndIndex() - fromIndex.getStartIndex());
    }

    public ByteBuffer getByteBuffer(int fromComponentBufferIndex, int toComponentBufferIndex) {
        getRangeCheck(fromComponentBufferIndex, toComponentBufferIndex, getComponentsCount());

        ComponentBufferIndex fromIndex = componentsBufferIndexList.get(fromComponentBufferIndex);
        ComponentBufferIndex toIndex = componentsBufferIndexList.get(toComponentBufferIndex);

        int startBufferPosition = fromIndex.getStartIndex();
        int endBufferPosition = toIndex.getEndIndex();

        logger.debug("getByteBuffer buffer-length:{}, {}~{}.", buffer.length, startBufferPosition, endBufferPosition);

        return ByteBuffer.wrap(buffer, fromIndex.getStartIndex(), toIndex.getEndIndex() - fromIndex.getStartIndex());
    }

    private void getRangeCheck(int fromComponentBufferIndex, int toComponentBufferIndex, int componentBufferListSize) {
        if (fromComponentBufferIndex < 0) {
            throw new IllegalArgumentException("fromComponentBufferIndex = " + fromComponentBufferIndex);
        }

        if (fromComponentBufferIndex > toComponentBufferIndex) {
            throw new IllegalArgumentException("fromComponentBufferIndex(" + fromComponentBufferIndex + ") > toComponentBufferIndex(" + toComponentBufferIndex
                    + ").");
        }

        if (toComponentBufferIndex >= componentBufferListSize) {
            throw new IllegalArgumentException("toComponentBufferIndex(" + toComponentBufferIndex + ") >= componentBufferListSize(" + componentBufferListSize
                    + ").");
        }
    }

    public boolean isLastComponentIndex(int compositeBufferIndex) {
        if (compositeBufferIndex == componentsBufferIndexList.size() - 1) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "CompositeSpanStreamData [buffer-length=" + buffer.length + ", componentsBuffer-size=" + componentsBufferIndexList.size()
                + ", componentBufferCapacity=" + componentBufferCapacity + "]";
    }

    public static class Builder {

        private byte[] buffer;
        private List<ComponentBufferIndex> componentsBufferIndexList = new ArrayList<ComponentBufferIndex>();

        public void setBuffer(byte[] buffer) {
            this.buffer = buffer;
        }

        public void addComponentsIndex(int bufferStartIndex, int bufferEndIndex) {
            ComponentBufferIndex lastComponentBufferIndex = ListUtils.getLast(componentsBufferIndexList);

            int lastComponentEndBufferIndex = 0;
            if (lastComponentBufferIndex != null) {
                lastComponentEndBufferIndex = lastComponentBufferIndex.getEndIndex();
            }

            addRangeCheck(bufferStartIndex, bufferEndIndex, lastComponentEndBufferIndex);

            componentsBufferIndexList.add(new ComponentBufferIndex(bufferStartIndex, bufferEndIndex));
        }

        private void addRangeCheck(int bufferStartIndex, int bufferEndIndex, int prevBufferEndIndex) {
            if (bufferStartIndex < 0) {
                throw new IllegalArgumentException("bufferStartIndex = " + bufferStartIndex);
            }

            if (bufferStartIndex > bufferEndIndex) {
                throw new IllegalArgumentException("bufferStartIndex(" + bufferStartIndex + ") > bufferEndIndex(" + bufferEndIndex + ").");
            }

            if (bufferStartIndex != prevBufferEndIndex) {
                throw new IllegalArgumentException("support only stream buffer index. bufferStartIndex(" + bufferStartIndex + ") != prevBufferEndIndex("
                        + prevBufferEndIndex + ").");
            }
        }

        public CompositeSpanStreamData build() {
            return new CompositeSpanStreamData(buffer, componentsBufferIndexList);
        }

    }

    private static class ComponentBufferIndex {

        private final int startIndex;
        private final int endIndex;
        private final int length;

        private ComponentBufferIndex(int startIndex, int endIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.length = endIndex - startIndex;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public int getLength() {
            return length;
        }

        @Override
        public String toString() {
            return "ComponentBufferIndex [startIndex=" + startIndex + ", endIndex=" + endIndex + ", length=" + length + "]";
        }

    }

}
