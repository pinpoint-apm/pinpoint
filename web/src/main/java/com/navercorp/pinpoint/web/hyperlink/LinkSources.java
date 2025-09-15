package com.navercorp.pinpoint.web.hyperlink;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;

import java.util.Objects;

public final class LinkSources {
    private LinkSources() {
    }

    public static LinkSource from(String hostName, String ip, ServiceType serviceType) {
        Objects.requireNonNull(hostName, "hostName");
        return new DefaultLinkSource(hostName, ip, serviceType);
    }

    public static LinkSource from(AgentInfo agentInfo) {
        Objects.requireNonNull(agentInfo, "agentInfo");
        return new DefaultLinkSource(agentInfo.getHostName(), agentInfo.getIp(), agentInfo.getServiceType());
    }

}
