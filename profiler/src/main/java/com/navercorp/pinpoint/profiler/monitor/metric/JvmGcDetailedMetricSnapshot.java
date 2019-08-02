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

/**
 * @author jaehong.kim
 */
public class JvmGcDetailedMetricSnapshot {
    private long jvmGcNewCount;
    private long jvmGcNewTime;
    private double jvmPoolCodeCacheUsed;
    private double jvmPoolNewGenUsed;
    private double jvmPoolOldGenUsed;
    private double jvmPoolSurvivorSpaceUsed;
    private double jvmPoolPermGenUsed;
    private double jvmPoolMetaspaceUsed;

    public long getJvmGcNewCount() {
        return jvmGcNewCount;
    }

    public void setJvmGcNewCount(long jvmGcNewCount) {
        this.jvmGcNewCount = jvmGcNewCount;
    }

    public long getJvmGcNewTime() {
        return jvmGcNewTime;
    }

    public void setJvmGcNewTime(long jvmGcNewTime) {
        this.jvmGcNewTime = jvmGcNewTime;
    }

    public double getJvmPoolCodeCacheUsed() {
        return jvmPoolCodeCacheUsed;
    }

    public void setJvmPoolCodeCacheUsed(double jvmPoolCodeCacheUsed) {
        this.jvmPoolCodeCacheUsed = jvmPoolCodeCacheUsed;
    }

    public double getJvmPoolNewGenUsed() {
        return jvmPoolNewGenUsed;
    }

    public void setJvmPoolNewGenUsed(double jvmPoolNewGenUsed) {
        this.jvmPoolNewGenUsed = jvmPoolNewGenUsed;
    }

    public double getJvmPoolOldGenUsed() {
        return jvmPoolOldGenUsed;
    }

    public void setJvmPoolOldGenUsed(double jvmPoolOldGenUsed) {
        this.jvmPoolOldGenUsed = jvmPoolOldGenUsed;
    }

    public double getJvmPoolSurvivorSpaceUsed() {
        return jvmPoolSurvivorSpaceUsed;
    }

    public void setJvmPoolSurvivorSpaceUsed(double jvmPoolSurvivorSpaceUsed) {
        this.jvmPoolSurvivorSpaceUsed = jvmPoolSurvivorSpaceUsed;
    }

    public double getJvmPoolPermGenUsed() {
        return jvmPoolPermGenUsed;
    }

    public void setJvmPoolPermGenUsed(double jvmPoolPermGenUsed) {
        this.jvmPoolPermGenUsed = jvmPoolPermGenUsed;
    }

    public double getJvmPoolMetaspaceUsed() {
        return jvmPoolMetaspaceUsed;
    }

    public void setJvmPoolMetaspaceUsed(double jvmPoolMetaspaceUsed) {
        this.jvmPoolMetaspaceUsed = jvmPoolMetaspaceUsed;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JvmGcDetailedMetricSnapshot{");
        sb.append("jvmGcNewCount=").append(jvmGcNewCount);
        sb.append(", jvmGcNewTime=").append(jvmGcNewTime);
        sb.append(", jvmPoolCodeCacheUsed=").append(jvmPoolCodeCacheUsed);
        sb.append(", jvmPoolNewGenUsed=").append(jvmPoolNewGenUsed);
        sb.append(", jvmPoolOldGenUsed=").append(jvmPoolOldGenUsed);
        sb.append(", jvmPoolSurvivorSpaceUsed=").append(jvmPoolSurvivorSpaceUsed);
        sb.append(", jvmPoolPermGenUsed=").append(jvmPoolPermGenUsed);
        sb.append(", jvmPoolMetaspaceUsed=").append(jvmPoolMetaspaceUsed);
        sb.append('}');
        return sb.toString();
    }
}