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

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class SampledAgentUriStat implements SampledAgentStatDataPoint {

    private final List<SampledEachUriStatBo> sampledEachUriStatBoList;

    public SampledAgentUriStat(List<SampledEachUriStatBo> sampledEachUriStatBoList) {
        this.sampledEachUriStatBoList = Objects.requireNonNull(sampledEachUriStatBoList, "sampledEachUriStatBoList");
    }

    public List<SampledEachUriStatBo> getSampledEachUriStatBoList() {
        return sampledEachUriStatBoList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampledAgentUriStat that = (SampledAgentUriStat) o;

        return sampledEachUriStatBoList != null ? sampledEachUriStatBoList.equals(that.sampledEachUriStatBoList) : that.sampledEachUriStatBoList == null;
    }

    @Override
    public int hashCode() {
        return sampledEachUriStatBoList != null ? sampledEachUriStatBoList.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledAgentUriStat{");
        sb.append("sampledEachUriStatBoList=").append(sampledEachUriStatBoList);
        sb.append('}');
        return sb.toString();
    }
}
