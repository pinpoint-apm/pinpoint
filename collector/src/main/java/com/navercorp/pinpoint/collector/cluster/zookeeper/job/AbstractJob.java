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

package com.navercorp.pinpoint.collector.cluster.zookeeper.job;

import java.util.concurrent.atomic.AtomicInteger;

import com.navercorp.pinpoint.rpc.server.PinpointServer;

public class AbstractJob implements Job {

    private final PinpointServer pinpointServer;

    private final int maxCount;
    private final AtomicInteger currentCount;

    public AbstractJob(PinpointServer pinpointServer) {
        this(pinpointServer, 3);
    }

    public AbstractJob(PinpointServer pinpointServer, int maxCount) {
        this.pinpointServer = pinpointServer;

        this.maxCount = maxCount;
        this.currentCount = new AtomicInteger(0);
    }

    @Override
    public PinpointServer getPinpointServer() {
        return pinpointServer;
    }

    @Override
    public int getMaxRetryCount() {
        return maxCount;
    }

    @Override
    public int getCurrentRetryCount() {
        return currentCount.get();
    }

    @Override
    public void incrementCurrentRetryCount() {
        currentCount.incrementAndGet();
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.getClass().getSimpleName());
        buffer.append(", PinpointServer=").append(pinpointServer);
        buffer.append(", Retry=").append(currentCount.get()).append("/").append(maxCount);

        return buffer.toString();
    }

}
