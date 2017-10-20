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

import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author Taejin Koo
 */
public class DataSourceBo implements AgentStatDataPoint {

    public static final int UNCOLLECTED_INT_VALUE = -1;
    public static final String UNCOLLECTED_STRING_VALUE = "";
    public static final ServiceType UNCOLLECTED_SERVICE_TYPE_VALUE = ServiceType.UNKNOWN;

    private String agentId;
    private long startTimestamp;
    private long timestamp;

    private int id = UNCOLLECTED_INT_VALUE;
    private short serviceTypeCode = UNCOLLECTED_SERVICE_TYPE_VALUE.getCode();
    private String databaseName = UNCOLLECTED_STRING_VALUE;
    private String jdbcUrl = UNCOLLECTED_STRING_VALUE;
    private int activeConnectionSize = UNCOLLECTED_INT_VALUE;
    private int maxConnectionSize = UNCOLLECTED_INT_VALUE;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public short getServiceTypeCode() {
        return serviceTypeCode;
    }

    public void setServiceTypeCode(short serviceTypeCode) {
        this.serviceTypeCode = serviceTypeCode;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public int getActiveConnectionSize() {
        return activeConnectionSize;
    }

    public void setActiveConnectionSize(int activeConnectionSize) {
        this.activeConnectionSize = activeConnectionSize;
    }

    public int getMaxConnectionSize() {
        return maxConnectionSize;
    }

    public void setMaxConnectionSize(int maxConnectionSize) {
        this.maxConnectionSize = maxConnectionSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSourceBo that = (DataSourceBo) o;

        if (startTimestamp != that.startTimestamp) return false;
        if (timestamp != that.timestamp) return false;
        if (id != that.id) return false;
        if (serviceTypeCode != that.serviceTypeCode) return false;
        if (activeConnectionSize != that.activeConnectionSize) return false;
        if (maxConnectionSize != that.maxConnectionSize) return false;
        if (agentId != null ? !agentId.equals(that.agentId) : that.agentId != null) return false;
        if (databaseName != null ? !databaseName.equals(that.databaseName) : that.databaseName != null) return false;
        return jdbcUrl != null ? jdbcUrl.equals(that.jdbcUrl) : that.jdbcUrl == null;

    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + id;
        result = 31 * result + (int) serviceTypeCode;
        result = 31 * result + (databaseName != null ? databaseName.hashCode() : 0);
        result = 31 * result + (jdbcUrl != null ? jdbcUrl.hashCode() : 0);
        result = 31 * result + activeConnectionSize;
        result = 31 * result + maxConnectionSize;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataSourceBo{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", id=").append(id);
        sb.append(", serviceTypeCode=").append(serviceTypeCode);
        sb.append(", databaseName='").append(databaseName).append('\'');
        sb.append(", jdbcUrl='").append(jdbcUrl).append('\'');
        sb.append(", activeConnectionSize=").append(activeConnectionSize);
        sb.append(", maxConnectionSize=").append(maxConnectionSize);
        sb.append('}');
        return sb.toString();
    }

}
