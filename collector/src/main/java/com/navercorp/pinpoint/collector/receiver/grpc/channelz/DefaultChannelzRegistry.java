package com.navercorp.pinpoint.collector.receiver.grpc.channelz;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

public class DefaultChannelzRegistry implements ChannelzRegistry {

    public static final long NO_EXIST = -1L;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConcurrentMap<ChannelzRegistry.AddressId, Set<Long>> socketMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<ChannelzRegistry.AddressId, RemoteId> remoteAddressSocketMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Long> serverMap = new ConcurrentHashMap<>();

    @Override
    public void addSocket(final long logId, InetSocketAddress remoteAddress, InetSocketAddress localAddress) {
        Assert.requireNonNull(remoteAddress, "remoteAddress");
        Assert.requireNonNull(localAddress, "localAddress");

        final ChannelzRegistry.AddressId targetId = ChannelzRegistry.AddressId.newAddressId(localAddress, remoteAddress);

        // WARNING : thread safety of Set<Long>
        this.socketMap.compute(targetId, new BiFunction<AddressId, Set<Long>, Set<Long>>() {
            @Override
            public Set<Long> apply(AddressId addressId, Set<Long> logIdSet) {
                if (logIdSet == null) {
                    Set<Long> newSet = newSynchronizedSet();
                    newSet.add(logId);
                    return newSet;
                } else {
                    logIdSet.add(logId);
                    return logIdSet;
                }
            }
        });

        AddressId remoteAddressId = AddressId.newAddressId(remoteAddress.getHostString(), remoteAddress.getPort());
        this.remoteAddressSocketMap.putIfAbsent(remoteAddressId, new RemoteId(logId, targetId));
    }


    private Set<Long> newSynchronizedSet() {
        return Collections.synchronizedSet(new HashSet<>());
    }

    // for test
    int getRemoteAddressSocketMapSize() {
        return remoteAddressSocketMap.size();
    }

    // for test
    int getSocketMapSize() {
        return socketMap.size();
    }

    @Override
    public Long removeSocket(InetSocketAddress remoteAddress) {
        Assert.requireNonNull(remoteAddress, "remoteAddress");

        AddressId remoteAddressId = AddressId.newAddressId(remoteAddress.getHostString(), remoteAddress.getPort());

        final RemoteId remoteId = this.remoteAddressSocketMap.remove(remoteAddressId);
        if (remoteId == null) {
            return NO_EXIST;
        }

        final Removed removed = new Removed();
        this.socketMap.compute(remoteId.targetAddress, new BiFunction<AddressId, Set<Long>, Set<Long>>() {
            @Override
            public Set<Long> apply(AddressId addressId, Set<Long> logIdSet) {
                if (logIdSet.remove(remoteId.logId)) {
                    removed.remove = true;
                }
                if (logIdSet.isEmpty()) {
                    return null;
                }
                return logIdSet;
            }
        });
        if (removed.remove) {
            return remoteId.logId;
        } else {
            return -1L;
        }

    }

    private static class Removed {
        boolean remove = false;
    }

    @Override
    public Set<Long> getSocketLogId(ChannelzRegistry.AddressId address) {
        Assert.requireNonNull(address, "address");

        Set<Long> logIds = this.socketMap.get(address);
        if (logIds == null) {
            return Collections.emptySet();
        }
        return new HashSet<>(logIds);
    }

    @Override
    public void addServer(long logId, String serverName) {
        Assert.requireNonNull(serverName, "serverName");

        final Long old = this.serverMap.putIfAbsent(serverName, logId);
        if (old != null) {
            // warning
            logger.warn("Already exist logId:{} serverName:{}", logId, serverName);
        }
    }

    @Override
    public Long getServerLogId(String serverName) {
        Assert.requireNonNull(serverName, "serverName");

        return this.serverMap.get(serverName);
    }

    private static class RemoteId {
        private final long logId;
        private final ChannelzRegistry.AddressId targetAddress;

        public RemoteId(long logId, ChannelzRegistry.AddressId targetAddress) {
            this.logId = logId;
            this.targetAddress = Assert.requireNonNull(targetAddress, "targetAddress");
        }
    }
}
