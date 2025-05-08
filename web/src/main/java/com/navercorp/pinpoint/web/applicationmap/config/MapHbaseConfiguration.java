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

package com.navercorp.pinpoint.web.applicationmap.config;

import com.navercorp.pinpoint.common.hbase.ConnectionFactoryBean;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate;
import com.navercorp.pinpoint.common.hbase.TableFactory;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.async.AsyncConnectionFactoryBean;
import com.navercorp.pinpoint.common.hbase.async.AsyncTableCustomizer;
import com.navercorp.pinpoint.common.hbase.async.AsyncTableFactory;
import com.navercorp.pinpoint.common.hbase.async.HbaseAsyncTemplate;
import com.navercorp.pinpoint.common.hbase.config.HbaseTemplateConfiguration;
import com.navercorp.pinpoint.common.hbase.config.ParallelScan;
import com.navercorp.pinpoint.common.hbase.scan.ResultScannerFactory;
import com.navercorp.pinpoint.common.hbase.util.ScanMetricReporter;
import com.navercorp.pinpoint.common.server.executor.ExecutorCustomizer;
import com.navercorp.pinpoint.common.server.executor.ExecutorProperties;
import com.navercorp.pinpoint.web.applicationmap.dao.MapInLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.HbaseMapInLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.HbaseMapOutLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.HbaseMapResponseTimeDao;
import com.navercorp.pinpoint.web.applicationmap.dao.hbase.MapScanFactory;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ResultExtractorFactory;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.RowMapperFactory;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.vo.RangeFactory;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.security.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@org.springframework.context.annotation.Configuration
@Import({
        MapMapperConfiguration.class
})
public class MapHbaseConfiguration {
    private final Logger logger = LogManager.getLogger(MapHbaseConfiguration.class);

    private final HbaseTemplateConfiguration config = new HbaseTemplateConfiguration();

    public MapHbaseConfiguration() {
        logger.info("Install {}", MapHbaseConfiguration.class.getSimpleName());
    }

    @Bean
    public FactoryBean<ExecutorService> mapHbaseThreadPool(@Qualifier("hbaseExecutorCustomizer") ExecutorCustomizer<ThreadPoolExecutorFactoryBean> executorCustomizer,
                                                           @Qualifier("hbaseClientExecutorProperties") ExecutorProperties properties) {
        ThreadPoolExecutorFactoryBean factory = new ThreadPoolExecutorFactoryBean();
        executorCustomizer.customize(factory, properties);
        factory.setThreadNamePrefix("Map-" + factory.getThreadNamePrefix());
        return factory;
    }

    @Bean
    public FactoryBean<Connection> mapHbaseConnection(Configuration configuration,
                                                      User user,
                                                      @Qualifier("mapHbaseThreadPool") ExecutorService executorService) {
        return new ConnectionFactoryBean(configuration, user, executorService);
    }

    @Bean
    public FactoryBean<AsyncConnection> mapHbaseAsyncConnection(Configuration configuration, User user) {
        return new AsyncConnectionFactoryBean(configuration, user);
    }


    @Bean
    public TableFactory mapHbaseTableFactory(@Qualifier("hbaseConnection") Connection connection) {
        return config.hbaseTableFactory(connection);
    }

    @Bean
    public AsyncTableFactory mapHbaseAsyncTableFactory(@Qualifier("hbaseAsyncConnection") AsyncConnection connection, AsyncTableCustomizer customizer) {
        return config.hbaseAsyncTableFactory(connection, customizer);
    }

    @Bean
    public HbaseAsyncTemplate mapHbaseAsyncTemplate(@Qualifier("mapHbaseAsyncTableFactory")
                                                    AsyncTableFactory tableFactory,
                                                    ScanMetricReporter scanMetricReporter,
                                                    ResultScannerFactory resultScannerFactory) {
        return config.asyncTemplate(tableFactory, scanMetricReporter, resultScannerFactory);
    }

    @Bean
    public HbaseTemplate mapHbaseTemplate(@Qualifier("hbaseConfiguration") Configuration configurable,
                                          @Qualifier("mapHbaseTableFactory") TableFactory tableFactory,
                                          @Qualifier("mapHbaseAsyncTemplate") HbaseAsyncTemplate asyncTemplate,
                                          Optional<ParallelScan> parallelScan,
                                          @Value("${hbase.client.nativeAsync:false}") boolean nativeAsync,
                                          ResultScannerFactory resultScannerFactory,
                                          ScanMetricReporter reporter) {
        return config.hbaseTemplate(configurable, tableFactory, asyncTemplate, parallelScan, nativeAsync, resultScannerFactory, reporter);
    }

    @Bean
    public MapScanFactory mapScanFactory(RangeFactory rangeFactory) {
        return new MapScanFactory(rangeFactory);
    }

    @Bean
    public MapResponseDao mapResponseDao(@Qualifier("mapHbaseTemplate")
                                         HbaseTemplate hbaseTemplate,
                                         TableNameProvider tableNameProvider,
                                         @Qualifier("responseTimeResultExtractor")
                                         ResultExtractorFactory<List<ResponseTime>> resultExtractFactory,
                                         @Qualifier("applicationResponseTimeResultExtractor")
                                         ResultExtractorFactory<ApplicationHistogram> applicationHistogramResultExtractor,
                                         MapScanFactory mapScanFactory,
                                         @Qualifier("mapSelfRowKeyDistributor")
                                         RowKeyDistributorByHashPrefix rowKeyDistributor) {
        return new HbaseMapResponseTimeDao(hbaseTemplate, tableNameProvider, resultExtractFactory, applicationHistogramResultExtractor, mapScanFactory, rowKeyDistributor);
    }

    @Bean
    public MapInLinkDao mapInLinkDao(@Qualifier("mapHbaseTemplate")
                                     HbaseTemplate hbaseTemplate,
                                     TableNameProvider tableNameProvider,
                                     @Qualifier("mapInLinkMapper")
                                     RowMapperFactory<LinkDataMap> inLinkMapper,
                                     MapScanFactory mapScanFactory,
                                     @Qualifier("mapInLinkRowKeyDistributor")
                                     RowKeyDistributorByHashPrefix rowKeyDistributor) {
        return new HbaseMapInLinkDao(hbaseTemplate, tableNameProvider, inLinkMapper, mapScanFactory, rowKeyDistributor);
    }

    @Bean
    public MapOutLinkDao mapOutLinkDao(@Qualifier("mapHbaseTemplate")
                                       HbaseTemplate hbaseTemplate,
                                       TableNameProvider tableNameProvider,
                                       @Qualifier("mapOutLinkMapper")
                                       RowMapperFactory<LinkDataMap> outLinkMapper,
                                       MapScanFactory mapScanFactory,
                                       @Qualifier("mapOutLinkRowKeyDistributor")
                                       RowKeyDistributorByHashPrefix rowKeyDistributor) {
        return new HbaseMapOutLinkDao(hbaseTemplate, tableNameProvider, outLinkMapper, mapScanFactory, rowKeyDistributor);
    }
}

