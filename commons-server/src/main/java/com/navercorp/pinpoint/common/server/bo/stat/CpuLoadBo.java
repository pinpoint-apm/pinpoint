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
public class CpuLoadBo extends AbstractStatDataPoint {

    public static final double UNCOLLECTED_VALUE = -1;

    private double jvmCpuLoad = UNCOLLECTED_VALUE;
    private double systemCpuLoad = UNCOLLECTED_VALUE;

    public CpuLoadBo(DataPoint point) {
        super(point);
    }

    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.CPU_LOAD;
    }

    public double getJvmCpuLoad() {
        return jvmCpuLoad;
    }

    public void setJvmCpuLoad(double jvmCpuLoad) {
        this.jvmCpuLoad = jvmCpuLoad;
    }

    public double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    public void setSystemCpuLoad(double systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }

    @Override
    public String toString() {
        return "CpuLoadBo{" +
                "point=" + point +
                ", jvmCpuLoad=" + jvmCpuLoad +
                ", systemCpuLoad=" + systemCpuLoad +
                '}';
    }
}
