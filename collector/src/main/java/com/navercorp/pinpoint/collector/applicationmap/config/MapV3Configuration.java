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
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.InLinkFactory;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.OutLinkFactory;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.SelfNodeFactory;
import com.navercorp.pinpoint.collector.applicationmap.dao.v3.InLinkFactoryV3;
import com.navercorp.pinpoint.collector.applicationmap.dao.v3.OutLinkFactoryV3;
import com.navercorp.pinpoint.collector.applicationmap.dao.v3.SelfNodeFactoryV3;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.dao.hbase.IgnoreStatFilter;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributor;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.collector.applicationmap.dao.v3",
})
@ConditionalOnProperty(name = "pinpoint.modules.uid.version", havingValue = "v3")
public class MapV3Configuration {
    private static final Logger logger = LogManager.getLogger(MapV3Configuration.class);

    public MapV3Configuration() {
        logger.info("Install {}", MapV3Configuration.class.getName());
    }


    @Bean
    public MapInLinkDao mapInLinkDao(MapLinkProperties mapLinkProperties,
                                     IgnoreStatFilter ignoreStatFilter,
                                     TimeSlot timeSlot,
                                     @Qualifier("inLinkBulkWriter") BulkWriter bulkWriter) {
        InLinkFactory inLinkFactory = new InLinkFactoryV3();
        return new HbaseMapInLinkDao(mapLinkProperties, ignoreStatFilter, timeSlot, bulkWriter, inLinkFactory);
    }

    @Bean
    public MapOutLinkDao mapOutLinkDao(MapLinkProperties mapLinkProperties,
                                       TimeSlot timeSlot,
                                       @Qualifier("outLinkBulkWriter") BulkWriter bulkWriter) {
        OutLinkFactory outLinkFactory = new OutLinkFactoryV3();
        return new HbaseMapOutLinkDao(mapLinkProperties, timeSlot, bulkWriter, outLinkFactory);
    }

    @Bean
    public MapResponseTimeDao mapResponseLinkDao(MapLinkProperties mapLinkProperties,
                                                 TimeSlot timeSlot,
                                                 @Qualifier("selfBulkWriter") BulkWriter bulkWriter) {
        SelfNodeFactory selfNodeFactory = new SelfNodeFactoryV3();
        return new HbaseMapResponseTimeDao(mapLinkProperties, timeSlot, bulkWriter, selfNodeFactory);
    }

    @Bean
    public HostApplicationMapDao hostApplicationMapDao(HbaseOperations hbaseTemplate,
                                                       TableNameProvider tableNameProvider,
                                                       @Qualifier("acceptApplicationRowKeyDistributor") RowKeyDistributor rowKeyDistributor,
                                                       TimeSlot timeSlot) {
        return new HbaseHostApplicationMapDao(hbaseTemplate, tableNameProvider, rowKeyDistributor, timeSlot);
    }
}
