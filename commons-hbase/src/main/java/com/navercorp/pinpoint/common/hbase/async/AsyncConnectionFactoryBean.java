/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.common.hbase.async;

import com.navercorp.pinpoint.common.hbase.HbaseSystemException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.client.AsyncConnectionCleaner;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.security.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author HyunGil Jeong
 */
@Component
public class AsyncConnectionFactoryBean implements FactoryBean<AsyncConnection>, InitializingBean, DisposableBean {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final User user;

    private final Configuration configuration;
    private AsyncConnection connection;

    private AsyncConnectionCleaner cleaner = new AsyncConnectionCleaner();

    public AsyncConnectionFactoryBean(Configuration configuration, User user) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.user = Objects.requireNonNull(user, "user");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            CompletableFuture<AsyncConnection> future = ConnectionFactory.createAsyncConnection(this.configuration, user);
            this.connection = future.get(10000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw new HbaseSystemException(e);
        }
    }

    @Override
    public AsyncConnection getObject() throws Exception {
        return connection;
    }

    @Override
    public Class<AsyncConnection> getObjectType() {
       return AsyncConnection.class;
    }

    @Override
    public void destroy() throws Exception {
        logger.info("Hbase AsyncConnection destroy()");
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                logger.warn("Hbase Connection.close() error: " + e.getMessage(), e);
            }
        }
        cleaner.clean();
    }
}
