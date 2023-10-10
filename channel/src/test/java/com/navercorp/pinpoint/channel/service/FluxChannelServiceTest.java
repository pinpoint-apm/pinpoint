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
package com.navercorp.pinpoint.channel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.channel.ChannelProviderRegistry;
import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import com.navercorp.pinpoint.channel.MemoryChannelProvider;
import com.navercorp.pinpoint.channel.serde.JacksonSerde;
import com.navercorp.pinpoint.channel.service.client.ChannelServiceClient;
import com.navercorp.pinpoint.channel.service.client.ChannelState;
import com.navercorp.pinpoint.channel.service.server.ChannelServiceServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class FluxChannelServiceTest {

    @Test
    @DisplayName("basic flux service scenario")
    public void testBasicFluxScenario() {
        ChannelProviderRegistry memChannel = ChannelProviderRegistry.of("mem", new MemoryChannelProvider());
        ChannelProviderRepository channelRepo = new ChannelProviderRepository(List.of(memChannel));

        FluxChannelServiceProtocol<String, String> protocol = defineProtocol();

        ChannelServiceServer.buildFlux(channelRepo, protocol, d -> Flux.fromArray(d.split(","))).listen();

        List<String> response = ChannelServiceClient.buildFlux(channelRepo, protocol, Schedulers.immediate())
                .request("Foo,Bar,Hello,World,END").collectList().block(Duration.ofSeconds(1));
        assertThat(response).hasSameElementsAs(List.of("Foo", "Bar", "Hello", "World"));
    }

    private static FluxChannelServiceProtocol<String, String> defineProtocol() {
        ObjectMapper objectMapper = new ObjectMapper();
        return ChannelServiceProtocol.<String, String>builder()
                .setDemandSerde(JacksonSerde.byClass(objectMapper, String.class))
                .setDemandPubChannelURIProvider(d -> URI.create("mem:split:demand"))
                .setDemandSubChannelURI(URI.create("mem:split:demand"))
                .setSupplySerde(JacksonSerde.byClass(objectMapper, String.class))
                .setSupplyChannelURIProvider(d -> URI.create("mem:split:supply"))
                .setDemandInterval(Duration.ZERO)
                .setBufferSize(4)
                .setChannelStateFn(supply -> supply.equals("END") ? ChannelState.TERMINATED : ChannelState.ALIVE)
                .buildFlux();
    }

}
