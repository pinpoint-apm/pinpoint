package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;

import java.util.List;

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
    public Long getCurrentPutOpsCount() {
        return 0L;
    }

}