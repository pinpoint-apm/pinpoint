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
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author youngjin.kim2
 */
public class ChannelServiceProtocolTest {

    @Test
    public void shouldThrowWithWrongSerde() {
        ChannelProviderRepository repo = new ChannelProviderRepository(List.of(
                ChannelProviderRegistry.of("mem", new MemoryChannelProvider())
        ));

        ObjectMapper objectMapper = new ObjectMapper();
        MonoChannelServiceProtocol<Foo, Bar> protocol = ChannelServiceProtocol.<Foo, Bar>builder()
                .setDemandSerde(JacksonSerde.byParameterized(objectMapper, Bar.class))
                .setDemandPubChannelURIProvider(el -> URI.create("mem:demand"))
                .setDemandSubChannelURI(URI.create("mem:demand"))
                .setSupplySerde(JacksonSerde.byClass(objectMapper, Bar.class))
                .setSupplyChannelURIProvider(el -> URI.create("mem:supply"))
                .setRequestTimeout(Duration.ofSeconds(1))
                .buildMono();
        MonoChannelServiceClient<Foo, Bar> client = ChannelServiceClient.buildMono(repo, protocol);
        assertThatThrownBy(
                () -> client.request(new Foo()).block(),
                "Client should throw when invalid serde is provided in protocol"
        );
    }

    private static class Foo {
        int foo;
    }

    private static class Bar {
        int bar;
    }

}
