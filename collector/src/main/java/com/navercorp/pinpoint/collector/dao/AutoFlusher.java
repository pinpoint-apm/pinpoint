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

package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class AutoFlusher {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ScheduledExecutorService executor;

    private long flushPeriod = 1000;

    @Autowired
    private List<CachedStatisticsDao> cachedStatisticsDaoList;

    public long getFlushPeriod() {
        return flushPeriod;
    }

    public void setFlushPeriod(long flushPeriod) {
        this.flushPeriod = flushPeriod;
    }

    private static final class Worker implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        private final CachedStatisticsDao dao;

        public Worker(CachedStatisticsDao dao) {
            this.dao = dao;
        }

        @Override
        public void run() {
            try {
                dao.flushAll();
            } catch (Throwable th) {
                logger.error("AutoFlusherWorker failed. Caused:{}", th.getMessage(), th);
            }
        }
    }

    public void initialize() {
        if (CollectionUtils.isEmpty(cachedStatisticsDaoList)) {
            return;
        }

        ThreadFactory threadFactory = PinpointThreadFactory.createThreadFactory(this.getClass().getSimpleName(), true);
        executor = Executors.newScheduledThreadPool(cachedStatisticsDaoList.size(), threadFactory);
        for (CachedStatisticsDao dao : cachedStatisticsDaoList) {
            executor.scheduleAtFixedRate(new Worker(dao), 0L, flushPeriod, TimeUnit.MILLISECONDS);
        }
        logger.info("Auto flusher initialized.");
    }

    public void shutdown() {
        logger.info("Shutdown auto flusher.");
        shutdownExecutor();
        for (CachedStatisticsDao dao : cachedStatisticsDaoList) {
            dao.flushAll();
        }
    }

    private void shutdownExecutor() {
        executor.shutdown();
        try {
            executor.awaitTermination(3000 + flushPeriod, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void setCachedStatisticsDaoList(List<CachedStatisticsDao> cachedStatisticsDaoList) {
        this.cachedStatisticsDaoList = cachedStatisticsDaoList;
    }
}
