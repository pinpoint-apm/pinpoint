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

package com.navercorp.pinpoint.test;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.AsyncSpanChunk;
import com.navercorp.pinpoint.profiler.context.LocalAsyncId;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Item implements Comparable<Item> {

    private final Object value;
    private final long time;
    private final long spanId;
    private final int sequence;

    private final TraceRoot traceRoot;

    private final LocalAsyncId localAsyncId;

//    public Item(Object value, long time, TraceRoot traceRoot, int sequence) {
//        this(value, time, traceRoot, sequence, OrderedSpanRecorder.ASYNC_ID_NOT_SET, OrderedSpanRecorder.ASYNC_SEQUENCE_NOT_SET);
//    }

    public Item(Object value, long time, TraceRoot traceRoot, int sequence) {
        this.value = value;
        this.time = time;
        this.traceRoot = Assert.requireNonNull(traceRoot, "traceRoot");
        this.spanId = traceRoot.getTraceId().getSpanId();
        this.sequence = sequence;
        if (value instanceof AsyncSpanChunk) {
            final AsyncSpanChunk spanChunk = (AsyncSpanChunk) value;
            this.localAsyncId = spanChunk.getLocalAsyncId();
        } else {
           this.localAsyncId = null;
        }
    }

    public Object getValue() {
        return value;
    }

    public long getSpanId() {
        return spanId;
    }

    public TraceRoot getTraceRoot() {
        return traceRoot;
    }

    @Override
    public int compareTo(Item o) {
        if (this.localAsyncId == null && o.localAsyncId == null) {
            return compareItems(this, o);
        } else if (this.localAsyncId != null && o.localAsyncId != null) {
            return compareAsyncItems(this, o);
        } else {
            if (this.localAsyncId == null) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private static int compareItems(Item lhs, Item rhs) {
        if (lhs.time < rhs.time) {
            return -1;
        } else if (lhs.time > rhs.time) {
            return 1;
        } else {
            if (lhs.spanId < rhs.spanId) {
                return -1;
            } else if (lhs.spanId > rhs.spanId) {
                return 1;
            } else {
                if (lhs.sequence < rhs.sequence) {
                    return -1;
                } else if (lhs.sequence > rhs.sequence) {
                    return 1;
                } else {
                    return compareHashes(lhs, rhs);
                }
            }
        }
    }

    private static int compareAsyncItems(Item lhs, Item rhs) {
        final LocalAsyncId localAsyncId1 = lhs.localAsyncId;
        final LocalAsyncId localAsyncId2 = rhs.localAsyncId;
        if (localAsyncId1.getAsyncId() < localAsyncId2.getAsyncId()) {
            return -1;
        } else if (localAsyncId1.getAsyncId() > localAsyncId2.getAsyncId()) {
            return 1;
        } else {
            if (localAsyncId1.getSequence() < localAsyncId2.getSequence()) {
                return -1;
            } else if (localAsyncId1.getSequence() > localAsyncId2.getSequence()) {
                return 1;
            } else {
                if (lhs.sequence < rhs.sequence) {
                    return -1;
                } else if (lhs.sequence > rhs.sequence) {
                    return 1;
                } else {
                    return compareHashes(lhs, rhs);
                }
            }
        }
    }

    private static int compareHashes(Item lhs, Item rhs) {
        int h1 = System.identityHashCode(lhs.value);
        int h2 = System.identityHashCode(rhs.value);

        return h1 < h2 ? -1 : (h1 > h2 ? 1 : 0);
    }

    @Override
    public String toString() {
        return "Item{" +
                "value=" + value +
                ", time=" + time +
                ", spanId=" + spanId +
                ", sequence=" + sequence +
                ", traceRoot=" + traceRoot +
                ", localAsyncId=" + localAsyncId +
                '}';
    }
}
