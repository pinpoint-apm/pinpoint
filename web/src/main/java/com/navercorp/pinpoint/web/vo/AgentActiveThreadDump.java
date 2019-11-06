/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.thrift.dto.command.TThreadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class AgentActiveThreadDump {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentActiveThreadDump.class);

    private final long threadId;
    private final String threadName;
    private final TThreadState threadState;

    private final long startTime;
    private final long execTime;
    private final long localTraceId;

    private final boolean sampled;
    private final String transactionId;
    private final String entryPoint;

    private final String detailMessage;

    private AgentActiveThreadDump(Builder builder) {
        this.threadId = builder.threadId;
        this.threadName = builder.threadName;
        this.threadState = builder.threadState;

        this.startTime = builder.startTime;
        this.execTime = builder.execTime;
        this.localTraceId = builder.localTraceId;

        this.sampled = builder.sampled;
        this.transactionId = builder.transactionId;
        this.entryPoint = builder.entryPoint;

        this.detailMessage = builder.detailMessage;
    }

    public long getThreadId() {
        return threadId;
    }

    public String getThreadName() {
        return threadName;
    }

    public TThreadState getThreadState() {
        return threadState;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getExecTime() {
        return execTime;
    }

    public long getLocalTraceId() {
        return localTraceId;
    }

    public boolean isSampled() {
        return sampled;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    static class Builder {

        private long threadId;
        private String threadName;
        private TThreadState threadState;

        private long startTime;
        private long execTime;
        private long localTraceId;

        private boolean sampled;
        private String transactionId;
        private String entryPoint;

        private String detailMessage;

        void setThreadId(long threadId) {
            this.threadId = threadId;
        }

        void setThreadName(String threadName) {
            this.threadName = threadName;
        }

        void setThreadState(TThreadState threadState) {
            this.threadState = threadState;
        }

        void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        void setExecTime(long execTime) {
            this.execTime = execTime;
        }

        void setLocalTraceId(long localTraceId) {
            this.localTraceId = localTraceId;
        }

        void setSampled(boolean sampled) {
            this.sampled = sampled;
        }

        void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        void setEntryPoint(String entryPoint) {
            this.entryPoint = entryPoint;
        }

        void setDetailMessage(String detailMessage) {
            this.detailMessage = detailMessage;
        }

        AgentActiveThreadDump build() {
            if (threadName == null) {
                throw new NullPointerException("threadName");
            }
            if (threadState == null) {
                throw new NullPointerException("threadState");
            }

            if (startTime <= 0) {
                throw new IllegalArgumentException("startTime must be positive number");
            }
            if (execTime <= 0) {
                // execTime can be negative number because of time issues between servers.
                LOGGER.warn("execTime is {}, you can get negativeNumber because of time issues between servers", execTime);
            }

            if (detailMessage == null) {
                throw new NullPointerException("detailMessage");
            }

            return new AgentActiveThreadDump(this);
        }

    }

}
