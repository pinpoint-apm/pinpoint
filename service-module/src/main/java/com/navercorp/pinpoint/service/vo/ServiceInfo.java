package com.navercorp.pinpoint.service.vo;

import java.util.Map;

public class ServiceInfo extends ServiceEntry {

    private Map<String, String> configuration;

    public ServiceInfo() {
    }

    public Map<String, String> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }
}
