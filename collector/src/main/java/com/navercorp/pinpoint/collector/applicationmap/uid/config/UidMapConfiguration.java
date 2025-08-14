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
import com.navercorp.pinpoint.collector.applicationmap.uid.MapSelfUidDao;
import com.navercorp.pinpoint.collector.applicationmap.uid.hbase.HbaseMapSelfUidDao;
import com.navercorp.pinpoint.collector.applicationmap.uid.service.HbaseUidLinkService;
import com.navercorp.pinpoint.collector.service.UidLinkService;
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

    @Bean
    public RowKeyDistributorByHashPrefix mapSelfUidRowKeyDistributor() {
        ByteHasher hasher = RangeDoubleHash.ofSecondary(0, 16, ByteHasher.MAX_BUCKETS, 4, 16, 20);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public BulkWriter selfUidBulkWriter(BulkFactory factory,
                                        HbaseAsyncTemplate asyncTemplate,
                                        TableNameProvider tableNameProvider,
                                        @Qualifier("mapSelfUidRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                        @Qualifier("selfBulkIncrementer") BulkIncrementer bulkIncrementer,
                                        @Qualifier("selfBulkUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(HbaseMapSelfUidDao.class.getName());
        return factory.newBulkWriter(loggerName, asyncTemplate, HbaseTables.MAP_SELF_V3_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }

    private String newBulkWriterName(String className) {
        return className + "-writer";
    }

    @Bean
    public MapSelfUidDao mapSelfUidDao(MapLinkProperties mapLinkProperties,
                                       TimeSlot timeSlot,
                                       @Qualifier("selfUidBulkWriter") BulkWriter bulkWriter) {
        return new HbaseMapSelfUidDao(mapLinkProperties, timeSlot, bulkWriter);
    }

    @Bean
    public UidLinkService uidLinkService(MapSelfUidDao mapSelfUidDao) {
        return new HbaseUidLinkService(mapSelfUidDao);
    }

}
