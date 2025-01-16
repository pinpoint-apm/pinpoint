package com.navercorp.pinpoint.profiler.micrometer;

import io.micrometer.registry.otlp.OtlpConfig;

import java.util.Properties;

public class AgentOtlpConfig {
    public static OtlpConfig getOtlpConfig(String url, String step, String batchSize,
                                           String serviceName, String applicationName, String agentId) {
        Properties propertiesConfig = new Properties();
        propertiesConfig.setProperty("otlp.url", url);
        propertiesConfig.setProperty("otlp.step", step);
        propertiesConfig.setProperty("otlp.batchSize", batchSize);

        String attribute = String.format("service.namespace=%s,service.name=%s,pinpoint.agentId=%s", serviceName, applicationName, agentId);
        propertiesConfig.setProperty("otlp.resourceAttributes", attribute);
        return propertiesConfig::getProperty;
    }
}
