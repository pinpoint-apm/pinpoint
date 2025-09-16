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


import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.dao.ApplicationResponse;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ApplicationResponseTimeResultExtractor;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.HostApplicationMapper;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.InLinkMapper;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.LinkFilter;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.OutLinkMapper;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ResponseTimeMapper;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ResponseTimeResultExtractor;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ResultExtractorFactory;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.RowMapperFactory;
import com.navercorp.pinpoint.web.applicationmap.map.AcceptApplication;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

@Configuration
public class MapMapperConfiguration {

    @Bean
    public ResultsExtractor<Set<AcceptApplication>> hostApplicationResultExtractor(ApplicationFactory applicationFactory) {
        return new HostApplicationMapper(applicationFactory);
    }

    @Bean
    public RowMapperFactory<LinkDataMap> mapOutLinkMapper(ApplicationFactory applicationFactory,
                                                          @Qualifier("mapLinkRowKeyDistributor")
                                                          RowKeyDistributorByHashPrefix rowKeyDistributor) {
        return (windowFunction) -> new OutLinkMapper(applicationFactory, rowKeyDistributor, LinkFilter::skip, windowFunction);
    }

    @Bean
    public RowMapperFactory<LinkDataMap> mapInLinkMapper(ServiceTypeRegistryService registry,
                                                         ApplicationFactory applicationFactory,
                                                         @Qualifier("mapLinkRowKeyDistributor")
                                                         RowKeyDistributorByHashPrefix rowKeyDistributor) {
        return (windowFunction) -> new InLinkMapper(registry, applicationFactory, rowKeyDistributor, LinkFilter::skip, windowFunction);
    }

    @Bean
    public RowMapperFactory<ResponseTime> responseTimeMapper(ServiceTypeRegistryService registry,
                                                      @Qualifier("mapSelfRowKeyDistributor")
                                                      RowKeyDistributorByHashPrefix rowKeyDistributor) {
        return (windowFunction) -> new ResponseTimeMapper(registry, rowKeyDistributor, windowFunction);
    }

    @Bean
    public ResultExtractorFactory<List<ResponseTime>> responseTimeResultExtractor(ServiceTypeRegistryService registry,
                                                                                @Qualifier("mapSelfRowKeyDistributor")
                                                      RowKeyDistributorByHashPrefix rowKeyDistributor) {
        return (windowFunction) -> new ResponseTimeResultExtractor(registry, rowKeyDistributor, windowFunction);
    }

    @Bean
    public ResultExtractorFactory<ApplicationResponse> applicationResponseTimeResultExtractor(ServiceTypeRegistryService registry,
                                                                                              @Qualifier("mapSelfRowKeyDistributor")
                                                                                RowKeyDistributorByHashPrefix rowKeyDistributor) {
        return (windowFunction) -> new ApplicationResponseTimeResultExtractor(registry, rowKeyDistributor, windowFunction);
    }

}
