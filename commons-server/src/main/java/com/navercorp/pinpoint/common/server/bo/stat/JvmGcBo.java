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

import com.navercorp.pinpoint.common.server.bo.JvmGcType;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class JvmGcBo extends AbstractStatDataPoint {

    private final JvmGcType gcType;

    private final long heapUsed;
    private final long heapMax;

    private final long nonHeapUsed;
    private final long nonHeapMax;

    private final long gcOldCount;
    private final long gcOldTime;

    public JvmGcBo(DataPoint point,
                   JvmGcType gcType,
                   long heapUsed, long heapMax,
                   long nonHeapUsed, long nonHeapMax,
                   long gcOldCount, long gcOldTime) {
        super(point);
        this.gcType = Objects.requireNonNull(gcType, "gcType");
        this.heapUsed = heapUsed;
        this.heapMax = heapMax;

        this.nonHeapUsed = nonHeapUsed;
        this.nonHeapMax = nonHeapMax;

        this.gcOldCount = gcOldCount;
        this.gcOldTime = gcOldTime;
    }

    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.JVM_GC;
    }

    public JvmGcType getGcType() {
        return gcType;
    }

    public long getHeapUsed() {
        return heapUsed;
    }

    public long getHeapMax() {
        return heapMax;
    }

    public long getNonHeapUsed() {
        return nonHeapUsed;
    }

    public long getNonHeapMax() {
        return nonHeapMax;
    }

    public long getGcOldCount() {
        return gcOldCount;
    }

    public long getGcOldTime() {
        return gcOldTime;
    }

    @Override
    public String toString() {
        return "JvmGcBo{" +
                "point=" + point +
                ", gcType=" + gcType +
                ", heapUsed=" + heapUsed +
                ", heapMax=" + heapMax +
                ", nonHeapUsed=" + nonHeapUsed +
                ", nonHeapMax=" + nonHeapMax +
                ", gcOldCount=" + gcOldCount +
                ", gcOldTime=" + gcOldTime +
                '}';
    }
}
