/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.monitor.metric.deadlock;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class DeadlockMetricSnapshot {
    private int deadlockedThreadCount;
    private List<ThreadDumpMetricSnapshot> deadlockedThreadList = new ArrayList<ThreadDumpMetricSnapshot>();

    public int getDeadlockedThreadCount() {
        return deadlockedThreadCount;
    }

    public void setDeadlockedThreadCount(int deadlockedThreadCount) {
        this.deadlockedThreadCount = deadlockedThreadCount;
    }

    public List<ThreadDumpMetricSnapshot> getDeadlockedThreadList() {
        return deadlockedThreadList;
    }

    public void addDeadlockedThread(ThreadDumpMetricSnapshot threadDumpMetricSnapshot) {
        this.deadlockedThreadList.add(threadDumpMetricSnapshot);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeadlockMetricSnapshot{");
        sb.append("deadlockedThreadCount=").append(deadlockedThreadCount);
        sb.append(", deadlockedThreadList=").append(deadlockedThreadList);
        sb.append('}');
        return sb.toString();
    }
}