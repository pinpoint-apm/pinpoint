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

/**
 * @author Taejin Koo
 */
public class DataSourceBo extends AbstractStatDataPoint {

    private final int id;
    private final short serviceTypeCode;
    private final String databaseName;
    private final String jdbcUrl;
    private final int activeConnectionSize;
    private final int maxConnectionSize;

    public DataSourceBo(DataPoint point,
                        int id,
                        short serviceTypeCode,
                        String databaseName,
                        String jdbcUrl,
                        int activeConnectionSize,
                        int maxConnectionSize) {
        super(point);
        this.id = id;
        this.serviceTypeCode = serviceTypeCode;
        this.databaseName = databaseName;
        this.jdbcUrl = jdbcUrl;
        this.activeConnectionSize = activeConnectionSize;
        this.maxConnectionSize = maxConnectionSize;
    }

    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.DATASOURCE;
    }

    public int getId() {
        return id;
    }

    public short getServiceTypeCode() {
        return serviceTypeCode;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public int getActiveConnectionSize() {
        return activeConnectionSize;
    }

    public int getMaxConnectionSize() {
        return maxConnectionSize;
    }

    @Override
    public String toString() {
        return "DataSourceBo{" +
                "point=" + point +
                ", id=" + id +
                ", serviceTypeCode=" + serviceTypeCode +
                ", databaseName='" + databaseName + '\'' +
                ", jdbcUrl='" + jdbcUrl + '\'' +
                ", activeConnectionSize=" + activeConnectionSize +
                ", maxConnectionSize=" + maxConnectionSize +
                '}';
    }
}
