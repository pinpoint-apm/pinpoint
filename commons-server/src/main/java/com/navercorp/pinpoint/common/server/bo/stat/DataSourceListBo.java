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

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class DataSourceListBo extends AbstractStatDataPoint implements AgentStatDataPointList<DataSourceBo>  {

    private final List<DataSourceBo> dataSourceBoList;

    public DataSourceListBo(DataPoint point, List<DataSourceBo> dataSourceBoList) {
        super(point);
        Objects.requireNonNull(dataSourceBoList, "dataSourceBoList");
        this.dataSourceBoList = List.copyOf(dataSourceBoList);
    }

    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.DATASOURCE;
    }

    @Override
    public int size() {
        return dataSourceBoList.size();
    }

    @Override
    public List<DataSourceBo> getList() {
        return dataSourceBoList;
    }


    @Override
    public String toString() {
        return "DataSourceListBo{" +
                "dataSourceBoList=" + dataSourceBoList +
                ", point=" + point +
                '}';
    }
}
