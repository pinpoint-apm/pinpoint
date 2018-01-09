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

package com.navercorp.pinpoint.profiler.monitor.collector.deadlock;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.DeadlockMetric;
import com.navercorp.pinpoint.profiler.util.ThreadDumpUtils;
import com.navercorp.pinpoint.thrift.dto.TDeadlock;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class DefaultDeadlockMetricCollector implements DeadlockMetricCollector {

    private Set<Long> prevDeadlockedThreadIdSet = new HashSet<Long>();

    private final DeadlockMetric deadlockMetric;

    public DefaultDeadlockMetricCollector(DeadlockMetric deadlockMetric) {
        if (deadlockMetric == null) {
            throw new NullPointerException("deadlockMetric must not be null");
        }
        this.deadlockMetric = deadlockMetric;
    }

    @Override
    public TDeadlock collect() {
        Set<Long> deadlockedThreadIdSet = deadlockMetric.deadlockedThreadsIdSet();
        if (CollectionUtils.isEmpty(deadlockedThreadIdSet)) {
            return null;
        }

        // Only send id values that have already been sent
        if (prevDeadlockedThreadIdSet.containsAll(deadlockedThreadIdSet)) {
            TDeadlock deadlock = new TDeadlock();
            deadlock.setDeadlockedThreadCount(deadlockedThreadIdSet.size());
            return deadlock;
        }

        // The first event sends id and threadinfo
        TDeadlock deadlock = new TDeadlock();
        deadlock.setDeadlockedThreadCount(deadlockedThreadIdSet.size());
        for (Long deadlockedThreadId : deadlockedThreadIdSet) {
            TThreadDump tThreadDump = ThreadDumpUtils.createTThreadDump(deadlockedThreadId);
            deadlock.addToDeadlockedThreadList(tThreadDump);
            prevDeadlockedThreadIdSet = deadlockedThreadIdSet;
        }
        return deadlock;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultDeadlockMetricCollector{");
        sb.append("prevDeadlockedThreadIdSet=").append(prevDeadlockedThreadIdSet);
        sb.append(", deadlockMetric=").append(deadlockMetric);
        sb.append('}');
        return sb.toString();
    }

}
