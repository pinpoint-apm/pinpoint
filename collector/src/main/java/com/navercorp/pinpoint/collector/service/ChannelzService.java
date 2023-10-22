package com.navercorp.pinpoint.collector.service;

import io.grpc.InternalChannelz.ServerStats;
import io.grpc.InternalChannelz.SocketStats;

import java.util.List;

public interface ChannelzService {

    SocketStatsWithId getSocketStats(long logId);

    List<SocketStatsWithId> getSocketStatsList(List<Long> ids);

    List<ServerStatsWithId> getAllServerStats();

    ServerStatsWithId getServerStats(String serverName);

    class SocketStatsWithId {
        public final Long id;
        public final SocketStats stats;

        public SocketStatsWithId(Long id, SocketStats stats) {
            this.id = id;
            this.stats = stats;
        }
    }

    class ServerStatsWithId {
        public final Long id;
        public final String name;
        public final ServerStats stats;

        public ServerStatsWithId(Long id, String name, ServerStats stats) {
            this.id = id;
            this.name = name;
            this.stats = stats;
        }
    }

}
