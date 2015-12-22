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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author Taejin Koo
 */
public class StandbySpanStreamDataStorage {

    private static final int DEFAULT_CAPACITY = 5;
    private static final long DEFAULT_MAX_WAIT_TIME = 5000L;

    private final int capacity;
    private final long maxWaitTimeMillis;

    private final PriorityQueue<SpanStreamSendData> priorityQueue;

    StandbySpanStreamDataStorage() {
        this(DEFAULT_CAPACITY, DEFAULT_MAX_WAIT_TIME);
    }

    StandbySpanStreamDataStorage(int capacity, long maxWaitTimeMillis) {
        this.capacity = capacity;
        this.maxWaitTimeMillis = maxWaitTimeMillis;

        this.priorityQueue = new PriorityQueue<SpanStreamSendData>(capacity, new SpanStreamSendDataComparator());
    }

    synchronized boolean addStandbySpanStreamData(SpanStreamSendData standbySpanStreamData) {
        SpanStreamSendDataMode flushMode = standbySpanStreamData.getFlushMode();

        if (flushMode == SpanStreamSendDataMode.FLUSH) {
            return false;
        }

        if (standbySpanStreamData.getAvailableBufferCapacity() > 0 && standbySpanStreamData.getAvailableGatheringComponentsCount() > 0) {
            if (priorityQueue.size() >= capacity) {
                return false;
            }

            return priorityQueue.offer(standbySpanStreamData);
        } else {
            return false;
        }
    }

    synchronized SpanStreamSendData getStandbySpanStreamSendData(int availableCapacity) {
        Iterator<SpanStreamSendData> standbySpanStreamSendDataIterator = priorityQueue.iterator();

        while (standbySpanStreamSendDataIterator.hasNext()) {
            SpanStreamSendData standbySpanStreamSendData = standbySpanStreamSendDataIterator.next();

            if (standbySpanStreamSendData.getAvailableBufferCapacity() > availableCapacity) {
                standbySpanStreamSendDataIterator.remove();
                return standbySpanStreamSendData;
            }
        }

        return null;
    }

    synchronized SpanStreamSendData getStandbySpanStreamSendData() {
        SpanStreamSendData mostAvailableBufferCapacityStreamSendData = null;

        Iterator<SpanStreamSendData> standbySpanStreamSendDataIterator = priorityQueue.iterator();
        while (standbySpanStreamSendDataIterator.hasNext()) {
            SpanStreamSendData standbySpanStreamSendData = standbySpanStreamSendDataIterator.next();

            if (mostAvailableBufferCapacityStreamSendData == null) {
                mostAvailableBufferCapacityStreamSendData = standbySpanStreamSendData;
            } else {
                if (mostAvailableBufferCapacityStreamSendData.getAvailableBufferCapacity() < standbySpanStreamSendData.getAvailableBufferCapacity()) {
                    mostAvailableBufferCapacityStreamSendData = standbySpanStreamSendData;
                }
            }
        }

        if (mostAvailableBufferCapacityStreamSendData != null) {
            priorityQueue.remove(mostAvailableBufferCapacityStreamSendData);
        }

        return mostAvailableBufferCapacityStreamSendData;
    }

    synchronized List<SpanStreamSendData> getForceFlushSpanStreamDataList() {
        List<SpanStreamSendData> forceFlushSpanStreamDataList = new ArrayList<SpanStreamSendData>(capacity);

        long currentTimeMillis = System.currentTimeMillis();

        while (true) {
            SpanStreamSendData standbySpanStreamSendData = priorityQueue.peek();
            if (standbySpanStreamSendData == null) {
                break;
            }
            
            if (standbySpanStreamSendData.getFirstAccessTime() + maxWaitTimeMillis < currentTimeMillis) {
                priorityQueue.remove();
                forceFlushSpanStreamDataList.add(standbySpanStreamSendData);
            } else {
                break;
            }
        }

        return forceFlushSpanStreamDataList;
    }

    synchronized long getLeftWaitTime(long defaultLeftWaitTime) {
        SpanStreamSendData standbySpanStreamSendData = priorityQueue.peek();
        if (standbySpanStreamSendData == null) {
            return defaultLeftWaitTime;
        }

        long firstAccessTime = standbySpanStreamSendData.getFirstAccessTime();
        return firstAccessTime + maxWaitTimeMillis - System.currentTimeMillis();
    }

    static class SpanStreamSendDataComparator implements Comparator<SpanStreamSendData> {

        @Override
        public int compare(SpanStreamSendData newValue, SpanStreamSendData oldValue) {
            if (newValue.getFirstAccessTime() == -1) {
                return 1;
            }

            if (newValue.getFirstAccessTime() < oldValue.getFirstAccessTime()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

}
