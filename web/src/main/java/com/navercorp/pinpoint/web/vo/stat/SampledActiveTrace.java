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
public class SampledActiveTrace implements SampledAgentStatDataPoint {

    private Point<Long, Integer> fastCounts;
    private Point<Long, Integer> normalCounts;
    private Point<Long, Integer> slowCounts;
    private Point<Long, Integer> verySlowCounts;

    public Point<Long, Integer> getFastCounts() {
        return fastCounts;
    }

    public void setFastCounts(Point<Long, Integer> fastCounts) {
        this.fastCounts = fastCounts;
    }

    public Point<Long, Integer> getNormalCounts() {
        return normalCounts;
    }

    public void setNormalCounts(Point<Long, Integer> normalCounts) {
        this.normalCounts = normalCounts;
    }

    public Point<Long, Integer> getSlowCounts() {
        return slowCounts;
    }

    public void setSlowCounts(Point<Long, Integer> slowCounts) {
        this.slowCounts = slowCounts;
    }

    public Point<Long, Integer> getVerySlowCounts() {
        return verySlowCounts;
    }

    public void setVerySlowCounts(Point<Long, Integer> verySlowCounts) {
        this.verySlowCounts = verySlowCounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampledActiveTrace that = (SampledActiveTrace) o;

        if (fastCounts != null ? !fastCounts.equals(that.fastCounts) : that.fastCounts != null) return false;
        if (normalCounts != null ? !normalCounts.equals(that.normalCounts) : that.normalCounts != null) return false;
        if (slowCounts != null ? !slowCounts.equals(that.slowCounts) : that.slowCounts != null) return false;
        return verySlowCounts != null ? verySlowCounts.equals(that.verySlowCounts) : that.verySlowCounts == null;
    }

    @Override
    public int hashCode() {
        int result = fastCounts != null ? fastCounts.hashCode() : 0;
        result = 31 * result + (normalCounts != null ? normalCounts.hashCode() : 0);
        result = 31 * result + (slowCounts != null ? slowCounts.hashCode() : 0);
        result = 31 * result + (verySlowCounts != null ? verySlowCounts.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledActiveTrace{");
        sb.append("fastCounts=").append(fastCounts);
        sb.append(", normalCounts=").append(normalCounts);
        sb.append(", slowCounts=").append(slowCounts);
        sb.append(", verySlowCounts=").append(verySlowCounts);
        sb.append('}');
        return sb.toString();
    }
}
