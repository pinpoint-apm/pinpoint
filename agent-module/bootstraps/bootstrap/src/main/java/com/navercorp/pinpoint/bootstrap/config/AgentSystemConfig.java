package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.ProductInfo;
import com.navercorp.pinpoint.bootstrap.AgentIdSourceType;
import com.navercorp.pinpoint.bootstrap.BootLogger;
import com.navercorp.pinpoint.common.util.SimpleProperty;
import com.navercorp.pinpoint.common.util.SystemProperty;

public class AgentSystemConfig {
    private final BootLogger logger = BootLogger.getLogger(getClass());

    private final SimpleProperty systemProperty;

    public AgentSystemConfig() {
        this.systemProperty = SystemProperty.INSTANCE;
    }

    public void saveAgentIdForLog(String agentId) {
        systemProperty.setProperty(AgentIdSourceType.SYSTEM.getAgentId(), agentId);
    }

    public void savePinpointVersion(String version) {
        logger.info(String.format("pinpoint version:%s", version));
        systemProperty.setProperty(ProductInfo.NAME + ".version", version);
    }

}
