package com.navercorp.pinpoint.web.service.component;

import com.navercorp.pinpoint.common.server.util.time.Range;

public interface LegacyAgentCompatibility {
    boolean isLegacyAgent(short serviceType);

    boolean isLegacyAgent(short serviceType, String version);
}
