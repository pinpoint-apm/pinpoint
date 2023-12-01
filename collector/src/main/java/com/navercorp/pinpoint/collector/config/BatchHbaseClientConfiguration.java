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

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.hbase.ConnectionFactoryBean;
import com.navercorp.pinpoint.common.server.executor.ExecutorCustomizer;
import com.navercorp.pinpoint.common.server.executor.ExecutorProperties;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.security.User;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import java.util.concurrent.ExecutorService;

@org.springframework.context.annotation.Configuration
public class BatchHbaseClientConfiguration {
    @Bean
    public FactoryBean<Connection> batchConnectionFactory(Configuration configuration, User user,
                                                     @Qualifier("batchHbaseThreadPool") ExecutorService executorService) {
        return new ConnectionFactoryBean(configuration, user, executorService);
    }

    @Bean
    public FactoryBean<ExecutorService> batchHbaseThreadPool(@Qualifier("hbaseExecutorCustomizer") ExecutorCustomizer<ThreadPoolExecutorFactoryBean> executorCustomizer,
                                                             @Qualifier("hbaseClientExecutorProperties")
                                                             ExecutorProperties properties) {
        ThreadPoolExecutorFactoryBean factory = new ThreadPoolExecutorFactoryBean();
        executorCustomizer.customize(factory, properties);
        return factory;
    }
}


