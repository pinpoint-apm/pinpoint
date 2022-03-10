package com.navercorp.pinpoint.common.hbase.batch;

import com.navercorp.pinpoint.common.hbase.HBaseAsyncOperation;
import com.navercorp.pinpoint.common.hbase.SimpleBatchWriter;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;

import java.util.Objects;

public class AsyncTemplateWriter implements SimpleBatchWriter {
    private final HBaseAsyncOperation asyncOperation;

    public AsyncTemplateWriter(HBaseAsyncOperation asyncOperation) {
        this.asyncOperation = Objects.requireNonNull(asyncOperation, "asyncOperation");

    }

    @Override
    public boolean write(TableName tableName, Put mutation) {
        return asyncOperation.put(tableName, mutation);
    }
}
