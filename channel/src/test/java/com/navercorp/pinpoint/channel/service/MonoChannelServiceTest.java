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
import com.navercorp.pinpoint.channel.service.client.MonoChannelServiceClient;
import com.navercorp.pinpoint.channel.service.server.ChannelServiceServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author youngjin.kim2
 */
public class MonoChannelServiceTest {

    @Test
    @DisplayName("basic mono service scenario")
    public void testBasicMonoScenario() {
        ChannelProviderRepository channelRepo = getChannelProviderRepository();

        MonoChannelServiceProtocol<String, String> protocol = defineProtocol();

        ChannelServiceServer.buildMono(channelRepo, protocol, d -> Mono.just("Hello, " + d + "!")).listen();

        String response = ChannelServiceClient.buildMono(channelRepo, protocol)
                .request("Channel").block(Duration.ofSeconds(1));
        assertThat(response).isEqualTo("Hello, Channel!");
    }

    @Test
    @DisplayName("Server should ignore demand when it is not related to self")
    public void shouldIgnore() {
        ChannelProviderRepository repo = getChannelProviderRepository();
        MonoChannelServiceProtocol<String, String> protocol = defineProtocol();

        ChannelServiceServer.buildMono(
                repo, protocol, d -> d.equals("Steve") ? Mono.just("Hello, " + d) : null).listen();

        ChannelServiceServer.buildMono(
                repo, protocol, d -> d.equals("Bob") ? Mono.just("Bye, " + d) : null).listen();

        MonoChannelServiceClient<String, String> client = ChannelServiceClient.buildMono(repo, protocol);
        assertThat(client.request("Steve").block(Duration.ofSeconds(1))).isEqualTo("Hello, Steve");
        assertThat(client.request("Bob").block(Duration.ofSeconds(1))).isEqualTo("Bye, Bob");
    }

    @Test
    @DisplayName("Client should throw when server backend throws")
    public void shouldThrowIdentically() {
        ChannelProviderRepository repo = getChannelProviderRepository();
        MonoChannelServiceProtocol<String, String> protocol = defineProtocol();

        ChannelServiceServer.buildMono(repo, protocol, d -> {
            throw new RuntimeException();
        }).listen();

        MonoChannelServiceClient<String, String> client = ChannelServiceClient.buildMono(repo, protocol);
        assertThatThrownBy(() -> client.request("Hi").block(Duration.ofMillis(100)));
    }

    private static ChannelProviderRepository getChannelProviderRepository() {
        ChannelProviderRegistry memChannel = ChannelProviderRegistry.of("mem", new MemoryChannelProvider());
        return new ChannelProviderRepository(List.of(memChannel));
    }

    private static MonoChannelServiceProtocol<String, String> defineProtocol() {
        ObjectMapper objectMapper = new ObjectMapper();
        return ChannelServiceProtocol.<String, String>builder()
                .setDemandSerde(JacksonSerde.byClass(objectMapper, String.class))
                .setDemandPubChannelURIProvider(d -> URI.create("mem:hello:demand"))
                .setDemandSubChannelURI(URI.create("mem:hello:demand"))
                .setSupplySerde(JacksonSerde.byClass(objectMapper, String.class))
                .setSupplyChannelURIProvider(d -> URI.create("mem:hello:supply"))
                .setRequestTimeout(Duration.ofSeconds(1))
                .buildMono();
    }

}
