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

import com.navercorp.pinpoint.web.vo.chart.Point;

/**
 * @author Taejin Koo
 */
public class SampledDataSource implements SampledAgentStatDataPoint {

    private int id;
    private short serviceTypeCode;
    private String databaseName;
    private String jdbcUrl;
    private Point<Long, Integer> activeConnectionSize;
    private Point<Long, Integer> maxConnectionSize;

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

    public Point<Long, Integer> getActiveConnectionSize() {
        return activeConnectionSize;
    }

    public void setActiveConnectionSize(Point<Long, Integer> activeConnectionSize) {
        this.activeConnectionSize = activeConnectionSize;
    }

    public Point<Long, Integer> getMaxConnectionSize() {
        return maxConnectionSize;
    }

    public void setMaxConnectionSize(Point<Long, Integer> maxConnectionSize) {
        this.maxConnectionSize = maxConnectionSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampledDataSource that = (SampledDataSource) o;

        if (id != that.id) return false;
        if (serviceTypeCode != that.serviceTypeCode) return false;
        if (databaseName != null ? !databaseName.equals(that.databaseName) : that.databaseName != null) return false;
        if (jdbcUrl != null ? !jdbcUrl.equals(that.jdbcUrl) : that.jdbcUrl != null) return false;
        if (activeConnectionSize != null ? !activeConnectionSize.equals(that.activeConnectionSize) : that.activeConnectionSize != null) return false;
        return maxConnectionSize != null ? maxConnectionSize.equals(that.maxConnectionSize) : that.maxConnectionSize == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (int) serviceTypeCode;
        result = 31 * result + (databaseName != null ? databaseName.hashCode() : 0);
        result = 31 * result + (jdbcUrl != null ? jdbcUrl.hashCode() : 0);
        result = 31 * result + (activeConnectionSize != null ? activeConnectionSize.hashCode() : 0);
        result = 31 * result + (maxConnectionSize != null ? maxConnectionSize.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledDataSource{");
        sb.append("id=").append(id);
        sb.append(", serviceTypeCode=").append(serviceTypeCode);
        sb.append(", databaseName='").append(databaseName).append('\'');
        sb.append(", jdbcUrl='").append(jdbcUrl).append('\'');
        sb.append(", activeConnectionSize=").append(activeConnectionSize);
        sb.append(", maxConnectionSize=").append(maxConnectionSize);
        sb.append('}');
        return sb.toString();
    }

}
