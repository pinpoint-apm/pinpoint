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
package com.navercorp.pinpoint.realtime.collector.receiver;

import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import com.navercorp.pinpoint.realtime.collector.receiver.grpc.GrpcAgentConnectionRepository;
import com.navercorp.pinpoint.realtime.collector.receiver.grpc.GrpcCommandService;
import com.navercorp.pinpoint.realtime.collector.sink.ErrorSinkRepository;
import com.navercorp.pinpoint.realtime.collector.sink.RealtimeCollectorSinkConfig;
import com.navercorp.pinpoint.realtime.collector.sink.SinkRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.MonoSink;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@Import({ RealtimeCollectorSinkConfig.class })
public class RealtimeCollectorReceiverConfig {

    @Bean
    GrpcAgentConnectionRepository grpcAgentConnectionRepository() {
        return new GrpcAgentConnectionRepository();
    }

    @Bean("commandService")
    GrpcCommandService grpcCommandService(
            GrpcAgentConnectionRepository agentConnectionRepository,
            ErrorSinkRepository errorSinkRepository,
            SinkRepository<FluxSink<PCmdActiveThreadCountRes>> activeThreadCountSinkRepository,
            SinkRepository<MonoSink<PCmdActiveThreadDumpRes>> activeThreadDumpSinkRepository,
            SinkRepository<MonoSink<PCmdActiveThreadLightDumpRes>> activeThreadLightDumpSinkRepository,
            SinkRepository<MonoSink<PCmdEchoResponse>> echoSinkRepository
    ) {
        return new GrpcCommandService(
                agentConnectionRepository,
                errorSinkRepository,
                activeThreadCountSinkRepository,
                activeThreadDumpSinkRepository,
                activeThreadLightDumpSinkRepository,
                echoSinkRepository
        );
    }

}
