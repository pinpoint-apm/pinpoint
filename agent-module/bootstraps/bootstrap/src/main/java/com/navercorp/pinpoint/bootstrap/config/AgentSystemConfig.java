package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.ProductInfo;
import com.navercorp.pinpoint.bootstrap.AgentIdSourceType;
import com.navercorp.pinpoint.bootstrap.BootLogger;

import java.util.Objects;
import java.util.Properties;

public class AgentSystemConfig {
    private final BootLogger logger = BootLogger.getLogger(getClass());

    private final Properties systemProperty;

    public AgentSystemConfig() {
        this(System.getProperties());
    }

    AgentSystemConfig(Properties properties) {
        this.systemProperty = Objects.requireNonNull(properties, "properties");
    }

    public void saveAgentIdForLog(String agentId) {
        systemProperty.setProperty(AgentIdSourceType.SYSTEM.getAgentId(), agentId);
    }

    public void savePinpointVersion(String version) {
        logger.info(String.format("pinpoint version:%s", version));
        systemProperty.setProperty(ProductInfo.NAME + ".version", version);
    }

}
