package com.navercorp.pinpoint.collector.service;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import io.grpc.InternalChannelz;
import io.grpc.InternalChannelz.ServerList;
import io.grpc.InternalChannelz.ServerStats;
import io.grpc.InternalChannelz.SocketStats;
import io.grpc.InternalInstrumented;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class DefaultChannelzService implements ChannelzService {

    private final InternalChannelz channelz;
    private final ChannelzRegistry channelzRegistry;

    public DefaultChannelzService(
            @Autowired(required = false) InternalChannelz channelz,
            ChannelzRegistry channelzRegistry
    ) {
        this.channelz = Objects.requireNonNullElseGet(channelz, InternalChannelz::instance);
        this.channelzRegistry = Objects.requireNonNull(channelzRegistry, "channelzRegistry");
    }

    @Override
    public SocketStatsWithId getSocketStats(long logId) {
        InternalInstrumented<SocketStats> socket = this.channelz.getSocket(logId);
        if (socket == null) {
            return null;
        }

        SocketStats stats = unwrapFuture(socket.getStats(), 3000);
        if (stats == null) {
            return null;
        }

        return new SocketStatsWithId(logId, stats);
    }

    @Override
    public List<SocketStatsWithId> getSocketStatsList(List<Long> ids) {
        List<ListenableFuture<SocketStats>> futures = new ArrayList<>();
        for (Long id: ids) {
            InternalInstrumented<SocketStats> socket = this.channelz.getSocket(id);
            if (socket != null) {
                futures.add(socket.getStats());
            }
        }
        List<SocketStats> stats = unwrapFuture(Futures.allAsList(futures), 10000);
        return zipSocketStatsWithId(ids, stats);
    }

    private static List<SocketStatsWithId> zipSocketStatsWithId(List<Long> ids, List<SocketStats> stats) {
        if (ids.size() != stats.size()) {
            throw new IllegalArgumentException("ids.size() != stats.size()");
        }
        List<SocketStatsWithId> dst = new ArrayList<>(ids.size());
        for (int i = 0; i < ids.size(); ++i) {
            dst.add(new SocketStatsWithId(ids.get(i), stats.get(i)));
        }
        return dst;
    }

    @Override
    public List<ServerStatsWithId> getAllServerStats() {
        ServerList servers = this.channelz.getServers(0, 10000);
        List<ServerStatsWithId> dst = new ArrayList<>(servers.servers.size());
        for (InternalInstrumented<ServerStats> server: servers.servers) {
            ServerStats stats = unwrapFuture(server.getStats(), 1000);
            if (stats == null) {
                continue;
            }

            long serverId = server.getLogId().getId();
            dst.add(new ServerStatsWithId(
                    serverId,
                    Objects.requireNonNullElse(
                            this.channelzRegistry.getServerName(serverId),
                            "NOT_FOUND"),
                    stats));
        }
        return dst;
    }

    @Override
    public ServerStatsWithId getServerStats(String serverName) {
        Long serverId = this.channelzRegistry.getLogId(serverName);
        if (serverId == null) {
            return null;
        }

        InternalInstrumented<ServerStats> server = this.channelz.getServer(serverId);
        if (server == null) {
            return null;
        }

        ServerStats stats = unwrapFuture(server.getStats(), 3000);
        if (stats == null) {
            return null;
        }

        return new ServerStatsWithId(serverId, serverName, stats);
    }

    private static <T> T unwrapFuture(ListenableFuture<T> future, long timeoutMs) {
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to unwrap future", e);
        }
    }

}
