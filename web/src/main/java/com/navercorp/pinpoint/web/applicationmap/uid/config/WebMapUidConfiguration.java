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

package com.navercorp.pinpoint.web.applicationmap.uid.config;

import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.hbase.wd.RangeDoubleHash;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ResultExtractorFactory;
import com.navercorp.pinpoint.web.applicationmap.uid.ApplicationUidResponse;
import com.navercorp.pinpoint.web.applicationmap.uid.hbase.HbaseMapSelfUidDao;
import com.navercorp.pinpoint.web.applicationmap.uid.hbase.MapSelfUidDao;
import com.navercorp.pinpoint.web.applicationmap.uid.mapper.ApplicationUidResponseTimeResultExtractor;
import com.navercorp.pinpoint.web.vo.RangeFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebMapUidConfiguration {

    public WebMapUidConfiguration() {
    }

    @Bean
    public RowKeyDistributorByHashPrefix mapSelfUidRowKeyDistributor() {
        ByteHasher hasher = RangeDoubleHash.ofSecondary(0, 16, ByteHasher.MAX_BUCKETS, 4, 16, 20);
        return new RowKeyDistributorByHashPrefix(hasher);
    }


    @Bean
    public ResultExtractorFactory<ApplicationUidResponse> applicationUidResponseExtractor(ServiceTypeRegistryService registry,
                                                                                          @Qualifier("mapSelfUidRowKeyDistributor")
                                                                                    RowKeyDistributorByHashPrefix rowKeyDistributor) {
        return (timeWindowFunction) -> new ApplicationUidResponseTimeResultExtractor(registry, rowKeyDistributor, timeWindowFunction);
    }

    @Bean
    public MapSelfUidDao mapSelfUidDao(@Qualifier("mapHbaseTemplate")
                                       HbaseOperations hbaseOperations,
                                       TableNameProvider tableNameProvider,
                                       RangeFactory rangeFactory,
                                       @Qualifier("mapSelfUidRowKeyDistributor")
                                       RowKeyDistributorByHashPrefix rowKeyDistributor,
                                       ResultExtractorFactory<ApplicationUidResponse> resultExtractor) {
        return new HbaseMapSelfUidDao(hbaseOperations, tableNameProvider, rangeFactory, rowKeyDistributor, resultExtractor);
    }
}
