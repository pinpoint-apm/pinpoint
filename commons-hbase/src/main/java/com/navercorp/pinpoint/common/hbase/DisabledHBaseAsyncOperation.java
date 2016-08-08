package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Author Taejin Koo
 */
public class DisabledHBaseAsyncOperation implements HBaseAsyncOperation {

    static final DisabledHBaseAsyncOperation INSTANCE = new DisabledHBaseAsyncOperation();

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public boolean put(TableName tableName, Put put) {
        return false;
    }

    @Override
    public List<Put> put(TableName tableName, List<Put> puts) {
        return puts;
    }

    @Override
    public Long getOpsCount() {
        return -1L;
    }

    @Override
    public Long getOpsRejectedCount() {
        return -1L;
    }

    @Override
    public Long getCurrentOpsCount() {
        return -1L;
    }

    @Override
    public Long getOpsFailedCount() {
        return -1L;
    }

    @Override
    public Long getOpsAverageLatency() {
        return -1L;
    }

    @Override
    public Map<String, Long> getCurrentOpsCountForEachRegionServer() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Long> getOpsFailedCountForEachRegionServer() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Long> getOpsAverageLatencyForEachRegionServer() {
        return Collections.emptyMap();
    }

}