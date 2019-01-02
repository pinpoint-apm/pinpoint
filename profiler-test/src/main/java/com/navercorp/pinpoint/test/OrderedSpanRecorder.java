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

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

/**
 * @author Jongho Moon
 */
public class OrderedSpanRecorder implements ListenableDataSender.Listener<Object>, Iterable<Object> {
    private static String LINE_SEPARATOR = System.getProperty("line.separator");
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
        synchronized (this) {
            final int pos = Collections.binarySearch(list, item);
            if (pos >= 0) {
                throw new IllegalArgumentException("Duplicated?? list: " + list + ", item: " + item);
            }

            int index = -(pos + 1);
            list.add(index, item);
        }
    }

    private void handleSpanEvent(SpanChunk spanChunk) {
        List<SpanEvent> spanEventList = spanChunk.getSpanEventList();
        if (spanEventList.size() != 1) {
            throw new IllegalStateException("spanEvent.size != 1");
        }

        final SpanEvent event = spanEventList.get(0);
        long startTime = event.getStartTime();
        Item item = new Item(spanChunk, startTime, spanChunk.getTraceRoot(), event.getSequence());
        insertItem(item);
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

    public void print(PrintStream out) {
        final StringBuilder buffer = new StringBuilder();
        synchronized (this) {
            appendln(buffer, "TRACES(" + list.size() + "):");
            for (Item item : list) {
                appendln(buffer, item);
            }
            for (Object obj : this) {
                appendln(buffer, obj);
            }
        }

        out.print(buffer.toString());
        out.flush();
    }

    private void appendln(StringBuilder buffer, Object object) {
        buffer.append(object);
        buffer.append(LINE_SEPARATOR);
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
        String listString;
        synchronized (this) {
            listString = list.toString();
        }
        return "OrderedSpanRecorder{" +
                "list=" + listString +
                '}';
    }
}
