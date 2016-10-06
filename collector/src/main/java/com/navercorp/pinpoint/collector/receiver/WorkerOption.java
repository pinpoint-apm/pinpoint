/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.collector.receiver;

/**
 * @author Taejin Koo
 */
public final class WorkerOption {

    private final int workerThreadSize;
    private final int workerThreadQueueSize;

    private final boolean enableCollectMetric;

    public WorkerOption(int workerThreadSize, int workerThreadQueueSize) {
        this(workerThreadSize, workerThreadQueueSize, false);
    }

    public WorkerOption(int workerThreadSize, int workerThreadQueueSize, boolean enableCollectMetric) {
        if (workerThreadSize <= 0) {
            throw new IllegalArgumentException("workerThreadSize must be greater than 0");
        }

        if (workerThreadQueueSize <= 0) {
            throw new IllegalArgumentException("workerThreadQueueSize must be greater than 0");
        }

        this.workerThreadSize = workerThreadSize;
        this.workerThreadQueueSize = workerThreadQueueSize;
        this.enableCollectMetric = enableCollectMetric;
    }

    public int getWorkerThreadSize() {
        return workerThreadSize;
    }

    public int getWorkerThreadQueueSize() {
        return workerThreadQueueSize;
    }

    public boolean isEnableCollectMetric() {
        return enableCollectMetric;
    }

    @Override
    public String toString() {
        return "WorkerOption{" +
                "workerThreadSize=" + workerThreadSize +
                ", workerThreadQueueSize=" + workerThreadQueueSize +
                ", enableCollectMetric=" + enableCollectMetric +
                '}';
    }

}
