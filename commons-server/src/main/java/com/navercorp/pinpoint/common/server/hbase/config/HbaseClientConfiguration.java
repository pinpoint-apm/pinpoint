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

package com.navercorp.pinpoint.common.server.hbase.config;

import com.navercorp.pinpoint.common.hbase.ConnectionFactoryBean;
import com.navercorp.pinpoint.common.hbase.HbaseConfigurationFactoryBean;
import com.navercorp.pinpoint.common.hbase.HbaseSecurityProvider;
import com.navercorp.pinpoint.common.hbase.SimpleHbaseSecurityProvider;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.async.AsyncConnectionFactoryBean;
import com.navercorp.pinpoint.common.hbase.config.Warmup;
import com.navercorp.pinpoint.common.server.executor.ExecutorCustomizer;
import com.navercorp.pinpoint.common.server.executor.ExecutorProperties;
import com.navercorp.pinpoint.common.server.executor.ThreadPoolExecutorCustomizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.util.ShutdownHookManagerProxy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.validation.annotation.Validated;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@org.springframework.context.annotation.Configuration
public class HbaseClientConfiguration {


    @Bean
    @ConfigurationProperties(prefix = "hbase.client")
    public HBaseClientProperties hBaseProperties() {
        return new HBaseClientProperties();
    }

    @Bean
    public FactoryBean<Configuration> hbaseConfiguration(HBaseClientProperties client, Environment env) {

        Properties properties = new Properties(client.getProperties());

        properties.putIfAbsent(HConstants.ZOOKEEPER_QUORUM, client.getHost());

        properties.putIfAbsent(HConstants.ZOOKEEPER_CLIENT_PORT, String.valueOf(client.getPort()));

        String znode = client.getZnode();
        if (znode == null) {
            znode = env.getProperty("hbase.zookeeper.znode.parent", "/hbase");
        }
        properties.putIfAbsent(HConstants.ZOOKEEPER_ZNODE_PARENT, znode);

        HbaseConfigurationFactoryBean factoryBean = new HbaseConfigurationFactoryBean();
        factoryBean.setProperties(properties);
        return factoryBean;
    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.modules.hbase.security.auth", havingValue = "simple", matchIfMissing = true)
    public User hbaseLoginUser(Configuration configuration) {
        HbaseSecurityProvider provider = new SimpleHbaseSecurityProvider(configuration);
        return provider.login();
    }

    @Bean
    public FactoryBean<Connection> hbaseConnection(Configuration configuration,
                                                   User user,
                                                   @Qualifier("hbaseThreadPool") ExecutorService executorService) {
        return new ConnectionFactoryBean(configuration, user, executorService);
    }

    @Bean
    public FactoryBean<AsyncConnection> hbaseAsyncConnection(Configuration configuration, User user) {
        return new AsyncConnectionFactoryBean(configuration, user);
    }

    @Bean
    public ExecutorCustomizer<ThreadPoolExecutorFactoryBean> hbaseExecutorCustomizer() {
        return new ThreadPoolExecutorCustomizer();
    }

    @Bean
    @Validated
    @ConfigurationProperties(prefix = "hbase.client.executor")
    public ExecutorProperties hbaseClientExecutorProperties() {
        return new ExecutorProperties();
    }

    @Bean
    public FactoryBean<ExecutorService> hbaseThreadPool(@Qualifier("hbaseExecutorCustomizer") ExecutorCustomizer<ThreadPoolExecutorFactoryBean> executorCustomizer,
                                                        @Qualifier("hbaseClientExecutorProperties") ExecutorProperties properties) {
        ThreadPoolExecutorFactoryBean factory = new ThreadPoolExecutorFactoryBean();
        executorCustomizer.customize(factory, properties);
        return factory;
    }

    @Bean
    public ShutdownHookManagerProxy shutdownHookManagerProxy() {
        return new ShutdownHookManagerProxy();
    }


    @Bean
    @ConditionalOnProperty(name = "hbase.client.warmup.enable", havingValue = "true")
    public Consumer<Connection> hbaseConnectionWarmup(TableNameProvider tableNameProvider) {
        return new Warmup(tableNameProvider);
    }
}
