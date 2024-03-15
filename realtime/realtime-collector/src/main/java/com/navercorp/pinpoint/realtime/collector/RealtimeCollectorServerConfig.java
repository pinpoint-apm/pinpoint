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
package com.navercorp.pinpoint.realtime.collector;

import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import com.navercorp.pinpoint.channel.legacy.DemandMessage;
import com.navercorp.pinpoint.channel.legacy.LegacyFluxBackendAdaptor;
import com.navercorp.pinpoint.channel.legacy.LegacyMonoBackendAdaptor;
import com.navercorp.pinpoint.channel.legacy.SupplyMessage;
import com.navercorp.pinpoint.channel.service.FluxChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.MonoChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.server.ChannelServiceServer;
import com.navercorp.pinpoint.realtime.collector.service.ActiveThreadCountService;
import com.navercorp.pinpoint.realtime.collector.service.ActiveThreadDumpService;
import com.navercorp.pinpoint.realtime.collector.service.EchoService;
import com.navercorp.pinpoint.realtime.config.ATCServiceProtocolConfig;
import com.navercorp.pinpoint.realtime.config.ATDServiceProtocolConfig;
import com.navercorp.pinpoint.realtime.config.EchoServiceProtocolConfig;
import com.navercorp.pinpoint.realtime.dto.ATCDemand;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import com.navercorp.pinpoint.realtime.dto.ATDDemand;
import com.navercorp.pinpoint.realtime.dto.ATDSupply;
import com.navercorp.pinpoint.realtime.dto.Echo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author youngjin.kim2
 */
@Configuration
@ConditionalOnProperty(value = "pinpoint.modules.realtime.enabled", havingValue = "true")
@Import({
        ATCServiceProtocolConfig.class,
        ATDServiceProtocolConfig.class,
        EchoServiceProtocolConfig.class,
        RealtimeCollectorServiceConfig.class,
})
public class RealtimeCollectorServerConfig {

    private static final Logger logger = LogManager.getLogger(RealtimeCollectorServerConfig.class);

    public RealtimeCollectorServerConfig() {
        logger.info("RealtimeCollectorServerConfig initialized");
    }

    @Bean
    public ChannelServiceServer legacyATCServer(
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
    public ChannelServiceServer ATCServer(
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

    @Bean
    public ChannelServiceServer legacyATDServer(
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
    public ChannelServiceServer ATDServer(
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

    @Bean
    public ChannelServiceServer legacyEchoServer(
            ChannelProviderRepository channelProviderRepository,
            MonoChannelServiceProtocol<DemandMessage<Echo>, SupplyMessage<Echo>> protocol,
            EchoService service
    ) {
        return ChannelServiceServer.buildMono(
                channelProviderRepository,
                protocol,
                new LegacyMonoBackendAdaptor<>(service::echo)
        );
    }

    @Bean
    public ChannelServiceServer echoServer(
            ChannelProviderRepository channelProviderRepository,
            MonoChannelServiceProtocol<Echo, Echo> protocol,
            EchoService service
    ) {
        return ChannelServiceServer.buildMono(
                channelProviderRepository,
                protocol,
                service::echo
        );
    }

}
