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

package com.navercorp.pinpoint.test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import org.apache.thrift.TBase;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;

/**
 * @author Jongho Moon
 */
public class OrderedSpanRecorder implements ListenableDataSender.Listener, Iterable<TBase<?, ?>> {
    private static final int ROOT_SEQUENCE = -1;
    private static final int ASYNC_ID_NOT_SET = -1;
    private static final int ASYNC_SEQUENCE_NOT_SET = -1;

    private final List<Item> list = new ArrayList<Item>();

    private static final class Item implements Comparable<Item> {

        private final TBase<?, ?> value;
        private final long time;
        private final long spanId;
        private final int sequence;
        private final int asyncId;
        private final int asyncSequence;

        public Item(TBase<?, ?> value, long time, long spanId, int sequence) {
            this(value, time, spanId, sequence, ASYNC_ID_NOT_SET, ASYNC_SEQUENCE_NOT_SET);
        }

        public Item(TBase<?, ?> value, long time, long spanId, int sequence, int asyncId, int asyncSequence) {
            this.value = value;
            this.time = time;
            this.spanId = spanId;
            this.sequence = sequence;
            this.asyncId = asyncId;
            this.asyncSequence = asyncSequence;
        }

        @Override
        public int compareTo(Item o) {
            if (this.asyncId == ASYNC_ID_NOT_SET && o.asyncId == ASYNC_ID_NOT_SET) {
                return compareItems(this, o);
            } else if (this.asyncId != ASYNC_ID_NOT_SET && o.asyncId != ASYNC_ID_NOT_SET) {
                return compareAsyncItems(this, o);
            } else {
                if (this.asyncId == ASYNC_ID_NOT_SET) {
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
            if (lhs.asyncId < rhs.asyncId) {
                return -1;
            } else if (lhs.asyncId > rhs.asyncId) {
                return 1;
            } else {
                if (lhs.asyncSequence < rhs.asyncSequence) {
                    return -1;
                } else if (lhs.asyncSequence > rhs.asyncSequence) {
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
    }

    @Override
    public synchronized boolean handleSend(TBase<?, ?> data) {
        if (data instanceof Span) {
            insertSpan((Span) data);
            return true;
        } else if (data instanceof SpanEvent) {
            handleSpanEvent((SpanEvent) data);
            return true;
        }

        return false;
    }

    private void insertSpan(Span span) {
        long startTime = span.getStartTime();
        long spanId = span.getSpanId();

        insertItem(new Item(span, startTime, spanId, ROOT_SEQUENCE));
    }

    private void insertItem(Item item) {
        int pos = Collections.binarySearch(list, item);

        if (pos >= 0) {
            throw new IllegalArgumentException("Duplicated?? list: " + list + ", item: " + item);
        }

        int index = -(pos + 1);
        list.add(index, item);
    }

    private void handleSpanEvent(SpanEvent event) {
        TraceRoot span = event.getTraceRoot();
        int asyncId = event.isSetAsyncId() ? event.getAsyncId() : ASYNC_ID_NOT_SET;
        TraceId traceId = span.getTraceId();
        int asyncSequence = event.isSetAsyncSequence() ? event.getAsyncSequence() : ASYNC_SEQUENCE_NOT_SET;
        insertItem(new Item(event, span.getTraceStartTime() + event.getStartElapsed(), traceId.getSpanId(), event.getSequence(), asyncId, asyncSequence));
    }

    public synchronized TBase<?, ?> pop() {
        if (list.isEmpty()) {
            return null;
        }

        return list.remove(0).value;
    }

    public synchronized void print(PrintStream out) {
        out.println("TRACES(" + list.size() + "):");

        for (TBase<?, ?> obj : this) {
            out.println(obj);
        }
    }

    public synchronized void clear() {
        list.clear();
    }

    public synchronized int size() {
        return list.size();
    }

    @Override
    public synchronized Iterator<TBase<?, ?>> iterator() {
        return new RecorderIterator();
    }

    private final class RecorderIterator implements Iterator<TBase<?, ?>> {
        private int current = -1;
        private int index = 0;

        @Override
        public boolean hasNext() {
            synchronized (OrderedSpanRecorder.this) {
                return index < list.size();
            }
        }

        @Override
        public TBase<?, ?> next() {
            synchronized (OrderedSpanRecorder.this) {
                current = index;
                index++;
                return list.get(current).value;
            }
        }

        @Override
        public void remove() {
            synchronized (OrderedSpanRecorder.this) {
                if (current == -1) {
                    throw new IllegalStateException();
                }

                list.remove(current);
                current = -1;
                index--;
            }
        }
    }

    @Override
    public String toString() {
        return "OrderedSpanRecorder{" +
                "list=" + list +
                '}';
    }
}
