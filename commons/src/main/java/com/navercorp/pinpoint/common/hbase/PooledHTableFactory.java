package com.nhn.pinpoint.common.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTableInterfaceFactory;
import org.apache.hadoop.hbase.client.HTablePool;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;

/**
 * HTablePool 기반의 HTableInterfaceFactory.
 * @author emeroad
 */
public class PooledHTableFactory implements HTableInterfaceFactory, DisposableBean {

    private HTablePool hTablePool;
    public static final int DEFAULT_POOL_SIZE = 256;

    public PooledHTableFactory(Configuration config) {
        this.hTablePool = new HTablePool(config, DEFAULT_POOL_SIZE);
    }

    public PooledHTableFactory(Configuration config, int poolSize) {
        this.hTablePool = new HTablePool(config, poolSize);
    }


    @Override
    public HTableInterface createHTableInterface(Configuration config, byte[] tableName) {
        return hTablePool.getTable(tableName);
    }

    @Override
    public void releaseHTableInterface(HTableInterface table) throws IOException {
        if (table != null) {
            table.close();
        }
    }


    @Override
    public void destroy() throws Exception {
        if (hTablePool != null) {
            this.hTablePool.close();
        }
    }
}
