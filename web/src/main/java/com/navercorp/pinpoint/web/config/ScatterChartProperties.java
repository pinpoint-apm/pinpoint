package com.navercorp.pinpoint.web.config;

import org.springframework.beans.factory.annotation.Value;

public class ScatterChartProperties {

    @Value("${web.scatter.serverside-scan.use-fuzzyrowfilter:false}")
    private boolean enableFuzzyRowFilter;

    @Value("${web.scatter.hbase.row-filter.enabled:true}")
    private boolean enableHbaseRowFilter;

    @Value("${web.scatter.hbase.value-filter.enabled:true}")
    private boolean enableHbaseValueFilter;

    public boolean isEnableFuzzyRowFilter() {
        return enableFuzzyRowFilter;
    }

    public boolean isEnableHbaseRowFilter() {
        return enableHbaseRowFilter;
    }

    public boolean isEnableHbaseValueFilter() {
        return enableHbaseValueFilter;
    }
}
