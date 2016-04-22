package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HTableMultiplexer;
import org.apache.hadoop.hbase.client.Put;

import java.io.IOException;
import java.util.List;

/**
 * @Author Taejin Koo
 */
public class HBaseAsyncTemplate implements HBaseAsyncOperation {

    private final HTableMultiplexer hTableMultiplexer;

    public HBaseAsyncTemplate(Configuration conf, int perRegionServerBufferQueueSize) throws IOException {
        this.hTableMultiplexer = new HTableMultiplexer(conf, perRegionServerBufferQueueSize);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean put(TableName tableName, Put put) {
        return hTableMultiplexer.put(tableName, put);
    }

    @Override
    public List<Put> put(TableName tableName, List<Put> puts) {
        return hTableMultiplexer.put(tableName, puts);
    }

    @Override
    public Long getCurrentPutOpsCount() {
        return hTableMultiplexer.getHTableMultiplexerStatus().getTotalBufferedCounter();
    }

}