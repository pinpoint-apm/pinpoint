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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.service.ChannelzService.ServerStatsWithId;
import com.navercorp.pinpoint.collector.service.ChannelzService.SocketStatsWithId;
import com.navercorp.pinpoint.collector.service.ChannelzTestUtils.SimpleInternalInstrumented;
import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import io.grpc.InternalChannelz;
import io.grpc.InternalChannelz.ServerStats;
import io.grpc.InternalChannelz.SocketStats;
import io.grpc.InternalLogId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.navercorp.pinpoint.collector.service.ChannelzTestUtils.mockServerStats;
import static com.navercorp.pinpoint.collector.service.ChannelzTestUtils.mockSocketStats;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class DefaultChannelzServiceTest {

    private final InternalLogId serverId = InternalLogId.allocate("testId", "for testing");
    private final InternalLogId socketId = InternalLogId.allocate("testId", "for testing");
    private final SocketStats socketStats = mockSocketStats("123.234.123.234", 2345);
    private final SimpleInternalInstrumented<SocketStats> socketInst =
            new SimpleInternalInstrumented<>(socketStats, socketId);
    private final ServerStats serverStats = mockServerStats(List.of(socketInst));
    private final SimpleInternalInstrumented<ServerStats> serverInst =
            new SimpleInternalInstrumented<>(serverStats, serverId);

    private InternalChannelz channelz;
    @Mock private ChannelzRegistry registry;
    private ChannelzService service;

    @BeforeEach
    public void setUp() {
        channelz = new InternalChannelz();
        service = new DefaultChannelzService(channelz, registry);

        channelz.addServer(serverInst);
        channelz.addServerSocket(serverInst, socketInst);
    }

    @Test
    public void testGetSocketStats() {
        SocketStatsWithId result = service.getSocketStats(socketId.getId());

        assertThat(result.stats).isSameAs(socketStats);
        assertThat(result.id).isEqualTo(socketId.getId());
    }

    @Test
    public void testGetSocketStatsList() {
        SocketStatsWithId result = service.getSocketStatsList(List.of(socketId.getId())).get(0);

        assertThat(result.stats).isSameAs(socketStats);
        assertThat(result.id).isEqualTo(socketId.getId());
    }

    @Test
    public void testGetAllServers() {
        when(registry.getServerName(eq(serverId.getId()))).thenReturn("server-1");

        ServerStatsWithId result = service.getAllServerStats().get(0);

        assertThat(result.stats).isSameAs(serverStats);
        assertThat(result.id).isEqualTo(serverId.getId());
        assertThat(result.name).isEqualTo("server-1");
    }

    @Test
    public void testGetServer() {
        when(registry.getLogId(eq("server-1"))).thenReturn(serverId.getId());

        ServerStatsWithId result = service.getServerStats("server-1");

        assertThat(result.stats).isSameAs(serverStats);
        assertThat(result.id).isEqualTo(serverId.getId());
        assertThat(result.name).isEqualTo("server-1");
    }

}
