/*
 * Copyright 2025 NAVER Corp.
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
 */

package com.navercorp.pinpoint.collector.applicationmap.statistics.config;

import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HbaseMapInLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HbaseMapOutLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HbaseMapResponseTimeDao;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkIncrementer;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkUpdater;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkWriter;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.async.HbaseAsyncTemplate;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        BulkProperties.class,

})
public class BulkConfiguration {

    @Bean
    public BulkIncrementerFactory bulkIncrementerFactory() {
        return new BulkIncrementerFactory();
    }

    @Bean
    public BulkOperationReporterFactory bulkOperationReporterFactory() {
        return new BulkOperationReporterFactory();
    }

    @Bean
    public BulkFactory bulkFactory(BulkProperties bulkProperties,
                                   BulkIncrementerFactory bulkIncrementerFactory,
                                   BulkOperationReporterFactory bulkOperationReporterFactory) {
        return new BulkFactory(bulkProperties, bulkIncrementerFactory, bulkOperationReporterFactory);
    }

    @Bean
    public BulkIncrementer outLinkBulkIncrementer(BulkFactory factory, BulkProperties bulkProperties) {
        String reporterName = "outLinkIncrementReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseTables.MAP_STATISTICS_CALLER_VER2_COUNTER;
        int limitSize = bulkProperties.getCallerLimitSize();

        return factory.newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }

    @Bean
    public BulkUpdater outLinkBulkUpdater(BulkFactory factory) {
        String reporterName = "outLinkUpdateReporter";
        return factory.getBulkUpdater(reporterName);
    }


    @Bean
    public BulkWriter outLinkBulkWriter(BulkFactory factory,
                                        HbaseAsyncTemplate asyncTemplate,
                                        TableNameProvider tableNameProvider,
                                        @Qualifier("mapLinkRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                        @Qualifier("outLinkBulkIncrementer") BulkIncrementer bulkIncrementer,
                                        @Qualifier("outLinkBulkUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(HbaseMapOutLinkDao.class.getName());
        return factory.newBulkWriter(loggerName, asyncTemplate, HbaseTables.MAP_STATISTICS_CALLEE_VER2_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }


    @Bean
    public BulkIncrementer inLinkBulkIncrementer(BulkFactory factory, BulkProperties bulkProperties) {
        String reporterName = "inLinkIncrementReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseTables.MAP_STATISTICS_CALLEE_VER2_COUNTER;
        int limitSize = bulkProperties.getCalleeLimitSize();

        return factory.newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }


    @Bean
    public BulkUpdater inLinkBulkUpdater(BulkFactory factory) {
        String reporterName = "inLinkUpdateReporter";
        return factory.getBulkUpdater(reporterName);
    }

    @Bean
    public BulkWriter inLinkBulkWriter(BulkFactory factory,
                                       HbaseAsyncTemplate asyncTemplate,
                                       TableNameProvider tableNameProvider,
                                       @Qualifier("mapLinkRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                       @Qualifier("inLinkBulkIncrementer") BulkIncrementer bulkIncrementer,
                                       @Qualifier("inLinkBulkUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(HbaseMapInLinkDao.class.getName());
        return factory.newBulkWriter(loggerName, asyncTemplate, HbaseTables.MAP_STATISTICS_CALLER_VER2_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }

    @Bean
    public BulkIncrementer selfBulkIncrementer(BulkFactory factory, BulkProperties bulkProperties) {
        String reporterName = "selfIncrementReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseTables.MAP_STATISTICS_SELF_VER2_COUNTER;
        int limitSize = bulkProperties.getSelfLimitSize();

        return factory.newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }

    @Bean
    public BulkUpdater selfBulkUpdater(BulkFactory factory) {
        String reporterName = "selfUpdateReporter";
        return factory.getBulkUpdater(reporterName);
    }

    @Bean
    public BulkWriter selfBulkWriter(BulkFactory factory,
                                     HbaseAsyncTemplate asyncTemplate,
                                     TableNameProvider tableNameProvider,
                                     @Qualifier("mapSelfRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                     @Qualifier("selfBulkIncrementer") BulkIncrementer bulkIncrementer,
                                     @Qualifier("selfBulkUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(HbaseMapResponseTimeDao.class.getName());
        return factory.newBulkWriter(loggerName, asyncTemplate, HbaseTables.MAP_STATISTICS_SELF_VER2_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }

    private String newBulkWriterName(String className) {
        return className + "-writer";
    }
}
