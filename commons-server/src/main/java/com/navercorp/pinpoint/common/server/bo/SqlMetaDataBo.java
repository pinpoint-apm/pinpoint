/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetaDataRowKey;


import java.util.Objects;

/**
 * @author emeroad
 */
public class SqlMetaDataBo implements MetaDataRowKey {
    private final String agentId;
    private final long startTime;

    private final int sqlId;

    private final String sql;

    public SqlMetaDataBo(String agentId, long startTime, int sqlId, String sql) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.startTime = startTime;
        this.sqlId = sqlId;
        this.sql = sql;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public long getAgentStartTime() {
        return startTime;
    }

    @Override
    public int getId() {
        return sqlId;
    }

    public String getSql() {
        return sql;
    }

    @Override
    public String toString() {
        return "SqlMetaDataBo{" +
                "agentId='" + agentId + '\'' +
                ", startTime=" + startTime +
                ", sqlId=" + sqlId +
                ", sql='" + sql + '\'' +
                '}';
    }

}
