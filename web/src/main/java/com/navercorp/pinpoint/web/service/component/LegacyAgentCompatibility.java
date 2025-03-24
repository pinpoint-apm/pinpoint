package com.navercorp.pinpoint.web.service.component;

public interface LegacyAgentCompatibility {
    boolean isLegacyAgent(int serviceType);

    boolean isLegacyAgent(int serviceType, String version);
}
