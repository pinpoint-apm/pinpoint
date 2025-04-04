/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.stat;

/**
 * @author HyunGil Jeong
 */
public class JvmGcDetailedBo extends AbstractStatDataPoint {

    private final long gcNewCount;
    private final long gcNewTime;

    private final double codeCacheUsed;

    private final double newGenUsed;
    private final double oldGenUsed;

    private final double survivorSpaceUsed;
    private final double permGenUsed;
    private final double metaspaceUsed;

    public JvmGcDetailedBo(DataPoint point,
                           long gcNewCount, long gcNewTime,
                           double codeCacheUsed,
                           double newGenUsed, double oldGenUsed,
                           double survivorSpaceUsed,
                           double permGenUsed, double metaspaceUsed) {
        super(point);
        this.gcNewCount = gcNewCount;
        this.gcNewTime = gcNewTime;
        this.codeCacheUsed = codeCacheUsed;
        this.newGenUsed = newGenUsed;
        this.oldGenUsed = oldGenUsed;
        this.survivorSpaceUsed = survivorSpaceUsed;
        this.permGenUsed = permGenUsed;
        this.metaspaceUsed = metaspaceUsed;
    }

    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.JVM_GC_DETAILED;
    }

    public long getGcNewCount() {
        return gcNewCount;
    }

    public long getGcNewTime() {
        return gcNewTime;
    }

    public double getCodeCacheUsed() {
        return codeCacheUsed;
    }

    public double getNewGenUsed() {
        return newGenUsed;
    }

    public double getOldGenUsed() {
        return oldGenUsed;
    }

    public double getSurvivorSpaceUsed() {
        return survivorSpaceUsed;
    }

    public double getPermGenUsed() {
        return permGenUsed;
    }

    public double getMetaspaceUsed() {
        return metaspaceUsed;
    }

    @Override
    public String toString() {
        return "JvmGcDetailedBo{" +
                "point=" + point +
                ", gcNewCount=" + gcNewCount +
                ", gcNewTime=" + gcNewTime +
                ", codeCacheUsed=" + codeCacheUsed +
                ", newGenUsed=" + newGenUsed +
                ", oldGenUsed=" + oldGenUsed +
                ", survivorSpaceUsed=" + survivorSpaceUsed +
                ", permGenUsed=" + permGenUsed +
                ", metaspaceUsed=" + metaspaceUsed +
                '}';
    }
}
