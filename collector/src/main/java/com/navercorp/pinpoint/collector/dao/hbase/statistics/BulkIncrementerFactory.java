/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.navercorp.pinpoint.collector.dao.hbase.BulkOperationReporter;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
@Component
public final class BulkIncrementerFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ScheduledExecutorService memoryObserver;

    private static ScheduledExecutorService defaultScheduler() {
        ThreadFactory threadFactory = PinpointThreadFactory.createThreadFactory("MemoryObserver-bulkOperation");
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    public BulkIncrementerFactory() {
        this(defaultScheduler());
    }

    public BulkIncrementerFactory(ScheduledExecutorService memoryObserver) {
        this.memoryObserver = Objects.requireNonNull(memoryObserver, "memoryObserver");
    }

    public BulkIncrementer wrap(BulkIncrementer bulkIncrementer, int limitSize, BulkOperationReporter reporter) {
        Objects.requireNonNull(bulkIncrementer, "bulkIncrementer");

        if (!hasLimit(limitSize)) {
            return bulkIncrementer;
        }

        SizeLimitedBulkIncrementer wrap = new SizeLimitedBulkIncrementer(bulkIncrementer, limitSize, reporter);

        attachObserver(wrap);

        return wrap;
    }

    public BulkUpdater wrap(BulkUpdater bulkUpdater, int limitSize, BulkOperationReporter reporter) {
        Objects.requireNonNull(bulkUpdater, "bulkUpdater");

        if (!hasLimit(limitSize)) {
            return bulkUpdater;
        }

        SizeLimitedBulkUpdater wrap = new SizeLimitedBulkUpdater(bulkUpdater, limitSize, reporter);

        attachObserver(wrap);

        return wrap;
    }

    private boolean hasLimit(int limitSize) {
        if (limitSize > 0) {
            if (limitSize != Integer.MAX_VALUE) {
                return true;
            }
        }
        return false;
    }

    private void attachObserver(BulkState bulkState) {
        Objects.requireNonNull(bulkState, "bulkState");

        memoryObserver.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                final boolean success = bulkState.checkState();
                if (!success) {
                    // TODO need incrementer name??
                    logger.warn("Incrementer.checkState() failed");
                }
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }


    @PreDestroy
    public void close() {
        memoryObserver.shutdown();
    }

}
