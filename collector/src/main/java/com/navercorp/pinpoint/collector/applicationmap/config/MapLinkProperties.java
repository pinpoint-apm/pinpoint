package com.navercorp.pinpoint.collector.applicationmap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MapLinkProperties {
    private final boolean enableAvg;
    private final boolean enableMax;

    public MapLinkProperties(@Value("${collector.map-link.avg.enable:true}") boolean enableAvg,
                             @Value("${collector.map-link.max.enable:true}") boolean enableMax) {
        this.enableAvg = enableAvg;
        this.enableMax = enableMax;
    }

    public boolean isEnableAvg() {
        return enableAvg;
    }

    public boolean isEnableMax() {
        return enableMax;
    }
}
