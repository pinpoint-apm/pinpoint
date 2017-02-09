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

import org.apache.thrift.TBase;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;

/**
 * @author Jongho Moon
 */
public class OrderedSpanRecorder implements ListenableDataSender.Listener, Iterable<TBase<?, ?>> {
    private static final int ROOT_SEQUENCE = -1;
    private static final int ASYNC_ID_NOT_SET = -1;

    private final List<Item> list = new ArrayList<Item>();

    private static final class Item implements Comparable<Item> {


        private final TBase<?, ?> value;
        private final long time;
        private final long spanId;
        private final int sequence;
        private final int asyncId;

        public Item(TBase<?, ?> value, long time, long spanId, int sequence) {
            this(value, time, spanId, sequence, ASYNC_ID_NOT_SET);
        }

        public Item(TBase<?, ?> value, long time, long spanId, int sequence, int asyncId) {
            this.value = value;
            this.time = time;
            this.spanId = spanId;
            this.sequence = sequence;
            this.asyncId = asyncId;
        }

        @Override
        public int compareTo(Item o) {
            if (this.asyncId == ASYNC_ID_NOT_SET) {
                if (o.asyncId == ASYNC_ID_NOT_SET) {
                    // fall through
                } else {
                    return -1;
                }
            } else {
                if (o.asyncId == ASYNC_ID_NOT_SET) {
                    return 1;
                } else {
                    if (this.asyncId < o.asyncId) {
                        return -1;
                    } else if (this.asyncId > o.asyncId) {
                        return 1;
                    }

                    // if both async events have the same asyncId, do normal event comparison
                }
            }


            if (this.time < o.time) {
                return -1;
            } else if (this.time > o.time) {
                return 1;
            } else {
                if (this.spanId < o.spanId) {
                    return -1;
                } else if (this.spanId > o.spanId) {
                    return 1;
                } else {
                    if (this.sequence < o.sequence) {
                        return -1;
                    } else if (this.sequence > o.sequence) {
                        return 1;
                    } else {
                        int h1 = System.identityHashCode(this.value);
                        int h2 = System.identityHashCode(o.value);

                        return h1 < h2 ? -1 : (h1 > h2 ? 1 : 0);
                    }
                }
            }
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
        Span span = event.getSpan();
        int asyncId = event.isSetAsyncId() ? event.getAsyncId() : ASYNC_ID_NOT_SET;
        insertItem(new Item(event, span.getStartTime() + event.getStartElapsed(), span.getSpanId(), event.getSequence(), asyncId));
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
