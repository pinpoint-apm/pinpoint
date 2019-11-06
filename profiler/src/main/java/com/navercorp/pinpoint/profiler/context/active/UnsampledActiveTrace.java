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
 * @author HyunGil Jeong
 */
public class UnsampledActiveTrace implements ActiveTrace {

    private final long id;
    private final long startTime;
    private final long threadId;

    UnsampledActiveTrace(long id, long startTime, long threadId) {
        this.id = id;
        this.startTime = startTime;
        // @Nullable
        this.threadId = threadId;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getId() {
        return id;
    }


    @Override
    public ActiveTraceSnapshot snapshot() {
        return new UnsampledActiveTraceSnapshot(id, startTime, threadId);
    }

    @Override
    public String toString() {
        return "UnsampledActiveTrace{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", threadId=" + threadId +
                '}';
    }
}
