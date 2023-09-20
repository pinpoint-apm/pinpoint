/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.common.hbase.config.Warmup;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * @author HyunGil Jeong
 */
@Component
public class ConnectionFactoryBean implements FactoryBean<Connection>, InitializingBean, DisposableBean {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private HbaseSecurityInterceptor hbaseSecurityInterceptor = new EmptyHbaseSecurityInterceptor();

    private HadoopResourceCleanerRegistry cleaner;

    private final Configuration configuration;
    private Connection connection;
    private ExecutorService executorService;

    private Warmup warmup;

    public ConnectionFactoryBean(Configuration configuration) {
        Objects.requireNonNull(configuration, "configuration");
        this.configuration = configuration;
    }

    public ConnectionFactoryBean(Configuration configuration, ExecutorService executorService) {
        Objects.requireNonNull(configuration, "configuration");
        Objects.requireNonNull(executorService, "executorService");
        this.configuration = configuration;
        this.executorService = executorService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.cleaner != null) {
            this.cleaner.register(configuration);
        }

        hbaseSecurityInterceptor.process(configuration);
        try {
            if (executorService == null) {
                connection = ConnectionFactory.createConnection(this.configuration);
            } else {
                connection = ConnectionFactory.createConnection(this.configuration, executorService);
            }
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }

        if (warmup != null) {
            warmup.warmup(connection);
        }
    }

    @Qualifier("hbaseSecurityInterceptor")
    @Autowired(required = false)
    public void setHbaseSecurityInterceptor(HbaseSecurityInterceptor hbaseSecurityInterceptor) {
        this.hbaseSecurityInterceptor = hbaseSecurityInterceptor;
    }

    @Autowired(required = false)
    public void setCleaner(HadoopResourceCleanerRegistry cleaner) {
        this.cleaner = cleaner;
    }

    @Autowired(required = false)
    public void setWarmup(Warmup warmup) {
        this.warmup = warmup;
    }

    @Override
    public Connection getObject() throws Exception {
        return connection;
    }

    @Override
    public Class<?> getObjectType() {
        if (connection == null) {
            return Connection.class;
        }
        return connection.getClass();
    }

    @Override
    public void destroy() throws Exception {
        logger.info("Hbase Connection destroy()");
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                logger.warn("Hbase Connection.close() error: " + e.getMessage(), e);
            }
        }

        if (this.cleaner != null) {
            this.cleaner.clean();
        }
    }
}
