package com.navercorp.pinpoint.common.hbase.batch;

import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.hbase.SimpleBatchWriter;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;

import java.util.Objects;

public class HbaseTemplateWriter implements SimpleBatchWriter {
    private final HbaseTemplate2 hbaseTemplate2;

    public HbaseTemplateWriter(HbaseTemplate2 hbaseTemplate2) {
        this.hbaseTemplate2 = Objects.requireNonNull(hbaseTemplate2, "asyncOperation");

    }

    @Override
    public boolean write(TableName tableName, Put mutation) {
        return hbaseTemplate2.asyncPut(tableName, mutation);
    }
}
