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
import com.navercorp.pinpoint.web.vo.chart.UncollectedPointCreatorFactory;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.DoubleAgentStatPoint;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class SampledTransaction implements SampledAgentStatDataPoint {

    public static final double UNCOLLECTED_VALUE = -1D;
    public static final Point.UncollectedPointCreator<DoubleAgentStatPoint> UNCOLLECTED_POINT_CREATOR = UncollectedPointCreatorFactory.createDoublePointCreator(UNCOLLECTED_VALUE);

    private final DoubleAgentStatPoint sampledNew;
    private final DoubleAgentStatPoint sampledContinuation;
    private final DoubleAgentStatPoint unsampledNew;
    private final DoubleAgentStatPoint unsampledContinuation;
    private final DoubleAgentStatPoint skippedNew;
    private final DoubleAgentStatPoint skippedContinuation;
    private final DoubleAgentStatPoint total;

    public SampledTransaction(DoubleAgentStatPoint sampledNew, DoubleAgentStatPoint sampledContinuation, DoubleAgentStatPoint unsampledNew, DoubleAgentStatPoint unsampledContinuation, DoubleAgentStatPoint skippedNew, DoubleAgentStatPoint skippedContinuation, DoubleAgentStatPoint total) {
        this.sampledNew = Objects.requireNonNull(sampledNew, "sampledNew");
        this.sampledContinuation = Objects.requireNonNull(sampledContinuation, "sampledContinuation");
        this.unsampledNew = Objects.requireNonNull(unsampledNew, "unsampledNew");
        this.unsampledContinuation = Objects.requireNonNull(unsampledContinuation, "unsampledContinuation");
        this.skippedNew = Objects.requireNonNull(skippedNew, "skippedNew");
        this.skippedContinuation = Objects.requireNonNull(skippedContinuation, "skippedContinuation");
        this.total = Objects.requireNonNull(total, "total");
    }

    public DoubleAgentStatPoint getSampledNew() {
        return sampledNew;
    }

    public DoubleAgentStatPoint getSampledContinuation() {
        return sampledContinuation;
    }

    public DoubleAgentStatPoint getUnsampledNew() {
        return unsampledNew;
    }

    public DoubleAgentStatPoint getUnsampledContinuation() {
        return unsampledContinuation;
    }

    public DoubleAgentStatPoint getTotal() {
        return total;
    }

    public DoubleAgentStatPoint getSkippedNew() {
        return skippedNew;
    }

    public DoubleAgentStatPoint getSkippedContinuation() {
        return skippedContinuation;
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
        if (skippedNew != null ? !skippedNew.equals(that.skippedNew) : that.skippedNew != null) return false;
        if (skippedContinuation != null ? !skippedContinuation.equals(that.skippedContinuation) : that.skippedContinuation != null)
            return false;

        return total != null ? total.equals(that.total) : that.total == null;
    }

    @Override
    public int hashCode() {
        int result = sampledNew != null ? sampledNew.hashCode() : 0;
        result = 31 * result + (sampledContinuation != null ? sampledContinuation.hashCode() : 0);
        result = 31 * result + (unsampledNew != null ? unsampledNew.hashCode() : 0);
        result = 31 * result + (unsampledContinuation != null ? unsampledContinuation.hashCode() : 0);
        result = 31 * result + (skippedNew != null ? skippedNew.hashCode() : 0);
        result = 31 * result + (skippedContinuation != null ? skippedContinuation.hashCode() : 0);
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
        sb.append(", skippedNew=").append(skippedNew);
        sb.append(", skippedContinuation=").append(skippedContinuation);
        sb.append(", total=").append(total);
        sb.append('}');
        return sb.toString();
    }
}
