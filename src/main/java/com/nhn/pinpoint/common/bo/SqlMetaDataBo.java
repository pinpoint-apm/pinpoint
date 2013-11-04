package com.nhn.pinpoint.common.bo;

import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.RowKeyUtils;
import com.nhn.pinpoint.common.util.TimeUtils;
import org.apache.hadoop.hbase.util.Bytes;

import static com.nhn.pinpoint.common.PinpointConstants.AGENT_NAME_MAX_LEN;
import static com.nhn.pinpoint.common.util.BytesUtils.LONG_BYTE_LENGTH;

/**
 * @author emeroad
 */
public class SqlMetaDataBo {
    private String agentId;
    private long startTime;

    private int hashCode;

    private String sql;

    public SqlMetaDataBo() {
    }


    public SqlMetaDataBo(String agentId, long startTime, int hashCode) {
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
        this.startTime = TimeUtils.recoveryCurrentTimeMillis(readTime(rowKey));
        this.hashCode = readKeyCode(rowKey);
    }


    private static long readTime(byte[] rowKey) {
        return BytesUtils.bytesToLong(rowKey, AGENT_NAME_MAX_LEN);
    }

    private static int readKeyCode(byte[] rowKey) {
        return BytesUtils.bytesToInt(rowKey, AGENT_NAME_MAX_LEN + LONG_BYTE_LENGTH);
    }

    public byte[] toRowKey() {
        return RowKeyUtils.getMetaInfoRowKey(this.agentId, this.startTime, this.hashCode);
    }

    @Override
    public String toString() {
        return "SqlMetaDataBo{" +
                "agentId='" + agentId + '\'' +
                ", startTime=" + startTime +
                ", hashCode=" + hashCode +
                ", sql='" + sql + '\'' +
                '}';
    }

}
