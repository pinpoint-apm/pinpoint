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
public class ApiMetaDataBo {
    private String agentId;
    private long startTime;

    private int apiId;

    private String apiInfo;
    private int lineNumber;

    public ApiMetaDataBo() {
    }

    public ApiMetaDataBo(String agentId, long startTime, int apiId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }

        this.agentId = agentId;
        this.startTime = startTime;
        this.apiId = apiId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }


    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getApiInfo() {
        return apiInfo;
    }

    public void setApiInfo(String apiInfo) {
        this.apiInfo = apiInfo;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void readRowKey(byte[] bytes) {
        this.agentId = Bytes.toString(bytes, 0, AGENT_NAME_MAX_LEN).trim();
        this.startTime = TimeUtils.recoveryCurrentTimeMillis(readTime(bytes));
        this.apiId = readKeyCode(bytes);
    }

    private static long readTime(byte[] rowKey) {
        return BytesUtils.bytesToLong(rowKey, AGENT_NAME_MAX_LEN);
    }

    private static int readKeyCode(byte[] rowKey) {
        return BytesUtils.bytesToInt(rowKey, AGENT_NAME_MAX_LEN + LONG_BYTE_LENGTH);
    }

    public byte[] toRowKey() {
        return RowKeyUtils.getMetaInfoRowKey(this.agentId, this.startTime, this.apiId);
    }

    @Override
    public String toString() {
        return "ApiMetaDataBo{" +
                "agentId='" + agentId + '\'' +
                ", apiId=" + apiId +
                ", startTime=" + startTime +
                ", apiInfo='" + apiInfo + '\'' +
                ", lineNumber=" + lineNumber +
                '}';
    }

}
