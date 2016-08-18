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

import com.navercorp.pinpoint.web.vo.chart.TitledPoint;

/**
 * @author HyunGil Jeong
 */
public class SampledActiveTrace implements SampledAgentStatDataPoint {

    private TitledPoint<Long, Integer> fastCounts;
    private TitledPoint<Long, Integer> normalCounts;
    private TitledPoint<Long, Integer> slowCounts;
    private TitledPoint<Long, Integer> verySlowCounts;

    public TitledPoint<Long, Integer> getFastCounts() {
        return fastCounts;
    }

    public void setFastCounts(TitledPoint<Long, Integer> fastCounts) {
        this.fastCounts = fastCounts;
    }

    public TitledPoint<Long, Integer> getNormalCounts() {
        return normalCounts;
    }

    public void setNormalCounts(TitledPoint<Long, Integer> normalCounts) {
        this.normalCounts = normalCounts;
    }

    public TitledPoint<Long, Integer> getSlowCounts() {
        return slowCounts;
    }

    public void setSlowCounts(TitledPoint<Long, Integer> slowCounts) {
        this.slowCounts = slowCounts;
    }

    public TitledPoint<Long, Integer> getVerySlowCounts() {
        return verySlowCounts;
    }

    public void setVerySlowCounts(TitledPoint<Long, Integer> verySlowCounts) {
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
        return "SampledActiveTrace{" +
                "fastCounts=" + fastCounts +
                ", normalCounts=" + normalCounts +
                ", slowCounts=" + slowCounts +
                ", verySlowCounts=" + verySlowCounts +
                '}';
    }
}
