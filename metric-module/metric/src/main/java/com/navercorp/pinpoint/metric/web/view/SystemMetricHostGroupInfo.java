package com.navercorp.pinpoint.metric.web.view;

import java.util.List;
import java.util.Objects;

public class SystemMetricHostGroupInfo {
    private final String hostGroupName;
    private final boolean excluded;

    public SystemMetricHostGroupInfo(String hostGroupName, boolean excluded) {
        this.hostGroupName = Objects.requireNonNull(hostGroupName, "hostGroupName");
        this.excluded = excluded;
    }

    public String getHostGroupName() {
        return hostGroupName;
    }

    public boolean isExcluded() {
        return excluded;
    }
}
