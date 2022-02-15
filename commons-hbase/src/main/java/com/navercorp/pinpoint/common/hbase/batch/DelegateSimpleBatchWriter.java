package com.navercorp.pinpoint.common.hbase.batch;

import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.SimpleBatchWriter;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Objects;

public class DelegateSimpleBatchWriter implements SimpleBatchWriter {
    private final boolean batchWriter;

    private final HbaseBatchWriter hbaseBatchWriter;
    private final HbaseOperations2 hbaseTemplate;

    public DelegateSimpleBatchWriter(BufferedMutatorConfiguration configuration,
                                     HbaseBatchWriter hbaseBatchWriter,
                                     @Qualifier("asyncPutHbaseTemplate") HbaseOperations2 hbaseTemplate) {
        this.batchWriter = configuration.isBatchWriter();
        this.hbaseBatchWriter = hbaseBatchWriter;
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
    }


    @Override
    public boolean write(TableName tableName, Put put) {
        if (batchWriter) {
            return hbaseBatchWriter.write(tableName, put);
        } else {
            return hbaseTemplate.asyncPut(tableName, put);
        }
    }
}
