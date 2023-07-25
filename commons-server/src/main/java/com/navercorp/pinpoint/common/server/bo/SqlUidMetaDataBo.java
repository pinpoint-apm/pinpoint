package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid.UidMetaDataRowKey;

import java.util.Arrays;
import java.util.Objects;

public class SqlUidMetaDataBo implements UidMetaDataRowKey {
    private final String agentId;
    private final long startTime;
    private final String applicationName;

    private final byte[] sqlUid;
    private final String sql;

    public SqlUidMetaDataBo(String agentId, long startTime, byte[] sqlUid, String sql) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.startTime = startTime;
        this.applicationName = null;
        this.sqlUid = Objects.requireNonNull(sqlUid, "sqlUid");
        this.sql = sql;
    }

    public SqlUidMetaDataBo(String agentId, long startTime, String applicationName, byte[] sqlUid, String sql) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.startTime = startTime;
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.sqlUid = Objects.requireNonNull(sqlUid, "sqlUid");
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
    public byte[] getUid() {
        return sqlUid;
    }

    public String getApplicationName() {
        if (applicationName == null) {
            // should not reach here
            return "";
        }
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
