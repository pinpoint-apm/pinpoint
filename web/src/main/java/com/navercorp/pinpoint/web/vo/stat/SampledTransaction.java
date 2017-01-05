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
public class SampledTransaction implements SampledAgentStatDataPoint {

    private Point<Long, Double> sampledNew;
    private Point<Long, Double> sampledContinuation;
    private Point<Long, Double> unsampledNew;
    private Point<Long, Double> unsampledContinuation;
    private Point<Long, Double> total;

    public Point<Long, Double> getSampledNew() {
        return sampledNew;
    }

    public void setSampledNew(Point<Long, Double> sampledNew) {
        this.sampledNew = sampledNew;
    }

    public Point<Long, Double> getSampledContinuation() {
        return sampledContinuation;
    }

    public void setSampledContinuation(Point<Long, Double> sampledContinuation) {
        this.sampledContinuation = sampledContinuation;
    }

    public Point<Long, Double> getUnsampledNew() {
        return unsampledNew;
    }

    public void setUnsampledNew(Point<Long, Double> unsampledNew) {
        this.unsampledNew = unsampledNew;
    }

    public Point<Long, Double> getUnsampledContinuation() {
        return unsampledContinuation;
    }

    public void setUnsampledContinuation(Point<Long, Double> unsampledContinuation) {
        this.unsampledContinuation = unsampledContinuation;
    }

    public Point<Long, Double> getTotal() {
        return total;
    }

    public void setTotal(Point<Long, Double> total) {
        this.total = total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampledTransaction that = (SampledTransaction) o;

        if (sampledNew != null ? !sampledNew.equals(that.sampledNew) : that.sampledNew != null) return false;
        if (sampledContinuation != null ? !sampledContinuation.equals(that.sampledContinuation) : that.sampledContinuation != null)
            return false;
        if (unsampledNew != null ? !unsampledNew.equals(that.unsampledNew) : that.unsampledNew != null) return false;
        if (unsampledContinuation != null ? !unsampledContinuation.equals(that.unsampledContinuation) : that.unsampledContinuation != null)
            return false;
        return total != null ? total.equals(that.total) : that.total == null;
    }

    @Override
    public int hashCode() {
        int result = sampledNew != null ? sampledNew.hashCode() : 0;
        result = 31 * result + (sampledContinuation != null ? sampledContinuation.hashCode() : 0);
        result = 31 * result + (unsampledNew != null ? unsampledNew.hashCode() : 0);
        result = 31 * result + (unsampledContinuation != null ? unsampledContinuation.hashCode() : 0);
        result = 31 * result + (total != null ? total.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledTransaction{");
        sb.append("sampledNew=").append(sampledNew);
        sb.append(", sampledContinuation=").append(sampledContinuation);
        sb.append(", unsampledNew=").append(unsampledNew);
        sb.append(", unsampledContinuation=").append(unsampledContinuation);
        sb.append(", total=").append(total);
        sb.append('}');
        return sb.toString();
    }
}
