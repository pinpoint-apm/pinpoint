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

import com.navercorp.pinpoint.common.util.ExecutorFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTableInterfaceFactory;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.hadoop.hbase.HbaseSystemException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * HTableInterfaceFactory based on HTablePool.
 * @author emeroad
 */
public class PooledHTableFactory implements HTableInterfaceFactory, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int DEFAULT_POOL_SIZE = 256;
    public static final int DEFAULT_WORKER_QUEUE_SIZE = 1024*5;

    private ExecutorService executor;
    private HConnection connection;


    public PooledHTableFactory(Configuration config) {
        this(config, DEFAULT_POOL_SIZE, DEFAULT_WORKER_QUEUE_SIZE);
    }
    public PooledHTableFactory(Configuration config, int poolSize) {
        this(config, poolSize, DEFAULT_WORKER_QUEUE_SIZE);
    }

    public PooledHTableFactory(Configuration config, int poolSize, int workerQueueSize) {
        this.executor = getExecutorService(poolSize, workerQueueSize);
        try {
            this.connection = HConnectionManager.createConnection(config, executor);
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }

    private ExecutorService getExecutorService(int poolSize, int workQueueMaxSize) {

        logger.info("create HConnectionThreadPoolExecutor poolSize:{}, workerQueueMaxSize:{}", poolSize, workQueueMaxSize);

        ThreadPoolExecutor threadPoolExecutor = ExecutorFactory.newFixedThreadPool(poolSize, workQueueMaxSize, "Pinpoint-HConnectionExecutor", true);
        threadPoolExecutor.prestartAllCoreThreads();
        return threadPoolExecutor;
    }


    @Override
    public HTableInterface createHTableInterface(Configuration config, byte[] tableName) {
        try {
            return connection.getTable(tableName, executor);
        } catch (IOException e) {
            throw new HbaseSystemException(e);
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

        if (this.executor != null) {
            this.executor.shutdown();
            try {
                final boolean shutdown = executor.awaitTermination(1000 * 5, TimeUnit.MILLISECONDS);
                if (!shutdown) {
                    final List<Runnable> discardTask = this.executor.shutdownNow();
                    logger.warn("discard task size:{}", discardTask.size());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
