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

package com.navercorp.pinpoint.web.vo.stat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class SampledDataSourceList implements SampledAgentStatDataPoint {

    private final List<SampledDataSource> sampledDataSourceList = new ArrayList<SampledDataSource>();

    public void addSampledDataSource(SampledDataSource sampledDataSource) {
        sampledDataSourceList.add(sampledDataSource);
    }

    public List<SampledDataSource> getSampledDataSourceList() {
        return sampledDataSourceList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampledDataSourceList that = (SampledDataSourceList) o;

        return sampledDataSourceList != null ? sampledDataSourceList.equals(that.sampledDataSourceList) : that.sampledDataSourceList == null;

    }

    @Override
    public int hashCode() {
        return sampledDataSourceList != null ? sampledDataSourceList.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledDataSourceList{");
        sb.append("sampledDataSourceList=").append(sampledDataSourceList);
        sb.append('}');
        return sb.toString();
    }

}
