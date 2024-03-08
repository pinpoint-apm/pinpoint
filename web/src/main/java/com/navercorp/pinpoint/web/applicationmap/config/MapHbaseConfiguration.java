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

package com.navercorp.pinpoint.web.applicationmap.config;

import com.navercorp.pinpoint.common.hbase.ConnectionFactoryBean;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate;
import com.navercorp.pinpoint.common.hbase.TableFactory;
import com.navercorp.pinpoint.common.hbase.async.AsyncConnectionFactoryBean;
import com.navercorp.pinpoint.common.hbase.async.AsyncTableCustomizer;
import com.navercorp.pinpoint.common.hbase.async.AsyncTableFactory;
import com.navercorp.pinpoint.common.hbase.config.HbaseTemplateConfiguration;
import com.navercorp.pinpoint.common.hbase.config.ParallelScan;
import com.navercorp.pinpoint.common.hbase.util.ScanMetricReporter;
import com.navercorp.pinpoint.common.server.executor.ExecutorCustomizer;
import com.navercorp.pinpoint.common.server.executor.ExecutorProperties;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.security.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

@org.springframework.context.annotation.Configuration
@ComponentScan({
        "com.navercorp.pinpoint.web.applicationmap.dao.hbase",
        "com.navercorp.pinpoint.web.applicationmap.dao.mapper"
})
public class MapHbaseConfiguration {
    private final Logger logger = LogManager.getLogger(MapHbaseConfiguration.class);
    private final HbaseTemplateConfiguration config = new HbaseTemplateConfiguration();

    public MapHbaseConfiguration() {
        logger.info("Install {}", MapHbaseConfiguration.class.getSimpleName());
    }

    @Bean
    public FactoryBean<ExecutorService> mapHbaseThreadPool(@Qualifier("hbaseExecutorCustomizer") ExecutorCustomizer<ThreadPoolExecutorFactoryBean> executorCustomizer,
                                                        @Qualifier("hbaseClientExecutorProperties") ExecutorProperties properties) {
        ThreadPoolExecutorFactoryBean factory = new ThreadPoolExecutorFactoryBean();
        executorCustomizer.customize(factory, properties);
        factory.setThreadNamePrefix("Map-" + factory.getThreadNamePrefix());
        return factory;
    }

    @Bean
    public FactoryBean<Connection> mapHbaseConnection(Configuration configuration,
                                                   User user,
                                                   @Qualifier("mapHbaseThreadPool") ExecutorService executorService) {
        return new ConnectionFactoryBean(configuration, user, executorService);
    }

    @Bean
    public FactoryBean<AsyncConnection> mapHbaseAsyncConnection(Configuration configuration, User user) {
        return new AsyncConnectionFactoryBean(configuration, user);
    }


    @Bean
    public TableFactory mapHbaseTableFactory(@Qualifier("hbaseConnection") Connection connection) {
        return config.hbaseTableFactory(connection);
    }

    @Bean
    public AsyncTableFactory mapHbaseAsyncTableFactory(@Qualifier("hbaseAsyncConnection") AsyncConnection connection, AsyncTableCustomizer customizer) {
        return config.hbaseAsyncTableFactory(connection, customizer);
    }


    @Bean
    public HbaseTemplate mapHbaseTemplate(@Qualifier("hbaseConfiguration") Configuration configurable,
                                          @Qualifier("mapHbaseTableFactory") TableFactory tableFactory,
                                          @Qualifier("mapHbaseAsyncTableFactory") AsyncTableFactory asyncTableFactory,
                                          Optional<ParallelScan> parallelScan,
                                          @Value("${hbase.client.nativeAsync:false}") boolean nativeAsync,
                                          ScanMetricReporter reporter) {
        return config.hbaseTemplate(configurable, tableFactory, asyncTableFactory, parallelScan, nativeAsync, reporter);
    }

}

