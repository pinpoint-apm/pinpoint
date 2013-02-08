package com.profiler.common.bo;

/**
 *
 */
public class SqlMetaDataBo {
    private String agentId;
    private short identifier;
    private int hashCode;
    private long startTime;

    private String sql;

    public SqlMetaDataBo() {
    }


    public SqlMetaDataBo(String agentId, short identifier, int hashCode, long startTime) {
        this.agentId = agentId;
        this.identifier = identifier;
        this.hashCode = hashCode;
        this.startTime = startTime;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public short getIdentifier() {
        return identifier;
    }

    public void setIdentifier(short identifier) {
        this.identifier = identifier;
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

    @Override
    public String toString() {
        return "SqlMetaDataBo{" +
                "agentId='" + agentId + '\'' +
                ", identifier=" + identifier +
                ", hashCode=" + hashCode +
                ", startTime=" + startTime +
                ", sql='" + sql + '\'' +
                '}';
    }
}
