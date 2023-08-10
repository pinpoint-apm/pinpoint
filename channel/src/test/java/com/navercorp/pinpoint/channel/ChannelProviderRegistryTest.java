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
package com.navercorp.pinpoint.channel;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author youngjin.kim2
 */
public class ChannelProviderRegistryTest {

    @Test()
    public void shouldThrowOnIllegalScheme() {
        ChannelProviderRepository repo = mockRepository();
        assertThatThrownBy(
                () -> repo.getPubChannel(URI.create("zoo:key")),
                "should throw IllegalArgumentException"
        );
    }

    @Test
    public void shouldReturnNonNull() {
        ChannelProviderRepository repo = mockRepository();
        assertThat(repo.getPubChannel(URI.create("foo:key"))).isNotNull();
    }

    private static ChannelProviderRepository mockRepository() {
        PubChannel pubChannel = Mockito.mock(PubChannel.class);
        SubChannel subChannel = Mockito.mock(SubChannel.class);
        ChannelProvider provider = Mockito.mock(ChannelProvider.class);
        when(provider.getPubChannel(any())).thenReturn(pubChannel);
        when(provider.getSubChannel(any())).thenReturn(subChannel);

        return new ChannelProviderRepository(List.of(
                ChannelProviderRegistry.of("foo", provider),
                ChannelProviderRegistry.of("bar", provider)
        ));
    }

}
