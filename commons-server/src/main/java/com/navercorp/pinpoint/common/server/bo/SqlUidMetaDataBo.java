/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid.UidMetaDataRowKey;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.NumberPrecondition;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Objects;

public class SqlUidMetaDataBo implements UidMetaDataRowKey {
    private final ServiceUid serviceUid;

    @NonNull
    private final String agentId;
    private final long startTime;
    @NonNull
    private final String applicationName;

    private final byte[] sqlUid;
    @NonNull
    private final String sql;

    // used by web only
    public SqlUidMetaDataBo(ServiceUid serviceUid, String agentId, long startTime, byte[] sqlUid, String sql) {
        this(serviceUid, agentId, startTime, "", sqlUid, sql);
    }

    public SqlUidMetaDataBo(ServiceUid serviceUid, String agentId, long startTime, String applicationName, byte[] sqlUid, String sql) {
        this.serviceUid = Objects.requireNonNull(serviceUid, "serviceUid");
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
        this.startTime = NumberPrecondition.requirePositiveOrZero(startTime, "startTime");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.sqlUid = Objects.requireNonNull(sqlUid, "sqlUid");
        this.sql = Objects.requireNonNull(sql, "sql");

        if (sqlUid.length != UidMetaDataRowKey.UID_LENGTH) {
            throw new IllegalArgumentException("sqlUid length must be 16: " + sqlUid.length);
        }
    }

    @Override
    public ServiceUid getServiceUid() {
        return serviceUid;
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
    public byte[] getUid() {
        return sqlUid;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getSql() {
        return sql;
    }

    @Override
    public String toString() {
        return "SqlUidMetaDataBo{" +
                "agentId='" + agentId + '\'' +
                ", startTime=" + startTime +
                ", applicationName='" + applicationName + '\'' +
                ", sqlUid=" + Arrays.toString(sqlUid) +
                ", sql='" + sql + '\'' +
                '}';
    }
}
