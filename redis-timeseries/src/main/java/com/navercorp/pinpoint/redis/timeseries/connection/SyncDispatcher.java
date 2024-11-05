package com.navercorp.pinpoint.redis.timeseries.connection;

import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import io.lettuce.core.protocol.Command;

import java.util.Objects;

public class SyncDispatcher<K, V> {
    private final RedisClusterCommands<K, V> command;

    public SyncDispatcher(RedisClusterCommands<K, V> command) {
        this.command = Objects.requireNonNull(command, "command");
    }

    public <T> T dispatch(Command<K, V, T> cmd) {
        return command.dispatch(cmd.getType(), cmd.getOutput(), cmd.getArgs());
    }
}
