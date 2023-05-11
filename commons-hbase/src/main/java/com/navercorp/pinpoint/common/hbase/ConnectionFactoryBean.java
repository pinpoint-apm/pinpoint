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
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * @author HyunGil Jeong
 */
public class ConnectionFactoryBean implements FactoryBean<Connection>, InitializingBean, DisposableBean {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired(required = false)
    private TableNameProvider tableNameProvider;

    @Qualifier("hbaseSecurityInterceptor")
    @Autowired(required = false)
    private HbaseSecurityInterceptor hbaseSecurityInterceptor = new EmptyHbaseSecurityInterceptor();

    @Autowired(required = false)
    private HadoopResourceCleanerRegistry cleaner;

    private final boolean warmUp;
    private final HbaseTable[] warmUpExclusive = {HbaseTable.AGENT_URI_STAT};
    private final Configuration configuration;
    private Connection connection;
    private ExecutorService executorService;

    public ConnectionFactoryBean(Configuration configuration) {
        Objects.requireNonNull(configuration, "configuration");
        this.configuration = configuration;
        warmUp = configuration.getBoolean("hbase.client.warmup.enable", false);
    }

    public ConnectionFactoryBean(Configuration configuration, ExecutorService executorService) {
        Objects.requireNonNull(configuration, "configuration");
        Objects.requireNonNull(executorService, "executorService");
        this.configuration = configuration;
        this.executorService = executorService;
        this.warmUp = configuration.getBoolean("hbase.client.warmup.enable", false);
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

        if (warmUp) {
            if (tableNameProvider != null && tableNameProvider.hasDefaultNameSpace()) {
                logger.info("warmup for hbase connection started");
                List<HbaseTable> warmUpInclusive = new ArrayList<>(List.of(HbaseTable.values()));
                warmUpInclusive.removeAll(List.of(warmUpExclusive));

                for (HbaseTable hBaseTable : warmUpInclusive) {
                    try {
                        TableName tableName = tableNameProvider.getTableName(hBaseTable);
                        logger.info("warmup for hbase table start: {}", tableName.toString());
                        RegionLocator regionLocator = connection.getRegionLocator(tableName);
                        regionLocator.getAllRegionLocations();
                    } catch (IOException e) {
                        logger.warn("Failed to warmup for Table:{}. message:{}", hBaseTable.getName(), e.getMessage(), e);
                    }
                }
            }
        }
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
    public boolean isSingleton() {
        return true;
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
