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

package com.navercorp.pinpoint.collector.applicationmap.config;

import com.navercorp.pinpoint.collector.applicationmap.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapInLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapResponseTimeDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HbaseHostApplicationMapDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HbaseMapInLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HbaseMapOutLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HbaseMapResponseTimeDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HostLinkFactory;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HostLinkFactoryV2;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HostRowKeyEncoder;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HostRowKeyEncoderV2;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.InLinkFactory;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.InLinkFactoryV2;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.OutLinkFactory;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.OutLinkFactoryV2;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.SelfNodeFactory;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.SelfNodeFactoryV2;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.applicationmap.statistics.config.BulkFactory;
import com.navercorp.pinpoint.collector.applicationmap.statistics.config.BulkProperties;
import com.navercorp.pinpoint.collector.dao.hbase.IgnoreStatFilter;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributor;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@ConditionalOnProperty(name = ServerNameVersion.KEY, havingValue = "v2")
@ConditionalOnMissingBean(MapV3Configuration.class)
public class MapV2Configuration {
    private static final Logger logger = LogManager.getLogger(MapV2Configuration.class);


    public MapV2Configuration() {
        logger.info("Install {}", MapV2Configuration.class.getName());
    }

    @Bean
    public BulkWriter outLinkBulkWriter(BulkFactory factory,
                                        BulkProperties bulkProperties,
                                        @Qualifier("mapLinkRowKeyDistributor")
                                        RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {

        int limitSize = bulkProperties.getCallerLimitSize();
        String loggerName = newBulkWriterName(HbaseMapOutLinkDao.class.getName());

        BulkFactory.Builder builder = factory.newBuilder(loggerName, HbaseTables.MAP_V2_COLUMN_FAMILY_NAME, rowKeyDistributorByHashPrefix);
        builder.setIncrementer("outLinkIncrementReporter", limitSize);
        builder.setMaxUpdater("outLinkUpdateReporter", limitSize);
        return builder.build();
    }

    @Bean
    public BulkWriter inLinkBulkWriter(BulkFactory factory,
                                       BulkProperties bulkProperties,
                                       @Qualifier("mapLinkRowKeyDistributor")
                                       RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {

        int limitSize = bulkProperties.getCalleeLimitSize();
        String loggerName = newBulkWriterName(HbaseMapInLinkDao.class.getName());

        BulkFactory.Builder builder = factory.newBuilder(loggerName, HbaseTables.MAP_V2_COLUMN_FAMILY_NAME, rowKeyDistributorByHashPrefix);
        builder.setIncrementer("inLinkIncrementReporter", limitSize);
        builder.setMaxUpdater("inLinkUpdateReporter", limitSize);
        return builder.build();
    }


    @Bean
    public BulkWriter selfBulkWriter(BulkFactory factory,
                                     BulkProperties bulkProperties,
                                     @Qualifier("mapSelfRowKeyDistributor")
                                     RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {

        int limitSize = bulkProperties.getSelfLimitSize();
        String loggerName = newBulkWriterName(HbaseMapResponseTimeDao.class.getName());

        BulkFactory.Builder builder = factory.newBuilder(loggerName, HbaseTables.MAP_V2_COLUMN_FAMILY_NAME, rowKeyDistributorByHashPrefix);
        builder.setIncrementer("selfIncrementReporter", limitSize);
        builder.setMaxUpdater("selfUpdateReporter", limitSize);
        return builder.build();
    }

    private String newBulkWriterName(String className) {
        return className + "-writer";
    }

    @Bean
    public MapInLinkDao mapInLinkDao(MapLinkProperties mapLinkProperties,
                                     IgnoreStatFilter ignoreStatFilter,
                                     TimeSlot timeSlot,
                                     TableNameProvider tableNameProvider,
                                     @Qualifier("inLinkBulkWriter") BulkWriter bulkWriter) {
        InLinkFactory inLinkFactory = new InLinkFactoryV2();
        HbaseColumnFamily table = HbaseTables.MAP_STATISTICS_CALLER_VER2_COUNTER;
        return new HbaseMapInLinkDao(mapLinkProperties, table, ignoreStatFilter, timeSlot, tableNameProvider, bulkWriter, inLinkFactory);
    }

    @Bean
    public MapOutLinkDao mapOutLinkDao(MapLinkProperties mapLinkProperties,
                                       TimeSlot timeSlot,
                                       TableNameProvider tableNameProvider,
                                       @Qualifier("outLinkBulkWriter") BulkWriter bulkWriter) {
        OutLinkFactory outLinkFactory = new OutLinkFactoryV2();
        HbaseColumnFamily table = HbaseTables.MAP_STATISTICS_CALLEE_VER2_COUNTER;
        return new HbaseMapOutLinkDao(mapLinkProperties, table, timeSlot, tableNameProvider, bulkWriter, outLinkFactory);
    }

    @Bean
    public MapResponseTimeDao mapResponseLinkDao(MapLinkProperties mapLinkProperties,
                                                 TimeSlot timeSlot,
                                                 TableNameProvider tableNameProvider,
                                                 @Qualifier("selfBulkWriter") BulkWriter bulkWriter) {
        SelfNodeFactory selfNodeFactory = new SelfNodeFactoryV2();
        HbaseColumnFamily table = HbaseTables.MAP_STATISTICS_SELF_VER2_COUNTER;
        return new HbaseMapResponseTimeDao(mapLinkProperties, table, timeSlot, tableNameProvider, bulkWriter, selfNodeFactory);
    }

    @Bean
    public HostApplicationMapDao hostApplicationMapDao(HbaseOperations hbaseTemplate,
                                                       TableNameProvider tableNameProvider,
                                                       @Qualifier("acceptApplicationRowKeyDistributor") RowKeyDistributor rowKeyDistributor,
                                                       TimeSlot timeSlot) {
        HostRowKeyEncoder encoder = new HostRowKeyEncoderV2(rowKeyDistributor.getByteHasher());
        HostLinkFactory hostLinkFactory = new HostLinkFactoryV2(encoder);
        HbaseColumnFamily table = HbaseTables.HOST_APPLICATION_MAP_VER2_MAP;
        return new HbaseHostApplicationMapDao(hbaseTemplate, table, tableNameProvider, hostLinkFactory, timeSlot);
    }
}
