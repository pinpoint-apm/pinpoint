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

package com.navercorp.pinpoint.collector.config;

/**
 * @author emeroad
 */
public class ExecutorConfiguration {
    private final int threadSize;
    private final int queueSize;
    private final boolean monitorEnable;

    public ExecutorConfiguration(int threadSize, int queueSize, boolean monitorEnable) {
        this.threadSize = threadSize;
        this.queueSize = queueSize;
        this.monitorEnable = monitorEnable;
    }

    public int getThreadSize() {
        return threadSize;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public boolean isMonitorEnable() {
        return monitorEnable;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private int threadSize = 128;
        private int queueSize = 1024 * 5;
        private boolean monitorEnable;

        Builder() {
        }

        public int getThreadSize() {
            return threadSize;
        }

        public void setThreadSize(int threadSize) {
            this.threadSize = threadSize;
        }

        public int getQueueSize() {
            return queueSize;
        }

        public void setQueueSize(int queueSize) {
            this.queueSize = queueSize;
        }

        public boolean isMonitorEnable() {
            return monitorEnable;
        }

        public void setMonitorEnable(boolean monitorEnable) {
            this.monitorEnable = monitorEnable;
        }

        public ExecutorConfiguration build() {
            return new ExecutorConfiguration(this.threadSize, this.queueSize, this.monitorEnable);
        }
    }

    @Override
    public String toString() {
        return "ExecutorConfiguration{" +
                "threadSize=" + threadSize +
                ", queueSize=" + queueSize +
                ", monitorEnable=" + monitorEnable +
                '}';
    }
}
