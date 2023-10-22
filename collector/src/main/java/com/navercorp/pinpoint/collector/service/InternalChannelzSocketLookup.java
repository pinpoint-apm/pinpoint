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

import com.google.common.base.Suppliers;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.InternalChannelz;
import io.grpc.InternalChannelz.ServerList;
import io.grpc.InternalChannelz.ServerSocketsList;
import io.grpc.InternalChannelz.ServerStats;
import io.grpc.InternalChannelz.SocketStats;
import io.grpc.InternalInstrumented;
import io.grpc.InternalWithLogId;
import io.grpc.netty.NettyUtils;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author youngjin.kim2
 */
@Service
public class InternalChannelzSocketLookup implements ChannelzSocketLookup {

    private final InternalChannelz channelz;
    private final Supplier<Collection<SocketEntry>> entriesMemo;

    public InternalChannelzSocketLookup(@Autowired(required = false) InternalChannelz channelz) {
        this.channelz = Objects.requireNonNullElseGet(channelz, InternalChannelz::instance);
        this.entriesMemo = Suppliers.memoizeWithExpiration(
                this::getAllEntries,
                5000,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public Collection<SocketEntry> find(@Nullable String remoteAddress, @Nullable Integer localPort) {
        List<SocketEntry> entries = new ArrayList<>();
        for (SocketEntry entry: this.entriesMemo.get()) {
            if (entry.match(remoteAddress, localPort)) {
                entries.add(entry);
            }
        }
        return entries;
    }

    private Collection<SocketEntry> getAllEntries() {
        List<SocketEntry> dst = new ArrayList<>();
        ServerList servers = channelz.getServers(0, 10000);
        for (InternalInstrumented<ServerStats> server: servers.servers) {
            ServerSocketsList sockets = channelz.getServerSockets(
                    server.getLogId().getId(), 0, 10000);
            if (sockets == null) {
                continue;
            }

            for (InternalWithLogId socket: sockets.sockets) {
                long socketId = socket.getLogId().getId();
                SocketEntry entry = resolveSocket(socket);
                if (entry != null) {
                    dst.add(entry);
                }
            }
        }
        return Collections.unmodifiableList(dst);
    }

    private SocketEntry resolveSocket(InternalWithLogId socket) {
        SocketEntry entry = resolveSocketWithNettyAssumption(socket);
        if (entry != null) {
            return entry;
        }

        return resolveSocketWithoutAssumption(socket);
    }

    /**
     * This method assumes that the socket is a NettyTransport.
     * It allows skipping unwrapping future, but directly extracting the data we need from the socket.
     * @param socket socket
     * @return socket entry
     */
    private SocketEntry resolveSocketWithNettyAssumption(InternalWithLogId socket) {
        Channel channel = NettyUtils.extractChannel(socket);
        if (channel == null) {
            return null;
        }

        return SocketEntry.compose(channel.remoteAddress(), channel.localAddress(), socket.getLogId().getId());
    }

    private SocketEntry resolveSocketWithoutAssumption(InternalWithLogId socket) {
        long socketId = socket.getLogId().getId();
        InternalInstrumented<SocketStats> socketInst = this.channelz.getSocket(socketId);
        if (socketInst == null) {
            return null;
        }

        SocketStats stats = unwrapFuture(socketInst.getStats());
        return SocketEntry.compose(stats.remote, stats.local, socketId);
    }

    private static <T> T unwrapFuture(ListenableFuture<T> future) {
        try {
            return future.get(1000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to unwrap future");
        }
    }

}
