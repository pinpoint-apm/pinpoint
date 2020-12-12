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

/**
 * @author Taejin Koo
 */
public class SampledEachUriStatBo implements SampledAgentStatDataPoint {

    private final String uri;

    private final SampledUriStatHistogramBo totalSampledUriStatHistogramBo;
    private final SampledUriStatHistogramBo failedSampledUriStatHistogramBo;

    public SampledEachUriStatBo(String uri, SampledUriStatHistogramBo totalSampledUriStatHistogramBo, SampledUriStatHistogramBo failedSampledUriStatHistogramBo) {
        this.uri = uri;
        this.totalSampledUriStatHistogramBo = totalSampledUriStatHistogramBo;
        this.failedSampledUriStatHistogramBo = failedSampledUriStatHistogramBo;
    }

    public String getUri() {
        return uri;
    }

    public SampledUriStatHistogramBo getTotalSampledUriStatHistogramBo() {
        return totalSampledUriStatHistogramBo;
    }

    public SampledUriStatHistogramBo getFailedSampledUriStatHistogramBo() {
        return failedSampledUriStatHistogramBo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampledEachUriStatBo that = (SampledEachUriStatBo) o;

        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;
        if (totalSampledUriStatHistogramBo != null ? !totalSampledUriStatHistogramBo.equals(that.totalSampledUriStatHistogramBo) : that.totalSampledUriStatHistogramBo != null) return false;
        return failedSampledUriStatHistogramBo != null ? failedSampledUriStatHistogramBo.equals(that.failedSampledUriStatHistogramBo) : that.failedSampledUriStatHistogramBo == null;
    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (totalSampledUriStatHistogramBo != null ? totalSampledUriStatHistogramBo.hashCode() : 0);
        result = 31 * result + (failedSampledUriStatHistogramBo != null ? failedSampledUriStatHistogramBo.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledEachUriStatBo{");
        sb.append("uri='").append(uri).append('\'');
        sb.append(", totalSampledUriStatHistogramBo=").append(totalSampledUriStatHistogramBo);
        sb.append(", failedSampledUriStatHistogramBo=").append(failedSampledUriStatHistogramBo);
        sb.append('}');
        return sb.toString();
    }
}
