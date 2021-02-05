package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.google.common.util.concurrent.AtomicLongMap;
import com.navercorp.pinpoint.collector.util.AtomicLongMapUtils;
import org.apache.hadoop.hbase.TableName;

import java.util.Map;

public class DefaultBulkIncrementer implements BulkUpdater {

    private final AtomicLongMap<RowInfo> max = AtomicLongMap.create();

    @Override
    public void updateMax(TableName tableName, RowKey rowKey, ColumnName columnName, long value) {
        RowInfo rowInfo = new DefaultRowInfo(tableName, rowKey, columnName);
        max.accumulateAndGet(rowInfo, value, Long::max);
    }

    @Override
    public Map<RowInfo, Long> getMaxUpdate() {
        return AtomicLongMapUtils.remove(max);
    }

}
