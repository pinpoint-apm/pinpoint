/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.realtime.dto;

import jakarta.annotation.Nullable;

/**
 * @author youngjin.kim2
 */
public class ActiveThreadDump {

    private long startTime; // required
    private long localTraceId; // required
    private @Nullable ThreadDump threadDump; // required
    private boolean sampled; // required
    private @Nullable String transactionId; // optional
    private @Nullable String entryPoint; // optional

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getLocalTraceId() {
        return localTraceId;
    }

    public void setLocalTraceId(long localTraceId) {
        this.localTraceId = localTraceId;
    }

    @Nullable
    public ThreadDump getThreadDump() {
        return threadDump;
    }

    public void setThreadDump(@Nullable ThreadDump threadDump) {
        this.threadDump = threadDump;
    }

    public boolean isSampled() {
        return sampled;
    }

    public void setSampled(boolean sampled) {
        this.sampled = sampled;
    }

    @Nullable
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(@Nullable String transactionId) {
        this.transactionId = transactionId;
    }

    @Nullable
    public String getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(@Nullable String entryPoint) {
        this.entryPoint = entryPoint;
    }
}
