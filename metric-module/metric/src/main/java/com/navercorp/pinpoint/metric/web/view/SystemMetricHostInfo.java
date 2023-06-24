package com.navercorp.pinpoint.metric.web.view;

import java.util.Objects;

public class SystemMetricHostInfo {
    private final String hostName;
    private final boolean excluded;

    public SystemMetricHostInfo(String hostName, boolean excluded) {
        this.hostName = Objects.requireNonNull(hostName, "hostName");
        this.excluded = excluded;
    }

    public String getHostName() {
        return hostName;
    }

    public boolean isExcluded() {
        return excluded;
    }
}
