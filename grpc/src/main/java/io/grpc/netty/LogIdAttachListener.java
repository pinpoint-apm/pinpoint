package io.grpc.netty;

import com.navercorp.pinpoint.grpc.server.MetadataServerTransportFilter;
import io.grpc.Attributes;
import io.grpc.Metadata;
import io.grpc.internal.ServerStream;
import io.grpc.internal.ServerTransportListener;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Objects;

public class LogIdAttachListener implements ServerTransportListener {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ServerTransportListener delegate;
    private final Long logId;

    public LogIdAttachListener(ServerTransportListener delegate, Long logId) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.logId = Objects.requireNonNull(logId, "logId");
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
}
