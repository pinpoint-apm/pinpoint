package com.navercorp.pinpoint.collector.manage;

import java.util.Map;

/**
 * @Author Taejin Koo
 */
public interface HBaseManagerMBean {

    Long getAsyncOpsCount();

    Long getAsyncOpsRejectedCount();

    Map<String, Long> getCurrentAsyncOpsCountForEachRegionServer();

    Map<String, Long> getAsyncOpsFailedCountForEachRegionServer();

    Map<String, Long> getAsyncOpsAverageLatencyForEachRegionServer();

}
