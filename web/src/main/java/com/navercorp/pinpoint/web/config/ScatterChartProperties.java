package com.navercorp.pinpoint.web.config;

import org.springframework.beans.factory.annotation.Value;

public class ScatterChartProperties {

    @Value("${web.scatter.serverside-scan.use-fuzzyrowfilter:false}")
    private boolean enableFuzzyRowFilter;

    // Requires HBase 2.2 or higher (HBASE-22969)
    @Value("${web.scatter.index.value-filter.enable:false}")
    private boolean enableIndexValueFilter;

    public boolean isEnableFuzzyRowFilter() {
        return enableFuzzyRowFilter;
    }

    public boolean isEnableIndexValueFilter() {
        return enableIndexValueFilter;
    }
}
