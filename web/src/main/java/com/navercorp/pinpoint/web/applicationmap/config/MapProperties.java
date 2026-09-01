package com.navercorp.pinpoint.web.applicationmap.config;

import org.springframework.beans.factory.annotation.Value;

public class MapProperties {
    @Value("${pinpoint.modules.web.servicemap.enabled:false}")
    private boolean enableServiceMap;

    public boolean isEnableServiceMap() {
        return enableServiceMap;
    }
}
