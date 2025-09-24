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

package com.navercorp.pinpoint.web.applicationmap.config;


import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributor;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.LinkRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.dao.ApplicationResponse;
import com.navercorp.pinpoint.web.applicationmap.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.applicationmap.dao.MapInLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.HbaseHostApplicationMapDao;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.HbaseMapInLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.HbaseMapOutLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.HbaseMapResponseTimeDao;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.MapScanFactory;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.MapScanKeyFactory;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.MapScanKeyFactoryV2;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ApplicationResponseTimeResultExtractor;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.HostApplicationMapper;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.HostScanKeyFactory;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.HostScanKeyFactoryV2;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.InLinkMapper;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.LinkFilter;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.LinkRowKeyDecoder;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.OutLinkMapper;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ResponseTimeMapper;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ResponseTimeResultExtractor;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ResultExtractorFactory;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.RowMapperFactory;
import com.navercorp.pinpoint.web.applicationmap.map.AcceptApplication;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.RangeFactory;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

@Configuration
@ConditionalOnMissingBean(MapV3MapperConfiguration.class)
public class MapMapperConfiguration {
    private static final Logger logger = LogManager.getLogger(MapMapperConfiguration.class);

    public MapMapperConfiguration() {
        logger.info("Install {}", MapMapperConfiguration.class.getSimpleName());
    }

    @Bean
    public ResultsExtractor<Set<AcceptApplication>> hostApplicationResultExtractor(ApplicationFactory applicationFactory) {
        return new HostApplicationMapper(applicationFactory);
    }

    @Bean
    public RowKeyDecoder<LinkRowKey> linkRowKeyRowKeyDecoder() {
        return new LinkRowKeyDecoder(ByteSaltKey.SALT.size());
    }

    @Bean
    public RowMapperFactory<LinkDataMap> mapOutLinkMapper(ApplicationFactory applicationFactory,
                                                          RowKeyDecoder<LinkRowKey> rowKeyDecoder) {
        return (windowFunction) -> new OutLinkMapper(applicationFactory, rowKeyDecoder, LinkFilter::skip, windowFunction);
    }

    @Bean
    public RowMapperFactory<LinkDataMap> mapInLinkMapper(ServiceTypeRegistryService registry,
                                                         ApplicationFactory applicationFactory,
                                                         RowKeyDecoder<LinkRowKey> rowKeyDecoder) {
        return (windowFunction) -> new InLinkMapper(registry, applicationFactory, rowKeyDecoder, LinkFilter::skip, windowFunction);
    }

    @Bean
    public RowMapperFactory<ResponseTime> responseTimeMapper(ServiceTypeRegistryService registry,
                                                             RowKeyDecoder<LinkRowKey> rowKeyDecoder) {
        HbaseColumnFamily table = HbaseTables.MAP_STATISTICS_SELF_VER2_COUNTER;
        return (windowFunction) -> new ResponseTimeMapper(table, registry, rowKeyDecoder, windowFunction);
    }

    @Bean
    public ResultExtractorFactory<List<ResponseTime>> responseTimeResultExtractor(ServiceTypeRegistryService registry,
                                                                                  RowKeyDecoder<LinkRowKey> rowKeyDecoder) {
        HbaseColumnFamily table = HbaseTables.MAP_STATISTICS_SELF_VER2_COUNTER;
        return (windowFunction) -> new ResponseTimeResultExtractor(table, registry, rowKeyDecoder, windowFunction);
    }

    @Bean
    public ResultExtractorFactory<ApplicationResponse> applicationResponseTimeResultExtractor(ServiceTypeRegistryService registry,
                                                                                              RowKeyDecoder<LinkRowKey> rowKeyDecoder) {
        HbaseColumnFamily table = HbaseTables.MAP_STATISTICS_SELF_VER2_COUNTER;
        return (windowFunction) -> new ApplicationResponseTimeResultExtractor(table, registry, rowKeyDecoder, windowFunction);
    }

    @Bean
    public MapScanFactory mapScanFactory(RangeFactory rangeFactory) {
        MapScanKeyFactory mapScanKeyFactory = new MapScanKeyFactoryV2();
        return new MapScanFactory(rangeFactory, mapScanKeyFactory);
    }

    @Bean
    public MapResponseDao mapResponseDao(@Qualifier("mapHbaseTemplate")
                                         HbaseTemplate hbaseTemplate,
                                         TableNameProvider tableNameProvider,
                                         @Qualifier("responseTimeResultExtractor")
                                         ResultExtractorFactory<List<ResponseTime>> resultExtractFactory,
                                         @Qualifier("applicationResponseTimeResultExtractor")
                                         ResultExtractorFactory<ApplicationResponse> applicationHistogramResultExtractor,
                                         MapScanFactory mapScanFactory,
                                         @Qualifier("mapSelfRowKeyDistributor")
                                         RowKeyDistributorByHashPrefix rowKeyDistributor) {
        HbaseColumnFamily table = HbaseTables.MAP_STATISTICS_SELF_VER2_COUNTER;
        return new HbaseMapResponseTimeDao(table, hbaseTemplate, tableNameProvider, resultExtractFactory, applicationHistogramResultExtractor, mapScanFactory, rowKeyDistributor);
    }

    @Bean
    public MapInLinkDao mapInLinkDao(@Qualifier("mapHbaseTemplate")
                                     HbaseTemplate hbaseTemplate,
                                     TableNameProvider tableNameProvider,
                                     @Qualifier("mapInLinkMapper")
                                     RowMapperFactory<LinkDataMap> inLinkMapper,
                                     MapScanFactory mapScanFactory,
                                     @Qualifier("mapLinkRowKeyDistributor")
                                     RowKeyDistributorByHashPrefix rowKeyDistributor) {
        HbaseColumnFamily table = HbaseTables.MAP_STATISTICS_CALLER_VER2_COUNTER;
        return new HbaseMapInLinkDao(table, hbaseTemplate, tableNameProvider, inLinkMapper, mapScanFactory, rowKeyDistributor);
    }

    @Bean
    public MapOutLinkDao mapOutLinkDao(@Qualifier("mapHbaseTemplate")
                                       HbaseTemplate hbaseTemplate,
                                       TableNameProvider tableNameProvider,
                                       @Qualifier("mapOutLinkMapper")
                                       RowMapperFactory<LinkDataMap> outLinkMapper,
                                       MapScanFactory mapScanFactory,
                                       @Qualifier("mapLinkRowKeyDistributor")
                                       RowKeyDistributorByHashPrefix rowKeyDistributor) {
        HbaseColumnFamily table = HbaseTables.MAP_STATISTICS_CALLEE_VER2_COUNTER;
        return new HbaseMapOutLinkDao(table, hbaseTemplate, tableNameProvider, outLinkMapper, mapScanFactory, rowKeyDistributor);
    }

    @Bean
    public HostApplicationMapDao hostApplicationMapDao(HbaseOperations hbaseOperations,
                                                       TableNameProvider tableNameProvider,
                                                       @Qualifier("hostApplicationResultExtractor")
                                                       ResultsExtractor<Set<AcceptApplication>> hostApplicationResultExtractor,
                                                       TimeSlot timeSlot,
                                                       @Qualifier("acceptApplicationRowKeyDistributor")
                                                       RowKeyDistributor acceptApplicationRowKeyDistributor) {
        HbaseColumnFamily table = HbaseTables.HOST_APPLICATION_MAP_VER2_MAP;
        HostScanKeyFactory hostScanKeyFactory = new HostScanKeyFactoryV2();
        return new HbaseHostApplicationMapDao(table, hbaseOperations, tableNameProvider, hostScanKeyFactory, hostApplicationResultExtractor, timeSlot, acceptApplicationRowKeyDistributor);
    }

}
