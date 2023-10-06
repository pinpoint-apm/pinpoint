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
import com.navercorp.pinpoint.common.hbase.HBaseAsyncOperation;
import com.navercorp.pinpoint.common.hbase.HbaseAdminFactory;
import com.navercorp.pinpoint.common.hbase.HbaseTableFactory;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.hbase.HbaseVersionCheckBean;
import com.navercorp.pinpoint.common.hbase.TableFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@org.springframework.context.annotation.Configuration
public class HbaseTemplateConfiguration {

    @Bean
    public TableFactory hbaseTableFactory(@Qualifier("hbaseConnection") Connection connection) {
        return new HbaseTableFactory(connection);
    }

    @Bean
    @ConditionalOnProperty(name = "hbase.client.parallel.scan.enable", havingValue = "true")
    @ConfigurationProperties("hbase.client.parallel.scan")
    public ParallelScan parallelScan() {
        return new ParallelScan();
    }

    @Bean
    public HbaseTemplate2 hbaseTemplate(@Qualifier("hbaseConfiguration") Configuration configurable,
                                        @Qualifier("hbaseTableFactory") TableFactory tableFactory,
                                        Optional<ParallelScan> parallelScan,
                                        Optional<HBaseAsyncOperation> asyncOperation) {
        HbaseTemplate2 template2 = new HbaseTemplate2();
        template2.setConfiguration(configurable);
        template2.setTableFactory(tableFactory);

        if (parallelScan.isPresent()) {
            ParallelScan scan = parallelScan.get();
            template2.setEnableParallelScan(true);
            template2.setMaxThreads(scan.getMaxThreads());
            template2.setMaxThreadsPerParallelScan(scan.getMaxThreadsPerParallelScan());
        }

        if (asyncOperation.isPresent()) {
            template2.setAsyncOperation(asyncOperation.get());
        }
        return template2;
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

