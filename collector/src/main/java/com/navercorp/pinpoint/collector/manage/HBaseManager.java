package com.navercorp.pinpoint.collector.manage;

import com.navercorp.pinpoint.common.hbase.HBaseAsyncOperation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @Author Taejin Koo
 */
public class HBaseManager extends AbstractCollectorManager implements HBaseManagerMBean {

    @Autowired
    private HBaseAsyncOperation hBaseAsyncOperation;

    @Override
    public Long getAsyncOpsCount() {
        return hBaseAsyncOperation.getOpsCount();
    }

    @Override
    public Long getAsyncOpsRejectedCount() {
        return hBaseAsyncOperation.getOpsRejectedCount();
    }

    @Override
    public Map<String, Long> getCurrentAsyncOpsCountForEachRegionServer() {
        return hBaseAsyncOperation.getCurrentOpsCountForEachRegionServer();
    }

    @Override
    public Map<String, Long> getAsyncOpsFailedCountForEachRegionServer() {
        return hBaseAsyncOperation.getOpsFailedCountForEachRegionServer();
    }

    @Override
    public Map<String, Long> getAsyncOpsAverageLatencyForEachRegionServer() {
        return hBaseAsyncOperation.getOpsAverageLatencyForEachRegionServer();
    }

}
