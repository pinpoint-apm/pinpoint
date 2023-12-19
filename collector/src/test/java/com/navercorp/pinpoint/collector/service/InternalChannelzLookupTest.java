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

import com.navercorp.pinpoint.collector.service.ChannelzSocketLookup.SocketEntry;
import com.navercorp.pinpoint.collector.service.ChannelzTestUtils.SimpleInternalInstrumented;
import io.grpc.InternalChannelz;
import io.grpc.InternalChannelz.ServerStats;
import io.grpc.InternalChannelz.SocketStats;
import io.grpc.InternalLogId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static com.navercorp.pinpoint.collector.service.ChannelzTestUtils.mockServerStats;
import static com.navercorp.pinpoint.collector.service.ChannelzTestUtils.mockSocketStats;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class InternalChannelzLookupTest {

    private final InternalLogId serverId = InternalLogId.allocate("testId", "for testing");
    private final InternalLogId socketId = InternalLogId.allocate("testId", "for testing");
    private final SocketStats socketStats = mockSocketStats("123.234.123.234", 2345);
    private final SimpleInternalInstrumented<SocketStats> socketInst =
            new SimpleInternalInstrumented<>(socketStats, socketId);
    private final ServerStats serverStats = mockServerStats(List.of(socketInst));
    private final SimpleInternalInstrumented<ServerStats> serverInst =
            new SimpleInternalInstrumented<>(serverStats, serverId);

    @Test
    public void testFind() {
        InternalChannelz channelz = new InternalChannelz();
        InternalChannelzSocketLookup lookup = new InternalChannelzSocketLookup(channelz);

        channelz.addServer(serverInst);
        channelz.addServerSocket(serverInst, socketInst);

        Collection<SocketEntry> entries = lookup.find("123.234.123.234", 2345);

        assertThat(entries).hasSize(1);
        SocketEntry entry = entries.iterator().next();
        assertThat(entry.getSocketId()).isEqualTo(socketId.getId());
    }

}
