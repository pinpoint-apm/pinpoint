package com.navercorp.pinpoint.common.server.cluster;

import org.springframework.util.Assert;

import java.util.Objects;

public class AgentInfoKey {
    public static final String DELIMITER = ":";

    private final String applicationName;
    private final String agentId;
    private final long startTimestamp;

    public static AgentInfoKey parse(String agentKeyStr) {
        Objects.requireNonNull(agentKeyStr, "agentKeyStr");

        String[] tokens = agentKeyStr.split(DELIMITER);
        Assert.isTrue(tokens.length == 3, "invalid token.length == 3");
        return new AgentInfoKey(tokens[0], tokens[1], Long.parseLong(tokens[2]));
    }

    public AgentInfoKey(String applicationName, String agentId, long startTimestamp) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.startTimestamp = startTimestamp;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentInfoKey that = (AgentInfoKey) o;

        if (startTimestamp != that.startTimestamp) return false;
        if (!applicationName.equals(that.applicationName)) return false;
        return agentId.equals(that.agentId);
    }

    @Override
    public int hashCode() {
        int result = applicationName.hashCode();
        result = 31 * result + agentId.hashCode();
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        builder.append(applicationName);
        builder.append(DELIMITER);
        builder.append(agentId);
        builder.append(DELIMITER);
        builder.append(startTimestamp);
        return builder.toString();
    }
}
