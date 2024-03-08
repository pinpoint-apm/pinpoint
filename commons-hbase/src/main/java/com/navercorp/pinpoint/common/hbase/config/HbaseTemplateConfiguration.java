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

package com.navercorp.pinpoint.common.hbase.config;

import com.navercorp.pinpoint.common.hbase.AdminFactory;
import com.navercorp.pinpoint.common.hbase.HBaseAdminTemplate;
import com.navercorp.pinpoint.common.hbase.HbaseAdminFactory;
import com.navercorp.pinpoint.common.hbase.HbaseTableFactory;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate;
import com.navercorp.pinpoint.common.hbase.HbaseVersionCheckBean;
import com.navercorp.pinpoint.common.hbase.TableFactory;
import com.navercorp.pinpoint.common.hbase.async.AsyncHbasePutWriter;
import com.navercorp.pinpoint.common.hbase.async.AsyncTableCustomizer;
import com.navercorp.pinpoint.common.hbase.async.AsyncTableFactory;
import com.navercorp.pinpoint.common.hbase.async.BatchAsyncHbasePutWriter;
import com.navercorp.pinpoint.common.hbase.async.DefaultAsyncTableCustomizer;
import com.navercorp.pinpoint.common.hbase.async.HbaseAsyncCacheConfiguration;
import com.navercorp.pinpoint.common.hbase.async.HbaseAsyncTableFactory;
import com.navercorp.pinpoint.common.hbase.async.HbasePutWriter;
import com.navercorp.pinpoint.common.hbase.async.LoggingHbasePutWriter;
import com.navercorp.pinpoint.common.hbase.util.DefaultScanMetricReporter;
import com.navercorp.pinpoint.common.hbase.util.EmptyScanMetricReporter;
import com.navercorp.pinpoint.common.hbase.util.ScanMetricReporter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.Optional;

@org.springframework.context.annotation.Configuration
@Import(HbaseAsyncCacheConfiguration.class)
public class HbaseTemplateConfiguration {
    private final Logger logger = LogManager.getLogger(HbaseTemplateConfiguration.class);

    @Bean
    public TableFactory hbaseTableFactory(@Qualifier("hbaseConnection") Connection connection) {
        return new HbaseTableFactory(connection);
    }

    @Bean
    public AsyncTableCustomizer asyncTableCustomizer() {
        return new DefaultAsyncTableCustomizer();
    }

    @Bean
    public AsyncTableFactory hbaseAsyncTableFactory(@Qualifier("hbaseAsyncConnection") AsyncConnection connection, AsyncTableCustomizer customizer) {
        return new HbaseAsyncTableFactory(connection, customizer);
    }


    @Bean
    @ConditionalOnProperty(name = "hbase.client.parallel.scan.enable", havingValue = "true")
    @ConfigurationProperties("hbase.client.parallel.scan")
    public ParallelScan parallelScan() {
        return new ParallelScan();
    }

    @Bean
    @ConditionalOnProperty(name = "hbase.client.scan-metric-reporter.enable", havingValue = "true")
    public ScanMetricReporter scannerMetricReporter() {
        return new DefaultScanMetricReporter();
    }

    @Bean("scannerMetricReporter")
    @ConditionalOnProperty(name = "hbase.client.scan-metric-reporter.enable", havingValue = "false", matchIfMissing = true)
    public ScanMetricReporter emptyScannerMetricReporter() {
        return new EmptyScanMetricReporter();
    }

    @Bean
    @Primary
    public HbaseTemplate hbaseTemplate(@Qualifier("hbaseConfiguration") Configuration configurable,
                                       @Qualifier("hbaseTableFactory") TableFactory tableFactory,
                                       @Qualifier("hbaseAsyncTableFactory") AsyncTableFactory asyncTableFactory,
                                       Optional<ParallelScan> parallelScan,
                                       @Value("${hbase.client.nativeAsync:false}") boolean nativeAsync,
                                       ScanMetricReporter scanMetricReporter) {
        HbaseTemplate template2 = new HbaseTemplate();
        template2.setConfiguration(configurable);
        template2.setTableFactory(tableFactory);

        if (parallelScan.isPresent()) {
            ParallelScan scan = parallelScan.get();
            template2.setEnableParallelScan(true);
            template2.setMaxThreads(scan.getMaxThreads());
            template2.setMaxThreadsPerParallelScan(scan.getMaxThreadsPerParallelScan());
        }

        template2.setAsyncTableFactory(asyncTableFactory);
        template2.setNativeAsync(nativeAsync);
        template2.setScanMetricReporter(scanMetricReporter);
        return template2;
    }

    @Bean
    @ConditionalOnProperty(name = "hbase.client.put-writer", havingValue = "asyncTable")
    public HbasePutWriter asyncHbasePutWriter(@Qualifier("hbaseAsyncTableFactory") AsyncTableFactory asyncTableFactory) {
        AsyncHbasePutWriter writer = new AsyncHbasePutWriter(asyncTableFactory);
        logger.info("HbasePutWriter {}", writer);
        return new LoggingHbasePutWriter(writer);
    }

    @Bean
    @ConditionalOnProperty(name = "hbase.client.put-writer", havingValue = "asyncBufferedMutator", matchIfMissing = true)
    public HbasePutWriter batchHbasePutWriter(@Qualifier("hbaseAsyncTableFactory") AsyncTableFactory asyncTableFactory) {
        BatchAsyncHbasePutWriter writer = new BatchAsyncHbasePutWriter(asyncTableFactory);
        logger.info("HbasePutWriter {}", writer);
        return new LoggingHbasePutWriter(writer);
    }

    @Bean
    public AdminFactory hbaseAdminFactory(@Qualifier("hbaseConnection") Connection connection) {
        return new HbaseAdminFactory(connection);
    }

    @Bean
    public HBaseAdminTemplate hbaseAdminTemplate(@Qualifier("hbaseAdminFactory") AdminFactory adminFactory) {
        return new HBaseAdminTemplate(adminFactory);
    }

    @Bean
    public HbaseVersionCheckBean hbaseVersionCheck(@Qualifier("hbaseAdminTemplate") HBaseAdminTemplate adminTemplate,
                                                   @Value("${hbase.client.compatibility-check:true}") boolean hbaseVersionCompatibility) {
        return new HbaseVersionCheckBean(adminTemplate, hbaseVersionCompatibility);
    }
}

