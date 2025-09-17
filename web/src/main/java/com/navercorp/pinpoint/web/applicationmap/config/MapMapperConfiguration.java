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
import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.LinkRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.dao.ApplicationResponse;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ApplicationResponseTimeResultExtractor;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.HostApplicationMapper;
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
import com.navercorp.pinpoint.web.vo.ResponseTime;
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
        return (windowFunction) -> new ResponseTimeMapper(registry, rowKeyDecoder, windowFunction);
    }

    @Bean
    public ResultExtractorFactory<List<ResponseTime>> responseTimeResultExtractor(ServiceTypeRegistryService registry,
                                                                                  RowKeyDecoder<LinkRowKey> rowKeyDecoder) {
        return (windowFunction) -> new ResponseTimeResultExtractor(registry, rowKeyDecoder, windowFunction);
    }

    @Bean
    public ResultExtractorFactory<ApplicationResponse> applicationResponseTimeResultExtractor(ServiceTypeRegistryService registry,
                                                                                              RowKeyDecoder<LinkRowKey> rowKeyDecoder) {
        return (windowFunction) -> new ApplicationResponseTimeResultExtractor(registry, rowKeyDecoder, windowFunction);
    }

}
