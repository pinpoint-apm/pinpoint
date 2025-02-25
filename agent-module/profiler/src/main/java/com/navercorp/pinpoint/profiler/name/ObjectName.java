package com.navercorp.pinpoint.profiler.name;

import java.util.HashMap;
import java.util.Map;

public interface ObjectName {

    int VERSION_V1 = 1;
    int VERSION_V4 = 4;

    String AGENT_ID = "pinpoint.agentId";
    String AGENT_NAME = "pinpoint.agentName";
    String APPLICATION_NAME = "pinpoint.applicationName";
    String SERVICE_NAME = "pinpoint.serviceName";
    String PROTOCOL_VERSION_NAME = "pinpoint.protocolVersion";

    int getVersion();

    String getAgentId();

    String getAgentName();

    String getApplicationName();

    String getServiceName();

    default Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put(AGENT_ID, getAgentId());
        map.put(AGENT_NAME, getAgentName());
        map.put(APPLICATION_NAME, getApplicationName());
        map.put(SERVICE_NAME, getServiceName());
        map.put(PROTOCOL_VERSION_NAME, getServiceName());
        return map;
    }

}
