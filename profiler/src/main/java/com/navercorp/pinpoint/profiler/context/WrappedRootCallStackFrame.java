package com.navercorp.pinpoint.profiler.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.RootCallStackFrame;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.trace.ServiceType;

public class WrappedRootCallStackFrame extends AbstractRecorder implements RootCallStackFrame {
    private final Logger logger = LoggerFactory.getLogger(DefaultTrace.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();
    
    private Span span;
    private TraceId traceId;
    private boolean sampling;
    
    public WrappedRootCallStackFrame(final TraceContext traceContext) {
        super(traceContext);
    }

    public void setSpan(final Span span) {
        this.span = span;
    }
    
    public void setTraceId(TraceId traceId) {
        this.traceId = traceId;
    }

    public void setSampling(boolean sampling) {
        this.sampling = sampling;
    }

    @Override
    public void recordStartTime(long startTime) {
        span.setStartTime(startTime);
    }

    @Override
    public void markBeforeTime() {
        span.markBeforeTime();
    }

    @Override
    public void markAfterTime() {
        span.markAfterTime();
    }

    @Override
    void setExceptionInfo(int exceptionClassId, String exceptionMessage) {
        span.setExceptionInfo(exceptionClassId, exceptionMessage);
        if (!span.isSetErrCode()) {
            span.setErrCode(1);
        }
    }

    @Override
    void recordApiId(int apiId) {
        span.setApiId(apiId);
    }

    @Override
    void addAnnotation(Annotation annotation) {
        span.addAnnotation(annotation);
    }

    @Override
    public void recordServiceType(ServiceType serviceType) {
        span.setServiceType(serviceType.getCode());
    }

    @Override
    public void recordRpcName(String rpc) {
        span.setRpc(rpc);
    }

    @Override
    public void recordRemoteAddress(String remoteAddress) {
        span.setRemoteAddr(remoteAddress);
    }

    @Override
    public void recordEndPoint(String endPoint) {
        span.setEndPoint(endPoint);
    }

    @Override
    public void recordParentApplication(String parentApplicationName, short parentApplicationType) {
            span.setParentApplicationName(parentApplicationName);
            span.setParentApplicationType(parentApplicationType);
            if (isDebug) {
                logger.debug("ParentApplicationName marked. parentApplicationName={}", parentApplicationName);
            }
    }

    @Override
    public void recordAcceptorHost(String host) {
        span.setAcceptorHost(host); // me
        if (isDebug) {
            logger.debug("Acceptor host received. host={}", host);
        }
    }

    @Override
    public boolean canSampled() {
        return sampling;
    }

    @Override
    public boolean isRoot() {
        return traceId.isRoot();
    }
}