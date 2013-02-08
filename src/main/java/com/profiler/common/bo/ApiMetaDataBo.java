package com.profiler.common.bo;

/**
 *
 */
public class ApiMetaDataBo {
    private String agentId;
    private short identifier;

    private int apiId;
    private long startTime;

    private String apiInfo;
    private int lineNumber;

    public ApiMetaDataBo() {
    }

    public ApiMetaDataBo(String agentId, short identifier, int apiId, long startTime) {
        this.agentId = agentId;
        this.identifier = identifier;
        this.apiId = apiId;
        this.startTime = startTime;
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

    public short getIdentifier() {
        return identifier;
    }

    public void setIdentifier(short identifier) {
        this.identifier = identifier;
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

    @Override
    public String toString() {
        return "ApiMetaDataBo{" +
                "agentId='" + agentId + '\'' +
                ", identifier=" + identifier +
                ", apiId=" + apiId +
                ", startTime=" + startTime +
                ", apiInfo='" + apiInfo + '\'' +
                ", lineNumber=" + lineNumber +
                '}';
    }


}
