package com.navercorp.pinpoint.web.service.component;

import com.navercorp.pinpoint.common.server.util.time.Range;

public class DisableAgentCompatibility implements LegacyAgentCompatibility {
    @Override
    public boolean isLegacyAgent(short serviceType) {
        return false;
    }

    @Override
    public boolean isLegacyAgent(short serviceType, String version) {
        return false;
    }

    @Override
    public boolean isActiveAgent(String agentId, Range range) {
        return false;
    }
}
