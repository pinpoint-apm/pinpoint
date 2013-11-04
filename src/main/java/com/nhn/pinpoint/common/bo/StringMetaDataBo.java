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
public class StringMetaDataBo {
    private String agentId;
    private long startTime;

    private int stringId;

    private String stringValue;

    public StringMetaDataBo() {
    }


    public StringMetaDataBo(String agentId, long startTime, int stringId) {
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
        this.startTime = TimeUtils.recoveryCurrentTimeMillis(readTime(rowKey));
        this.stringId = readKeyCode(rowKey);
    }


    private static long readTime(byte[] rowKey) {
        return BytesUtils.bytesToLong(rowKey, AGENT_NAME_MAX_LEN);
    }

    private static int readKeyCode(byte[] rowKey) {
        return BytesUtils.bytesToInt(rowKey, AGENT_NAME_MAX_LEN + LONG_BYTE_LENGTH);
    }

    public byte[] toRowKey() {
        return RowKeyUtils.getMetaInfoRowKey(this.agentId, this.startTime, this.stringId);
    }

    @Override
    public String toString() {
        return "StringMetaDataBo{" +
                "agentId='" + agentId + '\'' +
                ", startTime=" + startTime +
                ", stringId=" + stringId +
                ", stringValue='" + stringValue + '\'' +
                '}';
    }

}
