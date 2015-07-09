package com.navercorp.pinpoint.profiler.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.TraceHeader;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.trace.ServiceType;

public class DefaultTraceHeader extends AbstractTraceRecorder implements TraceHeader {
    private final Logger logger = LoggerFactory.getLogger(DefaultTrace.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();
    
    private final Span span;
    private TraceId traceId;
    private boolean sampling;
    
    public DefaultTraceHeader(final TraceContext traceContext) {
        super(traceContext);

        span = new Span();
        span.setAgentId(traceContext.getAgentId());
        span.setApplicationName(traceContext.getApplicationName());
        span.setAgentStartTime(traceContext.getAgentStartTime());
        span.setApplicationServiceType(traceContext.getServerTypeCode());
    }

    public Span getSpan() {
        return span;
    }
    
    public void recordTraceId(TraceId traceId) {
        span.recordTraceId(traceId);
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
    
    @Override
    public void recordLogging(boolean isLogging) {
        if (!span.isSetLoggingTransactionInfo()) {
            span.setLoggingTransactionInfo((short)(isLogging ? 1 : 0)); 
        }
    }
}