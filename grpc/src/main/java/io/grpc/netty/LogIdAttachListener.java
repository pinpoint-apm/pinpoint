package io.grpc.netty;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.server.MetadataServerTransportFilter;
import io.grpc.Attributes;
import io.grpc.Metadata;
import io.grpc.internal.ServerStream;
import io.grpc.internal.ServerTransportListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogIdAttachListener implements ServerTransportListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ServerTransportListener delegate;
    private final Long logId;

    public LogIdAttachListener(ServerTransportListener delegate, Long logId) {
        this.delegate = Assert.requireNonNull(delegate, "delegate");
        this.logId = Assert.requireNonNull(logId, "logId");
    }

    @Override
    public void streamCreated(ServerStream stream, String method, Metadata headers) {
        delegate.streamCreated(stream, method, headers);
    }

    @Override
    public Attributes transportReady(Attributes attributes) {
        Attributes.Builder builder = attributes.toBuilder();
        builder.set(MetadataServerTransportFilter.LOG_ID, logId);
        Attributes build = builder.build();
        if (logger.isDebugEnabled()) {
            logger.debug("logId:{} transportReady:{} ", logId, attributes);
        }
        return delegate.transportReady(build);
    }

    @Override
    public void transportTerminated() {
        delegate.transportTerminated();
    }
};
