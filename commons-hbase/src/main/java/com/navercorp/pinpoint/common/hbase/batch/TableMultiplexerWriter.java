package com.navercorp.pinpoint.common.hbase.batch;

import com.navercorp.pinpoint.common.hbase.HBaseAsyncOperation;
import com.navercorp.pinpoint.common.hbase.SimpleBatchWriter;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;

import java.util.Objects;

public class TableMultiplexerWriter implements SimpleBatchWriter {
    private final HBaseAsyncOperation hbaseAsyncOperation;

    public TableMultiplexerWriter(HBaseAsyncOperation hbaseAsyncOperation) {
        this.hbaseAsyncOperation = Objects.requireNonNull(hbaseAsyncOperation, "hbaseAsyncOperation");

    }

    @Override
    public boolean write(TableName tableName, Put mutation) {
        return hbaseAsyncOperation.put(tableName, mutation);
    }
}
