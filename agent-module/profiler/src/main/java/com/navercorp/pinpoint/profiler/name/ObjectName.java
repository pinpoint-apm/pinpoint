package com.navercorp.pinpoint.profiler.name;

public interface ObjectName {

    int VERSION_V1 = 1;
    int VERSION_V4 = 4;

    String AGENT_ID = "pinpoint.agentId";
    String AGENT_NAME = "pinpoint.agentName";
    String APPLICATION_NAME = "pinpoint.applicationName";
    String SERVICE_NAME = "pinpoint.serviceName";

    int getVersion();

    String getAgentId();

    String getAgentName();

    String getApplicationName();

    String getServiceName();

}
