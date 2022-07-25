package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.navercorp.pinpoint.common.server.bo.JvmInfoBo;
import com.navercorp.pinpoint.common.server.bo.ServerMetaDataBo;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DetailedAgentInfo {
    private final AgentInfo agentInfo;
    private final ServerMetaDataBo serverMetaData;
    private final JvmInfoBo jvmInfo;

    public DetailedAgentInfo(AgentInfo agentInfo, ServerMetaDataBo serverMetaData, JvmInfoBo jvmInfo) {
        this.agentInfo = Objects.requireNonNull(agentInfo, "agentInfo");
        this.serverMetaData = Objects.requireNonNull(serverMetaData, "serverMetaData");
        this.jvmInfo = Objects.requireNonNull(jvmInfo, "jvmInfo");
    }

    @JsonUnwrapped
    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    public ServerMetaDataBo getServerMetaData() {
        return serverMetaData;
    }

    public JvmInfoBo getJvmInfo() {
        return jvmInfo;
    }
}
