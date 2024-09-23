package com.navercorp.pinpoint.profiler.micrometer;

import io.micrometer.registry.otlp.OtlpConfig;

import java.util.Properties;

public class AgentOtlpConfig {
    public static OtlpConfig getOtlpConfig(String url, String step, String batchSize,
                                           String serviceName, String applicationName, String agentId) {
        Properties propertiesConfig = new Properties();
        propertiesConfig.put("otlp.url", url);
        propertiesConfig.put("otlp.step", String.valueOf(step));
        propertiesConfig.put("otlp.batchSize", String.valueOf(batchSize));

        String attribute = String.format("service.namespace=%s,service.name=%s,pinpoint.agentId=%s", serviceName, applicationName, agentId);
        propertiesConfig.put("otlp.resourceAttributes", attribute);
        OtlpConfig otlpConfig = (key -> (String) propertiesConfig.get(key));
        return otlpConfig;
    }
}
