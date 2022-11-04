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

package com.navercorp.pinpoint.common.server.bo.stat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class DataSourceListBo extends AbstractAgentStatDataPoint implements AgentStatDataPointList<DataSourceBo> {

    private final List<DataSourceBo> dataSourceBoList = new ArrayList<>();

    public DataSourceListBo() {
        super(AgentStatType.DATASOURCE);
    }

    @Override
    public boolean add(DataSourceBo element) {
        return dataSourceBoList.add(element);
    }


    @Override
    public int size() {
        return dataSourceBoList.size();
    }

    @Override
    public List<DataSourceBo> getList() {
        return new ArrayList<>(dataSourceBoList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DataSourceListBo that = (DataSourceListBo) o;

        return dataSourceBoList != null ? dataSourceBoList.equals(that.dataSourceBoList) : that.dataSourceBoList == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (dataSourceBoList != null ? dataSourceBoList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DataSourceListBo{" +
                "dataSourceBoList=" + dataSourceBoList +
                "} " + super.toString();
    }
}
