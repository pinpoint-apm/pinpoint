/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.collector.applicationmap.dao.hbase.statistics;

import com.navercorp.pinpoint.collector.dao.hbase.BulkOperationReporter;
import com.navercorp.pinpoint.collector.dao.hbase.HbaseMapStatisticsCallerDao;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.BulkConfiguration;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.BulkIncrementer;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.BulkIncrementerFactory;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.BulkOperationReporterFactory;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.BulkUpdater;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.DefaultBulkIncrementer;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.DefaultBulkUpdater;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.DefaultBulkWriter;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.RowKeyMerge;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.SyncWriter;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * @author intr3p1d
 */
@Configuration
public class ApplicationMapBulkFactory {
    private final BulkConfiguration bulkConfiguration;
    private final BulkIncrementerFactory bulkIncrementerFactory;
    private final BulkOperationReporterFactory bulkOperationReporterFactory;

    public ApplicationMapBulkFactory(BulkConfiguration bulkConfiguration,
                                     BulkIncrementerFactory bulkIncrementerFactory,
                                     BulkOperationReporterFactory bulkOperationReporterFactory) {
        this.bulkConfiguration = Objects.requireNonNull(bulkConfiguration, "bulkConfiguration");
        this.bulkIncrementerFactory = Objects.requireNonNull(bulkIncrementerFactory, "bulkIncrementerFactory");
        this.bulkOperationReporterFactory = Objects.requireNonNull(bulkOperationReporterFactory, "bulkOperationReporterFactory");
    }

    private BulkIncrementer newBulkIncrementer(String reporterName, HbaseColumnFamily hbaseColumnFamily, int limitSize) {
        BulkOperationReporter reporter = bulkOperationReporterFactory.getBulkOperationReporter(reporterName);
        RowKeyMerge merge = new RowKeyMerge(hbaseColumnFamily);
        BulkIncrementer bulkIncrementer = new DefaultBulkIncrementer(merge);

        return bulkIncrementerFactory.wrap(bulkIncrementer, limitSize, reporter);
    }


    private BulkUpdater getBulkUpdater(String reporterName) {
        BulkOperationReporter reporter = bulkOperationReporterFactory.getBulkOperationReporter(reporterName);
        BulkUpdater bulkUpdater = new DefaultBulkUpdater();
        return bulkIncrementerFactory.wrap(bulkUpdater, bulkConfiguration.getCalleeLimitSize(), reporter);
    }

    private BulkWriter newBulkWriter(String loggerName,
                                     HbaseOperations hbaseTemplate,
                                     HbaseColumnFamily descriptor,
                                     TableNameProvider tableNameProvider,
                                     RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                     BulkIncrementer bulkIncrementer,
                                     BulkUpdater bulkUpdater) {
        if (bulkConfiguration.enableBulk()) {
            return new DefaultBulkWriter(loggerName, hbaseTemplate, rowKeyDistributorByHashPrefix,
                    bulkIncrementer, bulkUpdater, descriptor, tableNameProvider);
        } else {
            return new SyncWriter(loggerName, hbaseTemplate, rowKeyDistributorByHashPrefix, descriptor, tableNameProvider);
        }
    }

    private static String newBulkWriterName(String className) {
        return className + "-writer";
    }

    @Bean
    public BulkIncrementer inboundBulkIncrementer() {
        String reporterName = "inboundBulkIncrementerReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseColumnFamily.MAP_STATISTICS_INBOUND_SERVICE_GROUP_COUNTER;
        int limitSize = bulkConfiguration.getCallerLimitSize();

        return newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }

    @Bean
    public BulkUpdater inboundBulkUpdater() {
        String reporterName = "inboundBulkUpdaterReporter";
        return getBulkUpdater(reporterName);
    }

    @Bean
    public BulkWriter inboundBulkWriter(HbaseOperations hbaseTemplate,
                                        TableNameProvider tableNameProvider,
                                        @Qualifier("applicationMapInboundRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                        @Qualifier("inboundBulkIncrementer") BulkIncrementer bulkIncrementer,
                                        @Qualifier("inboundBulkUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(HbaseMapStatisticsCallerDao.class.getName());
        return newBulkWriter(loggerName, hbaseTemplate, HbaseColumnFamily.MAP_STATISTICS_INBOUND_SERVICE_GROUP_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }


    @Bean
    public BulkIncrementer outboundBulkIncrementer() {
        String reporterName = "outboundBulkIncrementerReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseColumnFamily.MAP_STATISTICS_OUTBOUND_SERVICE_GROUP_COUNTER;
        int limitSize = bulkConfiguration.getCallerLimitSize();

        return newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }

    @Bean
    public BulkUpdater outboundBulkUpdater() {
        String reporterName = "outboundBulkUpdaterReporter";
        return getBulkUpdater(reporterName);
    }


    @Bean
    public BulkWriter outboundBulkWriter(HbaseOperations hbaseTemplate,
                                         TableNameProvider tableNameProvider,
                                         @Qualifier("applicationMapOutboundRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                         @Qualifier("outboundBulkIncrementer") BulkIncrementer bulkIncrementer,
                                         @Qualifier("outboundBulkUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(HbaseMapStatisticsCallerDao.class.getName());
        return newBulkWriter(loggerName, hbaseTemplate, HbaseColumnFamily.MAP_STATISTICS_OUTBOUND_SERVICE_GROUP_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }


    @Bean
    public BulkIncrementer applicationMapSelfBulkIncrementer() {
        String reporterName = "applicationMapSelfBulkIncrementerReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseColumnFamily.MAP_STATISTICS_SELF_SERVICE_GROUP_COUNTER;
        int limitSize = bulkConfiguration.getCallerLimitSize();

        return newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }

    @Bean
    public BulkUpdater applicationMapSelfBulkUpdater() {
        String reporterName = "ServiceGroupSelfBulkUpdaterReporter";
        return getBulkUpdater(reporterName);
    }

    @Bean
    public BulkWriter applicationMapSelfBulkWriter(HbaseOperations hbaseTemplate,
                                         TableNameProvider tableNameProvider,
                                         @Qualifier("applicationMapSelfRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                         @Qualifier("applicationMapSelfBulkIncrementer") BulkIncrementer bulkIncrementer,
                                         @Qualifier("applicationMapSelfBulkUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(HbaseMapStatisticsCallerDao.class.getName());
        return newBulkWriter(loggerName, hbaseTemplate, HbaseColumnFamily.MAP_STATISTICS_SELF_SERVICE_GROUP_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }

}
