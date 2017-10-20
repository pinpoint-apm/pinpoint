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
public class DataSourceListBo implements AgentStatDataPointList<DataSourceBo> {

    private final List<DataSourceBo> dataSourceBoList = new ArrayList<DataSourceBo>();

    private String agentId;
    private long startTimestamp;
    private long timestamp;

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public long getStartTimestamp() {
        return startTimestamp;
    }

    @Override
    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.DATASOURCE;
    }

    @Override
    public boolean add(DataSourceBo element) {
        return dataSourceBoList.add(element);
    }

    @Override
    public boolean remove(DataSourceBo element) {
        return dataSourceBoList.remove(element);
    }

    @Override
    public int size() {
        if (dataSourceBoList == null) {
            return 0;
        }

        return dataSourceBoList.size();
    }

    @Override
    public List<DataSourceBo> getList() {
        return new ArrayList<DataSourceBo>(dataSourceBoList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSourceListBo that = (DataSourceListBo) o;

        if (startTimestamp != that.startTimestamp) return false;
        if (timestamp != that.timestamp) return false;
        if (dataSourceBoList != null ? !dataSourceBoList.equals(that.dataSourceBoList) : that.dataSourceBoList != null) return false;
        return agentId != null ? agentId.equals(that.agentId) : that.agentId == null;

    }

    @Override
    public int hashCode() {
        int result = dataSourceBoList != null ? dataSourceBoList.hashCode() : 0;
        result = 31 * result + (agentId != null ? agentId.hashCode() : 0);
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataSourceListBo{");
        sb.append("dataSourceBoList=").append(dataSourceBoList);
        sb.append(", agentId='").append(agentId).append('\'');
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }

}
