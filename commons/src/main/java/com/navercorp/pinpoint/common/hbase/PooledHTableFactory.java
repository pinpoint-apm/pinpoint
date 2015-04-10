/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTableInterfaceFactory;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.hadoop.hbase.HbaseSystemException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HTableInterfaceFactory based on HTablePool.
 * @author emeroad
 */
public class PooledHTableFactory implements HTableInterfaceFactory, DisposableBean {

    private ExecutorService executor;
    private HConnection connection;
    public static final int DEFAULT_POOL_SIZE = 256;

    public PooledHTableFactory(Configuration config) {
        this.executor = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
        try {
            this.connection = HConnectionManager.createConnection(config, executor);
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }

    public PooledHTableFactory(Configuration config, int poolSize) {
        this.executor = Executors.newFixedThreadPool(poolSize);
        try {
            this.connection = HConnectionManager.createConnection(config, executor);
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }


    @Override
    public HTableInterface createHTableInterface(Configuration config, byte[] tableName) {
        try {
            return connection.getTable(tableName, executor);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void releaseHTableInterface(HTableInterface table) throws IOException {
        if (table != null) {
            table.close();
        }
    }


    @Override
    public void destroy() throws Exception {
        if (connection != null) {
            this.connection.close();
        }
    }
}
