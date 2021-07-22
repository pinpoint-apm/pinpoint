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

package com.navercorp.pinpoint.batch.alarm.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Taejin Koo
 * @author Jongjin.Bae
 */
@JsonSerialize(using = DataSourceAlarmVOSerializer.class)
public class DataSourceAlarmVO {

    private final int id;
    private final String databaseName;
    private final int activeConnectionAvg;
    private final int maxConnectionAvg;

    public DataSourceAlarmVO(int id, String databaseName, int activeConnectionAvg, int maxConnectionAvg) {
        this.id = id;
        this.databaseName = databaseName;

        this.activeConnectionAvg = activeConnectionAvg;
        this.maxConnectionAvg = maxConnectionAvg;
    }

    public int getId() {
        return id;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public int getActiveConnectionAvg() {
        return activeConnectionAvg;
    }

    public int getMaxConnectionAvg() {
        return maxConnectionAvg;
    }

    public long getConnectionUsedRate() {
        if (activeConnectionAvg == 0 || maxConnectionAvg == 0) {
            return 0;
        } else {
            return (activeConnectionAvg * 100L) / maxConnectionAvg;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSourceAlarmVO that = (DataSourceAlarmVO) o;

        if (id != that.id) return false;
        if (activeConnectionAvg != that.activeConnectionAvg) return false;
        if (maxConnectionAvg != that.maxConnectionAvg) return false;
        return databaseName != null ? databaseName.equals(that.databaseName) : that.databaseName == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (databaseName != null ? databaseName.hashCode() : 0);
        result = 31 * result + activeConnectionAvg;
        result = 31 * result + maxConnectionAvg;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataSourceAlarmVO{");
        sb.append("id=").append(id);
        sb.append(", databaseName='").append(databaseName).append('\'');
        sb.append(", activeConnectionAvg=").append(activeConnectionAvg);
        sb.append(", maxConnectionAvg=").append(maxConnectionAvg);
        sb.append('}');
        return sb.toString();
    }
}
