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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.security.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * @author HyunGil Jeong
 */
@Component
public class ConnectionFactoryBean implements FactoryBean<Connection>, InitializingBean, DisposableBean {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final User user;


    private HadoopResourceCleanerRegistry cleaner;

    private final Configuration configuration;
    private Connection connection;
    private ExecutorService executorService;

    private Consumer<Connection> postProcessor;

    public ConnectionFactoryBean(Configuration configuration, User user) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.user = Objects.requireNonNull(user, "user");
    }

    public ConnectionFactoryBean(Configuration configuration, User user, ExecutorService executorService) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.user = Objects.requireNonNull(user, "user");
        this.executorService = Objects.requireNonNull(executorService, "executorService");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.cleaner != null) {
            this.cleaner.register(configuration);
        }

        try {
            if (executorService == null) {
                connection = ConnectionFactory.createConnection(this.configuration, user);
            } else {
                connection = ConnectionFactory.createConnection(this.configuration, executorService, user);
            }
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }

        if (postProcessor != null) {
            postProcessor.accept(connection);
        }
    }

//    @Qualifier("hbaseSecurityInterceptor")
//    @Autowired(required = false)
//    public void setHbaseSecurityInterceptor(HbaseSecurityProvider hbaseSecurityProvider) {
//        this.hbaseSecurityProvider = hbaseSecurityProvider;
//    }

    @Autowired(required = false)
    public void setCleaner(HadoopResourceCleanerRegistry cleaner) {
        this.cleaner = cleaner;
    }

    @Autowired(required = false)
    public void setPostProcessor(Consumer<Connection> postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Override
    public Connection getObject() throws Exception {
        return connection;
    }

    @Override
    public Class<Connection> getObjectType() {
        return Connection.class;
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
