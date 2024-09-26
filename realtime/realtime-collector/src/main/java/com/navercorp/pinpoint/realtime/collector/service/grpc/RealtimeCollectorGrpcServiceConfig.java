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
package com.navercorp.pinpoint.realtime.collector.service.grpc;

import com.navercorp.pinpoint.realtime.collector.receiver.RealtimeCollectorReceiverConfig;
import com.navercorp.pinpoint.realtime.collector.receiver.grpc.GrpcAgentConnectionRepository;
import com.navercorp.pinpoint.realtime.collector.service.ActiveThreadCountService;
import com.navercorp.pinpoint.realtime.collector.service.ActiveThreadDumpService;
import com.navercorp.pinpoint.realtime.collector.service.EchoService;
import com.navercorp.pinpoint.realtime.collector.sink.ActiveThreadCountPublisher;
import com.navercorp.pinpoint.realtime.collector.sink.ActiveThreadDumpPublisher;
import com.navercorp.pinpoint.realtime.collector.sink.ActiveThreadLightDumpPublisher;
import com.navercorp.pinpoint.realtime.collector.sink.EchoPublisher;
import com.navercorp.pinpoint.realtime.collector.sink.SinkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Duration;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@Import({ RealtimeCollectorReceiverConfig.class })
public class RealtimeCollectorGrpcServiceConfig {

    @Value("${pinpoint.collector.realtime.atc.demand.duration:PT14.5S}")
    private Duration atcDemandDuration;

    @Bean
    public ActiveThreadCountService grpcActiveThreadCountService(
            GrpcAgentConnectionRepository connectionRepository,
            SinkRepository<ActiveThreadCountPublisher> sinkRepository
    ) {
        return new GrpcActiveThreadCountService(connectionRepository, sinkRepository, this.atcDemandDuration);
    }

    @Bean
    public ActiveThreadDumpService grpcActiveThreadDumpService(
            GrpcAgentConnectionRepository connectionRepository,
            SinkRepository<ActiveThreadDumpPublisher> sinkRepository,
            SinkRepository<ActiveThreadLightDumpPublisher> lightSinkRepository
    ) {
        return new GrpcActiveThreadDumpService(connectionRepository, sinkRepository, lightSinkRepository);
    }

    @Bean
    public EchoService grpcEchoService(
            GrpcAgentConnectionRepository connectionRepository,
            SinkRepository<EchoPublisher> sinkRepository
    ) {
        return new GrpcEchoService(connectionRepository, sinkRepository);
    }

}
