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
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidLinkRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.uid.ObjectNameVersion;
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
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.HostApplicationMapper;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.HostScanKeyFactory;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.LinkFilter;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.LinkRowKeyDecoder;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ResultExtractorFactory;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.RowMapperFactory;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.ApplicationResponseTimeV3ResultExtractor;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.HostScanKeyFactoryV3;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.InLinkV3Mapper;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.MapScanKeyFactoryV3;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.OutLinkV3Mapper;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.ResponseTimeV3Mapper;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.ResponseTimeV3ResultExtractor;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.UidLinkRowKeyDecoder;
import com.navercorp.pinpoint.web.applicationmap.map.AcceptApplication;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.RangeFactory;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

@Configuration
@ConditionalOnProperty(name = ObjectNameVersion.KEY, havingValue = "v3")
public class MapV3MapperConfiguration {

    private static final Logger logger = LogManager.getLogger(MapV3MapperConfiguration.class);

    public MapV3MapperConfiguration() {
        logger.info("Install {}", MapV3MapperConfiguration.class.getSimpleName());
    }

    @Bean
    public ResultsExtractor<Set<AcceptApplication>> hostApplicationResultExtractor(ApplicationFactory applicationFactory) {
        return new HostApplicationMapper(applicationFactory);
    }

    @Bean
    public RowKeyDecoder<UidLinkRowKey> uidLinkRowKeyDecoder() {
        return new UidLinkRowKeyDecoder(ByteSaltKey.SALT.size());
    }

    @Bean
    public RowKeyDecoder<LinkRowKey> linkRowKeyRowKeyDecoder() {
        return new LinkRowKeyDecoder(ByteSaltKey.SALT.size());
    }

    @Bean
    public RowMapperFactory<LinkDataMap> mapOutLinkMapper(ApplicationFactory applicationFactory,
                                                          RowKeyDecoder<UidLinkRowKey> rowKeyDecoder) {
        return (windowFunction) -> new OutLinkV3Mapper(applicationFactory, rowKeyDecoder, LinkFilter::skip, windowFunction);
    }

    @Bean
    public RowMapperFactory<LinkDataMap> mapInLinkMapper(ServiceTypeRegistryService registry,
                                                         ApplicationFactory applicationFactory,
                                                         RowKeyDecoder<UidLinkRowKey> rowKeyDecoder) {
        return (windowFunction) -> new InLinkV3Mapper(registry, applicationFactory, rowKeyDecoder, LinkFilter::skip, windowFunction);
    }

    @Bean
    public RowMapperFactory<ResponseTime> responseTimeMapper(ServiceTypeRegistryService registry,
                                                             RowKeyDecoder<UidLinkRowKey> rowKeyDecoder) {
        HbaseColumnFamily table = HbaseTables.MAP_APP_SELF;
        return (windowFunction) -> new ResponseTimeV3Mapper(table, registry, rowKeyDecoder, windowFunction);
    }

    @Bean
    public ResultExtractorFactory<List<ResponseTime>> responseTimeResultExtractor(ServiceTypeRegistryService registry,
                                                                                  RowKeyDecoder<UidLinkRowKey> rowKeyDecoder) {
        HbaseColumnFamily table = HbaseTables.MAP_APP_SELF;
        return (windowFunction) -> new ResponseTimeV3ResultExtractor(table, registry, rowKeyDecoder, windowFunction);
    }

    @Bean
    public ResultExtractorFactory<ApplicationResponse> applicationResponseTimeResultExtractor(ServiceTypeRegistryService registry,
                                                                                              RowKeyDecoder<UidLinkRowKey> rowKeyDecoder) {
        HbaseColumnFamily table = HbaseTables.MAP_APP_SELF;
        return (windowFunction) -> new ApplicationResponseTimeV3ResultExtractor(table, registry, rowKeyDecoder, windowFunction);
    }


    @Bean
    public MapScanFactory mapScanFactory(RangeFactory rangeFactory) {
        MapScanKeyFactory mapScanKeyFactory = new MapScanKeyFactoryV3();
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
                                         @Qualifier("uidRowKeyDistributor")
                                         RowKeyDistributorByHashPrefix rowKeyDistributor) {
        HbaseColumnFamily table = HbaseTables.MAP_APP_SELF;
        return new HbaseMapResponseTimeDao(table, hbaseTemplate, tableNameProvider, resultExtractFactory, applicationHistogramResultExtractor, mapScanFactory, rowKeyDistributor);
    }

    @Bean
    public MapInLinkDao mapInLinkDao(@Qualifier("mapHbaseTemplate")
                                     HbaseTemplate hbaseTemplate,
                                     TableNameProvider tableNameProvider,
                                     @Qualifier("mapInLinkMapper")
                                     RowMapperFactory<LinkDataMap> inLinkMapper,
                                     MapScanFactory mapScanFactory,
                                     @Qualifier("uidRowKeyDistributor")
                                     RowKeyDistributorByHashPrefix rowKeyDistributor) {
        HbaseColumnFamily table = HbaseTables.MAP_APP_IN;
        return new HbaseMapInLinkDao(table, hbaseTemplate, tableNameProvider, inLinkMapper, mapScanFactory, rowKeyDistributor);
    }

    @Bean
    public MapOutLinkDao mapOutLinkDao(@Qualifier("mapHbaseTemplate")
                                       HbaseTemplate hbaseTemplate,
                                       TableNameProvider tableNameProvider,
                                       @Qualifier("mapOutLinkMapper")
                                       RowMapperFactory<LinkDataMap> outLinkMapper,
                                       MapScanFactory mapScanFactory,
                                       @Qualifier("uidRowKeyDistributor")
                                       RowKeyDistributorByHashPrefix rowKeyDistributor) {
        HbaseColumnFamily table = HbaseTables.MAP_APP_OUT;
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
        HbaseColumnFamily table = HbaseTables.MAP_APP_HOST;
        HostScanKeyFactory hostScanKeyFactory = new HostScanKeyFactoryV3();
        return new HbaseHostApplicationMapDao(table, hbaseOperations, tableNameProvider, hostScanKeyFactory, hostApplicationResultExtractor, timeSlot, acceptApplicationRowKeyDistributor);
    }
}
