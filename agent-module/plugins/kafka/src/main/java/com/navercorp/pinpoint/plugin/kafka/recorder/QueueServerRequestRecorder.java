package com.navercorp.pinpoint.plugin.kafka.recorder;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;

import java.util.Objects;

public class QueueServerRequestRecorder<T> {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final RequestAdaptor<T> requestAdaptor;

    public QueueServerRequestRecorder(RequestAdaptor<T> requestAdaptor) {
        this.requestAdaptor = Objects.requireNonNull(requestAdaptor, "requestAdaptor");
    }

    // Records the server's request information.
    public void record(final SpanRecorder recorder, final T request) {
        if (recorder == null || request == null) {
            return;
        }
        final String rpcName = requestAdaptor.getRpcName(request);
        recorder.recordRpcName(rpcName);
        if (isDebug) {
            logger.debug("Record rpcName={}", rpcName);
        }

        final String endPoint = requestAdaptor.getEndPoint(request);
        recorder.recordEndPoint(endPoint);
        if (isDebug) {
            logger.debug("Record endPoint={}", endPoint);
        }

        final String remoteAddress = requestAdaptor.getRemoteAddress(request);
        recorder.recordRemoteAddress(remoteAddress);
        if (isDebug) {
            logger.debug("Record remoteAddress={}", remoteAddress);
        }
        recorder.recordAcceptorHost(remoteAddress);
        if (isDebug) {
            logger.debug("Record acceptorHost={}", remoteAddress);
        }
    }
}
