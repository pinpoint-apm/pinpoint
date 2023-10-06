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

import com.navercorp.pinpoint.collector.dao.hbase.BulkOperationReporter;
import com.navercorp.pinpoint.collector.manage.HBaseManager;
import com.navercorp.pinpoint.collector.monitor.BulkOperationMetrics;
import com.navercorp.pinpoint.collector.monitor.HBaseAsyncOperationMetrics;
import com.navercorp.pinpoint.common.hbase.HBaseAsyncOperation;
import com.navercorp.pinpoint.common.hbase.HBaseAsyncOperationFactory;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.hbase.SimpleBatchWriter;
import com.navercorp.pinpoint.common.hbase.batch.BufferedMutatorProperties;
import com.navercorp.pinpoint.common.hbase.batch.BufferedMutatorWriter;
import com.navercorp.pinpoint.common.hbase.batch.HbaseBatchWriter;
import com.navercorp.pinpoint.common.hbase.batch.SimpleBatchWriterFactoryBean;
import com.navercorp.pinpoint.common.hbase.config.HbaseMultiplexerProperties;
import org.apache.hadoop.hbase.client.Connection;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class HbaseAsyncConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "hbase.client.async")
    public HbaseMultiplexerProperties hbaseMultiplexerProperties() {
        return new HbaseMultiplexerProperties();
    }

    @Bean
    public FactoryBean<HBaseAsyncOperation> asyncOperation(@Qualifier("hbaseConnection") Connection connection) {
        return new HBaseAsyncOperationFactory(connection);
    }

    @Bean
    public HBaseAsyncOperationMetrics asyncOperationMetrics(List<HBaseAsyncOperation> hBaseAsyncOperationList) {
        return new HBaseAsyncOperationMetrics(hBaseAsyncOperationList);
    }

    @Bean
    public FactoryBean<HBaseAsyncOperation> batchAsyncOperation(@Qualifier("batchConnectionFactory") Connection connection) {
        return new HBaseAsyncOperationFactory(connection);
    }

    @Bean
    public BufferedMutatorProperties bufferedMutatorConfiguration() {
        return new BufferedMutatorProperties();
    }

    @Bean
    public HbaseBatchWriter hbaseBatchWriter(@Qualifier("batchConnectionFactory") Connection connection) {
        BufferedMutatorProperties properties = bufferedMutatorConfiguration();
        return new BufferedMutatorWriter(connection, properties);
    }

    @Bean
    public FactoryBean<SimpleBatchWriter> simpleBatchWriter(@Qualifier("hbaseBatchWriter") HbaseBatchWriter writer,
                                                            HBaseAsyncOperation asyncOperation,
                                                            @Qualifier("hbaseTemplate") HbaseTemplate2 hbaseTemplate2) {
        BufferedMutatorProperties properties = bufferedMutatorConfiguration();
        return new SimpleBatchWriterFactoryBean(properties, writer, asyncOperation, hbaseTemplate2);
    }

    @Bean
    public BulkOperationMetrics cachedStatisticsDaoMetrics(List<BulkOperationReporter> bulkOperationReporters) {
        return new BulkOperationMetrics(bulkOperationReporters);
    }

    @Bean
    public HBaseManager hBaseManager(@Qualifier("asyncOperation") HBaseAsyncOperation hBaseAsyncOperation) {
        return new HBaseManager(hBaseAsyncOperation);
    }



}
