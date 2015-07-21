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

package com.navercorp.pinpoint.profiler.context;


/**
 * @author Taejin Koo
 */
public class ActiveTraceInfo {

    private final long startTime;
    private final long traceObjectId;
    private final Thread currentThread;

    public ActiveTraceInfo(long traceObjectId, long startTime) {
        this(traceObjectId, startTime, Thread.currentThread());
    }

    public ActiveTraceInfo(long traceObjectId, long startTime, Thread currentThread) {
        if (currentThread == null) {
            throw new NullPointerException("currentThread must not be null");
        }
        this.startTime = startTime;
        this.traceObjectId = traceObjectId;
        this.currentThread = currentThread;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getTraceObjectId() {
        return traceObjectId;
    }

    public Thread getCurrentThread() {
        return currentThread;
    }
}
