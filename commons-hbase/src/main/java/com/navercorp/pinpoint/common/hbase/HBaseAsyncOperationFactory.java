/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.HTableMultiplexer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * @author Taejin Koo
 */
public class HBaseAsyncOperationFactory implements DisposableBean, FactoryBean<HBaseAsyncOperation> {

    private static final Logger logger = LogManager.getLogger(HBaseAsyncOperationFactory.class);

    public static final String ENABLE_ASYNC_METHOD = "hbase.client.async.enable";
    public static final boolean DEFAULT_ENABLE_ASYNC_METHOD = false;

    public static final String ASYNC_IN_QUEUE_SIZE = "hbase.client.async.in.queuesize";
    public static final int DEFAULT_ASYNC_IN_QUEUE_SIZE = 10000;

    public static final String ASYNC_PERIODIC_FLUSH_TIME = HTableMultiplexer.TABLE_MULTIPLEXER_FLUSH_PERIOD_MS;
    public static final int DEFAULT_ASYNC_PERIODIC_FLUSH_TIME = 100;

    public static final String ASYNC_MAX_RETRIES_IN_QUEUE = HTableMultiplexer.TABLE_MULTIPLEXER_MAX_RETRIES_IN_QUEUE;
    public static final int DEFAULT_ASYNC_RETRY_COUNT = 10000;

    private final Connection connection;
    private final Configuration configuration;
    private volatile HTableMultiplexer hTableMultiplexer;


    public HBaseAsyncOperationFactory(Connection connection, Configuration configuration) {
        this.connection = Objects.requireNonNull(connection, "connection");
        this.configuration = Objects.requireNonNull(configuration, "configuration");
    }

    @Override
    public HBaseAsyncOperation getObject() throws Exception {
        boolean enableAsyncMethod = configuration.getBoolean(ENABLE_ASYNC_METHOD, DEFAULT_ENABLE_ASYNC_METHOD);
        if (!enableAsyncMethod) {
            logger.info("hBase async operation is disabled");
            return DisabledHBaseAsyncOperation.INSTANCE;
        }

        return new TableMultiplexerAsyncOperation(this.getHTableMultiplexer());
    }

    private HTableMultiplexer getHTableMultiplexer() {
        synchronized (HBaseAsyncOperationFactory.class) {
            if (this.hTableMultiplexer != null) {
                logger.info("Returning cached HTableMultiplexer: {}", this.hTableMultiplexer);
                return this.hTableMultiplexer;
            }

            if (configuration.get(ASYNC_PERIODIC_FLUSH_TIME, null) == null) {
                configuration.setInt(ASYNC_PERIODIC_FLUSH_TIME, DEFAULT_ASYNC_PERIODIC_FLUSH_TIME);
            }

            if (configuration.get(ASYNC_MAX_RETRIES_IN_QUEUE, null) == null) {
                configuration.setInt(ASYNC_MAX_RETRIES_IN_QUEUE, DEFAULT_ASYNC_RETRY_COUNT);
            }

            int queueSize = configuration.getInt(ASYNC_IN_QUEUE_SIZE, DEFAULT_ASYNC_IN_QUEUE_SIZE);

            this.hTableMultiplexer = new HTableMultiplexer(connection, configuration, queueSize);
            logger.info("Initialized new HTableMultiplexer: {} (queueSize: {})", this.hTableMultiplexer, queueSize);
        }

        return this.hTableMultiplexer;
    }

    @Override
    public void destroy() throws Exception {
        closeHTableMultiplexer();
        closeHTableFlushWorkers();
    }

    private void closeHTableFlushWorkers() {
        if (this.hTableMultiplexer == null) {
            logger.info("Skipped closing HTableFlushWorkers: multiplexer not found");
            return;
        }

        try {
            logger.info("Closing hTableFlushWorkers");
            final Field executorField = HTableMultiplexer.class.getDeclaredField("executor");
            executorField.setAccessible(true);
            final Object executorObj = executorField.get(this.hTableMultiplexer);
            if (executorObj instanceof ExecutorService) {
                ((ExecutorService) executorObj).shutdown();
            } else {
                throw new RuntimeException("Invalid executorService");
            }
            logger.info("Closed hTableFlushWorkers");
        } catch (Exception e) {
            logger.warn("Failed to close hTableFlushWorkers", e);
        }
    }

    private void closeHTableMultiplexer() {
        if (this.hTableMultiplexer == null) {
            logger.info("Skipped closing HTableMultiplexer: multiplexer not found");
            return;
        }

        try {
            logger.info("Closing hTableMultiplexer");
            this.hTableMultiplexer.close();
            logger.info("Closed hTableMultiplexer");
        } catch (Exception e) {
            logger.warn("Failed to close hTableMultiplexer", e);
        }
    }

    @Override
    public Class<HBaseAsyncOperation> getObjectType() {
        return HBaseAsyncOperation.class;
    }

    @Override
    public boolean isSingleton() {
        return FactoryBean.super.isSingleton();
    }
}