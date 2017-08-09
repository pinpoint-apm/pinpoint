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

package com.navercorp.pinpoint.profiler.monitor.metric.cpu;

import com.navercorp.pinpoint.common.util.CpuUtils;

/**
 * @author HyunGil Jeong
 */
public class JvmCpuUsageCalculator {

    private static final int CPU_COUNT = CpuUtils.cpuCount();

    private static final int UNSUPPORTED = -1;
    private static final int UNINITIALIZED = -1;

    private long lastCpuTimeNS = UNINITIALIZED;
    private long lastUpTimeMS = UNINITIALIZED;

    public double getJvmCpuUsage(final long cpuTimeNS, final long upTimeMS) {
        if (cpuTimeNS == UNSUPPORTED) {
            return CpuLoadMetric.UNCOLLECTED_USAGE;
        }

        if (this.lastCpuTimeNS == UNINITIALIZED || this.lastUpTimeMS == UNINITIALIZED) {
            this.lastCpuTimeNS = cpuTimeNS;
            this.lastUpTimeMS = upTimeMS;
            return 0.0D;
        }

        final long totalCpuTimeNS = cpuTimeNS - lastCpuTimeNS;
        final long diffUpTimeMS = upTimeMS - lastUpTimeMS;
        final long totalUpTimeNS = (diffUpTimeMS * 1000000) * CPU_COUNT;

        final double cpuLoad = totalUpTimeNS > 0 ?
                Math.min(100F, totalCpuTimeNS / (float) totalUpTimeNS) : UNSUPPORTED;

        this.lastCpuTimeNS = cpuTimeNS;
        this.lastUpTimeMS = upTimeMS;

        return cpuLoad;
    }
}
