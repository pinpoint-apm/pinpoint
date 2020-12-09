/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo.stat;

import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;

import java.util.Map;

/**
 * @author Taejin Koo
 */
public class SampledUriStatHistogramBo {

    private final AgentStatPoint<Integer> countPoint;
    private final AgentStatPoint<Long> maxTimePoint;
    private final AgentStatPoint<Double> avgTimePoint;
    private final Map<UriStatHistogramBucket, Integer> uriStatHistogramCountMap;
    private final long totalElapsedTime;

    public SampledUriStatHistogramBo(AgentStatPoint<Integer> countPoint, AgentStatPoint<Long> maxTimePoint, AgentStatPoint<Double> avgTimePoint, Map<UriStatHistogramBucket, Integer> uriStatHistogramCountMap, long totalElapsedTime) {
        this.countPoint = countPoint;
        this.maxTimePoint = maxTimePoint;
        this.avgTimePoint = avgTimePoint;
        this.uriStatHistogramCountMap = uriStatHistogramCountMap;
        this.totalElapsedTime = totalElapsedTime;
    }

    public AgentStatPoint<Integer> getCountPoint() {
        return countPoint;
    }

    public AgentStatPoint<Long> getMaxTimePoint() {
        return maxTimePoint;
    }

    public AgentStatPoint<Double> getAvgTimePoint() {
        return avgTimePoint;
    }

    public long getTotalElapsedTime() {
        return totalElapsedTime;
    }

    public int[] getUriStatHistogramValue() {
        int[] newArrayValue = UriStatHistogramBucket.createNewArrayValue();

        for (Map.Entry<UriStatHistogramBucket, Integer> entry : uriStatHistogramCountMap.entrySet()) {
            int index = entry.getKey().getIndex();
            newArrayValue[index] = entry.getValue();
        }

        return newArrayValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampledUriStatHistogramBo that = (SampledUriStatHistogramBo) o;

        if (totalElapsedTime != that.totalElapsedTime) return false;
        if (countPoint != null ? !countPoint.equals(that.countPoint) : that.countPoint != null) return false;
        if (maxTimePoint != null ? !maxTimePoint.equals(that.maxTimePoint) : that.maxTimePoint != null) return false;
        if (avgTimePoint != null ? !avgTimePoint.equals(that.avgTimePoint) : that.avgTimePoint != null) return false;
        return uriStatHistogramCountMap != null ? uriStatHistogramCountMap.equals(that.uriStatHistogramCountMap) : that.uriStatHistogramCountMap == null;
    }

    @Override
    public int hashCode() {
        int result = countPoint != null ? countPoint.hashCode() : 0;
        result = 31 * result + (maxTimePoint != null ? maxTimePoint.hashCode() : 0);
        result = 31 * result + (avgTimePoint != null ? avgTimePoint.hashCode() : 0);
        result = 31 * result + (uriStatHistogramCountMap != null ? uriStatHistogramCountMap.hashCode() : 0);
        result = 31 * result + (int) (totalElapsedTime ^ (totalElapsedTime >>> 32));
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledUriStatHistogramBo{");
        sb.append("countPoint=").append(countPoint);
        sb.append(", maxTimePoint=").append(maxTimePoint);
        sb.append(", avgTimePoint=").append(avgTimePoint);
        sb.append(", uriStatHistogramCountMap=").append(uriStatHistogramCountMap);
        sb.append(", totalElapsedTime=").append(totalElapsedTime);
        sb.append('}');
        return sb.toString();
    }
}
