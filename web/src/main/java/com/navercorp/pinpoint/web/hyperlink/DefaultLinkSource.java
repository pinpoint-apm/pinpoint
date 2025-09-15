package com.navercorp.pinpoint.web.hyperlink;


import com.navercorp.pinpoint.common.trace.ServiceType;

import javax.annotation.Nullable;

public class DefaultLinkSource implements LinkSource {
    private final String hostName;
    private final String ip;
    private final ServiceType serviceType;


    public DefaultLinkSource(String hostName, @Nullable String ip, @Nullable ServiceType serviceType) {
        this.hostName = hostName;
        this.ip = ip;
        this.serviceType = serviceType;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public ServiceType getServiceType() {
        return serviceType;
    }
}
