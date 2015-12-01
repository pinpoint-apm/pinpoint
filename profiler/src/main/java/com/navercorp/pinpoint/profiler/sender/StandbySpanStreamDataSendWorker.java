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

package com.navercorp.pinpoint.profiler.sender;

import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * @author Taejin Koo
 */
public class StandbySpanStreamDataSendWorker implements Runnable {

    private static final long DEFAULT_BLOCK_TIME = 1000;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final StandbySpanStreamDataFlushHandler flushHandler;
    private final StandbySpanStreamDataStorage standbySpanStreamDataStorage;
    private final long blockTime;

    private final Object lock = new Object();
    private Thread workerThread;

    private boolean isStarted = false;

    public StandbySpanStreamDataSendWorker(StandbySpanStreamDataFlushHandler flushHandler, StandbySpanStreamDataStorage dataStorage) {
        this(flushHandler, dataStorage, DEFAULT_BLOCK_TIME);
    }

    public StandbySpanStreamDataSendWorker(StandbySpanStreamDataFlushHandler flushHandler, StandbySpanStreamDataStorage dataStorage, long blockTime) {
        this.flushHandler = flushHandler;
        this.standbySpanStreamDataStorage = dataStorage;
        this.blockTime = blockTime;
    }

    public void start() {
        final ThreadFactory threadFactory = new PinpointThreadFactory(this.getClass().getSimpleName(), true);
        this.workerThread = threadFactory.newThread(this);

        logger.info("start() started.");
        if (!workerThread.isAlive()) {
            this.isStarted = true;
            this.workerThread.start();
            logger.info("start() completed.");
        } else {
            logger.info("start() failed. caused:already started.", this.getClass().getSimpleName());
        }
    }

    public void stop() {
        logger.info("stop() started.");

        this.isStarted = false;

        long startTimeMillis = System.currentTimeMillis();
        long maxWaitTimeMillis = 3000;

        while (workerThread != null && workerThread.isAlive()) {
            workerThread.interrupt();
            try {
                workerThread.join(100L);

                if (System.currentTimeMillis() - startTimeMillis > maxWaitTimeMillis) {
                    break;
                }
            } catch (InterruptedException ignored) {
            }
        }

        logger.info("stop() completed.");
    }

    boolean addStandbySpanStreamData(SpanStreamSendData standbySpanStreamData) {
        synchronized (lock) {
            boolean isAdded = standbySpanStreamDataStorage.addStandbySpanStreamData(standbySpanStreamData);
            lock.notifyAll();
            return isAdded;
        }
    }

    SpanStreamSendData getStandbySpanStreamSendData(int availableCapacity) {
        synchronized (lock) {
            return standbySpanStreamDataStorage.getStandbySpanStreamSendData(availableCapacity);
        }
    }

    SpanStreamSendData getStandbySpanStreamSendData() {
        synchronized (lock) {
            return standbySpanStreamDataStorage.getStandbySpanStreamSendData();
        }
    }

    private List<SpanStreamSendData> getForceFlushSpanStreamDataList() {
        synchronized (lock) {
            return standbySpanStreamDataStorage.getForceFlushSpanStreamDataList();
        }
    }

    @Override
    public void run() {
        while (isStarted) {
            boolean onEvent = await();
            if (!isStarted) {
                break;
            }

            List<SpanStreamSendData> forceFlushSpanStreamDataList = getForceFlushSpanStreamDataList();
            forceFlush(forceFlushSpanStreamDataList);
        }
    }

    private boolean await() {
        synchronized (lock) {
            long timeBlocked = standbySpanStreamDataStorage.getLeftWaitTime(blockTime);

            long startTimeMillis = System.currentTimeMillis();

            if (timeBlocked > 0) {
                try {
                    lock.wait(timeBlocked);
                } catch (InterruptedException ignore) {
                }

                if (isOverWaitTime(timeBlocked, startTimeMillis)) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean isOverWaitTime(long waitTimeMillis, long startTimeMillis) {
        return waitTimeMillis < (System.currentTimeMillis() - startTimeMillis);
    }

    private void forceFlush(List<SpanStreamSendData> forceFlushSpanStreamDataList) {
        if (forceFlushSpanStreamDataList == null) {
            return;
        }

        for (SpanStreamSendData spanStreamSendData : forceFlushSpanStreamDataList) {
            try {
                flushHandler.handleFlush(spanStreamSendData);
            } catch (Exception e) {
                flushHandler.exceptionCaught(spanStreamSendData, e);
            }
        }
    }

}
