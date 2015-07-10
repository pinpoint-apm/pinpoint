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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TBase;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;

/**
 * @author Jongho Moon
 */
public class OrderedSpanRecorder implements ListenableDataSender.Listener, Iterable<TBase<?, ?>> {
    private final List<Item> list = new ArrayList<Item>();
    
    private final Map<Long, List<SpanEvent>> waitingEventTable = new HashMap<Long, List<SpanEvent>>();
    private final Map<Long, List<SpanEvent>> waitingAsyncEventTable = new HashMap<Long, List<SpanEvent>>();
    private final Map<Long, Long> spanStartTimeTable = new HashMap<Long, Long>();
    
    private static final class Item implements Comparable<Item> {
        
        private static final int ASYNC_ID_NOT_SET = 0;

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
            // shift async events to the back, and only compare against other async events
            if (o.asyncId != ASYNC_ID_NOT_SET) {
                if (this.asyncId != ASYNC_ID_NOT_SET) {
                    if (this.asyncId < o.asyncId) {
                        return -1;
                    } else if (this.asyncId > o.asyncId) {
                        return 1;
                    } // if both async events have the same asyncId, do normal event comparison
                } else {
                    return -1;
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
    
    private static final Comparator<SpanEvent> ASYNC_SPAN_EVENT_COMPARATOR = new Comparator<SpanEvent>() {

        @Override
        public int compare(SpanEvent o1, SpanEvent o2) {
            int asyncId1 = o1.getAsyncId();
            int asyncId2 = o2.getAsyncId();
            
            if (asyncId1 < asyncId2) {
                return -1;
            } else if (asyncId1 > asyncId2) {
                return 1;
            } else {
                return SPAN_EVENT_COMPARATOR.compare(o1, o2);
            }
        }
        
    };
    
    private static final Comparator<SpanEvent> SPAN_EVENT_COMPARATOR = new Comparator<SpanEvent>() {

        @Override
        public int compare(SpanEvent o1, SpanEvent o2) {
            int t1 = o1.getStartElapsed();
            int t2 = o2.getStartElapsed();
            
            if (t1 < t2) {
                return -1;
            } else if (t1 > t2) {
                return 1;
            } else {
                int s1 = o1.getSequence();
                int s2 = o2.getSequence();
                
                return s1 < s2 ? -1 : (s1 > s2 ? 1 : o1.compareTo(o2));
            }
        }
        
    };
    
    @Override
    public synchronized boolean handleSend(TBase<?, ?> data) {
        if (data instanceof Span) {
            insertSpan((Span)data);
            return true;
        } else if (data instanceof SpanEvent) {
            SpanEvent event = (SpanEvent)data;
            if (event.isSetAsyncId()) {
                handleAsyncSpanEvent(event);
            } else {
                handleSpanEvent(event);
            }
            return true;
        }
        
        return false;
    }
    
    private void insertSpan(Span span) {
        long startTime = span.getStartTime();
        long spanId = span.getSpanId();

        spanStartTimeTable.put(spanId, startTime);

        insertItem(new Item(span, startTime, spanId, -1));

        List<SpanEvent> events = waitingEventTable.remove(span.getSpanId());
        
        if (events != null) {
            
            for (SpanEvent event : events) {
                insertItem(new Item(event, startTime + event.getStartElapsed(), spanId, event.getSequence()));
            }
        }
    }
    
    private void insertItem(Item item) {
        int pos = Collections.binarySearch(list, item);
        
        if (pos >= 0) {
            throw new IllegalArgumentException("Duplicated?? list: " + list + ", item: " + item);
        }
        
        int index = -(pos + 1);
        list.add(index, item);
    }
    
    private void insertSpanEvent(List<SpanEvent> events, SpanEvent event, Comparator<SpanEvent> comparator) {
        int pos = Collections.binarySearch(events, event, comparator);
        
        if (pos >= 0) {
            throw new IllegalArgumentException("Duplicated?? list: " + events + ", item: " + event);
        }
        
        int index = -(pos + 1);
        events.add(index, event);
    }
    
    private void handleAsyncSpanEvent(SpanEvent asyncEvent) {
        long spanId = asyncEvent.getSpan().getSpanId();
        List<SpanEvent> asyncEvents = waitingAsyncEventTable.get(spanId);
        if (asyncEvents == null) {
            asyncEvents = new ArrayList<SpanEvent>();
            asyncEvents.add(asyncEvent);
            waitingAsyncEventTable.put(spanId, asyncEvents);
        } else {
            insertSpanEvent(asyncEvents, asyncEvent, ASYNC_SPAN_EVENT_COMPARATOR);
        }
    }

    private void handleSpanEvent(SpanEvent event) {
        long spanId = event.getSpan().getSpanId();
        Long spanStartTime = spanStartTimeTable.get(spanId);
        
        if (spanStartTime != null) {
            insertItem(new Item(event, spanStartTime + event.getStartElapsed(), spanId, event.getSequence()));
            return;
        }
        
        List<SpanEvent> events = waitingEventTable.get(spanId);
        
        if (events == null) {
            events = new ArrayList<SpanEvent>();
            waitingEventTable.put(spanId, events);
        }
        
        insertSpanEvent(events, event, SPAN_EVENT_COMPARATOR);
    }
    
    public synchronized TBase<?, ?> pop() {
        handleDanglingEvents();
        
        if (list.isEmpty()) {
            return null;
        }
        
        return list.remove(0).value;
    }
    
    public synchronized void print(PrintStream out) {
        handleDanglingEvents();
        
        out.println("TRACES(" + list.size() + "):");
        
        for (TBase<?, ?> obj : this) {
            out.println(obj);
        }
    }
        
    private void handleDanglingEvents() {
        for (List<SpanEvent> events : waitingEventTable.values()) {
            for (SpanEvent event : events) {
                Span span = event.getSpan();
                insertItem(new Item(event, span.getStartTime() + event.getStartElapsed(), span.getSpanId(), event.getSequence()));
            }
        }
        for (List<SpanEvent> asyncEvents : waitingAsyncEventTable.values()) {
            for (SpanEvent asyncEvent : asyncEvents) {
                Span span = asyncEvent.getSpan();
                insertItem(new Item(asyncEvent, span.getStartTime() + asyncEvent.getStartElapsed(), span.getSpanId(), asyncEvent.getSequence(), asyncEvent.getAsyncId()));
            }
        }
        waitingEventTable.clear();
        waitingAsyncEventTable.clear();
    }
    
    public synchronized void clear() {
        list.clear();
        waitingEventTable.clear();
        waitingAsyncEventTable.clear();
        spanStartTimeTable.clear();
    }
    
    public synchronized int size() {
        handleDanglingEvents();
        return list.size();
    }

    @Override
    public synchronized Iterator<TBase<?, ?>> iterator() {
        handleDanglingEvents();
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
}
