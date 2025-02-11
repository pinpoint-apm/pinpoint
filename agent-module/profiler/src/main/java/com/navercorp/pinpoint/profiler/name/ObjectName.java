package com.navercorp.pinpoint.profiler.name;

import com.navercorp.pinpoint.grpc.protocol.ProtocolVersion;

import java.util.HashMap;
import java.util.Map;

public interface ObjectName {
    String AGENT_ID = "pinpoint.agentId";
    String AGENT_NAME = "pinpoint.agentName";
    String APPLICATION_NAME = "pinpoint.applicationName";
    String SERVICE_NAME = "pinpoint.serviceName";
    String PROTOCOL_VERSION_NAME = "pinpoint.protocolVersion";

    ProtocolVersion getVersion();

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
