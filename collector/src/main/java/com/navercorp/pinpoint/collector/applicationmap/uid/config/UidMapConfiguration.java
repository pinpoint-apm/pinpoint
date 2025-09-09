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

package com.navercorp.pinpoint.collector.applicationmap.uid.config;

import com.navercorp.pinpoint.collector.applicationmap.config.MapLinkProperties;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkIncrementer;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkUpdater;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.applicationmap.statistics.config.BulkFactory;
import com.navercorp.pinpoint.collector.applicationmap.statistics.config.BulkProperties;
import com.navercorp.pinpoint.collector.applicationmap.uid.dao.MapInLinkUidDao;
import com.navercorp.pinpoint.collector.applicationmap.uid.dao.MapOutLinkUidDao;
import com.navercorp.pinpoint.collector.applicationmap.uid.dao.MapSelfUidDao;
import com.navercorp.pinpoint.collector.applicationmap.uid.dao.hbase.HbaseMapInLinkUidDao;
import com.navercorp.pinpoint.collector.applicationmap.uid.dao.hbase.HbaseMapOutLinkUidDao;
import com.navercorp.pinpoint.collector.applicationmap.uid.dao.hbase.HbaseMapSelfUidDao;
import com.navercorp.pinpoint.collector.applicationmap.uid.service.HbaseUidLinkService;
import com.navercorp.pinpoint.collector.dao.hbase.IgnoreStatFilter;
import com.navercorp.pinpoint.collector.service.UidLinkService;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.async.HbaseAsyncTemplate;
import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.hbase.wd.RangeDoubleHash;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
@Configuration
public class UidMapConfiguration {
    private static final Logger logger = LogManager.getLogger(UidMapConfiguration.class);

    public UidMapConfiguration() {
        logger.info("Install UidMapConfiguration");
    }

    private String newBulkWriterName(String className) {
        return className + "-uid-writer";
    }

    @Bean
    public RowKeyDistributorByHashPrefix selfUidRowKeyDistributor() {
        ByteHasher hasher = RangeDoubleHash.ofSecondary(0, 16, ByteHasher.MAX_BUCKETS, 4, 16, 20);
        return new RowKeyDistributorByHashPrefix(hasher);
    }


    @Bean
    public BulkIncrementer selfUidIncrementer(BulkFactory factory, BulkProperties bulkProperties) {
        String reporterName = "selfUidIncrementReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseTables.MAP_SELF_V3_COUNTER;
        int limitSize = bulkProperties.getSelfLimitSize();

        return factory.newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }


    @Bean
    public BulkUpdater selfUidUpdater(BulkFactory factory) {
        String reporterName = "selfUidUpdateReporter";
        return factory.getBulkUpdater(reporterName);
    }


    @Bean
    public BulkWriter selfUidBulkWriter(BulkFactory factory,
                                        HbaseAsyncTemplate asyncTemplate,
                                        TableNameProvider tableNameProvider,
                                        @Qualifier("selfUidRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                        @Qualifier("selfUidIncrementer") BulkIncrementer bulkIncrementer,
                                        @Qualifier("selfUidUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(HbaseMapSelfUidDao.class.getName());
        return factory.newBulkWriter(loggerName, asyncTemplate, HbaseTables.MAP_SELF_V3_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }



    @Bean
    public MapSelfUidDao mapSelfUidDao(MapLinkProperties mapLinkProperties,
                                       TimeSlot timeSlot,
                                       @Qualifier("selfUidBulkWriter") BulkWriter bulkWriter) {
        return new HbaseMapSelfUidDao(mapLinkProperties, timeSlot, bulkWriter);
    }


    // outLink -----------------


    @Bean
    public BulkIncrementer outLinkUidIncrementer(BulkFactory factory, BulkProperties bulkProperties) {
        String reporterName = "outLinkUidIncrementReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseTables.MAP_OUT_V3_COUNTER;
        int limitSize = bulkProperties.getCalleeLimitSize();

        return factory.newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }


    @Bean
    public BulkUpdater outLinkUidUpdater(BulkFactory factory) {
        String reporterName = "inLinkUpdaterReporter";
        return factory.getBulkUpdater(reporterName);
    }

    @Bean
    public BulkWriter outLinkUidBulkWriter(BulkFactory factory,
                                           HbaseAsyncTemplate asyncTemplate,
                                           TableNameProvider tableNameProvider,
                                           @Qualifier("linkUidRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                           @Qualifier("outLinkUidIncrementer") BulkIncrementer bulkIncrementer,
                                           @Qualifier("outLinkUidUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(MapOutLinkUidDao.class.getName());
        return factory.newBulkWriter(loggerName, asyncTemplate, HbaseTables.MAP_OUT_V3_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }

    @Bean
    public MapOutLinkUidDao mapOutLinkUidDao(MapLinkProperties mapLinkProperties,
                                             TimeSlot timeSlot,
                                             @Qualifier("outLinkUidBulkWriter") BulkWriter bulkWriter) {
        return new HbaseMapOutLinkUidDao(mapLinkProperties, timeSlot, bulkWriter);
    }

    // inLink ----------------


    @Bean
    public BulkIncrementer inLinkUidIncrementer(BulkFactory factory, BulkProperties bulkProperties) {
        String reporterName = "inLinkUidIncrementReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseTables.MAP_IN_V3_COUNTER;
        int limitSize = bulkProperties.getCallerLimitSize();

        return factory.newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }

    @Bean
    public BulkUpdater inLinkUidUpdater(BulkFactory factory) {
        String reporterName = "inLinkUpdateReporter";
        return factory.getBulkUpdater(reporterName);
    }


    @Bean
    public BulkWriter inLinkUidBulkWriter(BulkFactory factory,
                                        HbaseAsyncTemplate asyncTemplate,
                                        TableNameProvider tableNameProvider,
                                        @Qualifier("linkUidRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                        @Qualifier("inLinkUidIncrementer") BulkIncrementer bulkIncrementer,
                                        @Qualifier("inLinkUidUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(MapInLinkUidDao.class.getName());
        return factory.newBulkWriter(loggerName, asyncTemplate, HbaseTables.MAP_IN_V3_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }


    @Bean
    public MapInLinkUidDao mapInLinkUidDao(MapLinkProperties mapLinkProperties,
                                           IgnoreStatFilter ignoreStatFilter,
                                           TimeSlot timeSlot,
                                           @Qualifier("inLinkUidBulkWriter") BulkWriter bulkWriter) {
        return new HbaseMapInLinkUidDao(mapLinkProperties, ignoreStatFilter, timeSlot, bulkWriter);
    }



    @Bean
    public UidLinkService uidLinkService(MapSelfUidDao mapSelfUidDao, MapOutLinkUidDao mapOutLinkUidDao, MapInLinkUidDao mapInLinkUidDao) {
        return new HbaseUidLinkService(mapSelfUidDao, mapOutLinkUidDao, mapInLinkUidDao);
    }


    @Bean
    public RowKeyDistributorByHashPrefix linkUidRowKeyDistributor() {
        return selfUidRowKeyDistributor();
    }
}
