package com.navercorp.pinpoint.common.hbase.batch;

import com.navercorp.pinpoint.common.hbase.SimpleBatchWriter;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;

import java.util.Objects;

public class SimpleBufferWriter implements SimpleBatchWriter {
    private final HbaseBatchWriter batchWriter;

    public SimpleBufferWriter(HbaseBatchWriter batchWriter) {
        this.batchWriter = Objects.requireNonNull(batchWriter, "batchWriter");
    }

    @Override
    public boolean write(TableName tableName, Put mutation) {
        return batchWriter.write(tableName, mutation);
    }
}
