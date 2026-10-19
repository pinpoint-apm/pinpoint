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

import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;

import java.util.Objects;

/**
 * A Redis connection of the link index, standalone or cluster.
 * The commands used by the link index (hash, key, expire) are common to both, so callers only
 * see {@link RedisClusterAsyncCommands}.
 */
public class LinkIndexConnection implements AutoCloseable {

    private final StatefulConnection<byte[], byte[]> connection;
    private final RedisClusterAsyncCommands<byte[], byte[]> commands;

    public LinkIndexConnection(StatefulConnection<byte[], byte[]> connection,
                               RedisClusterAsyncCommands<byte[], byte[]> commands) {
        this.connection = Objects.requireNonNull(connection, "connection");
        this.commands = Objects.requireNonNull(commands, "commands");
    }

    public RedisClusterAsyncCommands<byte[], byte[]> async() {
        return commands;
    }

    /**
     * @see StatefulConnection#setAutoFlushCommands(boolean)
     */
    public void setAutoFlushCommands(boolean autoFlush) {
        connection.setAutoFlushCommands(autoFlush);
    }

    public void flushCommands() {
        connection.flushCommands();
    }

    @Override
    public void close() {
        connection.close();
    }
}
