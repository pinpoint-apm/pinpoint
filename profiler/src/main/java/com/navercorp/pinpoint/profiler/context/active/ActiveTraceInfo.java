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

package com.navercorp.pinpoint.profiler.context.active;

/**
 * @author Taejin Koo
 */
public class ActiveTraceInfo {

    private final long localTraceId;
    private final long startTime;
    private final Thread thread;
    private final boolean sampled;
    private final String transactionId;
    private final String entryPoint;

    public ActiveTraceInfo(long id, long startTime) {
        this(id, startTime, null);
    }

    public ActiveTraceInfo(long id, long startTime, Thread thread) {
        this(id, startTime, thread, false, null, null);
    }

    public ActiveTraceInfo(long id, long startTime, Thread thread, boolean sampled, String transactionId, String entryPoint) {
        this.localTraceId = id;
        this.startTime = startTime;
        this.thread = thread;

        this.sampled = sampled;
        this.transactionId = transactionId;
        this.entryPoint = entryPoint;
    }

    public long getLocalTraceId() {
        return localTraceId;
    }

    public long getStartTime() {
        return startTime;
    }

    public Thread getThread() {
        return thread;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActiveTraceInfo{");
        sb.append("localTraceId=").append(localTraceId);
        sb.append(", startTime=").append(startTime);
        sb.append(", thread=").append(thread);
        sb.append(", sampled=").append(sampled);
        sb.append(", transactionId='").append(transactionId).append('\'');
        sb.append(", entryPoint='").append(entryPoint).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
