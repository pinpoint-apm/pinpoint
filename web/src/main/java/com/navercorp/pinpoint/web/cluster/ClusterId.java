package com.navercorp.pinpoint.web.cluster;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class ClusterId {
    public static final String APPLICATION_NAME_SEPARATOR = "$$";

    private final String zkCollectorPath;
    private final String collectorId;
    private final String applicationName;


    public static ClusterId newClusterId(String zkPath) {
        Objects.requireNonNull(zkPath, "zkPath");

        int collectorIdIndex = StringUtils.ordinalIndexOf(zkPath, "/", 3);
        if (collectorIdIndex == -1) {
            throw new IllegalArgumentException("invalid zkPath:" + zkPath);
        }
        String path = zkPath.substring(0, collectorIdIndex);
        String collectorId = zkPath.substring(collectorIdIndex + 1);
        return new ClusterId(path, collectorId, null);
    }

    public static ClusterId newClusterId(String zkParentPath, String collectorId) {
        Objects.requireNonNull(zkParentPath, "zkParentPath");
        Objects.requireNonNull(collectorId, "collectorId");

        final int appNameIndex = collectorId.lastIndexOf(APPLICATION_NAME_SEPARATOR);
        if (appNameIndex != -1) {
            String hostId = collectorId.substring(0, appNameIndex);
            String applicationName = collectorId.substring(appNameIndex + APPLICATION_NAME_SEPARATOR.length());
            return new ClusterId(zkParentPath, hostId, applicationName);
        }
        return new ClusterId(zkParentPath, collectorId, null);
    }


    public ClusterId(String zkCollectorPath, String collectorId, String applicationName) {
        this.zkCollectorPath = Objects.requireNonNull(zkCollectorPath, "zkCollectorPath");
        this.collectorId = Objects.requireNonNull(collectorId, "collectorId");
        this.applicationName = applicationName;
    }

    public String getParentPath() {
        return zkCollectorPath;
    }

    public String getCollectorId() {
        return collectorId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClusterId clusterId = (ClusterId) o;

        if (!zkCollectorPath.equals(clusterId.zkCollectorPath)) return false;
        if (!collectorId.equals(clusterId.collectorId)) return false;
        return applicationName != null ? applicationName.equals(clusterId.applicationName) : clusterId.applicationName == null;
    }

    @Override
    public int hashCode() {
        int result = zkCollectorPath.hashCode();
        result = 31 * result + collectorId.hashCode();
        result = 31 * result + (applicationName != null ? applicationName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClusterId{" +
                "parentPath='" + zkCollectorPath + '\'' +
                ", collectorId='" + collectorId + '\'' +
                ", applicationName='" + applicationName + '\'' +
                '}';
    }
}
