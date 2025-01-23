package com.navercorp.pinpoint.web.service.component;

import com.navercorp.pinpoint.common.server.util.time.Range;

import java.util.List;

public class DefaultLegacyAgentCompatibility implements LegacyAgentCompatibility {


    // legacy node agent support
    private final LegacyAgent[] legacyAgents = {
            new LegacyAgent(List.of((short)1400, (short)1401), "0.8.0"),
    };

    @Override
    public boolean isLegacyAgent(short serviceType) {
        return isLegacyAgent(serviceType, null);
    }

    @Override
    public boolean isLegacyAgent(short serviceType, String version) {
        for (LegacyAgent legacyAgent : legacyAgents) {
            if (!legacyAgent.isLegacyType(serviceType)) {
                return false;
            }
            if (!legacyAgent.isLegacyVersion(version)) {
                return false;
            }
        }
        return true;
    }
}
