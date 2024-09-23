package com.navercorp.pinpoint.profiler.micrometer;

import io.micrometer.registry.otlp.OtlpConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentOtlpConfigTest {

    @Test
    void getOtlpConfig() {
        String url = "http://localhost:8080";
        String step = "10s";
        String batchSize = "100";
        String serviceName = "serviceName";
        String applicationName = "applicationName";
        String agentId = "agentId";

        OtlpConfig otlpConfig = AgentOtlpConfig.getOtlpConfig(url, step, batchSize, serviceName, applicationName, agentId);
        assertEquals(otlpConfig.get("otlp.resourceAttributes"), "service.namespace=serviceName,service.name=applicationName,pinpoint.agentId=agentId");
    }

}