package com.navercorp.pinpoint.plugin.druid;

public class DruidDataSourceTest extends DruidDataSourceMonitorTest {

    private String url;
    private int maxActive;
    private int activeCount;

    public String getUrl() {
        return "";
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getMaxActive() {
        return 0;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getActiveCount() {
        return 0;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }
}