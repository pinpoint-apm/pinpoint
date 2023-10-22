/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.grpc.channelz;

import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class DefaultChannelzRegistryTest {

    @Test
    public void shouldAddServer() {
        ChannelzRegistry registry = new DefaultChannelzRegistry();

        registry.register(1, "server-1");
        registry.register(2, "server-2");
        registry.register(3, "server-2");

        assertThat(registry.getLogId("server-1")).isEqualTo(1);
        assertThat(registry.getLogId("server-2")).isEqualTo(2);
        assertThat(registry.getServerName(1)).isEqualTo("server-1");
        assertThat(registry.getServerName(2)).isEqualTo("server-2");
    }

}
