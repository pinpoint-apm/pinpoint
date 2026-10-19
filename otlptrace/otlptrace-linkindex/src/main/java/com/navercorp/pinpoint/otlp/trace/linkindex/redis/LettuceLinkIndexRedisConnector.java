/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.otlp.trace.linkindex.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lettuce connector for a standalone Redis or a Redis Cluster given as a list of {@code host:port}.
 */
public class LettuceLinkIndexRedisConnector implements LinkIndexRedisConnector {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public enum Mode {
        STANDALONE,
        CLUSTER
    }

    private final Mode mode;
    private final List<RedisURI> uris;
    private final Duration connectTimeout;

    private RedisClient standaloneClient;
    private RedisClusterClient clusterClient;

    public LettuceLinkIndexRedisConnector(Mode mode, List<String> nodes, String password,
                                          Duration connectTimeout, Duration commandTimeout) {
        this.mode = Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(nodes, "nodes");
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("nodes must not be empty");
        }
        if (mode == Mode.STANDALONE && nodes.size() != 1) {
            throw new IllegalArgumentException("standalone mode takes one node:" + nodes);
        }
        this.connectTimeout = Objects.requireNonNull(connectTimeout, "connectTimeout");
        Objects.requireNonNull(commandTimeout, "commandTimeout");
        this.uris = new ArrayList<>(nodes.size());
        for (String node : nodes) {
            this.uris.add(toUri(node, password, commandTimeout));
        }
    }

    static RedisURI toUri(String node, String password, Duration commandTimeout) {
        final int colon = node.lastIndexOf(':');
        if (colon <= 0 || colon == node.length() - 1) {
            throw new IllegalArgumentException("node must be host:port:" + node);
        }
        final String host = node.substring(0, colon).trim();
        final int port = Integer.parseInt(node.substring(colon + 1).trim());
        final RedisURI.Builder builder = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withTimeout(commandTimeout);
        if (password != null && !password.isEmpty()) {
            builder.withPassword(password.toCharArray());
        }
        return builder.build();
    }

    @Override
    public synchronized LinkIndexConnection connect() {
        if (mode == Mode.CLUSTER) {
            if (clusterClient == null) {
                clusterClient = newClusterClient();
            }
            final StatefulRedisClusterConnection<byte[], byte[]> connection = clusterClient.connect(ByteArrayCodec.INSTANCE);
            return new LinkIndexConnection(connection, connection.async());
        }
        if (standaloneClient == null) {
            standaloneClient = RedisClient.create(uris.get(0));
            standaloneClient.setOptions(ClientOptions.builder()
                    .socketOptions(socketOptions())
                    .timeoutOptions(TimeoutOptions.enabled())
                    .build());
        }
        final StatefulRedisConnection<byte[], byte[]> connection = standaloneClient.connect(ByteArrayCodec.INSTANCE);
        return new LinkIndexConnection(connection, connection.async());
    }

    private RedisClusterClient newClusterClient() {
        final RedisClusterClient client = RedisClusterClient.create(uris);
        final ClusterTopologyRefreshOptions topologyRefresh = ClusterTopologyRefreshOptions.builder()
                .enableAllAdaptiveRefreshTriggers()
                .enablePeriodicRefresh(Duration.ofSeconds(30))
                .dynamicRefreshSources(true)
                .build();
        client.setOptions(ClusterClientOptions.builder()
                .topologyRefreshOptions(topologyRefresh)
                .socketOptions(socketOptions())
                .timeoutOptions(TimeoutOptions.enabled())
                .build());
        return client;
    }

    private SocketOptions socketOptions() {
        return SocketOptions.builder()
                .connectTimeout(connectTimeout)
                .build();
    }

    @Override
    public synchronized void close() {
        if (clusterClient != null) {
            clusterClient.shutdown();
            clusterClient = null;
        }
        if (standaloneClient != null) {
            standaloneClient.shutdown();
            standaloneClient = null;
        }
        logger.info("Closed link index Redis connector mode:{}", mode);
    }

    @Override
    public String toString() {
        return "LettuceLinkIndexRedisConnector{" +
                "mode=" + mode +
                ", nodes=" + uris.size() +
                '}';
    }
}
