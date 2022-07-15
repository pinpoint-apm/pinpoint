package com.navercorp.pinpoint.web.hyperlink;


public class DefaultLinkSource implements LinkSource {
    private final String hostName;
    private final String ip;


    public DefaultLinkSource(String hostName, String ip) {
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
