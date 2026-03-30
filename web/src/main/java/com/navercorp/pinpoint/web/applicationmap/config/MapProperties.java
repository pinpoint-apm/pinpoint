package com.navercorp.pinpoint.web.applicationmap.config;

import org.springframework.beans.factory.annotation.Value;

public class MapProperties {
    @Value("${experimental.enableServiceMap.value:false}")
    private boolean enableServiceMap;

    public boolean isEnableServiceMap() {
        return enableServiceMap;
    }
}
