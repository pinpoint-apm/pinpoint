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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TBase;

import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;

/**
 * @author Jongho Moon
 */
public class OrderedSpanRecorder implements ListenableDataSender.Listener, Iterable<TBase<?, ?>> {
    private final List<Item> list = new ArrayList<Item>();
    
    private final Map<Long, List<TSpanEvent>> waitingEventTable = new HashMap<Long, List<TSpanEvent>>();
    private final Map<Long, Long> spanStartTimeTable = new HashMap<Long, Long>();
    
    
    private static final class Item implements Comparable<Item> {
        private final TBase<?, ?> value;
        private final long time;
        private final long spanId;
        private final int sequence;

        public Item(TBase<?, ?> value, long time, long spanId, int sequence) {
            this.value = value;
            this.time = time;
            this.spanId = spanId;
            this.sequence = sequence;
        }

        @Override
        public int compareTo(Item o) {
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
                    return this.sequence < o.sequence ? -1 : (this.sequence > o.sequence ? 1 : 0);
                }
            }
        }
    }
    
    private static final Comparator<TSpanEvent> SPAN_EVENT_COMPARATOR = new Comparator<TSpanEvent>() {

        @Override
        public int compare(TSpanEvent o1, TSpanEvent o2) {
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
    public boolean handleSend(TBase<?, ?> data) {
        if (data instanceof TSpan) {
            insertSpan((TSpan)data);
            return true;
        } else if (data instanceof TSpanEvent) {
            insertSpanEvent((TSpanEvent)data);
            return true;
        }
        
        return false;
    }
    
    private void insertSpan(TSpan span) {
        long startTime = span.getStartTime();
        long spanId = span.getSpanId();

        spanStartTimeTable.put(spanId, startTime);

        insertItem(new Item(span, startTime, spanId, -1));

        List<TSpanEvent> events = waitingEventTable.remove(span.getSpanId());
        
        if (events != null) {
            
            for (TSpanEvent event : events) {
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

    private void insertSpanEvent(TSpanEvent event) {
        long spanId = event.getSpanId();
        Long spanStartTime = spanStartTimeTable.get(spanId);
        
        if (spanStartTime != null) {
            insertItem(new Item(event, spanStartTime + event.getStartElapsed(), spanId, event.getSequence()));
            return;
        }
        
        List<TSpanEvent> events = waitingEventTable.get(spanId);
        
        if (events == null) {
            events = new ArrayList<TSpanEvent>();
            waitingEventTable.put(spanId, events);
        }
        
        
        int pos = Collections.binarySearch(events, event, SPAN_EVENT_COMPARATOR);
        
        if (pos >= 0) {
            throw new IllegalArgumentException("Duplicated?? list: " + events + ", item: " + event);
        }
        
        int index = -(pos + 1);
        events.add(index, event);
    }
    
    public TBase<?, ?> pop() {
        handleDanglingEvents();
        
        if (list.isEmpty()) {
            return null;
        }
        
        return list.remove(0).value;
    }
    
    private void handleDanglingEvents() {
        for (List<TSpanEvent> events : waitingEventTable.values()) {
            for (TSpanEvent event : events) {
                insertItem(new Item(event, event.getStartElapsed(), event.getSpanId(), event.getSequence()));
            }
        }
        
        waitingEventTable.clear();
    }
    
    public void clear() {
        list.clear();
        waitingEventTable.clear();
        spanStartTimeTable.clear();
    }
    
    public int size() {
        handleDanglingEvents();
        return list.size();
    }

    @Override
    public Iterator<TBase<?, ?>> iterator() {
        handleDanglingEvents();
        return new RecorderIterator();
    }

    private final class RecorderIterator implements Iterator<TBase<?, ?>> {
        private int pos;

        @Override
        public boolean hasNext() {
            return pos < list.size(); 
        }

        @Override
        public TBase<?, ?> next() {
            return list.get(pos++).value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
}
