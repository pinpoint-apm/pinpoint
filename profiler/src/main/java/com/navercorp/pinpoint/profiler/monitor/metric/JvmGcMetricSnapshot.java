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

package com.navercorp.pinpoint.profiler.monitor.metric;

import com.navercorp.pinpoint.profiler.monitor.metric.gc.JvmGcType;

/**
 * @author jaehong.kim
 */
public class JvmGcMetricSnapshot {
    private JvmGcType type;
    private long jvmMemoryHeapUsed;
    private long jvmMemoryHeapMax;
    private long jvmMemoryNonHeapUsed;
    private long jvmMemoryNonHeapMax;
    private long jvmGcOldCount;
    private long jvmGcOldTime;
    private JvmGcDetailedMetricSnapshot jvmGcDetailed;

    public JvmGcType getType() {
        return type;
    }

    public void setType(JvmGcType type) {
        this.type = type;
    }

    public long getJvmMemoryHeapUsed() {
        return jvmMemoryHeapUsed;
    }

    public void setJvmMemoryHeapUsed(long jvmMemoryHeapUsed) {
        this.jvmMemoryHeapUsed = jvmMemoryHeapUsed;
    }

    public long getJvmMemoryHeapMax() {
        return jvmMemoryHeapMax;
    }

    public void setJvmMemoryHeapMax(long jvmMemoryHeapMax) {
        this.jvmMemoryHeapMax = jvmMemoryHeapMax;
    }

    public long getJvmMemoryNonHeapUsed() {
        return jvmMemoryNonHeapUsed;
    }

    public void setJvmMemoryNonHeapUsed(long jvmMemoryNonHeapUsed) {
        this.jvmMemoryNonHeapUsed = jvmMemoryNonHeapUsed;
    }

    public long getJvmMemoryNonHeapMax() {
        return jvmMemoryNonHeapMax;
    }

    public void setJvmMemoryNonHeapMax(long jvmMemoryNonHeapMax) {
        this.jvmMemoryNonHeapMax = jvmMemoryNonHeapMax;
    }

    public long getJvmGcOldCount() {
        return jvmGcOldCount;
    }

    public void setJvmGcOldCount(long jvmGcOldCount) {
        this.jvmGcOldCount = jvmGcOldCount;
    }

    public long getJvmGcOldTime() {
        return jvmGcOldTime;
    }

    public void setJvmGcOldTime(long jvmGcOldTime) {
        this.jvmGcOldTime = jvmGcOldTime;
    }

    public JvmGcDetailedMetricSnapshot getJvmGcDetailed() {
        return jvmGcDetailed;
    }

    public void setJvmGcDetailed(JvmGcDetailedMetricSnapshot jvmGcDetailed) {
        this.jvmGcDetailed = jvmGcDetailed;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JvmGcMetricSnapshot{");
        sb.append("type=").append(type);
        sb.append(", jvmMemoryHeapUsed=").append(jvmMemoryHeapUsed);
        sb.append(", jvmMemoryHeapMax=").append(jvmMemoryHeapMax);
        sb.append(", jvmMemoryNonHeapUsed=").append(jvmMemoryNonHeapUsed);
        sb.append(", jvmMemoryNonHeapMax=").append(jvmMemoryNonHeapMax);
        sb.append(", jvmGcOldCount=").append(jvmGcOldCount);
        sb.append(", jvmGcOldTime=").append(jvmGcOldTime);
        sb.append(", jvmGcDetailed=").append(jvmGcDetailed);
        sb.append('}');
        return sb.toString();
    }
}