/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.collector.deadlock;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.DeadlockMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.DeadlockMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.ThreadDumpMetricSnapshot;
import com.navercorp.pinpoint.profiler.util.ThreadDumpUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class DefaultDeadlockMetricCollector implements AgentStatMetricCollector<DeadlockMetricSnapshot> {

    private Set<Long> prevDeadlockedThreadIdSet = new HashSet<Long>();

    private final DeadlockMetric deadlockMetric;

    public DefaultDeadlockMetricCollector(DeadlockMetric deadlockMetric) {
        this.deadlockMetric = Assert.requireNonNull(deadlockMetric, "deadlockMetric");
    }

    @Override
    public DeadlockMetricSnapshot collect() {
        Set<Long> deadlockedThreadIdSet = deadlockMetric.deadlockedThreadsIdSet();
        if (CollectionUtils.isEmpty(deadlockedThreadIdSet)) {
            return null;
        }

        // Only send id values that have already been sent
        if (prevDeadlockedThreadIdSet.containsAll(deadlockedThreadIdSet)) {
            DeadlockMetricSnapshot deadlock = new DeadlockMetricSnapshot();
            deadlock.setDeadlockedThreadCount(deadlockedThreadIdSet.size());
            return deadlock;
        }

        // The first event sends id and threadinfo
        final DeadlockMetricSnapshot deadlockMetricSnapshot = new DeadlockMetricSnapshot();
        deadlockMetricSnapshot.setDeadlockedThreadCount(deadlockedThreadIdSet.size());
        for (Long deadlockedThreadId : deadlockedThreadIdSet) {
            final ThreadDumpMetricSnapshot tThreadDump = ThreadDumpUtils.createTThreadDump(deadlockedThreadId);
            deadlockMetricSnapshot.addDeadlockedThread(tThreadDump);
            prevDeadlockedThreadIdSet = deadlockedThreadIdSet;
        }
        return deadlockMetricSnapshot;
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