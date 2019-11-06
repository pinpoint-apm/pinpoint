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

package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.view.AgentStatPointSerializer;
import com.navercorp.pinpoint.web.vo.chart.Point;

/**
 * @author HyunGil Jeong
 */
@JsonSerialize(using = AgentStatPointSerializer.class)
public class AgentStatPoint<Y extends Number> implements Point {

    private final long xVal;
    private final Y minYVal;
    private final Y maxYVal;
    private final Double avgYVal;
    private final Y sumYVal;

    public AgentStatPoint(long xVal, Y yVal) {
        this.xVal = xVal;
        this.minYVal = yVal;
        this.maxYVal = yVal;
        if (yVal == null) {
            this.avgYVal = null;
        } else {
            this.avgYVal = yVal.doubleValue();
        }
        this.sumYVal = yVal;
    }

    public AgentStatPoint(long xVal, Y minYVal, Y maxYVal, Double avgYVal, Y sumYVal) {
        this.xVal = xVal;
        this.minYVal = minYVal;
        this.maxYVal = maxYVal;
        this.avgYVal = avgYVal;
        this.sumYVal = sumYVal;
    }

    @Override
    public long getXVal() {
        return xVal;
    }

    public Y getMinYVal() {
        return minYVal;
    }

    public Y getMaxYVal() {
        return maxYVal;
    }

    public Double getAvgYVal() {
        return avgYVal;
    }

    public Y getSumYVal() {
        return sumYVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentStatPoint<?> that = (AgentStatPoint<?>) o;

        if (xVal != that.xVal) return false;
        if (minYVal != null ? !minYVal.equals(that.minYVal) : that.minYVal != null) return false;
        if (maxYVal != null ? !maxYVal.equals(that.maxYVal) : that.maxYVal != null) return false;
        if (avgYVal != null ? !avgYVal.equals(that.avgYVal) : that.avgYVal != null) return false;
        return sumYVal != null ? sumYVal.equals(that.sumYVal) : that.sumYVal == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (xVal ^ (xVal >>> 32));
        result = 31 * result + (minYVal != null ? minYVal.hashCode() : 0);
        result = 31 * result + (maxYVal != null ? maxYVal.hashCode() : 0);
        result = 31 * result + (avgYVal != null ? avgYVal.hashCode() : 0);
        result = 31 * result + (sumYVal != null ? sumYVal.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentStatPoint{");
        sb.append("xVal=").append(xVal);
        sb.append(", minYVal=").append(minYVal);
        sb.append(", maxYVal=").append(maxYVal);
        sb.append(", avgYVal=").append(avgYVal);
        sb.append(", sumYVal=").append(sumYVal);
        sb.append('}');
        return sb.toString();
    }
}
