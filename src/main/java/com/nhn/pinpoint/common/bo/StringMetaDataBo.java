package com.nhn.pinpoint.common.bo;

import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.RowKeyUtils;
import com.nhn.pinpoint.common.util.TimeUtils;
import org.apache.hadoop.hbase.util.Bytes;

import static com.nhn.pinpoint.common.PinpointConstants.AGENT_NAME_MAX_LEN;
import static com.nhn.pinpoint.common.util.BytesUtils.INT_BYTE_LENGTH;

/**
 *
 */
public class StringMetaDataBo {
    private String agentId;
    private int stringId;
    private long startTime;

    private String stringValue;

    public StringMetaDataBo() {
    }


    public StringMetaDataBo(String agentId, int stringId, long startTime) {
        this.agentId = agentId;
        this.stringId = stringId;
        this.startTime = startTime;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }


    public int getStringId() {
        return stringId;
    }

    public void setStringId(int stringId) {
        this.stringId = stringId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public void readRowKey(byte[] rowKey) {
        this.agentId = Bytes.toString(rowKey, 0, AGENT_NAME_MAX_LEN).trim();
        this.stringId = readKeyCode(rowKey);
        this.startTime = TimeUtils.recoveryCurrentTimeMillis(readTime(rowKey));
    }


    private static long readTime(byte[] rowKey) {
        return BytesUtils.bytesToLong(rowKey, AGENT_NAME_MAX_LEN + INT_BYTE_LENGTH);
    }

    private static int readKeyCode(byte[] rowKey) {
        return BytesUtils.bytesToInt(rowKey, AGENT_NAME_MAX_LEN);
    }

    public byte[] toRowKey() {
        return RowKeyUtils.getMetaInfoRowKey(this.agentId, this.stringId, this.startTime);
    }

    @Override
    public String toString() {
        return "StringMetaDataBo{" +
                "agentId='" + agentId + '\'' +
                ", stringId=" + stringId +
                ", startTime=" + startTime +
                ", stringValue='" + stringValue + '\'' +
                '}';
    }

}
