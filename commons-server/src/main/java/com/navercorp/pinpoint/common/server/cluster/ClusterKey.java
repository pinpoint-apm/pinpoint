package com.navercorp.pinpoint.common.server.cluster;

import com.navercorp.pinpoint.common.id.AgentId;
import org.springframework.util.Assert;

import java.util.Objects;

public class ClusterKey {
    public static final String DELIMITER = ":";

    private final String applicationName;
    private final String agentId;
    private final long startTimestamp;

    public static ClusterKey parse(String clusterKeyFormat) {
        Objects.requireNonNull(clusterKeyFormat, "clusterKeyFormat");

        String[] tokens = clusterKeyFormat.split(DELIMITER, 3);
        Assert.isTrue(tokens.length == 3, "invalid token.length == 3");
        return new ClusterKey(tokens[0], tokens[1], Long.parseLong(tokens[2]));
    }

    public static String compose(String applicationName, String agentId, long startTimestamp) {
        return applicationName +
                DELIMITER +
                agentId +
                DELIMITER +
                startTimestamp;
    }

    public ClusterKey(String applicationName, String agentId, long startTimestamp) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.startTimestamp = startTimestamp;
    }

    public ClusterKey(String applicationName, AgentId agentId, long startTimestamp) {
        this(applicationName, Objects.requireNonNull(agentId, "agentId").value(), startTimestamp);
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

        ClusterKey that = (ClusterKey) o;

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

    public String format() {
        StringBuilder builder = new StringBuilder(64);
        builder.append(applicationName);
        builder.append(DELIMITER);
        builder.append(agentId);
        builder.append(DELIMITER);
        builder.append(startTimestamp);
        return builder.toString();
    }

    @Override
    public String toString() {
        return format();
    }
}
