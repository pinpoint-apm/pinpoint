/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.active;


/**
 * @author Taejin Koo
 */
public class UnsampledActiveTraceSnapshot implements ActiveTraceSnapshot {

    private final long localTraceId;
    private final long startTime;
    private final long threadId;

    public UnsampledActiveTraceSnapshot(long id, long startTime, long threadId) {
        this.localTraceId = id;
        this.startTime = startTime;
        // @Nullable
        this.threadId = threadId;
    }


    @Override
    public long getLocalTransactionId() {
        return localTraceId;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getThreadId() {
        return threadId;
    }

    @Override
    public boolean isSampled() {
        return false;
    }

    @Override
    public String getTransactionId() {
        return null;
    }

    @Override
    public String getEntryPoint() {
        return null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UnsampledActiveTraceSnapshot{");
        sb.append("localTraceId=").append(localTraceId);
        sb.append(", startTime=").append(startTime);
        sb.append(", threadId=").append(threadId);
        sb.append('}');
        return sb.toString();
    }

}
