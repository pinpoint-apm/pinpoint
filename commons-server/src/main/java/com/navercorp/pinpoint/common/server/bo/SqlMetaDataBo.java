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

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

/**
 * @author emeroad
 */
public class SqlMetaDataBo {
    private String agentId;
    private long startTime;

    private int sqlId;

    private String sql;

    public SqlMetaDataBo() {
    }


    public SqlMetaDataBo(String agentId, long startTime, int sqlId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        this.agentId = agentId;
        this.sqlId = sqlId;
        this.startTime = startTime;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * @deprecated Since 1.6.1. Use {@link #getSqlId()}
     */
    @Deprecated
    public int getHashCode() {
        return getSqlId();
    }


    /**
     * @deprecated Since 1.6.1. Use {@link #setSqlId(int)}
     */
    @Deprecated
    public void setHashCode(int sqlId) {
        this.setSqlId(sqlId);
    }

    public int getSqlId() {
        return sqlId;
    }

    public void setSqlId(int sqlId) {
        this.sqlId = sqlId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void readRowKey(byte[] rowKey) {
        this.agentId = BytesUtils.safeTrim(BytesUtils.toString(rowKey, 0, PinpointConstants.AGENT_NAME_MAX_LEN));
        this.startTime = TimeUtils.recoveryTimeMillis(readTime(rowKey));
        this.sqlId = readSqlId(rowKey);
    }


    private static long readTime(byte[] rowKey) {
        return BytesUtils.bytesToLong(rowKey, PinpointConstants.AGENT_NAME_MAX_LEN);
    }

    private static int readSqlId(byte[] rowKey) {
        return BytesUtils.bytesToInt(rowKey, PinpointConstants.AGENT_NAME_MAX_LEN + BytesUtils.LONG_BYTE_LENGTH);
    }

    public byte[] toRowKey() {
        return RowKeyUtils.getMetaInfoRowKey(this.agentId, this.startTime, this.sqlId);
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
