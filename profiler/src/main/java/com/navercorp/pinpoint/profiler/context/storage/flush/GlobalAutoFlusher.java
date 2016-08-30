/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.profiler.context.storage.flush;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.UnsafeArrayCollection;
import com.navercorp.pinpoint.rpc.util.AssertUtils;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.thrift.dto.TSpanAndSpanChunkList;

/**
 * @author Taejin Koo
 */
public class GlobalAutoFlusher implements StorageFlusher {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final LinkedBlockingQueue<SpanChunk> spanChunkHolder = new LinkedBlockingQueue<SpanChunk>(500);
    private final LinkedBlockingQueue<Span> spanHolder = new LinkedBlockingQueue<Span>(500);

    private final int flushBufferSize;

    private final DataSender dataSender;

    private ScheduledExecutorService executor;
    private long period;


    public GlobalAutoFlusher(DataSender dataSender, int flushBufferSize) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender may not be null");
        }
        if (flushBufferSize <= 0) {
            throw new IllegalArgumentException("flushBufferSize must be positive number");
        }

        this.dataSender = dataSender;
        this.flushBufferSize = flushBufferSize;
    }

    @Override
    public void flush(SpanChunk spanChunk) {
        AssertUtils.assertNotNull(spanChunk);
        AssertUtils.assertTrue(ListUtils.size(spanChunk.getSpanEventList()) <= flushBufferSize,
                "SpanEvent size(" + ListUtils.size(spanChunk.getSpanEventList()) + ") must be less than " + flushBufferSize);

        boolean offered = spanChunkHolder.offer(spanChunk);
        if (!offered) {
            logger.warn("Can't insert {}. caused: Queue is full.", spanChunk);
        }
    }

    @Override
    public void flush(Span span) {
        AssertUtils.assertNotNull(span);
        AssertUtils.assertTrue(ListUtils.size(span.getSpanEventList()) <= flushBufferSize,
                "SpanEvent size(" + ListUtils.size(span.getSpanEventList()) + ") must be less than " + flushBufferSize);

        boolean offered = spanHolder.offer(span);
        if (!offered) {
            logger.warn("Can't insert {}. caused: Queue is full.", span);
        }
    }

    public void start(long period) {
        executor = Executors.newScheduledThreadPool(1, new PinpointThreadFactory("Pinpoint-Global-Storage-Auto-Flusher", true));
        executor.scheduleAtFixedRate(new FlushTask(), 0L, period, TimeUnit.MILLISECONDS);
        this.period = period;
    }

    @Override
    public void stop() {
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(3000 + period, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private final class FlushTask implements Runnable {

        public FlushTask() {
        }

        @Override
        public void run() {
            int spanChunkSize = spanChunkHolder.size();
            Collection<SpanChunk> copiedSpanChunkCollection = Collections.EMPTY_LIST;
            if (spanChunkSize != 0) {
                copiedSpanChunkCollection = new UnsafeArrayCollection<SpanChunk>(spanChunkSize);
                int i = spanChunkHolder.drainTo(copiedSpanChunkCollection, spanChunkSize);
            }

            int spanSize = spanHolder.size();
            Collection<Span> copiedSpanCollection = Collections.EMPTY_LIST;
            if (spanSize != 0) {
                copiedSpanCollection = new UnsafeArrayCollection<Span>(spanSize);
                int i = spanHolder.drainTo(copiedSpanCollection, spanSize);
            }

            flush0(copiedSpanChunkCollection, copiedSpanCollection);
        }

        private void flush0(Collection<SpanChunk> spanChunkCollection, Collection<Span> spanCollection) {
            SpanAndSpanChunkListDataHolder dataHolder = new SpanAndSpanChunkListDataHolder(flushBufferSize);

            Object[] spanChunks = spanChunkCollection.toArray();
            for (Object object : spanChunks) {
                SpanChunk spanChunk = (SpanChunk) object;
                boolean added = dataHolder.addSpanChunk(spanChunk);
                if (!added) {
                    send(dataHolder.createSpanAndSpanChunkList());
                    dataHolder.clear();

                    dataHolder.addSpanChunk(spanChunk);
                }
            }

            Object[] spans = spanCollection.toArray();
            for (Object object : spans) {
                Span span = (Span) object;
                boolean added = dataHolder.addSpan(span);
                if (!added) {
                    send(dataHolder.createSpanAndSpanChunkList());
                    dataHolder.clear();

                    dataHolder.addSpan(span);
                }
            }

            send(dataHolder.createSpanAndSpanChunkList());
        }

        private void send(TSpanAndSpanChunkList spanAndSpanChunkList) {
            if (spanAndSpanChunkList == null) {
                return;
            }

            int spanSize = spanAndSpanChunkList.getSpanListSize();
            int spanChunkSize = spanAndSpanChunkList.getSpanChunkListSize();

            if (spanSize + spanChunkSize == 1) {
                if (spanSize == 1) {
                    dataSender.send(ListUtils.getFirst(spanAndSpanChunkList.getSpanList()));
                } else {
                    dataSender.send(ListUtils.getFirst(spanAndSpanChunkList.getSpanChunkList()));
                }
            } else {
                dataSender.send(spanAndSpanChunkList);
            }
        }

    }

    private class SpanAndSpanChunkListDataHolder {

        private final int maxSpanEventSize;
        private int canStoreSpanEventSize;

        private List<SpanChunk> spanChunkList = new ArrayList<SpanChunk>();
        private List<Span> spanList = new ArrayList<Span>();

        private SpanAndSpanChunkListDataHolder(int maxSpanEventSize) {
            this.maxSpanEventSize = maxSpanEventSize;
            this.canStoreSpanEventSize = maxSpanEventSize;
        }

        private boolean addSpanChunk(SpanChunk spanChunk) {
            AssertUtils.assertNotNull(spanChunk);

            int spanEventSize = ListUtils.size(spanChunk.getSpanEventList());
            if (spanEventSize > canStoreSpanEventSize) {
                return false;
            }

            spanChunkList.add(spanChunk);
            canStoreSpanEventSize -= spanEventSize;
            return true;
        }

        private boolean addSpan(Span span) {
            AssertUtils.assertNotNull(span);

            int spanEventSize = ListUtils.size(span.getSpanEventList());
            if (spanEventSize > canStoreSpanEventSize) {
                return false;
            }

            spanList.add(span);
            canStoreSpanEventSize -= spanEventSize;
            return true;
        }

        private TSpanAndSpanChunkList createSpanAndSpanChunkList() {
            TSpanAndSpanChunkList spanAndSpanChunkList = new TSpanAndSpanChunkList();

            boolean empty = true;
            if (!ListUtils.isEmpty(spanChunkList)) {
                empty = false;
                spanAndSpanChunkList.setSpanChunkList((List) spanChunkList);
            }
            if (!ListUtils.isEmpty(spanList)) {
                empty = false;
                spanAndSpanChunkList.setSpanList((List) spanList);
            }

            if (empty) {
                return null;
            }
            return spanAndSpanChunkList;
        }

        private void clear() {
            spanChunkList = new ArrayList<SpanChunk>();
            spanList = new ArrayList<Span>();
            canStoreSpanEventSize = maxSpanEventSize;
        }

        private boolean isEmpty() {
            return ListUtils.size(spanChunkList) == 0 && ListUtils.size(spanList) == 0;
        }

    }

}
