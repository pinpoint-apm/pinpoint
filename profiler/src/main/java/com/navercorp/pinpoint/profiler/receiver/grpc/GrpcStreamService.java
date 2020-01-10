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

import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Taejin Koo
 */
public class GrpcStreamService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Timer timer;
    private final long flushDelay;

    private final Object lock = new Object();

    private final AtomicReference<TimerTask> currentTaskReference = new AtomicReference<TimerTask>();

    private final List<GrpcProfilerStreamSocket> grpcProfilerStreamSocketList = new CopyOnWriteArrayList<GrpcProfilerStreamSocket>();

    public GrpcStreamService(String name, long flushDelay) {
        Assert.requireNonNull(name, "name");
        Assert.isTrue(flushDelay > 0, "flushDelay must be >= 0");
        this.timer = new Timer("Pinpoint-Grpc-" + name + "-Timer", true);
        this.flushDelay = flushDelay;
    }

    public GrpcProfilerStreamSocket[] getStreamSocketList() {
        return grpcProfilerStreamSocketList.toArray(new GrpcProfilerStreamSocket[0]);
    }

    public boolean register(GrpcProfilerStreamSocket streamSocket, TimerTask timerTask) {
        synchronized (lock) {
            grpcProfilerStreamSocketList.add(streamSocket);
            boolean turnOn = currentTaskReference.compareAndSet(null, timerTask);
            if (turnOn) {
                logger.info("turn on TimerTask.");
                timer.scheduleAtFixedRate(timerTask, 0, flushDelay);
                return true;
            }
        }
        return false;
    }

    public boolean unregister(GrpcProfilerStreamSocket streamSocket) {
        synchronized (lock) {
            grpcProfilerStreamSocketList.remove(streamSocket);
            // turnoff
            if (grpcProfilerStreamSocketList.isEmpty()) {
                TimerTask activeThreadCountTimerTask = currentTaskReference.get();
                if (activeThreadCountTimerTask != null) {
                    currentTaskReference.compareAndSet(activeThreadCountTimerTask, null);
                    activeThreadCountTimerTask.cancel();

                    logger.info("turn off TimerTask.");
                }
                return true;
            }
        }
        return false;
    }

    public void close() {
        synchronized (lock) {
            if (timer != null) {
                timer.cancel();
            }

            GrpcProfilerStreamSocket[] streamSockets = grpcProfilerStreamSocketList.toArray(new GrpcProfilerStreamSocket[0]);
            for (GrpcProfilerStreamSocket streamSocket : streamSockets) {
                if (streamSocket != null) {
                    streamSocket.close();
                }
            }

            grpcProfilerStreamSocketList.clear();
        }
    }

}
