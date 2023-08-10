/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.realtime.collector.activethread.count;

import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import com.navercorp.pinpoint.channel.legacy.DemandMessage;
import com.navercorp.pinpoint.channel.legacy.LegacyFluxBackendAdaptor;
import com.navercorp.pinpoint.channel.legacy.SupplyMessage;
import com.navercorp.pinpoint.channel.service.FluxChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.server.ChannelServiceServer;
import com.navercorp.pinpoint.realtime.collector.activethread.count.service.ActiveThreadCountService;
import com.navercorp.pinpoint.realtime.collector.activethread.count.service.CollectorActiveThreadCountServiceConfig;
import com.navercorp.pinpoint.realtime.config.ATCServiceProtocolConfig;
import com.navercorp.pinpoint.realtime.dto.ATCDemand;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ CollectorActiveThreadCountServiceConfig.class, ATCServiceProtocolConfig.class })
public class CollectorActiveThreadCountConfig {

    @Bean
    ChannelServiceServer legacyATCServer(
            ChannelProviderRepository channelProviderRepository,
            FluxChannelServiceProtocol<DemandMessage<ATCDemand>, SupplyMessage<ATCSupply>> protocol,
            ActiveThreadCountService service
    ) {
        return ChannelServiceServer.buildFlux(
                channelProviderRepository,
                protocol,
                new LegacyFluxBackendAdaptor<>(service::requestAsync)
        );
    }

    @Bean
    ChannelServiceServer ATCServer(
            ChannelProviderRepository channelProviderRepository,
            FluxChannelServiceProtocol<ATCDemand, ATCSupply> protocol,
            ActiveThreadCountService service
    ) {
        return ChannelServiceServer.buildFlux(
                channelProviderRepository,
                protocol,
                service::requestAsync
        );
    }

}
