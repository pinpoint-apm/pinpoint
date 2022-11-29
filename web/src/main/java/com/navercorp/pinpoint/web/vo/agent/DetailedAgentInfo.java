package com.navercorp.pinpoint.web.vo.agent;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.navercorp.pinpoint.common.server.bo.JvmInfoBo;
import com.navercorp.pinpoint.common.server.bo.ServerMetaDataBo;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DetailedAgentInfo {
    private final AgentInfo agentInfo;
    private final ServerMetaDataBo serverMetaData;
    private final JvmInfoBo jvmInfo;

    public DetailedAgentInfo(AgentInfo agentInfo, @Nullable ServerMetaDataBo serverMetaData, @Nullable JvmInfoBo jvmInfo) {
        this.agentInfo = Objects.requireNonNull(agentInfo, "agentInfo");
        this.serverMetaData = serverMetaData;
        this.jvmInfo = jvmInfo;
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

    @Override
    public String toString() {
        return "DetailedAgentInfo{" +
                "agentInfo=" + agentInfo +
                ", serverMetaData=" + serverMetaData +
                ", jvmInfo=" + jvmInfo +
                '}';
    }
}
