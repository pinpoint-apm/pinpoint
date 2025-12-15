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
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidAgentRowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidAppRowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidLinkRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.uid.ObjectNameVersion;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.dao.ApplicationResponse;
import com.navercorp.pinpoint.web.applicationmap.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.applicationmap.dao.MapInLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.HbaseHostApplicationMapDao;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.HbaseMapAgentResponseTimeDao;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.HbaseMapInLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.HbaseMapOutLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.HbaseMapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.MapScanFactory;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.MapScanKeyFactory;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.HostApplicationMapper;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.HostScanKeyFactory;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.LinkFilter;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ResultExtractorFactory;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.RowMapperFactory;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.ApplicationResponseTimeV3ResultExtractor;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.HostApplicationMapperV3;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.HostScanKeyFactoryV3;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.InLinkV3Mapper;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.MapAgentScanKeyFactoryV3;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.MapAppScanKeyFactoryV3;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.MapLinkScanKeyFactoryV3;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.OutLinkV3Mapper;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.ResponseTimeV3ResultExtractor;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.RowFilter;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.UidAgentLinkRowKeyDecoder;
import com.navercorp.pinpoint.web.applicationmap.dao.v3.UidAppRowKeyDecoder;
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
import java.util.function.Predicate;

@Configuration
@ConditionalOnProperty(name = ObjectNameVersion.KEY, havingValue = "v3")
public class MapV3DaoConfiguration {

    private static final Logger logger = LogManager.getLogger(MapV3DaoConfiguration.class);

    private static final ByteSaltKey SALT_KEY = ByteSaltKey.SALT;

    public MapV3DaoConfiguration() {
        logger.info("Install {}", MapV3DaoConfiguration.class.getSimpleName());
    }

    @Bean
    public ResultsExtractor<Set<AcceptApplication>> hostApplicationResultExtractor(ApplicationFactory applicationFactory) {
        return new HostApplicationMapper(applicationFactory);
    }

    @Bean
    public RowKeyDecoder<UidLinkRowKey> uidLinkRowKeyDecoder() {
        return new UidLinkRowKeyDecoder(SALT_KEY.size());
    }

    @Bean
    public RowKeyDecoder<UidAgentRowKey> uidAgentIdLinkRowKeyDecoder() {
        return new UidAgentLinkRowKeyDecoder(SALT_KEY.size());
    }

    @Bean
    public RowKeyDecoder<UidAppRowKey> uidAppRowKeyDecoder() {
        return new UidAppRowKeyDecoder(SALT_KEY.size());
    }

    @Bean
    public RowMapperFactory<LinkDataMap> mapOutLinkMapper(ApplicationFactory applicationFactory,
                                                          RowKeyDecoder<UidLinkRowKey> rowKeyDecoder) {
        return (timeWindow, application) -> {
            Predicate<UidLinkRowKey> rowFilter = new RowFilter<>(application);
            return new OutLinkV3Mapper(applicationFactory, rowKeyDecoder, LinkFilter::skip, timeWindow, rowFilter);
        };
    }

    @Bean
    public RowMapperFactory<LinkDataMap> mapInLinkMapper(ServiceTypeRegistryService registry,
                                                         ApplicationFactory applicationFactory,
                                                         RowKeyDecoder<UidLinkRowKey> rowKeyDecoder) {
        return (timeWindow, application) -> {
            Predicate<UidLinkRowKey> rowFilter = new RowFilter<>(application);
            return new InLinkV3Mapper(registry, applicationFactory, rowKeyDecoder, LinkFilter::skip, timeWindow, rowFilter);
        };
    }

    @Bean
    public ResultExtractorFactory<List<ResponseTime>> agentResponseTimeResultExtractor(ServiceTypeRegistryService registry,
                                                                                       RowKeyDecoder<UidAgentRowKey> rowKeyDecoder) {
        HbaseColumnFamily table = HbaseTables.MAP_AGENT_SELF;

        return (windowFunction, application) -> {
            Predicate<UidAgentRowKey> rowFilter = new RowFilter<>(application);
            return new ResponseTimeV3ResultExtractor(table, registry, rowKeyDecoder, windowFunction, rowFilter);
        };
    }

    @Bean
    public ResultExtractorFactory<ApplicationResponse> applicationResponseTimeResultExtractor(ServiceTypeRegistryService registry,
                                                                                                 RowKeyDecoder<UidAppRowKey> rowKeyDecoder) {
        HbaseColumnFamily table = HbaseTables.MAP_APP_SELF;
        return (windowFunction, application) -> {
            Predicate<UidAppRowKey> rowFilter = new RowFilter<>(application);
            return new ApplicationResponseTimeV3ResultExtractor(table, registry, rowKeyDecoder, windowFunction, rowFilter);
        };
    }


    @Bean
    public MapScanFactory mapLinkScanFactory(RangeFactory rangeFactory) {
        MapScanKeyFactory mapScanKeyFactory = new MapLinkScanKeyFactoryV3();
        return new MapScanFactory(rangeFactory, mapScanKeyFactory);
    }

    @Bean
    public MapScanFactory mapAppScanFactory(RangeFactory rangeFactory) {
        MapScanKeyFactory mapScanKeyFactory = new MapAppScanKeyFactoryV3();
        return new MapScanFactory(rangeFactory, mapScanKeyFactory);
    }

    @Bean
    public MapScanFactory mapAgentScanFactory(RangeFactory rangeFactory) {
        MapScanKeyFactory mapScanKeyFactory = new MapAgentScanKeyFactoryV3();
        return new MapScanFactory(rangeFactory, mapScanKeyFactory);
    }

    @Bean
    public MapAgentResponseDao mapAgentResponseDao(@Qualifier("mapHbaseTemplate")
                                              HbaseTemplate hbaseTemplate,
                                              TableNameProvider tableNameProvider,
                                              @Qualifier("agentResponseTimeResultExtractor")
                                              ResultExtractorFactory<List<ResponseTime>> resultExtractFactory,
                                              @Qualifier("mapAgentScanFactory")
                                              MapScanFactory mapScanFactory,
                                              @Qualifier("uidRowKeyDistributor")
                                              RowKeyDistributorByHashPrefix rowKeyDistributor) {
        HbaseColumnFamily table = HbaseTables.MAP_AGENT_SELF;
        return new HbaseMapAgentResponseTimeDao(table, hbaseTemplate, tableNameProvider, resultExtractFactory, mapScanFactory, rowKeyDistributor);
    }


    @Bean
    public MapResponseDao mapResponseDao(@Qualifier("mapHbaseTemplate")
                                                    HbaseTemplate hbaseTemplate,
                                                    TableNameProvider tableNameProvider,
                                                    @Qualifier("applicationResponseTimeResultExtractor")
                                                    ResultExtractorFactory<ApplicationResponse> resultExtractFactory,
                                                    @Qualifier("mapAppScanFactory")
                                                    MapScanFactory mapScanFactory,
                                                    @Qualifier("uidRowKeyDistributor")
                                                    RowKeyDistributorByHashPrefix rowKeyDistributor) {
        HbaseColumnFamily table = HbaseTables.MAP_APP_SELF;
        return new HbaseMapResponseDao(table, hbaseTemplate, tableNameProvider, resultExtractFactory, mapScanFactory, rowKeyDistributor);
    }

    @Bean
    public MapInLinkDao mapInLinkDao(@Qualifier("mapHbaseTemplate")
                                     HbaseTemplate hbaseTemplate,
                                     TableNameProvider tableNameProvider,
                                     @Qualifier("mapInLinkMapper")
                                     RowMapperFactory<LinkDataMap> inLinkMapper,
                                     @Qualifier("mapLinkScanFactory")
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
                                       @Qualifier("mapLinkScanFactory")
                                       MapScanFactory mapScanFactory,
                                       @Qualifier("uidRowKeyDistributor")
                                       RowKeyDistributorByHashPrefix rowKeyDistributor) {
        HbaseColumnFamily table = HbaseTables.MAP_APP_OUT;
        return new HbaseMapOutLinkDao(table, hbaseTemplate, tableNameProvider, outLinkMapper, mapScanFactory, rowKeyDistributor);
    }

    @Bean
    public ResultsExtractor<Set<AcceptApplication>> hostApplicationResultExtractorV3(ApplicationFactory applicationFactory,
                                                                                     @Qualifier("uidRowKeyDistributor")
                                                                                     RowKeyDistributor rowKeyDistributor) {
        return new HostApplicationMapperV3(applicationFactory, rowKeyDistributor);
    }

    @Bean
    public HostApplicationMapDao hostApplicationMapDao(HbaseOperations hbaseOperations,
                                                       TableNameProvider tableNameProvider,
                                                       @Qualifier("hostApplicationResultExtractorV3")
                                                       ResultsExtractor<Set<AcceptApplication>> hostApplicationResultExtractor,
                                                       TimeSlot timeSlot,
                                                       @Qualifier("uidRowKeyDistributor")
                                                       RowKeyDistributor rowKeyDistributor) {
        HbaseColumnFamily table = HbaseTables.MAP_APP_HOST;
        HostScanKeyFactory hostScanKeyFactory = new HostScanKeyFactoryV3();
        return new HbaseHostApplicationMapDao(table, hbaseOperations, tableNameProvider, hostScanKeyFactory, hostApplicationResultExtractor, timeSlot, rowKeyDistributor);
    }
}
