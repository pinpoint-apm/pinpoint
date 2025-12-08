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

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.controller.FilteredMapController;
import com.navercorp.pinpoint.web.applicationmap.controller.MapController;
import com.navercorp.pinpoint.web.applicationmap.controller.MapHistogramController;
import com.navercorp.pinpoint.web.applicationmap.controller.ServerMapHistogramController;
import com.navercorp.pinpoint.web.applicationmap.service.FilteredMapService;
import com.navercorp.pinpoint.web.applicationmap.service.HistogramService;
import com.navercorp.pinpoint.web.applicationmap.service.MapService;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramService;
import com.navercorp.pinpoint.web.applicationmap.service.TraceIndexService;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.filter.FilterBuilder;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.util.ApplicationValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class MapControllerConfiguration {

    @Bean
    public MapController mapController(MapService mapService,
                                       ApplicationValidator applicationValidator,
                                       ConfigProperties configProperties) {
        Duration maxPeriod = Duration.ofDays(configProperties.getServerMapPeriodMax());
        return new MapController(mapService, applicationValidator, maxPeriod);
    }

    @Bean
    public MapHistogramController mapHistogramController(ResponseTimeHistogramService responseTimeHistogramService,
                                                         HistogramService histogramService,
                                                         ApplicationFactory applicationFactory,
                                                         ApplicationValidator applicationValidator,
                                                         HyperLinkFactory hyperLinkFactory,
                                                         ConfigProperties configProperties) {
        Duration maxPeriod = Duration.ofDays(configProperties.getServerMapPeriodMax());
        return new MapHistogramController(responseTimeHistogramService, histogramService, applicationFactory, applicationValidator, hyperLinkFactory, maxPeriod);
    }

    @Bean
    public ServerMapHistogramController serverMapHistogramController(ResponseTimeHistogramService responseTimeHistogramService,
                                                                     HistogramService histogramService,
                                                                     ApplicationFactory applicationFactory,
                                                                     ServiceTypeRegistryService registry,
                                                                     ApplicationValidator applicationValidator,
                                                                     HyperLinkFactory hyperLinkFactory,
                                                                     ConfigProperties configProperties) {
        Duration maxPeriod = Duration.ofDays(configProperties.getServerMapPeriodMax());
        return new ServerMapHistogramController(responseTimeHistogramService, histogramService, applicationFactory, registry, applicationValidator, hyperLinkFactory, maxPeriod);
    }

    @Bean
    public FilteredMapController filteredMapController(FilteredMapService filteredMapService,
                                                       TraceIndexService traceIndexService,
                                                       FilterBuilder<List<SpanBo>> filterBuilder,
                                                       HyperLinkFactory hyperLinkFactory,
                                                       ServiceTypeRegistryService serviceTypeRegistryService,
                                                       @Value("${pinpoint.web.trace.index.read.v2:false}") boolean defaultReadTraceIndexV2) {
        return new FilteredMapController(filteredMapService, traceIndexService, filterBuilder, hyperLinkFactory, serviceTypeRegistryService, defaultReadTraceIndexV2);
    }
}
