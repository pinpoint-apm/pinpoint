/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.receiver.grpc;

import com.google.common.util.concurrent.MoreExecutors;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdStreamResponse;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogram;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class GrpcStreamService implements AutoCloseable {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public static final long DEFAULT_FLUSH_DELAY = 1000;

    private final ScheduledExecutorService scheduler;
    private final long flushDelay;

    private final Object lock = new Object();

    private ScheduledFuture<?> jobHandle;

    private final List<ActiveThreadCountStreamSocket> grpcProfilerStreamSocketList = new CopyOnWriteArrayList<>();

    private final ActiveTraceRepository activeTrace;

    public GrpcStreamService(String name, long flushDelay, ActiveTraceRepository activeTrace) {
        Objects.requireNonNull(name, "name");
        Assert.isTrue(flushDelay > 0, "flushDelay must be >= 0");

        this.scheduler = newScheduledExecutorService(name);

        this.flushDelay = flushDelay;
        this.activeTrace = Objects.requireNonNull(activeTrace, "ActiveTrace");
    }

    private ScheduledExecutorService newScheduledExecutorService(String name) {
        PinpointThreadFactory threadFactory = new PinpointThreadFactory("Pinpoint-" + name + "-Timer", true);
        return Executors.newScheduledThreadPool(1, threadFactory);
    }

    ActiveThreadCountStreamSocket[] getStreamSocketList() {
        return grpcProfilerStreamSocketList.toArray(new ActiveThreadCountStreamSocket[0]);
    }

    public boolean register(ActiveThreadCountStreamSocket streamSocket) {
        synchronized (lock) {
            grpcProfilerStreamSocketList.add(streamSocket);

            if (!isStarted()) {
                logger.info("turn on ActiveThreadCountTask");
                Runnable job = new ActiveThreadCountTimerTask();
                this.jobHandle = scheduler.scheduleAtFixedRate(job, flushDelay, flushDelay, TimeUnit.MILLISECONDS);
                return true;
            }
        }
        return false;
    }




    void stopTask() {
        if (jobHandle == null) {
            return;
        }
        logger.info("turn off TimerTask");
        jobHandle.cancel(false);
        jobHandle = null;
    }

    boolean isStarted() {
        return jobHandle != null;
    }

    public boolean unregister(ActiveThreadCountStreamSocket streamSocket) {
        synchronized (lock) {
            grpcProfilerStreamSocketList.remove(streamSocket);
            // turnoff
            if (grpcProfilerStreamSocketList.isEmpty()) {
                stopTask();
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() {
        synchronized (lock) {
            if (scheduler != null) {
                if (!MoreExecutors.shutdownAndAwaitTermination(scheduler, Duration.ofSeconds(3))) {
                    logger.warn("GrpcStreamService shutdown failed");
                }
            }

            ActiveThreadCountStreamSocket[] streamSockets = getStreamSocketList();
            for (ActiveThreadCountStreamSocket streamSocket : streamSockets) {
                if (streamSocket != null) {
                    streamSocket.close();
                }
            }

            grpcProfilerStreamSocketList.clear();
        }
    }


    private PCmdActiveThreadCountRes.Builder builder() {
        final long currentTime = System.currentTimeMillis();
        final ActiveTraceHistogram histogram = activeTrace.getActiveTraceHistogram(currentTime);

        PCmdActiveThreadCountRes.Builder responseBuilder = PCmdActiveThreadCountRes.newBuilder();
        responseBuilder.setTimeStamp(currentTime);
        responseBuilder.setHistogramSchemaType(histogram.getHistogramSchema().getTypeCode());

        final List<Integer> activeTraceCountList = histogram.getCounter();
        for (Integer activeTraceCount : activeTraceCountList) {
            responseBuilder.addActiveThreadCount(activeTraceCount);
        }

        return responseBuilder;
    }

    private class ActiveThreadCountTimerTask implements Runnable {

        @Override
        public void run() {
            ActiveThreadCountStreamSocket[] streamSocketList = getStreamSocketList();
            if (logger.isDebugEnabled()) {
                logger.debug("ActiveThreadCountTimerTask run. streamSocketList:{}", Arrays.toString(streamSocketList));
            }

            final PCmdActiveThreadCountRes.Builder builder = builder();
            for (ActiveThreadCountStreamSocket streamSocket : streamSocketList) {
                try {
                    PCmdStreamResponse header = streamSocket.newHeader();
                    builder.setCommonStreamResponse(header);
                    PCmdActiveThreadCountRes activeThreadCount = builder.build();

                    streamSocket.send(activeThreadCount);
                    if (logger.isDebugEnabled()) {
                        logger.debug("ActiveThreadCountStreamSocket. {}", streamSocket);
                    }
                } catch (Throwable e) {
                    logger.warn("failed to execute ActiveThreadCountTimerTask.run method. streamSocket:{}, message:{}", streamSocket, e.getMessage(), e);
                    streamSocket.close(e);
                }
            }
        }

    }
}
