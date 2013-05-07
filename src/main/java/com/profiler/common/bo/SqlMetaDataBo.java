package com.profiler.common.bo;

import com.profiler.common.util.BytesUtils;
import com.profiler.common.util.RowKeyUtils;
import com.profiler.common.util.TimeUtils;
import org.apache.hadoop.hbase.util.Bytes;

import static com.profiler.common.hbase.HBaseTables.AGENT_NAME_MAX_LEN;
import static com.profiler.common.util.BytesUtils.INT_BYTE_LENGTH;

/**
 *
 */
public class SqlMetaDataBo {
    private String agentId;
    private int hashCode;
    private long startTime;

    private String sql;

    public SqlMetaDataBo() {
    }


    public SqlMetaDataBo(String agentId, int hashCode, long startTime) {
        this.agentId = agentId;
        this.hashCode = hashCode;
        this.startTime = startTime;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }


    public int getHashCode() {
        return hashCode;
    }

    public void setHashCode(int hashCode) {
        this.hashCode = hashCode;
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
        this.agentId = Bytes.toString(rowKey, 0, AGENT_NAME_MAX_LEN).trim();
        this.hashCode = readKeyCode(rowKey);
        this.startTime = TimeUtils.recoveryCurrentTimeMillis(readTime(rowKey));
    }


    private static long readTime(byte[] rowKey) {
        return BytesUtils.bytesToLong(rowKey, AGENT_NAME_MAX_LEN + INT_BYTE_LENGTH);
    }

    private static int readKeyCode(byte[] rowKey) {
        return BytesUtils.bytesToInt(rowKey, AGENT_NAME_MAX_LEN);
    }

    public byte[] toRowKey() {
        return RowKeyUtils.getMetaInfoRowKey(this.agentId, this.hashCode, this.startTime);
    }

    @Override
    public String toString() {
        return "SqlMetaDataBo{" +
                "agentId='" + agentId + '\'' +
                ", hashCode=" + hashCode +
                ", startTime=" + startTime +
                ", sql='" + sql + '\'' +
                '}';
    }

}
