package com.navercorp.pinpoint.collector.receiver.grpc.channelz;

import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultChannelzRegistry implements ChannelzRegistry {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ConcurrentMap<Long, String> serverMap = new ConcurrentHashMap<>();

    @Override
    public void register(long logId, String serverName) {
        Objects.requireNonNull(serverName, "serverName");

        String old = this.serverMap.putIfAbsent(logId, serverName);
        if (old != null) {
            logger.warn("Duplicated key: {} -> {}", logId, serverName);
        }
    }

    @Override
    public Long getLogId(String serverName) {
        for (Entry<Long, String> entry: this.serverMap.entrySet()) {
            if (entry.getValue().equals(serverName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public String getServerName(long logId) {
        return this.serverMap.get(logId);
    }

}
