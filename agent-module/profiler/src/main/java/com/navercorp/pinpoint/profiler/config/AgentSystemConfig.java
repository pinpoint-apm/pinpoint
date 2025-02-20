package com.navercorp.pinpoint.profiler.config;

import com.navercorp.pinpoint.profiler.name.IdSourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Properties;

public class AgentSystemConfig {
    private final Logger logger = LogManager.getLogger(getClass());

    private final Properties systemProperty;

    public AgentSystemConfig() {
        this(System.getProperties());
    }

    AgentSystemConfig(Properties properties) {
        this.systemProperty = Objects.requireNonNull(properties, "properties");
    }

    public void saveAgentIdForLog(String agentId) {
        systemProperty.setProperty(IdSourceType.SYSTEM.getAgentId(), agentId);
    }

    public void savePinpointVersion(String version) {
        logger.info("pinpoint version:{}", version);
        systemProperty.setProperty("pinpoint.version", version);
    }

}
