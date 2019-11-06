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

package com.navercorp.pinpoint.web.alarm.vo;

/**
 * @author Taejin Koo
 */
public class DataSourceAlarmVO {

    private final int id;
    private final String databaseName;
    private final long connectionUsedRate;

    public DataSourceAlarmVO(int id, String databaseName, long connectionUsedRate) {
        this.id = id;
        this.databaseName = databaseName;
        this.connectionUsedRate = connectionUsedRate;
    }

    public int getId() {
        return id;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public long getConnectionUsedRate() {
        return connectionUsedRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSourceAlarmVO that = (DataSourceAlarmVO) o;

        if (id != that.id) return false;
        if (connectionUsedRate != that.connectionUsedRate) return false;
        return databaseName != null ? databaseName.equals(that.databaseName) : that.databaseName == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (databaseName != null ? databaseName.hashCode() : 0);
        result = 31 * result + (int) (connectionUsedRate ^ (connectionUsedRate >>> 32));
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataSourceAlarmVO{");
        sb.append("id=").append(id);
        sb.append(", databaseName='").append(databaseName).append('\'');
        sb.append(", connectionUsedRate=").append(connectionUsedRate);
        sb.append('}');
        return sb.toString();
    }

}
