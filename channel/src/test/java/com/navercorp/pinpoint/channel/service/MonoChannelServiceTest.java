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

import tools.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.channel.ChannelProviderRegistry;
import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import com.navercorp.pinpoint.channel.MemoryChannelProvider;
import com.navercorp.pinpoint.channel.serde.JacksonSerdeFactory;
import com.navercorp.pinpoint.channel.serde.JsonSerdeFactory;
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

    JsonSerdeFactory factory = new JacksonSerdeFactory(new ObjectMapper());

    @Test
    @DisplayName("basic mono service scenario")
    public void testBasicMonoScenario() {
        ChannelProviderRepository channelRepo = getChannelProviderRepository();

        MonoChannelServiceProtocol<String, String> protocol = defineProtocol();

        ChannelServiceServer.buildMono(channelRepo, protocol, d -> Mono.just("Hello, " + d + "!")).start();

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
                repo, protocol, d -> d.equals("Steve") ? Mono.just("Hello, " + d) : null).start();

        ChannelServiceServer.buildMono(
                repo, protocol, d -> d.equals("Bob") ? Mono.just("Bye, " + d) : null).start();

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
        }).start();

        MonoChannelServiceClient<String, String> client = ChannelServiceClient.buildMono(repo, protocol);
        assertThatThrownBy(() -> client.request("Hi").block(Duration.ofMillis(100)));
    }

    @Test
    @DisplayName("Server should serve demands only between start and stop")
    public void shouldServeBetweenStartAndStop() {
        ChannelProviderRepository repo = getChannelProviderRepository();
        MonoChannelServiceProtocol<String, String> protocol = defineProtocol();
        ChannelServiceServer server = ChannelServiceServer.buildMono(repo, protocol, d -> Mono.just("Hello, " + d));
        MonoChannelServiceClient<String, String> client = ChannelServiceClient.buildMono(repo, protocol);

        // not subscribed by the builder
        assertThat(server.isRunning()).isFalse();
        assertThat(server.isAutoStartup()).isTrue();
        assertThatThrownBy(() -> client.request("Steve").block(Duration.ofMillis(100)));

        server.start();
        assertThat(server.isRunning()).isTrue();
        assertThat(client.request("Steve").block(Duration.ofSeconds(1))).isEqualTo("Hello, Steve");

        server.stop();
        assertThat(server.isRunning()).isFalse();
        assertThatThrownBy(() -> client.request("Steve").block(Duration.ofMillis(100)));
    }

    private ChannelProviderRepository getChannelProviderRepository() {
        ChannelProviderRegistry memChannel = ChannelProviderRegistry.of("mem", new MemoryChannelProvider());
        return new ChannelProviderRepository(List.of(memChannel));
    }

    private MonoChannelServiceProtocol<String, String> defineProtocol() {
        return ChannelServiceProtocol.<String, String>builder()
                .setDemandSerde(factory.byClass(String.class))
                .setDemandPubChannelURIProvider(d -> URI.create("mem:hello:demand"))
                .setDemandSubChannelURI(URI.create("mem:hello:demand"))
                .setSupplySerde(factory.byClass(String.class))
                .setSupplyChannelURIProvider(d -> URI.create("mem:hello:supply"))
                .setRequestTimeout(Duration.ofSeconds(1))
                .buildMono();
    }

}
