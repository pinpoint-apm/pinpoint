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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.navercorp.pinpoint.profiler.context.LocalAsyncId;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;


/**
 * @author Jongho Moon
 */
public class OrderedSpanRecorder implements ListenableDataSender.Listener<Object>, Iterable<Object> {
    public static final int ROOT_SEQUENCE = -1;
    public static final int ASYNC_ID_NOT_SET = -1;
    public static final int ASYNC_SEQUENCE_NOT_SET = -1;

    private final List<Item> list = new ArrayList<Item>();

    public OrderedSpanRecorder() {
    }


    @Override
    public synchronized boolean handleSend(Object data) {

        if (data instanceof Span) {
            insertSpan((Span) data);
            return true;
        }
        if (data instanceof SpanChunk) {
            handleSpanEvent((SpanChunk) data);
            return true;
        }
//        throw new IllegalStateException("unknown data type:" + data);
        return false;
    }


    private void insertSpan(Span span) {
        long startTime = span.getStartTime();
        TraceRoot traceRoot = span.getTraceRoot();

        Item item = new Item(span, startTime, traceRoot, ROOT_SEQUENCE);
        insertItem(item);
    }

    private void insertItem(Item item) {
        int pos = Collections.binarySearch(list, item);

        if (pos >= 0) {
            throw new IllegalArgumentException("Duplicated?? list: " + list + ", item: " + item);
        }

        int index = -(pos + 1);
        list.add(index, item);
    }

    private void handleSpanEvent(SpanChunk spanChunk) {
        List<SpanEvent> spanEventList = spanChunk.getSpanEventList();
        for (SpanEvent event : spanEventList) {
            final LocalAsyncId localAsyncId = event.getLocalAsyncId();
            int asyncId = ASYNC_ID_NOT_SET;
            int asyncSequence = ASYNC_SEQUENCE_NOT_SET;
            if (localAsyncId != null) {
                asyncId = localAsyncId.getAsyncId();
                asyncSequence = localAsyncId.getSequence();
            }

            long startTime = event.getStartTime();
            Item item = new Item(event, startTime, spanChunk.getTraceRoot(), event.getSequence(), asyncId, asyncSequence);
            insertItem(item);
        }
    }

    public synchronized Object pop() {
        final Item item = popItem();
        if (item == null) {
            return null;
        }
        return item.getValue();
    }

    public synchronized Item popItem() {
        if (list.isEmpty()) {
            return null;
        }

        return list.remove(0);
    }

    public synchronized void print(PrintStream out) {
        out.println("TRACES(" + list.size() + "):");

        for (Item item : list) {
            out.println(item);
        }
        for (Object obj : this) {
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
    public synchronized Iterator<Object> iterator() {
        return new RecorderIterator();
    }

    private final class RecorderIterator implements Iterator<Object> {
        private int current = -1;
        private int index = 0;

        @Override
        public boolean hasNext() {
            synchronized (OrderedSpanRecorder.this) {
                return index < list.size();
            }
        }

        @Override
        public Object next() {
            synchronized (OrderedSpanRecorder.this) {
                current = index;
                index++;
                Item item = list.get(current);
                return item.getValue();
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
