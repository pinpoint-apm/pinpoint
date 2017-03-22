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

package com.navercorp.pinpoint.web.vo.stat;

import com.navercorp.pinpoint.web.vo.chart.Point;

/**
 * @author HyunGil Jeong
 */
public class SampledCpuLoad implements SampledAgentStatDataPoint {

    private Point<Long, Double> jvmCpuLoad;
    private Point<Long, Double> systemCpuLoad;

    public Point<Long, Double> getJvmCpuLoad() {
        return jvmCpuLoad;
    }

    public void setJvmCpuLoad(Point<Long, Double> jvmCpuLoad) {
        this.jvmCpuLoad = jvmCpuLoad;
    }

    public Point<Long, Double> getSystemCpuLoad() {
        return systemCpuLoad;
    }

    public void setSystemCpuLoad(Point<Long, Double> systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampledCpuLoad that = (SampledCpuLoad) o;

        if (jvmCpuLoad != null ? !jvmCpuLoad.equals(that.jvmCpuLoad) : that.jvmCpuLoad != null) return false;
        return systemCpuLoad != null ? systemCpuLoad.equals(that.systemCpuLoad) : that.systemCpuLoad == null;
    }

    @Override
    public int hashCode() {
        int result = jvmCpuLoad != null ? jvmCpuLoad.hashCode() : 0;
        result = 31 * result + (systemCpuLoad != null ? systemCpuLoad.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledCpuLoad{");
        sb.append("jvmCpuLoad=").append(jvmCpuLoad);
        sb.append(", systemCpuLoad=").append(systemCpuLoad);
        sb.append('}');
        return sb.toString();
    }
}
