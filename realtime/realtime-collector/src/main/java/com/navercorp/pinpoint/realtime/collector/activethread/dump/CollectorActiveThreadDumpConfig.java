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
package com.navercorp.pinpoint.realtime.collector.activethread.dump;

import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import com.navercorp.pinpoint.channel.legacy.DemandMessage;
import com.navercorp.pinpoint.channel.legacy.LegacyMonoBackendAdaptor;
import com.navercorp.pinpoint.channel.legacy.SupplyMessage;
import com.navercorp.pinpoint.channel.service.MonoChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.server.ChannelServiceServer;
import com.navercorp.pinpoint.realtime.collector.activethread.dump.service.ActiveThreadDumpService;
import com.navercorp.pinpoint.realtime.collector.activethread.dump.service.CollectorActiveThreadDumpServiceConfig;
import com.navercorp.pinpoint.realtime.config.ATDServiceProtocolConfig;
import com.navercorp.pinpoint.realtime.dto.ATDDemand;
import com.navercorp.pinpoint.realtime.dto.ATDSupply;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ CollectorActiveThreadDumpServiceConfig.class, ATDServiceProtocolConfig.class })
public class CollectorActiveThreadDumpConfig {

    @Bean
    ChannelServiceServer legacyATDServer(
            ChannelProviderRepository channelProviderRepository,
            MonoChannelServiceProtocol<DemandMessage<ATDDemand>, SupplyMessage<ATDSupply>> protocol,
            ActiveThreadDumpService service
    ) {
        return ChannelServiceServer.buildMono(
                channelProviderRepository,
                protocol,
                new LegacyMonoBackendAdaptor<>(service::getDump)
        );
    }

    @Bean
    ChannelServiceServer ATDServer(
            ChannelProviderRepository channelProviderRepository,
            MonoChannelServiceProtocol<ATDDemand, ATDSupply> protocol,
            ActiveThreadDumpService service
    ) {
        return ChannelServiceServer.buildMono(
                channelProviderRepository,
                protocol,
                service::getDump
        );
    }

}
