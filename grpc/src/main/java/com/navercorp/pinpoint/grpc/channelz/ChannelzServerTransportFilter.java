package com.navercorp.pinpoint.grpc.channelz;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.server.MetadataServerTransportFilter;
import com.navercorp.pinpoint.grpc.server.SocketAddressUtils;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import io.grpc.Attributes;
import io.grpc.ServerTransportFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;


public class ChannelzServerTransportFilter extends ServerTransportFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ChannelzRegistry registry;

    public ChannelzServerTransportFilter(ChannelzRegistry registry) {
        this.registry = Assert.requireNonNull(registry, "registry");
    }

    @Override
    public Attributes transportReady(Attributes transportAttrs) {
        final TransportMetadata transportMetadata = getTransportMetadata(transportAttrs);

        final long logId = transportMetadata.getLogId();
        final InetSocketAddress remoteAddress = transportMetadata.getRemoteAddress();
        final InetSocketAddress localAddress = transportMetadata.getLocalAddress();

        final String target = remoteAddress.getHostString() + ":" + localAddress.getPort();
        if (logger.isDebugEnabled()) {
            logger.debug("Add logId:{} remoteAddress:{} localAddress:{} target:{}",
                    logId,
                    SocketAddressUtils.toString(remoteAddress),
                    SocketAddressUtils.toString(localAddress),
                    target
            );
        }

        registry.addSocket(logId, target);

        return transportAttrs;
    }


    private TransportMetadata getTransportMetadata(Attributes transportAttrs) {
        return transportAttrs.get(MetadataServerTransportFilter.TRANSPORT_METADATA_KEY);
    }

    @Override
    public void transportTerminated(Attributes transportAttrs) {
        final TransportMetadata transportMetadata = getTransportMetadata(transportAttrs);

        final InetSocketAddress remoteAddress = transportMetadata.getRemoteAddress();
        final InetSocketAddress localAddress = transportMetadata.getLocalAddress();
        final String target = remoteAddress.getHostString() + ":" + localAddress.getPort();


        final Long logId = registry.removeSocket(target);
        if (logger.isDebugEnabled()) {
            logger.debug("Remove logId:{} remoteAddress:{}", logId, remoteAddress);
        }
    }


}
