package com.navercorp.pinpoint.grpc.channelz;

import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChannelzRegistry {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ConcurrentMap<String, Long> socketMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Long> serverMap = new ConcurrentHashMap<>();

    public void addServer(long logId, String serverName) {
        Assert.requireNonNull(serverName, "serverName");
        final Long old = this.serverMap.put(serverName, logId);
        if (old != null) {
            // warning
            logger.warn("Already exist logId:{} serverName:{}", logId, serverName);
        }
    }

    public Long getServerLogId(String serverName) {
        Assert.requireNonNull(serverName, "serverName");
        return this.serverMap.get(serverName);
    }

    public void addSocket(long logId, String taregetAddress) {
        Assert.requireNonNull(taregetAddress, "taregetAddress");
        final Long old = this.socketMap.put(taregetAddress, logId);
        if (old != null) {
            // warning
            logger.warn("Already exist logId:{} remoteAddress:{}", logId, taregetAddress);
        }
    }

    public Long removeSocket(String remoteAddress) {
        Assert.requireNonNull(remoteAddress, "remoteAddress");
        return this.socketMap.remove(remoteAddress);
    }

    public Long getSocketLogId(String address) {
        Assert.requireNonNull(address, "address");
        return this.socketMap.get(address);
    }
}
