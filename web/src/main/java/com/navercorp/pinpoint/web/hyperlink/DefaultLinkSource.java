package com.navercorp.pinpoint.web.hyperlink;


import javax.annotation.Nullable;

public class DefaultLinkSource implements LinkSource {
    private final String hostName;
    private final String ip;


    public DefaultLinkSource(String hostName, @Nullable String ip) {
        this.hostName = hostName;
        this.ip = ip;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public String getIp() {
        return ip;
    }
}
