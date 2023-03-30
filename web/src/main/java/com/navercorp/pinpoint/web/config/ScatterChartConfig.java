package com.navercorp.pinpoint.web.config;

import org.springframework.beans.factory.annotation.Value;

public class ScatterChartConfig {

    @Value("${web.scatter.serverside-scan.use-fuzzyrowfilter:false}")
    private boolean enableFuzzyRowFilter;

    public boolean isEnableFuzzyRowFilter() {
        return enableFuzzyRowFilter;
    }
}
