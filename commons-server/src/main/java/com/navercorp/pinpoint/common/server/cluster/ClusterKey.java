package com.navercorp.pinpoint.common.server.cluster;

import com.google.common.base.Preconditions;

import java.util.Objects;

public class ClusterKey {
    public static final char DELIMITER_CHAR = ':';
    public static final String DELIMITER = "" + DELIMITER_CHAR;

    private final String serviceName;
    private final String applicationName;
    private final String agentId;
    private final long startTimestamp;

    // use for test
    public static ClusterKey parse(String clusterKeyFormat) {
        Objects.requireNonNull(clusterKeyFormat, "clusterKeyFormat");

        String[] tokens = clusterKeyFormat.split(DELIMITER, 4);
        Preconditions.checkArgument(tokens.length == 4, "invalid token.length == 4");
        return new ClusterKey(tokens[0], tokens[1], tokens[2], Long.parseLong(tokens[3]));
    }

    public static String compose(String serviceName, String applicationName, String agentId, long startTimestamp) {
        return String.join(DELIMITER, serviceName, applicationName, agentId, String.valueOf(startTimestamp));
    }

    public ClusterKey(String serviceName, String applicationName, String agentId, long startTimestamp) {
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.startTimestamp = startTimestamp;
    }

    public String getServiceName() {
        return serviceName;
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
        if (!serviceName.equals(that.serviceName)) return false;
        if (!applicationName.equals(that.applicationName)) return false;
        return agentId.equals(that.agentId);
    }

    @Override
    public int hashCode() {
        int result = serviceName.hashCode();
        result = 31 * result + applicationName.hashCode();
        result = 31 * result + agentId.hashCode();
        result = 31 * result + Long.hashCode(startTimestamp);
        return result;
    }

    public String format() {
        return compose(serviceName, applicationName, agentId, startTimestamp);
    }

    @Override
    public String toString() {
        return format();
    }
}
