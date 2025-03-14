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
public class DataSourceBo extends AbstractStatDataPoint {

    public static final int UNCOLLECTED_INT_VALUE = -1;
    public static final String UNCOLLECTED_STRING_VALUE = "";
    public static final ServiceType UNCOLLECTED_SERVICE_TYPE_VALUE = ServiceType.UNKNOWN;

    private int id = UNCOLLECTED_INT_VALUE;
    private short serviceTypeCode = UNCOLLECTED_SERVICE_TYPE_VALUE.getCode();
    private String databaseName = UNCOLLECTED_STRING_VALUE;
    private String jdbcUrl = UNCOLLECTED_STRING_VALUE;
    private int activeConnectionSize = UNCOLLECTED_INT_VALUE;
    private int maxConnectionSize = UNCOLLECTED_INT_VALUE;

    public DataSourceBo(DataPoint point) {
        super(point);
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
