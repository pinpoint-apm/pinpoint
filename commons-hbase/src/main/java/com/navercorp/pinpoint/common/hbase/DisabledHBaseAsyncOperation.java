package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.client.Put;

import java.util.List;

/**
 * @Author Taejin Koo
 */
public class DisabledHBaseAsyncOperation implements HBaseAsyncOperation {

    @Override
    public boolean put(String tableName, Put put) {
        return false;
    }

    @Override
    public List<Put> put(String tableName, List<Put> puts) {
        return puts;
    }

}