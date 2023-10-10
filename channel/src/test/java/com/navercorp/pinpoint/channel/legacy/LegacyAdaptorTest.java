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
package com.navercorp.pinpoint.channel.legacy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.channel.ChannelProviderRegistry;
import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import com.navercorp.pinpoint.channel.MemoryChannelProvider;
import com.navercorp.pinpoint.channel.serde.JacksonSerde;
import com.navercorp.pinpoint.channel.service.ChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.FluxChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.MonoChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.client.ChannelServiceClient;
import com.navercorp.pinpoint.channel.service.client.ChannelState;
import com.navercorp.pinpoint.channel.service.server.ChannelServiceServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class LegacyAdaptorTest {

    @Test
    @DisplayName("basic flux service scenario")
    public void testBasicFluxScenario() {
        ChannelProviderRegistry memChannel = ChannelProviderRegistry.of("mem", new MemoryChannelProvider());
        ChannelProviderRepository channelRepo = new ChannelProviderRepository(List.of(memChannel));

        FluxChannelServiceProtocol<DemandMessage<String>, SupplyMessage<String>> protocol = defineFluxProtocol();

        ChannelServiceServer.buildFlux(
                channelRepo,
                protocol,
                new LegacyFluxBackendAdaptor<>(d -> Flux.fromArray(d.split(",")))
        ).listen();

        List<String> response = new LegacyFluxClientAdaptor<>(
                ChannelServiceClient.buildFlux(channelRepo, protocol, Schedulers.immediate()),
                el -> 0L
        ).request("Foo,Bar,Hello,World,END").collectList().block(Duration.ofSeconds(1));
        assertThat(response).hasSameElementsAs(List.of("Foo", "Bar", "Hello", "World"));
    }

    private static FluxChannelServiceProtocol<DemandMessage<String>, SupplyMessage<String>> defineFluxProtocol() {
        ObjectMapper objectMapper = new ObjectMapper();
        return ChannelServiceProtocol.<DemandMessage<String>, SupplyMessage<String>>builder()
                .setDemandSerde(JacksonSerde.byParameterized(objectMapper, DemandMessage.class, String.class))
                .setDemandPubChannelURIProvider(d -> URI.create("mem:split:demand"))
                .setDemandSubChannelURI(URI.create("mem:split:demand"))
                .setSupplySerde(JacksonSerde.byParameterized(objectMapper, SupplyMessage.class, String.class))
                .setSupplyChannelURIProvider(d -> URI.create("mem:split:supply"))
                .setChannelStateFn(
                        supply -> supply.getContent().equals("END") ? ChannelState.TERMINATED : ChannelState.ALIVE)
                .buildFlux();
    }

    @Test
    @DisplayName("basic mono service scenario")
    public void testBasicMonoScenario() {
        ChannelProviderRegistry memChannel = ChannelProviderRegistry.of("mem", new MemoryChannelProvider());
        ChannelProviderRepository channelRepo = new ChannelProviderRepository(List.of(memChannel));

        MonoChannelServiceProtocol<DemandMessage<String>, SupplyMessage<String>> protocol = defineMonoProtocol();

        ChannelServiceServer.buildMono(
                channelRepo,
                protocol,
                new LegacyMonoBackendAdaptor<>(d -> Mono.just("Hello, " + d + "!"))
        ).listen();

        String response = new LegacyMonoClientAdaptor<>(ChannelServiceClient.buildMono(channelRepo, protocol), el -> 0L)
                .request("Channel").block(Duration.ofSeconds(1));
        assertThat(response).isEqualTo("Hello, Channel!");
    }

    private static MonoChannelServiceProtocol<DemandMessage<String>, SupplyMessage<String>> defineMonoProtocol() {
        ObjectMapper objectMapper = new ObjectMapper();
        return ChannelServiceProtocol.<DemandMessage<String>, SupplyMessage<String>>builder()
                .setDemandSerde(JacksonSerde.byParameterized(objectMapper, DemandMessage.class, String.class))
                .setDemandPubChannelURIProvider(d -> URI.create("mem:hello:demand"))
                .setDemandSubChannelURI(URI.create("mem:hello:demand"))
                .setSupplySerde(JacksonSerde.byParameterized(objectMapper, SupplyMessage.class, String.class))
                .setSupplyChannelURIProvider(d -> URI.create("mem:hello:supply"))
                .setRequestTimeout(Duration.ofSeconds(1))
                .buildMono();
    }

}
