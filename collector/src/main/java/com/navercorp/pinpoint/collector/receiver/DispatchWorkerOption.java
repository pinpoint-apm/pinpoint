/*
 * Copyright 2017 NAVER Corp.
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
public class DispatchWorkerOption {

    private final String name;

    private final int threadSize;
    private final int queueSize;
    private final int recordLogRate;

    private final boolean enableCollectMetric;

    public DispatchWorkerOption(String name, int threadSize, int queueSize) {
        this(name, threadSize, queueSize, 100);
    }

    public DispatchWorkerOption(String name, int threadSize, int queueSize, boolean enableCollectMetric) {
        this(name, threadSize, queueSize, 100, enableCollectMetric);
    }

    public DispatchWorkerOption(String name, int threadSize, int queueSize, int recordLogRate) {
        this(name, threadSize, queueSize, recordLogRate, false);
    }

    public DispatchWorkerOption(String name, int threadSize, int queueSize, int recordLogRate, boolean enableCollectMetric) {
        if (threadSize <= 0) {
            throw new IllegalArgumentException("threadSize must be greater than 0");
        }

        if (queueSize <= 0) {
            throw new IllegalArgumentException("queueSize must be greater than 0");
        }

        if (recordLogRate <= 0) {
            throw new IllegalArgumentException("recordLogRate must be greater than 0");
        }

        this.name = name;
        this.threadSize = threadSize;
        this.queueSize = queueSize;
        this.recordLogRate = recordLogRate;
        this.enableCollectMetric = enableCollectMetric;
    }

    public String getName() {
        return name;
    }

    public int getThreadSize() {
        return threadSize;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public int getRecordLogRate() {
        return recordLogRate;
    }

    public boolean isEnableCollectMetric() {
        return enableCollectMetric;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DispatchWorkerOption{");
        sb.append("name='").append(name).append('\'');
        sb.append(", threadSize=").append(threadSize);
        sb.append(", queueSize=").append(queueSize);
        sb.append(", recordLogRate=").append(recordLogRate);
        sb.append(", enableCollectMetric=").append(enableCollectMetric);
        sb.append('}');
        return sb.toString();
    }

}
