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
import com.navercorp.pinpoint.collector.applicationmap.dao.HostApplicationMapDaoDelegate;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapApplicationResponseDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapInLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.service.ApplicationMapService;
import com.navercorp.pinpoint.collector.applicationmap.service.HbaseApplicationMapService;
import com.navercorp.pinpoint.collector.applicationmap.service.LinkService;
import com.navercorp.pinpoint.collector.applicationmap.service.LinkServiceImpl;
import com.navercorp.pinpoint.collector.applicationmap.statistics.config.BulkConfiguration;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

@Configuration
@Import(value = {
        BulkConfiguration.class,

        MapLinkProperties.class,

        MapV3Configuration.class
})
public class ApplicationMapModule {
    private static final Logger logger = LogManager.getLogger(ApplicationMapModule.class);


    public ApplicationMapModule() {
        logger.info("Install {}", ApplicationMapModule.class.getName());
    }

    @Bean
    @Validated
    public LinkService statisticsService(MapInLinkDao inLinkDao,
                                         MapOutLinkDao outLinkDao,
                                         MapAgentResponseDao responseAgentTimeDao,
                                         MapApplicationResponseDao responseTimeDao) {
        return new LinkServiceImpl(inLinkDao, outLinkDao, responseAgentTimeDao, responseTimeDao);
    }

    @Bean
    public ApplicationMapService applicationMapService(HostApplicationMapDao[] hostApplicationMapDaos,
                                                       LinkService linkService,
                                                       ServiceTypeRegistryService registry) {
        HostApplicationMapDao hostApplicationMapDao = deleageHostApplicationMapDao(hostApplicationMapDaos);
        return new HbaseApplicationMapService(hostApplicationMapDao, linkService, registry);
    }

    private HostApplicationMapDao deleageHostApplicationMapDao(HostApplicationMapDao[] hostApplicationMapDaos) {
        Objects.requireNonNull(hostApplicationMapDaos, "hostApplicationMapDaos");
        if (hostApplicationMapDaos.length == 1) {
            return hostApplicationMapDaos[0];
        } else {
            return new HostApplicationMapDaoDelegate(hostApplicationMapDaos);
        }
    }


}
